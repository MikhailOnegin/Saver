package digital.fact.saver.data.database.dao


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

    @Query("SELECT * FROM OPERATIONS WHERE (from_source_id = :itemId OR to_source_id = :itemId) AND operation_date <= :date")
    fun getByDate(itemId: Long, date: Long): List<Operation>
}