package digital.fact.saver.data.database.dao


import androidx.room.*
import digital.fact.saver.data.database.dto.Plan

@Dao
interface PlansDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Plan): Long

    @Delete
    fun delete(item: Plan): Int

    @Update
    fun update(item: Plan): Int

    @Query("DELETE FROM PLANS")
    fun deleteAll(): Int

    @Query("SELECT * FROM PLANS")
    fun getAll(): List<Plan>

    @Query("SELECT * FROM PLANS WHERE :periodStart <= planning_date AND planning_date <= :periodEnd")
    fun getPlansByPeriod(periodStart: Long, periodEnd: Long): List<Plan>
}