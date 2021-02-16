package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.domain.models.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface SourcesRepository {
    fun insert(item: Source)

    fun update(item: Source)

    fun delete(item:Source)

    fun deleteAll()

    fun getAll(): LiveData<List<Source>>
}
