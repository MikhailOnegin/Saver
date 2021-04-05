package digital.fact.saver.utils

import digital.fact.saver.App
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.domain.models.toOperations
import java.util.*

fun getOperationsForADate(timeInMillis: Long): List<Operation> {
    val calendar = Calendar.getInstance(Locale.getDefault())
    calendar.timeInMillis = resetTimeInMillis(timeInMillis)
    val startTimeInMillis = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val endTimeInMillis = calendar.timeInMillis
    val dao = App.db.operationsDao()
    val operations = dao.getOperationsForADateRange(startTimeInMillis, endTimeInMillis)
    return operations.toOperations()
}