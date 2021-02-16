package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.domain.models.Operation

interface PlansRepository {
    fun insert(item: Operation)

    fun update()

    fun delete(item: Operation)

    fun deleteAll()

    fun getAll(): LiveData<List<Operation>>
}