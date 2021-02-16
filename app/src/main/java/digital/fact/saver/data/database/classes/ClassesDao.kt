package digital.fact.saver.data.database.classes

import androidx.lifecycle.LiveData
import androidx.room.*
import digital.fact.saver.domain.models.Class1

@Dao
interface ClassesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Class1)

    @Delete
    fun delete(item: Class1)

    @Update
    fun update(item: Class1)

    @Query("DELETE FROM CLASSES1")
    fun deleteAll()

    @Query("SELECT * FROM CLASSES1")
    fun getAll(): LiveData<List<Class1>>
}