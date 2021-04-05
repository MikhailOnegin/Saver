package digital.fact.saver.data.database.dao

import androidx.room.*
import digital.fact.saver.data.database.dto.Operation

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

    @Query("SELECT * FROM OPERATIONS WHERE operation_date >= :startTimeInMillis AND operation_date < :endTimeInMillis ORDER BY adding_date DESC")
    fun getOperationsForADateRange(startTimeInMillis: Long, endTimeInMillis: Long): List<Operation>

    @Query("SELECT * FROM OPERATIONS WHERE operation_date < :date")
    fun getAllOperationsUntilADate(date: Long): List<Operation>

}