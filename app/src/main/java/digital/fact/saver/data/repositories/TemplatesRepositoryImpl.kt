package digital.fact.saver.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.data.database.classes.MainDb
import digital.fact.saver.data.database.dao.TemplatesDao
import digital.fact.saver.data.database.dto.Template
import digital.fact.saver.domain.repository.TemplatesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TemplatesRepositoryImpl(context: Context): TemplatesRepository {
    private var templatesDao: TemplatesDao
    private val _templates: MutableLiveData<List<Template>> = MutableLiveData()
    private val templates: LiveData<List<Template>> = _templates

    init {
        val db =
            MainDb.getInstance(
                context
            )
        templatesDao = db.templatesDao()
        CoroutineScope(Dispatchers.IO).launch {
            val templates = templatesDao.getAll()
                this@TemplatesRepositoryImpl._templates.postValue(templates)
        }
    }
    override fun insert(item: Template): LiveData<Long> {
        val result: MutableLiveData<Long> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue( templatesDao.insert(item))
        }
        return result
    }

    override fun update(item: Template): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue( templatesDao.update(item))
        }
        return  result
    }

    override fun delete(item: Template): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue( templatesDao.delete(item))
        }
        return result
    }

    override fun deleteAll(): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(templatesDao.deleteAll())
        }
        return result
    }

    override fun getAll(): LiveData<List<Template>> {
        return templates
    }

    override fun updateAll(): LiveData<List<Template>> {
        CoroutineScope(Dispatchers.IO).launch {
            val templates = templatesDao.getAll()
            this@TemplatesRepositoryImpl._templates.postValue(templates)
        }
        return templates
    }
}