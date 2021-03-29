package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.data.database.dto.Source

interface SourcesRepository {
    fun insert(item: Source): LiveData<Long>

    fun update(item: Source): LiveData<Int>

    fun updateAll(): LiveData<List<Source>>

    fun delete(item: Source): LiveData<Int>

    fun deleteAll(): LiveData<Int>

    fun getAll(): LiveData<List<Source>>
}
