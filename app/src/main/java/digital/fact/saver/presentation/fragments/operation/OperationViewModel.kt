package digital.fact.saver.presentation.fragments.operation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import digital.fact.saver.App
import digital.fact.saver.data.database.dto.Operation
import digital.fact.saver.data.database.dto.Operation.OperationType
import digital.fact.saver.data.database.dto.PlanTable
import digital.fact.saver.domain.models.Sources
import digital.fact.saver.domain.models.Sources.Companion.SourceType
import digital.fact.saver.utils.events.OneTimeEvent
import digital.fact.saver.utils.getLongSumFromString
import digital.fact.saver.utils.getSaversForADate
import digital.fact.saver.utils.getWalletsForADate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.util.*

class OperationViewModel : ViewModel() {

    private val _date = MutableLiveData(Date())
    val date: LiveData<Date> = _date

    fun setOperationDate(time: Long) {
        _date.value = Date(time)
    }

    private val _sum = MutableLiveData<String>()
    val sum: LiveData<String> = _sum
    private val builder = StringBuilder()

    fun hasZeroSum(): Boolean {
        val newBuilder = StringBuilder()
        for (char in builder.toString()) if (char.isDigit()) newBuilder.append(char)
        return newBuilder.toString().toLong() == 0L
    }

    fun onKeyboardButtonClicked(input: String) {
        val chars = charsCountAfterComma()
        if (chars >= 2 || builder.length >= 12) return
        if (builder.length == 1 && builder.first() == '0' && input != ",") return
        builder.append(input)
        _sum.value = builder.toString()
    }

    fun onBackspaceClicked() {
        if (builder.isNotEmpty()) builder.deleteCharAt(builder.length - 1)
        _sum.value = builder.toString()
    }

    private fun builderHasComma(): Boolean {
        return builder.contains(',')
    }

    private fun charsCountAfterComma(): Int {
        if (builderHasComma()) {
            val commaIndex = builder.indexOf(",")
            return builder.length - commaIndex - 1
        }
        return 0
    }

    fun setSumFromExtra(sum: String) {
        builder.clear()
        builder.append(sum)
        _sum.value = builder.toString()
    }

    private val _sources = MutableLiveData<List<Sources>>()
    val sources: LiveData<List<Sources>> = _sources

    fun initializeSources(operationType: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _sources.postValue(when (getSourceTypeForOperationType(operationType)) {
                SourceType.WALLET ->
                    getWalletsForADate(_date.value ?: Date())
                SourceType.SAVER ->
                    getSaversForADate(_date.value ?: Date())
            })
        }
    }

    private fun getSourceTypeForOperationType(operationType: Int): SourceType {
        return when (operationType) {
            OperationType.EXPENSES.value,
            OperationType.INCOME.value,
            OperationType.PLANNED_EXPENSES.value,
            OperationType.PLANNED_INCOME.value,
            OperationType.TRANSFER.value -> SourceType.WALLET
            OperationType.SAVER_EXPENSES.value,
            OperationType.SAVER_INCOME.value -> SourceType.SAVER
            else -> throw IllegalArgumentException("Wrong operation type.")
        }
    }

    private val _operationCreatedEvent = MutableLiveData<OneTimeEvent>()
    val operationCreatedEvent: LiveData<OneTimeEvent> = _operationCreatedEvent

    fun createNewOperation(
            operationType: Int,
            operationName: String,
            fromSourceId: Long,
            toSourceId: Long,
            planId: Long,
            comment: String
    ) {
        val operationDate = date.value?.time ?: Date().time
        val operation = Operation(
                type = operationType,
                name = operationName,
                operation_date = operationDate,
                adding_date = Date().time,
                sum = getLongSumFromString(builder.toString()),
                from_source_id = fromSourceId,
                to_source_id = toSourceId,
                plan_id = planId,
                category_id = 0L,
                comment = comment
        )
        viewModelScope.launch(Dispatchers.IO) {
            val newOperationId = App.db.operationsDao().insert(operation)
            if (planId != 0L) {
                App.db.plansDao().getPlan(planId)?.run {
                    App.db.plansDao().update(PlanTable(
                            id = id,
                            type = type,
                            sum = sum,
                            name = name,
                            operation_id = newOperationId,
                            planning_date = operationDate))
                }
            }
            _operationCreatedEvent.postValue(OneTimeEvent())
        }
    }

    init {
        _sum.value = builder.toString()
    }

}