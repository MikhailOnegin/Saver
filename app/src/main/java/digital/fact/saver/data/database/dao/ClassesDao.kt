package digital.fact.saver.data.database.dao


import androidx.room.*
import digital.fact.saver.domain.models.Class

@Dao
interface ClassesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Class): Long

    @Delete
    fun delete(item: Class): Int

    @Update
    fun update(item: Class): Int

    @Query("DELETE FROM CLASSES")
    fun deleteAll(): Int

    @Query("SELECT * FROM CLASSES")
    fun getAll(): List<Class>
}