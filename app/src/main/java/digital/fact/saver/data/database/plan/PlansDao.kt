package digital.fact.saver.data.database.plan

import androidx.lifecycle.LiveData
import androidx.room.*
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.domain.models.Plan

@Dao
interface PlansDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Plan)

    @Delete
    fun delete(item: Plan)

    @Update
    fun update(item: Plan)

    @Query("DELETE FROM PLANS")
    fun deleteAll()

    @Query("SELECT * FROM PLANS")
    fun getAll(): List<Plan>
}