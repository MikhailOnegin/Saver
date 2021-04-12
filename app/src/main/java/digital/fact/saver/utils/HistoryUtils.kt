package digital.fact.saver.utils

import digital.fact.saver.App
import digital.fact.saver.data.database.dto.Operation.OperationType
import digital.fact.saver.data.database.dto.PlanTable
import digital.fact.saver.data.database.dto.PlanTable.PlanType
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.domain.models.Sources
import digital.fact.saver.domain.models.toOperations
import digital.fact.saver.domain.models.toSources
import java.util.*

fun getOperationsForADate(timeInMillis: Long): List<Operation> {
    val calendar = Calendar.getInstance(Locale.getDefault())
    calendar.timeInMillis = resetTimeInMillis(timeInMillis)
    val startTimeInMillis = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val endTimeInMillis = calendar.timeInMillis
    val dao = App.db.operationsDao()
    val operations = dao.getOperationsForADateRange(startTimeInMillis, endTimeInMillis)
    return fillOperationsWithSourcesCurrentSumsAndNamesOnADate(
            operations.toOperations(), Date(timeInMillis))
}

fun getWalletsForADate(date: Date): List<Sources> {
    val query = App.db.sourcesDao().getWalletsOnDate(getTomorrow(date).time)
    return fillSourcesWithCurrentSumsOnADate(query.toSources(), date)
}

fun getSaversForADate(date: Date): List<Sources> {
    val query = App.db.sourcesDao().getSaversOnDate(getTomorrow(date).time)
    return fillSourcesWithCurrentSumsOnADate(query.toSources(), date)
}

private fun fillSourcesWithCurrentSumsOnADate(sources: List<Sources>, date: Date): List<Sources> {
    val operations = App.db.operationsDao().getAllOperationsUntilADate(getTomorrow(date).time)
    for (operation in operations) {
        if (operation.from_source_id != 0L) {
            sources.firstOrNull { it.id == operation.from_source_id }?.run {
                currentSum -= operation.sum
            }
        }
        if (operation.to_source_id != 0L) {
            sources.firstOrNull { it.id == operation.to_source_id }?.run {
                currentSum += operation.sum
            }
        }
    }
    return sources
}

private fun fillOperationsWithSourcesCurrentSumsAndNamesOnADate(
        operations: List<Operation>,
        date: Date
): List<Operation> {
    val sources = fillSourcesWithCurrentSumsOnADate(App.db.sourcesDao().getAll().toSources(), date)
    for (operation in operations) {
        if (operation.fromSourceId != 0L) {
            sources.firstOrNull { it.id == operation.fromSourceId }?.run {
                operation.fromSourceName = name
                operation.fromSourceSum = currentSum
            }
        }
        if (operation.toSourceId != 0L) {
            sources.firstOrNull { it.id == operation.toSourceId }?.run {
                operation.toSourceName = name
                operation.toSourceSum = currentSum
            }
        }
    }
    return correctOperationsSourcesCurrentSumsConsideringAddingDate(operations)
}

private fun correctOperationsSourcesCurrentSumsConsideringAddingDate(
        operations: List<Operation>
): List<Operation> {
    for (operation in operations) {
        if (operation.fromSourceId != 0L) {
            val operationsAfterThis = operations.filter {
                it.addingDate > operation.addingDate &&
                        (it.fromSourceId == operation.fromSourceId
                                || it.toSourceId == operation.fromSourceId)
            }
            for (afterOperation in operationsAfterThis) {
                if (afterOperation.fromSourceId != 0L) operation.fromSourceSum += afterOperation.sum
                if (afterOperation.toSourceId != 0L) operation.fromSourceSum -= afterOperation.sum
            }
        }
        if (operation.toSourceId != 0L) {
            val operationsAfterThis = operations.filter {
                it.addingDate > operation.addingDate &&
                        (it.fromSourceId == operation.toSourceId
                                || it.toSourceId == operation.toSourceId)
            }
            for (afterOperation in operationsAfterThis) {
                if (afterOperation.fromSourceId != 0L) operation.toSourceSum += afterOperation.sum
                if (afterOperation.toSourceId != 0L) operation.toSourceSum -= afterOperation.sum
            }
        }
    }
    return operations
}

fun deleteOperationAndUndoneRelatedPlan(operationId: Long) {
    val operation = App.db.operationsDao().getOperation(operationId)
    operation?.run {
        val plan = App.db.plansDao().getPlan(operation.plan_id)
        if (plan != null) {
            val updatedPlan = PlanTable(
                    id = plan.id,
                    type = plan.type,
                    sum = plan.sum,
                    name = plan.name,
                    operation_id = 0L,
                    planning_date = 0L
            )
            App.db.plansDao().update(updatedPlan)
        }
        App.db.operationsDao().delete(operation)
    }
}

