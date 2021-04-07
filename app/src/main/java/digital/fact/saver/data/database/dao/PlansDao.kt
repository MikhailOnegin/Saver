package digital.fact.saver.data.database.dao


import androidx.room.*
import digital.fact.saver.data.database.dto.PlanTable

@Dao
interface PlansDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: PlanTable): Long

    @Delete
    fun delete(item: PlanTable): Int

    @Query("DELETE FROM PLANS WHERE ID = :id")
    fun deleteById(id: Long)

    @Update
    fun update(item: PlanTable): Int

    @Query("DELETE FROM PLANS")
    fun deleteAll(): Int

    @Query("SELECT * FROM PLANS")
    fun getAll(): List<PlanTable>

    @Query("SELECT * FROM PLANS WHERE :periodStart <= planning_date AND planning_date <= :periodEnd")
    fun getPlansByPeriod(periodStart: Long, periodEnd: Long): List<PlanTable>
}