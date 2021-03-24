package digital.fact.saver.data.database.classes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import digital.fact.saver.data.database.dao.*
import digital.fact.saver.domain.models.*

@Database(entities = [Class::class, Operation::class, Plan::class, Source::class, Template::class], version = 1, exportSchema = false)
abstract class MainDb: RoomDatabase() {
    companion object {
        private var db: MainDb? = null
        const val dbName = "main.db"
        private val lock = Any()

        fun getInstance(context: Context): MainDb {
            db?.let {
                return it
            }
            synchronized(lock) {
                val instance = Room.databaseBuilder(
                    context,
                    MainDb::class.java,
                    dbName
                ).build()
                db = instance
                return instance
            }
        }
    }
    abstract fun classesDao(): ClassesDao
    abstract fun operationsDao(): OperationsDao
    abstract fun plansDao():PlansDao
    abstract fun sourcesDao():SourcesDao
    abstract fun templatesDao(): TemplatesDao
}