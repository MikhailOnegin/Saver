package digital.fact.saver.data.database.plan

import androidx.lifecycle.LiveData
import androidx.room.*
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.domain.models.Plan

@Dao
interface PlansDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Plan)

    @Delete
    suspend fun delete(item: Plan)

    @Update
    suspend fun update()

    @Query("DELETE FROM PLANS")
    suspend fun deleteAll()

    @Query("SELECT * FROM PLANS")
    suspend fun getAll(): LiveData<List<Plan>>
}