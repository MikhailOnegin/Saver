package digital.fact.saver.data.database.templates

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import digital.fact.saver.data.database.source.SourcesDao
import digital.fact.saver.data.database.source.SourcesDb
import digital.fact.saver.domain.models.Template

@Database(entities = [Template::class], version = 1, exportSchema = false)
abstract class TemplateDb: RoomDatabase() {
    companion object {
        private var db: TemplateDb? = null
        private const val dbName = "tempalte.db"
        private val lock = Any()

        fun getInstance(context: Context): TemplateDb {
            db?.let {
                return it
            }
            synchronized(lock) {
                val instance = Room.databaseBuilder(
                    context,
                    TemplateDb::class.java,
                    dbName
                ).build()
                db = instance
                return instance
            }
        }
    }
    abstract fun templatesDao(): TemplatesDao
}