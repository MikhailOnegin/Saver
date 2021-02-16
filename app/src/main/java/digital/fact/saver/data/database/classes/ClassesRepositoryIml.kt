package digital.fact.saver.data.database.classes

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.domain.models.Class1
import digital.fact.saver.domain.repository.ClassesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClassesRepositoryIml(context: Context): ClassesRepository {

    private var classesDao: ClassesDao
    private val _classes: MutableLiveData<List<Class1>> = MutableLiveData()
    private val classes: LiveData<List<Class1>> = _classes

    init {
        val db = ClassesDb.getInstance(context)
        classesDao = db.classesDao()
        CoroutineScope(Dispatchers.IO).launch {
            val classes = classesDao.getAll()
            this@ClassesRepositoryIml._classes.postValue(classes.value)
        }
    }

    override fun insert(item: Class1) {
        CoroutineScope(Dispatchers.IO).launch {
            classesDao.insert(item)
        }
    }

    override fun update(item: Class1) {
        CoroutineScope(Dispatchers.IO).launch {
            classesDao.update(item)
        }
    }

    override fun delete(item: Class1) {
        CoroutineScope(Dispatchers.IO).launch {
            classesDao.delete(item)
        }
    }

    override fun deleteAll() {
        CoroutineScope(Dispatchers.IO).launch {
            classesDao.deleteAll()
        }
    }

    override fun getAll(): LiveData<List<Class1>> {
        return classes
    }
}