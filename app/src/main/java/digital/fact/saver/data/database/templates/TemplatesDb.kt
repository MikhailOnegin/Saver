package digital.fact.saver.data.database.templates

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import digital.fact.saver.domain.models.Template

@Database(entities = [Template::class], version = 1, exportSchema = false)
abstract class TemplatesDb: RoomDatabase() {
    companion object {
        private var db: TemplatesDb? = null
        private const val dbName = "templates.db"
        private val lock = Any()

        fun getInstance(context: Context): TemplatesDb {
            db?.let {
                return it
            }
            synchronized(lock) {
                val instance = Room.databaseBuilder(
                    context,
                    TemplatesDb::class.java,
                    dbName
                ).build()
                db = instance
                return instance
            }
        }
    }
    abstract fun templatesDao(): TemplatesDao
}