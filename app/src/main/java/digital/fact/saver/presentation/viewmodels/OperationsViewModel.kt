package digital.fact.saver.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.data.database.dto.Operation
import digital.fact.saver.data.repositories.*
import digital.fact.saver.domain.repository.*

class OperationsViewModel(application: Application) : AndroidViewModel(application) {

    private var operationsRepository: OperationsRepository = OperationsRepositoryIml()
    var operations: LiveData<List<Operation>> = MutableLiveData()

    init {
        operations = operationsRepository.getAll()
    }

    fun getAllOperations(): LiveData<List<Operation>> {
        return operations
    }

}