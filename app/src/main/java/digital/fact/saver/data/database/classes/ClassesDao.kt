package digital.fact.saver.data.database.classes

import androidx.lifecycle.LiveData
import androidx.room.*
import digital.fact.saver.domain.models.Class

@Dao
interface ClassesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Class)

    @Delete
    fun delete(item: Class)

    @Update
    fun update(item: Class)

    @Query("DELETE FROM CLASSES")
    fun deleteAll()

    @Query("SELECT * FROM CLASSES")
    fun getAll(): List<Class>
}