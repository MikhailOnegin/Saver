package digital.fact.saver.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.data.database.templates.TemplatesDao
import digital.fact.saver.data.database.templates.TemplatesDb
import digital.fact.saver.domain.models.Template
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
            TemplatesDb.getInstance(
                context
            )
        templatesDao = db.templatesDao()
        CoroutineScope(Dispatchers.IO).launch {
            val templates = templatesDao.getAll()
                this@TemplatesRepositoryImpl._templates.postValue(templates)
        }
    }
    override fun insert(item: Template) {
        CoroutineScope(Dispatchers.IO).launch {
            templatesDao.insert(item)
        }
    }

    override fun update(item: Template) {
        CoroutineScope(Dispatchers.IO).launch {
           templatesDao.update(item)
        }
    }

    override fun delete(item: Template) {
        CoroutineScope(Dispatchers.IO).launch {
            templatesDao.delete(item)
        }
    }

    override fun deleteAll() {
        CoroutineScope(Dispatchers.IO).launch {
            templatesDao.deleteAll()
        }
    }

    override fun getAll(): LiveData<List<Template>> {
        return templates
    }

    override fun updateAll() {
        CoroutineScope(Dispatchers.IO).launch {
            val templates = templatesDao.getAll()
            this@TemplatesRepositoryImpl._templates.postValue(templates)
        }
    }
}