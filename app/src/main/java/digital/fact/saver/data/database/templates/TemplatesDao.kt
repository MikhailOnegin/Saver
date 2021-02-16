package digital.fact.saver.data.database.templates

import androidx.lifecycle.LiveData
import androidx.room.*
import digital.fact.saver.domain.models.Source
import digital.fact.saver.domain.models.Template

@Dao
interface TemplatesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Template)

    @Delete
    suspend fun delete(item: Template)

    @Update
    suspend fun update()

    @Query("DELETE FROM TEMPLATES")
    suspend fun deleteAll()

    @Query("SELECT * FROM TEMPLATES")
    suspend fun getAll(): LiveData<List<Template>>
}