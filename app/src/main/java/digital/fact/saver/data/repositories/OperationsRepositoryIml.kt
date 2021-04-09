package digital.fact.saver.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.data.database.classes.MainDb
import digital.fact.saver.data.database.dao.OperationsDao
import digital.fact.saver.data.database.dto.Operation
import digital.fact.saver.domain.repository.OperationsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OperationsRepositoryIml(context: Context) : OperationsRepository {

    private var operationsDao: OperationsDao
    private val _operations: MutableLiveData<List<Operation>> = MutableLiveData()
    private val operations: LiveData<List<Operation>> = _operations
    private val _operationsFiltered: MutableLiveData<List<Operation>> = _operations
    val operationsFiltered: LiveData<List<Operation>> = _operationsFiltered

    init {
        val db =
            MainDb.getInstance(
                context
            )
        operationsDao = db.operationsDao()
        CoroutineScope(Dispatchers.IO).launch {
            val operations = operationsDao.getAll()
            this@OperationsRepositoryIml._operations.postValue(operations)
        }
    }

    override fun insert(item: Operation): LiveData<Long> {
        val result: MutableLiveData<Long> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(operationsDao.insert(item))
        }
        return result
    }

    override fun update(item: Operation): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(operationsDao.update(item))
        }
        return result
    }

    override fun delete(item: Operation): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(operationsDao.delete(item))
        }
        return result
    }

    override fun deleteAll(): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(operationsDao.deleteAll())
        }
        return result
    }

    override fun getAll(): LiveData<List<Operation>> {
        return operations
    }

    override fun updateAll(): LiveData<List<Operation>> {
        CoroutineScope(Dispatchers.IO).launch {
            val operations = operationsDao.getAll()
            this@OperationsRepositoryIml._operations.postValue(operations)
        }
        return operations
    }

    override fun getByDate(itemId: List<Long>, date: Long): LiveData<List<Operation>> {
        CoroutineScope(Dispatchers.IO).launch {
            val filtered = operationsDao.getByDate(itemId = itemId, date = date)
            this@OperationsRepositoryIml._operationsFiltered.postValue(filtered)
        }
        return operationsFiltered
    }

}