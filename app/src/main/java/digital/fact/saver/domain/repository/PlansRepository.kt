package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.domain.models.Plan

interface PlansRepository {
    fun insert(item: Plan)

    fun update()

    fun delete(item: Plan)

    fun deleteAll()

    fun getAll(): LiveData<List<Plan>>
}