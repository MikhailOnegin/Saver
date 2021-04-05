package digital.fact.saver.presentation.fragments.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.utils.getOperationsForADate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class OperationsViewModel : ViewModel() {

    private val _operations = MutableLiveData<List<Operation>>()
    val operations: LiveData<List<Operation>> = _operations

    fun initialize(initialDate: Long, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.timeInMillis = initialDate
            val offset = position - (Int.MAX_VALUE/2)
            calendar.add(Calendar.DAY_OF_YEAR, offset)
            _operations.postValue(getOperationsForADate(calendar.timeInMillis))
        }
    }

}