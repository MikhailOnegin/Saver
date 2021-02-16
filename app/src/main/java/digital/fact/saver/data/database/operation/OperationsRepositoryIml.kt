package digital.fact.saver.data.database.operation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.domain.repository.OperationsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OperationsRepositoryIml(context: Context): OperationsRepository {

    private var operationsDao: OperationsDao
    private val _operations: MutableLiveData<List<Operation>> = MutableLiveData()
    private val operations: LiveData<List<Operation>> = _operations

    init {
        val db = OperationsDb.getInstance(context)
        operationsDao = db.operationsDao()
        CoroutineScope(Dispatchers.IO).launch {
            val sources = operationsDao.getAll()
            this@OperationsRepositoryIml._operations.postValue(sources.value)
        }
    }

    override fun insert(item: Operation) {
        CoroutineScope(Dispatchers.IO).launch {
            operationsDao.insert(item)
        }
    }

    override fun update(item: Operation) {
        CoroutineScope(Dispatchers.IO).launch {
            operationsDao.update(item)
        }
    }

    override fun delete(item:Operation) {
        CoroutineScope(Dispatchers.IO).launch {
            operationsDao.delete(item)
        }
    }

    override fun deleteAll() {
        CoroutineScope(Dispatchers.IO).launch {
            operationsDao.deleteAll()
        }
    }

    override fun getAll(): LiveData<List<Operation>> {
        return operations
    }
}