private fun getAvailableMoneyForADate(date: Date): Long {
    val activeWalletsSum = getWalletsForADate(date).filter {
        it.type == Source.Type.ACTIVE.value
    }.sumOf { it.currentSum }
    val saversSum = getSaversForADate(date).sumOf { it.currentSum }
    return activeWalletsSum - saversSum
}

private fun getPlannedSumForADate(date: Date, planType: PlanType): Long {
    val calendar = Calendar.getInstance(Locale.getDefault())
    calendar.timeInMillis = resetTimeInMillis(date.time)
    val startTimeInMillis = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val endTimeInMillis = calendar.timeInMillis
    val dao = App.db.operationsDao()
    val operations = dao.getOperationsForADateRange(startTimeInMillis, endTimeInMillis)
    val plannedOperations = when (planType) {
        PlanType.EXPENSES -> operations.filter { it.type == OperationType.PLANNED_EXPENSES.value }
        PlanType.INCOME -> operations.filter { it.type == OperationType.PLANNED_INCOME.value }
    }
    var result = 0L
    for (operation in plannedOperations) {
        val plan = App.db.plansDao().getPlan(operation.plan_id)
        if (plan != null) result += plan.sum
    }
    return result
}

private fun getPlannedSumForAPeriod(periodStart: Long, periodEnd: Long, planType: PlanType): Long {
    val plans = App.db.plansDao().getPlansForAPeriod(periodStart, periodEnd)
    val filteredPlans = when (planType) {
        PlanType.EXPENSES -> plans.filter { it.type == PlanType.EXPENSES.value }
        PlanType.INCOME -> plans.filter { it.type == PlanType.INCOME.value }
    }
    return filteredPlans.sumOf { it.sum }
}

private fun getAverageDailyExpenses(periodStart: Long, periodEnd: Long): Long {
    val periodLength = getDaysDifference(Date(periodEnd), Date(periodStart))
    if (periodLength <= 0L) return 0L
    val available = getAvailableMoneyForADate(getYesterday(Date(periodStart)))
    val plannedExpenses = getPlannedSumForAPeriod(periodStart, periodEnd, PlanType.EXPENSES)
    val plannedIncome = getPlannedSumForAPeriod(periodStart, periodEnd, PlanType.INCOME)
    return (available + plannedIncome - plannedExpenses) / periodLength
}

fun calculateEconomy(periodStart: Long, periodEnd: Long, date: Date): Long {
    if (date.time !in periodStart until periodEnd) return 0L
    val yesterday = getYesterday(date)
    val availableAtMorning = getAvailableMoneyForADate(yesterday)
    val availableAtEvening = getAvailableMoneyForADate(date)
    val plannedExpenses = getPlannedSumForADate(date, PlanType.EXPENSES)
    val plannedIncome = getPlannedSumForADate(date, PlanType.INCOME)
    val averageDailyExpenses = getAverageDailyExpenses(periodStart, periodEnd)
    return averageDailyExpenses + availableAtEvening -
            availableAtMorning + plannedExpenses - plannedIncome
}

private fun getDonePlansPlannedSumForADate(
        plansType: PlanType,
        periodStart: Long,
        date: Date,
): Long {
    val dao = App.db.operationsDao()
    val operations = dao.getOperationsForADateRange(periodStart, getTomorrow(date).time)
    val plannedOperations = when (plansType) {
        PlanType.EXPENSES -> operations.filter { it.type == OperationType.PLANNED_EXPENSES.value }
        PlanType.INCOME -> operations.filter { it.type == OperationType.PLANNED_INCOME.value }
    }
    var result = 0L
    for (operation in plannedOperations) {
        val plan = App.db.plansDao().getPlan(operation.plan_id)
        if (plan != null) result += plan.sum
    }
    return result
}

fun calculateSavings(
        periodStart: Long,
        periodEnd: Long,
        date: Date
): Long {
    if (date.time !in periodStart until periodEnd) return 0L
    val daysGone = 1 + getDaysDifference(date, Date(periodStart))
    val availableAtPeriodStart = getAvailableMoneyForADate(getYesterday(Date(periodStart)))
    val availableAtDate = getAvailableMoneyForADate(date)
    val plannedExpenses = getDonePlansPlannedSumForADate(PlanType.EXPENSES, periodStart, date)
    val plannedIncome = getDonePlansPlannedSumForADate(PlanType.INCOME, periodStart, date)
    val averageDailyExpenses = getAverageDailyExpenses(periodStart, periodEnd)
    return daysGone * averageDailyExpenses -
            (availableAtPeriodStart - availableAtDate + plannedIncome - plannedExpenses)
}