package digital.fact.saver.data.database.operation

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import digital.fact.saver.domain.models.Operation

@Database(entities = [Operation::class], version = 1, exportSchema = false)
abstract class OperationsDb: RoomDatabase(){

    companion object {
        private var db: OperationsDb? = null
        private const val dbName = "operations.db"
        private val lock = Any()

        fun getInstance(context: Context): OperationsDb {
            db?.let {
                return it
            }
            synchronized(lock) {
                val instance = Room.databaseBuilder(
                        context,
                        OperationsDb::class.java,
                        dbName
                ).build()
                db = instance
                return instance
            }
        }
    }
    abstract fun operationsDao(): OperationsDao
}