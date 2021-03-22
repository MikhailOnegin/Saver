package digital.fact.saver.data.database.dao


import androidx.room.*
import digital.fact.saver.domain.models.Source

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
}