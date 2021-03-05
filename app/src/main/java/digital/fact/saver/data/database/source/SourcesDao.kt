package digital.fact.saver.data.database.source

import androidx.lifecycle.LiveData
import androidx.room.*
import digital.fact.saver.domain.models.Source

@Dao
interface SourcesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Source)

    @Delete
    fun delete(item: Source)

    @Update
    fun update(item: Source)

    @Query("DELETE FROM SOURCES")
    fun deleteAll()

    @Query("SELECT * FROM SOURCES")
    fun getAll(): List<Source>
}