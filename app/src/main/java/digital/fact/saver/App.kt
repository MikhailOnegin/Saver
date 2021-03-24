package digital.fact.saver

import android.app.Application
import androidx.room.Room
import digital.fact.saver.data.database.classes.MainDb
import java.lang.IllegalStateException

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        reference = this
        db = Room.databaseBuilder(
            this,
            MainDb::class.java,
            MainDb.dbName
        ).build()
    }

    @Suppress("unused")
    companion object {

        private var reference: App? = null

        lateinit var db: MainDb

        fun getInstance(): App {
            return reference ?: throw IllegalStateException("App is not initialized.")
        }

        const val DEBUG_TAG = "SAVER_DEBUG"
        val LOG_ENABLED = BuildConfig.DEBUG

    }

}