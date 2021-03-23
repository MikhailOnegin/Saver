package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.domain.models.Operation

interface OperationsRepository {
    fun insert(item: Operation): LiveData<Long>

    fun update(item: Operation): LiveData<Int>

    fun updateAll(): LiveData<List<Operation>>

    fun delete(item: Operation): LiveData<Int>

    fun deleteAll(): LiveData<Int>

    fun getAll(): LiveData<List<Operation>>

    fun getByDate(itemId: Long, date: Long): LiveData<List<Operation>>
}
