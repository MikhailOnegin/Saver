package digital.fact.saver.data.database.operation

import androidx.lifecycle.LiveData
import androidx.room.*
import digital.fact.saver.domain.models.Operation

@Dao
interface OperationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Operation)

    @Delete
    suspend fun delete(item: Operation)

    @Update
    suspend fun update()

    @Query("DELETE FROM OPERATIONS")
    suspend fun deleteAll()

    @Query("SELECT * FROM OPERATIONS")
    suspend fun getAll(): LiveData<List<Operation>>
}