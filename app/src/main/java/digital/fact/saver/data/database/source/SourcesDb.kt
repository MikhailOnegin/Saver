package digital.fact.saver.data.database.source


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import digital.fact.saver.domain.models.Source

@Database(entities = [Source::class], version = 1, exportSchema = false)
abstract  class SourcesDb: RoomDatabase() {


companion object {
    private var db: SourcesDb? = null
    private const val dbName = "sources.db"
    private val lock = Any()

    fun getInstance(context: Context): SourcesDb {
        db?.let {
            return it
        }
        synchronized(lock) {
            val instance = Room.databaseBuilder(
                context,
                SourcesDb::class.java,
                dbName
            ).build()
            db = instance
            return instance
        }
    }
}
    abstract fun sourceDao(): SourcesDao
}