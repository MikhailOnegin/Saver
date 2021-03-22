package digital.fact.saver

import android.app.Application
import java.lang.IllegalStateException

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        reference = this
    }

    @Suppress("unused")
    companion object {

        private var reference: App? = null

        fun getInstance(): App {
            return reference ?: throw IllegalStateException("App is not initialized.")
        }

        const val DEBUG_TAG = "SAVER_DEBUG"
        val LOG_ENABLED = BuildConfig.DEBUG

    }

}