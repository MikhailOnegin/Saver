package digital.fact.saver.data.database.classes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.data.database.operation.OperationsDao
import digital.fact.saver.data.database.operation.OperationsDb
import digital.fact.saver.domain.models.Class
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.domain.repository.ClassesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClassesRepositoryIml: ClassesRepository {

    private var classesDao: ClassesDao
    private val _classes: MutableLiveData<List<Class>> = MutableLiveData()
    private val classes: LiveData<List<Class>> = _classes

    init {
        val db = ClassesDb.getInstance(context)
        classesDao = db.classesDao()
        CoroutineScope(Dispatchers.IO).launch {
            val classes = classesDao.getAll()
            this@ClassesRepositoryIml._classes.postValue(sources.value)
        }
    }

    override fun insert(item: Class) {
        classesDao.insert(item)
    }

    override fun update() {
        CoroutineScope(Dispatchers.IO).launch {
            val classes = classesDao.getAll()
            this@ClassesRepositoryIml._classes.postValue(sources.value)
        }
    }

    override fun delete(item: Class) {
        classesDao.delete(item)
    }

    override fun deleteAll() {
        classesDao.deleteAll()
    }

    override fun getAll(): LiveData<List<Class>> {
        return classes
    }
}