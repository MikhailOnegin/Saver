package digital.fact.saver.data.database.classes

import androidx.lifecycle.LiveData
import androidx.room.*
import digital.fact.saver.domain.models.Class
import digital.fact.saver.domain.models.Operation

@Dao
interface ClassesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Class)

    @Delete
    fun delete(item: Class)

    @Query("DELETE FROM OPERATIONS" )
    fun deleteAll()

    @Query("SELECT * FROM OPERATIONS")
    fun getAll(): LiveData<List<Class>>
}