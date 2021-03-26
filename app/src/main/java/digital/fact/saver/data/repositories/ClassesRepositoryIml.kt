package digital.fact.saver.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.data.database.classes.MainDb
import digital.fact.saver.data.database.dao.ClassesDao
import digital.fact.saver.data.database.dto.Class
import digital.fact.saver.domain.repository.ClassesRepository
import kotlinx.coroutines.*

class ClassesRepositoryIml(context: Context): ClassesRepository {

    private var classesDao: ClassesDao
    private val _classes: MutableLiveData<List<Class>> = MutableLiveData()
    private val classes: LiveData<List<Class>> = _classes

    init {
        val db = MainDb.getInstance(context)
        classesDao = db.classesDao()
        CoroutineScope(Dispatchers.IO).launch {
            val classes = classesDao.getAll()
            _classes.postValue(classes)
        }
    }

    override fun insert(item: Class): LiveData<Long> {
        val result: MutableLiveData<Long> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(classesDao.insert(item))
        }
        return result
    }


    override fun update(item: Class): LiveData<Int> {
        val result = MutableLiveData<Int>()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(classesDao.update(item))
        }
        return result
    }


    override fun delete(item: Class): LiveData<Int> {
        val result = MutableLiveData<Int>()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(classesDao.delete(item))
        }
        return result
    }

    override fun deleteAll(): LiveData<Int> {
        val result = MutableLiveData<Int>()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(classesDao.deleteAll())
        }
        return result
    }

    override fun getAll(): LiveData<List<Class>> {
        return classes
    }

    override fun updateAll(): LiveData <List<Class>> {
        CoroutineScope(Dispatchers.IO).launch {
            val classes = classesDao.getAll()
            _classes.postValue(classes)
        }
        return classes
    }
}