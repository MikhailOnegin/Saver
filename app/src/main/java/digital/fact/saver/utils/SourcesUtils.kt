package digital.fact.saver.utils

import digital.fact.saver.App
import digital.fact.saver.domain.models.Sources
import java.util.*

fun fillSourceWithCurrentSumForToday(source: Sources) {
    val operations = App.db.operationsDao().getAllSourceOperationsUntilADate(
        sourceId = source.id,
        date = getTomorrow(Date()).time)
    for (operation in operations) {
        if (operation.from_source_id == source.id) source.currentSum -= operation.sum
        if (operation.to_source_id == source.id) source.currentSum += operation.sum
    }
}