package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.data.database.dto.DbOperation

interface OperationsRepository {
    fun insert(item: DbOperation): LiveData<Long>

    fun update(item: DbOperation): LiveData<Int>

    fun updateAll(): LiveData<List<DbOperation>>

    fun delete(item: DbOperation): LiveData<Int>

    fun deleteAll(): LiveData<Int>

    fun getAll(): LiveData<List<DbOperation>>

    fun getByDate(itemId: List<Long>, date: Long): LiveData<List<DbOperation>>
}
