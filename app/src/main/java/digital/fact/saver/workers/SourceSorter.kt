package digital.fact.saver.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import digital.fact.saver.App
import digital.fact.saver.data.database.dto.DbSource
import digital.fact.saver.utils.getMonthBefore
import digital.fact.saver.utils.resetTimeInMillis
import java.util.*
import kotlin.Exception

class SourceSorter(
    context: Context,
    workerParameters: WorkerParameters
): Worker(context, workerParameters) {

    override fun doWork(): Result {
        try {
            recountSourcesUsage()
            if (App.LOG_ENABLED)
                Log.d(App.DEBUG_TAG, "Sources sorting successfully completed.")
        } catch (exc: Exception) {
            if (App.LOG_ENABLED)
                Log.d(App.DEBUG_TAG, "Exception during sources sorting: $exc")
        }
        return Result.success()
    }

    private fun recountSourcesUsage() {
        val map = mutableMapOf<Long, Int>()
        val sources = App.db.sourcesDao().getAll()
        for (source in sources) map[source._id] = 0
        val end = resetTimeInMillis(Date().time)
        val start = getMonthBefore(Date(end)).time
        val operations = App.db.operationsDao().getOperationsForADateRange(start, end)
        for (operation in operations) {
            if (map.containsKey(operation.to_source_id))
                map[operation.to_source_id] = map[operation.to_source_id]?.inc() ?: 0
            if (map.containsKey(operation.from_source_id))
                map[operation.from_source_id] = map[operation.from_source_id]?.inc() ?: 0
        }
        for (entry in map) {
            val source = sources.firstOrNull { it._id == entry.key }
            source?.run {
                App.db.sourcesDao().update(
                    DbSource(
                        _id = _id,
                        name = name,
                        type = type,
                        start_sum = start_sum,
                        adding_date = adding_date,
                        aim_sum = aim_sum,
                        aim_date = aim_date,
                        sort_order = entry.value,
                        visibility = visibility
                    )
                )
            }
        }
    }

}