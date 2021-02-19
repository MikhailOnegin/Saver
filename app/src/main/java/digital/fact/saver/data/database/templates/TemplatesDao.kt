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

    @Update
    fun update(item: Template)

    @Query("DELETE FROM TEMPLATES")
    fun deleteAll()

    @Query("SELECT * FROM TEMPLATES")
    fun getAll(): List<Template>
}