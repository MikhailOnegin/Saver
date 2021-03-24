package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.domain.models.Plan

interface PlansRepository {
    fun insert(item: Plan): LiveData<Long>

    fun update(item: Plan): LiveData<Int>

    fun updateAll(): LiveData<List<Plan>>

    fun delete(item: Plan): LiveData<Int>

    fun deleteAll(): LiveData<Int>

    fun getAll(): LiveData<List<Plan>>

    fun getPlansByPeriod(periodStart: Long, periodEnd: Long): LiveData<List<Plan>>
}