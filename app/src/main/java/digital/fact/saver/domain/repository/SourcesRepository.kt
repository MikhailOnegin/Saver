package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.domain.models.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface SourcesRepository {
    fun insert(item: Source): LiveData<Long>

    fun update(item: Source): LiveData<Int>

    fun updateAll(): LiveData<List<Source>>

    fun delete(item:Source): LiveData<Int>

    fun deleteAll(): LiveData<Int>

    fun getAll(): LiveData<List<Source>>
}
