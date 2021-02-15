package digital.fact.saver.data.database.templates

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.data.database.source.SourcesDao
import digital.fact.saver.data.database.source.SourcesDb
import digital.fact.saver.domain.models.Source
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
        val db = TemplateDb.getInstance(context)
        templatesDao = db.templatesDao()
        CoroutineScope(Dispatchers.IO).launch {
            val sources = templatesDao.getAll()
                this@TemplatesRepositoryImpl._templates.postValue(sources.value)
        }
    }
    override fun insert(item: Template) {
        templatesDao.insert(item)
    }

    override fun update() {
        CoroutineScope(Dispatchers.IO).launch {
            val sources = templatesDao.getAll()
                this@TemplatesRepositoryImpl._templates.postValue(sources.value)
        }
    }

    override fun delete(item: Template) {
        templatesDao.delete(item)
    }

    override fun deleteAll() {
        templatesDao.deleteAll()
    }

    override fun getAll(): LiveData<List<Template>> {
        return templates
    }
}