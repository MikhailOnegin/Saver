package digital.fact.saver.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.App
import digital.fact.saver.data.database.dao.OperationsDao
import digital.fact.saver.data.database.dto.DbOperation
import digital.fact.saver.domain.repository.OperationsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OperationsRepositoryIml : OperationsRepository {

    private var operationsDao: OperationsDao
    private val _operations: MutableLiveData<List<DbOperation>> = MutableLiveData()
    private val operations: LiveData<List<DbOperation>> = _operations
    private val _operationsFiltered: MutableLiveData<List<DbOperation>> = _operations
    val operationsFiltered: LiveData<List<DbOperation>> = _operationsFiltered

    init {
        val db = App.db
        operationsDao = db.operationsDao()
        CoroutineScope(Dispatchers.IO).launch {
            val operations = operationsDao.getAll()
            this@OperationsRepositoryIml._operations.postValue(operations)
        }
    }

    override fun insert(item: DbOperation): LiveData<Long> {
        val result: MutableLiveData<Long> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(operationsDao.insert(item))
        }
        return result
    }

    override fun update(item: DbOperation): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(operationsDao.update(item))
        }
        return result
    }

    override fun delete(item: DbOperation): LiveData<Int> {
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

    override fun getAll(): LiveData<List<DbOperation>> {
        return operations
    }

    override fun updateAll(): LiveData<List<DbOperation>> {
        CoroutineScope(Dispatchers.IO).launch {
            val operations = operationsDao.getAll()
            this@OperationsRepositoryIml._operations.postValue(operations)
        }
        return operations
    }

    override fun getByDate(itemId: List<Long>, date: Long): LiveData<List<DbOperation>> {
        CoroutineScope(Dispatchers.IO).launch {
            val filtered = operationsDao.getByDate(itemId = itemId, date = date)
            this@OperationsRepositoryIml._operationsFiltered.postValue(filtered)
        }
        return operationsFiltered
    }

}