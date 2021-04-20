package digital.fact.saver.data.database.dao

import androidx.room.*
import digital.fact.saver.data.database.dto.DbPlan

@Dao
interface PlansDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: DbPlan): Long

    @Delete
    fun delete(item: DbPlan): Int

    @Query("DELETE FROM PLANS WHERE ID = :id")
    fun deleteById(id: Long)

    @Update
    fun update(item: DbPlan): Int

    @Query("DELETE FROM PLANS")
    fun deleteAll(): Int

    @Query("SELECT * FROM PLANS WHERE id = :planId")
    fun getPlan(planId: Long): DbPlan?

    @Query("SELECT * FROM PLANS")
    fun getAll(): List<DbPlan>

    @Query("SELECT * FROM PLANS WHERE (:periodStart <= planning_date AND planning_date < :periodEnd) OR planning_date = 0")
    fun getPlansForAPeriod(periodStart: Long, periodEnd: Long): List<DbPlan>

    @Query("SELECT * FROM PLANS WHERE ((:periodStart <= planning_date AND planning_date < :periodEnd) OR planning_date = 0) AND operation_id = 0")
    fun getCurrentPlans(periodStart: Long, periodEnd: Long): List<DbPlan>

}