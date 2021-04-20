package digital.fact.saver.data.database.dao

import androidx.room.*
import digital.fact.saver.data.database.dto.DbOperation

@Dao
interface OperationsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: DbOperation): Long

    @Delete
    fun delete(item: DbOperation): Int

    @Update
    fun update(item: DbOperation): Int

    @Query("SELECT * FROM OPERATIONS WHERE id = :operationId")
    fun getOperation(operationId: Long): DbOperation?

    @Query("DELETE FROM OPERATIONS")
    fun deleteAll(): Int

    @Query("SELECT * FROM OPERATIONS")
    fun getAll(): List<DbOperation>

    @Query("SELECT * FROM OPERATIONS WHERE (from_source_id IN (:itemId) OR to_source_id IN (:itemId)) AND operation_date < :date")
    fun getByDate(itemId: List<Long>, date: Long): List<DbOperation>

    @Query("SELECT * FROM OPERATIONS WHERE operation_date >= :startTimeInMillis AND operation_date < :endTimeInMillis ORDER BY adding_date DESC")
    fun getOperationsForADateRange(startTimeInMillis: Long, endTimeInMillis: Long): List<DbOperation>

    @Query("SELECT * FROM OPERATIONS WHERE operation_date < :date")
    fun getAllOperationsUntilADate(date: Long): List<DbOperation>

    @Query("SELECT * FROM OPERATIONS WHERE (from_source_id = :sourceId OR to_source_id = :sourceId) AND operation_date < :date")
    fun getAllSourceOperationsUntilADate(sourceId: Long, date: Long): List<DbOperation>

}