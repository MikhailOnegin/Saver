package digital.fact.saver.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import digital.fact.saver.domain.models.Operation

@Dao
interface OperationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Operation): Long

    @Delete
    fun delete(item: Operation): Int

    @Update
    fun update(item: Operation): Int

    @Query("DELETE FROM OPERATIONS")
    fun deleteAll(): Int

    @Query("SELECT * FROM OPERATIONS")
    fun getAll(): List<Operation>
}