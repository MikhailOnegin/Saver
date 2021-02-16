package digital.fact.saver.data.database.source

import androidx.lifecycle.LiveData
import androidx.room.*
import digital.fact.saver.domain.models.Source

@Dao
interface SourcesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Source)

    @Delete
    suspend fun delete(item: Source)

    @Update
    suspend fun update()

    @Query("DELETE FROM SOURCES")
    suspend fun deleteAll()

    @Query("SELECT * FROM SOURCES")
    suspend fun getAll(): LiveData<List<Source>>
}