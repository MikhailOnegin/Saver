package digital.fact.saver.data.database.plan

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import digital.fact.saver.data.database.operation.OperationsDao
import digital.fact.saver.data.database.operation.OperationsDb
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.domain.models.Plan

@Database(entities = [Plan::class], version = 1, exportSchema = false)
abstract class PlansDb: RoomDatabase() {
    companion object {
        private var db: PlansDb? = null
        private const val dbName = "plans.db"
        private val lock = Any()

        fun getInstance(context: Context): PlansDb {
            db?.let {
                return it
            }
            synchronized(lock) {
                val instance = Room.databaseBuilder(
                    context,
                    PlansDb::class.java,
                    dbName
                ).build()
                db = instance
                return instance
            }
        }
    }
    abstract fun plansDao(): PlansDao
}