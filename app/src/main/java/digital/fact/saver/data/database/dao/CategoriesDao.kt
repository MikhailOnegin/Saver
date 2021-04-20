package digital.fact.saver.data.database.dao


import androidx.room.*
import digital.fact.saver.data.database.dto.DbCategory

@Dao
interface CategoriesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: DbCategory): Long

    @Delete
    fun delete(item: DbCategory): Int

    @Update
    fun update(item: DbCategory): Int

    @Query("DELETE FROM CLASSES")
    fun deleteAll(): Int

    @Query("SELECT * FROM CLASSES")
    fun getAll(): List<DbCategory>
}