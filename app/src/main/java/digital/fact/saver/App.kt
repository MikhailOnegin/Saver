package digital.fact.saver

import android.app.Application
import androidx.room.Room
import digital.fact.saver.data.database.classes.MainDatabase
import java.lang.IllegalStateException

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        reference = this
        createNewDatabase()
    }

    fun createNewDatabase() {
        db = Room.databaseBuilder(
            this,
            MainDatabase::class.java,
            MainDatabase.dbName
        )
            .addMigrations(MainDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration().build()
    }

    @Suppress("unused")
    companion object {

        private var reference: App? = null

        lateinit var db: MainDatabase

        fun getInstance(): App {
            return reference ?: throw IllegalStateException("App is not initialized.")
        }

        const val DEBUG_TAG = "SAVER_DEBUG"
        val LOG_ENABLED = BuildConfig.DEBUG

    }

}