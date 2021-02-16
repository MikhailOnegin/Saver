package digital.fact.saver.data.database.templates

import androidx.lifecycle.LiveData
import androidx.room.*
import digital.fact.saver.domain.models.Source
import digital.fact.saver.domain.models.Template

@Dao
interface TemplatesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Template)

    @Delete
    fun delete(item: Template)

    @Query("DELETE FROM SOURCES" )
    fun deleteAll()

    @Query("SELECT * FROM SOURCES")
    fun getAll(): LiveData<List<Template>>
}