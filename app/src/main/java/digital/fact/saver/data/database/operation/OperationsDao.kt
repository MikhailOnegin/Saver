package digital.fact.saver.data.database.operation

import androidx.lifecycle.LiveData
import androidx.room.*
import digital.fact.saver.domain.models.Operation

@Dao
interface OperationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Operation)

    @Delete
    fun delete(item: Operation)

    @Update
    fun update(item: Operation)

    @Query("DELETE FROM OPERATIONS")
    fun deleteAll()

    @Query("SELECT * FROM OPERATIONS")
    fun getAll(): LiveData<List<Operation>>
}