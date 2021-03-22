package digital.fact.saver.data.database.dao


import androidx.room.*
import digital.fact.saver.domain.models.Plan

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
}