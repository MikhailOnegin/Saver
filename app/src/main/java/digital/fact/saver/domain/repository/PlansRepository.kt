package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.data.database.dto.PlanTable

interface PlansRepository {
    fun insert(item: PlanTable): LiveData<Long>

    fun update(item: PlanTable): LiveData<Int>

    fun updateAll(): LiveData<List<PlanTable>>

    fun delete(item: PlanTable): LiveData<Int>

    fun deleteAll(): LiveData<Int>

    fun getAll(): LiveData<List<PlanTable>>

    fun getPlansByPeriod(periodStart: Long, periodEnd: Long): LiveData<List<PlanTable>>
}