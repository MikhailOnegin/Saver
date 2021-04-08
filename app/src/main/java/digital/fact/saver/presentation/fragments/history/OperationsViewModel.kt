package digital.fact.saver.presentation.fragments.history

import androidx.lifecycle.*
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.utils.deleteOperationAndUndoneRelatedPlan
import digital.fact.saver.utils.getOperationsForADate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class OperationsViewModel(
        private val mainVM: MainViewModel
) : ViewModel() {

    private var initialDate: Long? = null
    private var position: Int? = null

    private val _operations = MutableLiveData<List<Operation>>()
    val operations: LiveData<List<Operation>> = _operations

    fun initialize(initialDate: Long, position: Int) {
        this.initialDate = initialDate
        this.position = position
        viewModelScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.timeInMillis = initialDate
            val offset = position - (Int.MAX_VALUE/2)
            calendar.add(Calendar.DAY_OF_YEAR, offset)
            _operations.postValue(getOperationsForADate(calendar.timeInMillis))
        }
    }

    fun deleteOperation(operationId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteOperationAndUndoneRelatedPlan(operationId)
            mainVM.sendConditionsChangedNotification()
        }
    }

    init {
        mainVM.conditionsChanged.observeForever {
            if (initialDate != null && position != null) {
                initialize(initialDate ?: 0L, position ?: 0)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class OperationsViewModelFactory(
            private val mainVM: MainViewModel
    ): ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return OperationsViewModel(mainVM) as T
        }

    }

}