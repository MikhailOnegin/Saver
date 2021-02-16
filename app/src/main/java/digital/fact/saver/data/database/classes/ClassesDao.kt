package digital.fact.saver.data.database.classes

import androidx.lifecycle.LiveData
import androidx.room.*
import digital.fact.saver.domain.models.Class

@Dao
interface ClassesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Class)

    @Delete
    suspend fun delete(item: Class)

    @Update
    suspend fun update()

    @Query("DELETE FROM CLASSES")
    suspend fun deleteAll()

    @Query("SELECT * FROM CLASSES")
    suspend fun getAll(): LiveData<List<Class>>
}