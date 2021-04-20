package digital.fact.saver.presentation.fragments.operation

import androidx.lifecycle.*
import digital.fact.saver.App
import digital.fact.saver.data.database.dto.DbOperation
import digital.fact.saver.data.database.dto.DbOperation.OperationType
import digital.fact.saver.data.database.dto.DbPlan
import digital.fact.saver.domain.models.Source
import digital.fact.saver.data.database.dto.DbSource.SourceType
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.utils.decimalSeparator
import digital.fact.saver.utils.events.OneTimeEvent
import digital.fact.saver.utils.getLongSumFromString
import digital.fact.saver.utils.getVisibleSaversForADate
import digital.fact.saver.utils.getVisibleWalletsForADate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.util.*

class OperationViewModel(
        private val mainVM: MainViewModel
) : ViewModel() {

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
        if (builder.length == 1 && builder.first() == '0' && input != decimalSeparator.toString())
            return
        builder.append(input)
        _sum.value = builder.toString()
    }

    fun onBackspaceClicked() {
        if (builder.isNotEmpty()) builder.deleteCharAt(builder.length - 1)
        _sum.value = builder.toString()
    }

    private fun builderHasComma(): Boolean {
        return builder.contains(decimalSeparator)
    }

    private fun charsCountAfterComma(): Int {
        if (builderHasComma()) {
            val commaIndex = builder.indexOf(decimalSeparator)
            return builder.length - commaIndex - 1
        }
        return 0
    }

    fun setSumFromExtra(sum: String) {
        builder.clear()
        builder.append(sum)
        _sum.value = builder.toString()
    }

    private val _sources = MutableLiveData<List<Source>>()
    val source: LiveData<List<Source>> = _sources

    fun initializeSources(operationType: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _sources.postValue(when (getSourceTypeForOperationType(operationType)) {
                SourceType.WALLET ->
                    getVisibleWalletsForADate(_date.value ?: Date())
                SourceType.SAVER ->
                    getVisibleSaversForADate(_date.value ?: Date())
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
            comment: String,
            isPartOfPlan: Boolean = false
    ) {
        val operationDate = date.value?.time ?: Date().time
        val operationSum = getLongSumFromString(builder.toString())
        val operation = DbOperation(
                type = operationType,
                name = operationName,
                operation_date = operationDate,
                adding_date = Date().time,
                sum = operationSum,
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
                    App.db.plansDao().update(DbPlan(
                            id = id,
                            type = type,
                            sum = sum,
                            name = name,
                            operation_id = newOperationId,
                            planning_date = operationDate))
                    if (isPartOfPlan) {
                        App.db.plansDao().insert(DbPlan(
                                type = type,
                                sum = sum - operationSum,
                                name = name,
                                operation_id = 0L,
                                planning_date = 0L
                        ))
                    }
                }
            }
            _operationCreatedEvent.postValue(OneTimeEvent())
            mainVM.notifyConditionsChanged()
        }
    }

    init {
        _sum.value = builder.toString()
    }

    @Suppress("UNCHECKED_CAST")
    class OperationViewModelFactory(
            private val mainVM: MainViewModel
    ): ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return OperationViewModel(mainVM) as T
        }

    }

}