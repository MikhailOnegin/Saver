package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.domain.models.Source

interface OperationsRepository {
    fun insert(item: Operation)

    fun update(item: Operation)

    fun updateAll()

    fun delete(item: Operation)

    fun deleteAll()

    fun getAll(): LiveData<List<Operation>>
}
