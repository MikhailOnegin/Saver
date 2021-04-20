package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.data.database.dto.DbPlan

interface PlansRepository {
    fun insert(item: DbPlan): LiveData<Long>

    fun update(item: DbPlan): LiveData<Int>

    fun updateAll(): LiveData<List<DbPlan>>

    fun delete(item: DbPlan): LiveData<Int>

    fun deleteAll(): LiveData<Int>

    fun getAll(): LiveData<List<DbPlan>>

    fun getPlansByPeriod(periodStart: Long, periodEnd: Long): LiveData<List<DbPlan>>
}