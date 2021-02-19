package digital.fact.saver.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.data.database.source.SourcesDao
import digital.fact.saver.data.database.source.SourcesDb
import digital.fact.saver.domain.models.Source
import digital.fact.saver.domain.repository.SourcesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SourcesRepositoryImpl(context:Context): SourcesRepository {
    private var sourcesDao: SourcesDao
    private val _sources: MutableLiveData<List<Source>> = MutableLiveData()
    private val sources: LiveData<List<Source>> = _sources

    init {
        val db =
            SourcesDb.getInstance(
                context
            )
        sourcesDao = db.sourceDao()
        CoroutineScope(Dispatchers.IO).launch {
            val sources = sourcesDao.getAll()
            this@SourcesRepositoryImpl._sources.postValue(sources)
        }
    }
    override fun insert(item: Source) {
        CoroutineScope(Dispatchers.IO).launch {
            sourcesDao.insert(item)
        }
    }

    override fun update(item: Source) {
        CoroutineScope(Dispatchers.IO).launch {
            sourcesDao.update(item)
        }
    }

    override fun delete(item:Source) {
        CoroutineScope(Dispatchers.IO).launch {
            sourcesDao.delete(item)
        }
    }

    override fun deleteAll() {
        CoroutineScope(Dispatchers.IO).launch {
            sourcesDao.deleteAll()
        }
    }

    override fun getAll(): LiveData<List<Source>> {
        return sources
    }

    override fun updateAll() {
        CoroutineScope(Dispatchers.IO).launch {
            val classes = sourcesDao.getAll()
            this@SourcesRepositoryImpl._sources.postValue(classes)
        }
    }
}