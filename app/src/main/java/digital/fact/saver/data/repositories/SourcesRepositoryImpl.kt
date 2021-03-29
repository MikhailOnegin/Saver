package digital.fact.saver.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.data.database.classes.MainDb
import digital.fact.saver.data.database.dao.SourcesDao
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.domain.repository.SourcesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SourcesRepositoryImpl(context:Context): SourcesRepository {
    private var sourcesDao: SourcesDao
    private val _sources: MutableLiveData<List<Source>> = MutableLiveData()
    private val sources: LiveData<List<Source>> = _sources

    init {
        val db = MainDb.getInstance(context)
        sourcesDao = db.sourcesDao()
        CoroutineScope(Dispatchers.IO).launch {
            val sources = sourcesDao.getAll()
            this@SourcesRepositoryImpl._sources.postValue(sources)
        }
    }
    override fun insert(item: Source): LiveData<Long> {
        val result: MutableLiveData<Long> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(   sourcesDao.insert(item))
        }
        return result
    }

    override fun update(item: Source): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue( sourcesDao.update(item))
        }
        return result
    }

    override fun delete(item: Source): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue( sourcesDao.delete(item))
        }
        return result
    }

    override fun deleteAll(): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(sourcesDao.deleteAll())
        }
        return result
    }

    override fun getAll(): LiveData<List<Source>> {
        return sources
    }

    override fun updateAll(): LiveData<List<Source>> {
        CoroutineScope(Dispatchers.IO).launch {
            val classes = sourcesDao.getAll()
            this@SourcesRepositoryImpl._sources.postValue(classes)
        }
        return sources
    }
}