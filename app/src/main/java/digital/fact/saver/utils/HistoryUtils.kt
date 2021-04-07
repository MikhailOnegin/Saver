package digital.fact.saver.utils

import digital.fact.saver.App
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
            sources.firstOrNull { it.id == operation.fromSourceId}?.run {
                operation.fromSourceName = name
                operation.fromSourceSum = currentSum
            }
        }
        if (operation.toSourceId != 0L) {
            sources.firstOrNull { it.id == operation.toSourceId}?.run {
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
                (it.fromSourceId == operation.fromSourceId || it.toSourceId == operation.fromSourceId)
            }
            for (afterOperation in operationsAfterThis) {
                if (afterOperation.fromSourceId != 0L) operation.fromSourceSum += afterOperation.sum
                if (afterOperation.toSourceId != 0L) operation.fromSourceSum -= afterOperation.sum
            }
        }
        if (operation.toSourceId != 0L) {
            val operationsAfterThis = operations.filter {
                it.addingDate > operation.addingDate &&
                        (it.fromSourceId == operation.toSourceId || it.toSourceId == operation.toSourceId)
            }
            for (afterOperation in operationsAfterThis) {
                if (afterOperation.fromSourceId != 0L) operation.toSourceSum += afterOperation.sum
                if (afterOperation.toSourceId != 0L) operation.toSourceSum -= afterOperation.sum
            }
        }
    }
    return operations
}