package digital.fact.saver.utils

import digital.fact.saver.App
import digital.fact.saver.data.database.dto.DbOperation.OperationType
import digital.fact.saver.data.database.dto.DbPlan
import digital.fact.saver.data.database.dto.DbPlan.PlanType
import digital.fact.saver.data.database.dto.DbSource
import digital.fact.saver.domain.models.*
import java.util.*

fun getOperationsForADate(timeInMillis: Long): List<Operation> {
    val calendar = Calendar.getInstance(Locale.getDefault())
    calendar.timeInMillis = resetTimeInMillis(timeInMillis)
    val startTimeInMillis = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val endTimeInMillis = calendar.timeInMillis
    val dao = App.db.operationsDao()
    val operations = dao.getOperationsForADateRange(startTimeInMillis, endTimeInMillis)
    val operationsWithSourcesInfo = fillOperationsWithSourcesCurrentSumsAndNamesOnADate(
        operations.toOperations(), Date(timeInMillis)
    )
    return fillOperationsWithPlanData(operationsWithSourcesInfo)
}

fun getVisibleWalletsForADate(date: Date): List<Source> {
    val query = App.db.sourcesDao().getVisibleWalletsOnDate(getTomorrow(date).time)
    return fillSourcesWithCurrentSumsOnADate(query.toSources(), date)
}

fun getAllWalletsForADate(date: Date): List<Source> {
    val query = App.db.sourcesDao().getAllWalletsOnDate(getTomorrow(date).time)
    return fillSourcesWithCurrentSumsOnADate(query.toSources(), date)
}

fun getVisibleSaversForADate(date: Date): List<Source> {
    val query = App.db.sourcesDao().getVisibleSaversOnDate(getTomorrow(date).time)
    return fillSourcesWithCurrentSumsOnADate(query.toSources(), date)
}

fun getAllSaversForADate(date: Date): List<Source> {
    val query = App.db.sourcesDao().getAllSaversOnDate(getTomorrow(date).time)
    return fillSourcesWithCurrentSumsOnADate(query.toSources(), date)
}

private fun fillSourcesWithCurrentSumsOnADate(sources: List<Source>, date: Date): List<Source> {
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
                operation.sourceAimDate = aimDate
                operation.sourceAimSum = aimSum
            }
        }
        if (operation.toSourceId != 0L) {
            sources.firstOrNull { it.id == operation.toSourceId }?.run {
                operation.toSourceName = name
                operation.toSourceSum = currentSum
                operation.sourceAimDate = aimDate
                operation.sourceAimSum = aimSum
            }
        }
    }
    return correctOperationsSourcesCurrentSumsConsideringAddingDate(operations)
}

private fun fillOperationsWithPlanData(operations: List<Operation>): List<Operation> {
    for (operation in operations) {
        if (operation.type == OperationType.PLANNED_EXPENSES.value
            || operation.type == OperationType.PLANNED_INCOME.value) {
            val plan = App.db.plansDao().getPlan(operation.planId)
            plan?.run { operation.planSum = sum }
        }
    }
    return operations
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
            val updatedPlan = DbPlan(
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
    val activeWalletsSum = getVisibleWalletsForADate(date).filter {
        it.type == DbSource.Type.ACTIVE.value
    }.sumOf { it.currentSum }
    val saversSum = getVisibleSaversForADate(date).sumOf { it.currentSum }
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

fun getAverageDailyExpenses(periodStart: Long, periodEnd: Long): Long {
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

fun getDailyFees(date: Date): List<DailyFee> {
    val result = mutableListOf<DailyFee>()
    val saversWithAimsAndAimDates = getVisibleSaversForADate(date).filter {
        it.aimSum != 0L && it.aimDate != 0L
    }
    val operations = getOperationsForADate(date.time)
    for ((index, saver) in saversWithAimsAndAimDates.withIndex()) {
        val incomeOperation = operations.firstOrNull { it.toSourceId == saver.id }
        if (incomeOperation != null) continue
        var daysLeft = getDaysDifference(Date(saver.aimDate), date)
        val remainingSum = saver.aimSum - saver.currentSum
        if (remainingSum <= 0L) continue
        if (daysLeft <= 0L) daysLeft = 1
        val fee = (saver.aimSum - saver.currentSum) / daysLeft
        result.add(DailyFee(
                id = index + 1L,
                saverId = saver.id,
                saverName = saver.name,
                fee = fee,
                daysLeft = daysLeft
        ))
    }
    return result
}

fun getTemplates(): List<Template> {
    val start = getYearBefore(Date())
    val originOperations = App.db.operationsDao().getAllOperationsForTemplates(start.time)
    val distinctOperations = originOperations.distinctBy { it.name }
    val map = mutableMapOf<String, Int>()
    for (operation in distinctOperations) map[operation.name] = 0
    for (operation in originOperations) map[operation.name] = (map[operation.name] ?: 0).inc()
    val shortMap = mutableMapOf<String, Int>()
    for (i in 0 until 50) {
        map.maxByOrNull { it.value }?.run {
            shortMap[this.key] = this.value
            map.remove(this.key)
        }
    }
    val templates = mutableListOf<Template>()
    val sortedOperations = originOperations.sortedBy { -it.adding_date }
    shortMap.onEachIndexed { index, entry ->
        sortedOperations.firstOrNull { it.name == entry.key }?.run {
            templates.add(
                Template(
                    id = index + 1L,
                    operationType = type,
                    operationName = name,
                    operationSum = sum,
                    sourceId = when (type) {
                        OperationType.EXPENSES.value -> from_source_id
                        OperationType.INCOME.value -> to_source_id
                        else -> from_source_id
                    }
                )
            )
        }
    }
    return templates
}