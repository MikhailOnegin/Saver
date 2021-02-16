package digital.fact.saver.data.database.classes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import digital.fact.saver.domain.models.Class

@Database(entities = [Class::class], version = 1, exportSchema = false)
abstract class ClassesDb: RoomDatabase() {
    companion object {
        private var db: ClassesDb? = null
        private const val dbName = "classes.db"
        private val lock = Any()

        fun getInstance(context: Context): ClassesDb {
            db?.let {
                return it
            }
            synchronized(lock) {
                val instance = Room.databaseBuilder(
                    context,
                    ClassesDb::class.java,
                    dbName
                ).build()
                db = instance
                return instance
            }
        }
    }
    abstract fun classesDao(): ClassesDao
}