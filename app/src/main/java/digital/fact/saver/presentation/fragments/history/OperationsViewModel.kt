package digital.fact.saver.presentation.fragments.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import digital.fact.saver.domain.models.Operation

class OperationsViewModel : ViewModel() {

    private val _operations = MutableLiveData(Operation.getTestList())
    val operations: LiveData<List<Operation>> = _operations

}