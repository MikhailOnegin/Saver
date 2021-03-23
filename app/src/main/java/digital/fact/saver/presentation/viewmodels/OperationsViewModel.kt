package digital.fact.saver.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.data.repositories.*
import digital.fact.saver.domain.models.*
import digital.fact.saver.domain.repository.*

class OperationsViewModel(application: Application) : AndroidViewModel(application) {

    private var operationsRepository: OperationsRepository
    var operations: LiveData<List<Operation>> = MutableLiveData()
    var operationsFiltered: LiveData<List<Operation>> = MutableLiveData()

    init {
        operationsRepository = OperationsRepositoryIml(application)
        operations = operationsRepository.getAll()
    }

    fun insertOperation(item: Operation) {
        operationsRepository.insert(item)
    }

    fun getAllOperations(): LiveData<List<Operation>> {
        return operations
    }

    fun deleteOperation(item: Operation) {
        operationsRepository.delete(item)
    }

    fun updateOperation(item: Operation) {
        operationsRepository.update(item)
    }

    fun updateOperation() {
        operationsRepository.updateAll()
    }

    fun getByDate(itemId: Long, date: Long): LiveData<List<Operation>> {
        operationsFiltered = operationsRepository.getByDate(itemId = itemId, date = date)
        return operationsFiltered
    }

}