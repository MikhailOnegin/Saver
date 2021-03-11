package digital.fact.saver.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import digital.fact.saver.domain.models.Source
import digital.fact.saver.domain.models.Template

@Dao
interface TemplatesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Template): Long

    @Delete
    fun delete(item: Template): Int

    @Update
    fun update(item: Template): Int

    @Query("DELETE FROM TEMPLATES")
    fun deleteAll(): Int

    @Query("SELECT * FROM TEMPLATES")
    fun getAll(): List<Template>
}