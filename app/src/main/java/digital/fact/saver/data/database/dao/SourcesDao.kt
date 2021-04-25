package digital.fact.saver.data.database.dao

import androidx.room.*
import digital.fact.saver.data.database.dto.DbSource

@Dao
interface SourcesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: DbSource): Long

    @Delete
    fun delete(item: DbSource): Int

    @Update
    fun update(item: DbSource): Int

    @Query("DELETE FROM SOURCES WHERE _id = :sourceId")
    fun deleteSource(sourceId: Long)

    @Query("DELETE FROM SOURCES")
    fun deleteAll(): Int

    @Query("SELECT * FROM SOURCES WHERE _id = :sourceId")
    fun getSource(sourceId: Long): DbSource

    @Query("SELECT * FROM SOURCES")
    fun getAll(): List<DbSource>

    @Query("SELECT * FROM SOURCES WHERE adding_date < :date AND (type = 0 OR type = 2) AND visibility = 0")
    fun getVisibleWalletsUntilDate(date: Long): List<DbSource>

    @Query("SELECT * FROM SOURCES WHERE adding_date < :date AND (type = 0 OR type = 2)")
    fun getAllWalletsUntilDate(date: Long): List<DbSource>

    @Query("SELECT * FROM SOURCES WHERE adding_date < :date AND type = 1 AND visibility = 0")
    fun getVisibleSaversUntilDate(date: Long): List<DbSource>

    @Query("SELECT * FROM SOURCES WHERE adding_date < :date AND type = 1")
    fun getAllSaversUntilDate(date: Long): List<DbSource>
    
}