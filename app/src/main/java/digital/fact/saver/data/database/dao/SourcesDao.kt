package digital.fact.saver.data.database.dao

import androidx.room.*
import digital.fact.saver.data.database.dto.Source

@Dao
interface SourcesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Source): Long

    @Delete
    fun delete(item: Source): Int

    @Update
    fun update(item: Source): Int

    @Query("DELETE FROM SOURCES")
    fun deleteAll(): Int

    @Query("SELECT * FROM SOURCES")
    fun getAll(): List<Source>

    @Query("SELECT * FROM SOURCES WHERE adding_date < :date AND (type = 0 OR type = 2) AND visibility = 0")
    fun getWalletsOnDate(date: Long): List<Source>

    @Query("SELECT * FROM SOURCES WHERE adding_date < :date AND type = 1 AND visibility = 0")
    fun getSaversOnDate(date: Long): List<Source>
    
}