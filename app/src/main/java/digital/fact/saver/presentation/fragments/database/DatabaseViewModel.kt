package digital.fact.saver.presentation.fragments.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import digital.fact.saver.App
import digital.fact.saver.data.database.dto.Class
import digital.fact.saver.data.database.dto.Operation
import digital.fact.saver.data.database.dto.PlanTable
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.data.legacyDatabase.LegacyDbContract.*
import digital.fact.saver.data.legacyDatabase.LegacyDbHelper
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.utils.events.Event
import digital.fact.saver.utils.getMonthAfter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.nio.channels.FileChannel
import java.util.*
import kotlin.Exception

class DatabaseViewModel(
    private val mainVM: MainViewModel
) : ViewModel() {

    private val _importStateEvent = MutableLiveData<Event<State>>()
    val importStateEvent: LiveData<Event<State>> = _importStateEvent

    private val _exportStateEvent = MutableLiveData<Event<State>>()
    val exportStateEvent: LiveData<Event<State>> = _exportStateEvent

    //sergeev: Файл базы данных не всегда содержит последние транзакии.
    fun exportDatabase(currentDb: File, outputUri: Uri) {
        _exportStateEvent.value = Event(State.WORKING)
        viewModelScope.launch(Dispatchers.IO) {
            App.db.openHelper.close()
            var src: FileChannel? = null
            var dst: FileChannel? = null
            try {
                if (currentDb.exists()) {
                    val resolver = App.getInstance().contentResolver
                    val descriptor = resolver.openFileDescriptor(outputUri, "w")?.fileDescriptor
                    src = FileInputStream(currentDb).channel
                    dst = FileOutputStream(descriptor).channel
                    dst.transferFrom(src, 0, src.size())
                    _exportStateEvent.postValue(Event(State.SUCCESS))
                } else _exportStateEvent.postValue(Event(State.ERROR))
            } catch (exc: Exception) {
                _exportStateEvent.postValue(Event(State.ERROR))
            } finally {
                try { src?.close() } catch (exc: Exception) { }
                try { dst?.close() } catch (exc: Exception) { }
            }
        }
    }

    fun importDatabase(currentDb: File, newDBUri: Uri) {
        _importStateEvent.value = Event(State.WORKING)
        viewModelScope.launch(Dispatchers.IO) {
            App.db.openHelper.close()
            val dbDir = File(currentDb.parent ?: "")
            val files = dbDir.list()
            files?.run {
                for (file in files) {
                    val toDelete = File(file)
                    if (toDelete.exists()) toDelete.delete()
                }
            }
            var src: FileChannel? = null
            var dst: FileChannel? = null
            try {
                val contentResolver = App.getInstance().contentResolver
                src = (contentResolver.openInputStream(newDBUri) as FileInputStream).channel
                dst = FileOutputStream(currentDb).channel
                dst.transferFrom(src, 0, src.size())
                _importStateEvent.postValue(Event(State.SUCCESS))
            } catch (exc: Exception) {
                _importStateEvent.postValue(Event(State.ERROR))
            } finally {
                try { src?.close() } catch (exc: Exception) { }
                try { dst?.close() } catch (exc: Exception) { }
                App.getInstance().createNewDatabase()
            }
            mainVM.notifyConditionsChanged()
        }
    }

    fun importLegacyDb(context: Context, uri: Uri) {
        _importStateEvent.value = Event(State.WORKING)
        viewModelScope.launch(Dispatchers.IO) {
            App.db.openHelper.close()
            var src: FileChannel? = null
            var dst: FileChannel? = null
            try {
                val legacyDb = context.getDatabasePath(LegacyDbHelper.DATABASE_NAME)
                legacyDb.parentFile?.mkdirs()
                src =
                    (context.contentResolver.openInputStream(uri) as FileInputStream).channel
                dst = FileOutputStream(legacyDb).channel
                dst.transferFrom(src, 0, src.size())
                transferDataFromLegacyDatabase(context)
                _importStateEvent.postValue(Event(State.SUCCESS))
            } catch (exc: Exception) {
                _importStateEvent.postValue(Event(State.ERROR))
            } finally {
                try { src?.close() } catch (exc: Exception) { }
                try { dst?.close() } catch (exc: Exception) { }
            }
            mainVM.notifyConditionsChanged()
        }
    }

    private fun transferDataFromLegacyDatabase(context: Context) {
        val legacyDb = LegacyDbHelper(context).readableDatabase
        transferSourcesTable(legacyDb)
        transferOperationsTable(legacyDb)
        transferPlansTable(legacyDb)
        transferCategoriesTable(legacyDb)
        deleteLegacyDb(context, legacyDb)
    }

    private fun transferSourcesTable(db: SQLiteDatabase) {
        App.db.sourcesDao().deleteAll()
        val cursor = db.query(
            TSources.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) {
            cursor.run {
                do {
                    val source = Source(
                        _id = getLong(getColumnIndex(TSources._ID)),
                        name = getString(getColumnIndex(TSources.COLUMN_NAME)),
                        type = getInt(getColumnIndex(TSources.COLUMN_CATEGORY)),
                        start_sum = getLong(getColumnIndex(TSources.COLUMN_START_SUM)),
                        adding_date = getLong(getColumnIndex(TSources.COLUMN_ADDING_DATE)),
                        aim_sum = getLong(getColumnIndex(TSources.COLUMN_AIM_SUM)),
                        sort_order = getInt(getColumnIndex(TSources.COLUMN_ORDER)),
                        visibility = getInt(getColumnIndex(TSources.COLUMN_VISIBILITY)),
                        aim_date = getMonthAfter(Date()).time
                    )
                    App.db.sourcesDao().insert(source)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        if (App.LOG_ENABLED) Log.d(App.DEBUG_TAG, "Sources table imported.")
    }

    private fun transferOperationsTable(db: SQLiteDatabase) {
        App.db.operationsDao().deleteAll()
        val cursor = db.query(
            TOperations.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) {
            cursor.run {
                do {
                    val operation = Operation(
                        id = getLong(getColumnIndex(TOperations._ID)),
                        type = getInt(getColumnIndex(TOperations.COLUMN_CATEGORY)),
                        name = getString(getColumnIndex(TOperations.COLUMN_NAME)),
                        operation_date = getLong(getColumnIndex(TOperations.COLUMN_DATE)),
                        adding_date = -getLong(getColumnIndex(TOperations.COLUMN_ADDING_DATE)),
                        sum = getLong(getColumnIndex(TOperations.COLUMN_SUM)),
                        from_source_id = getLong(getColumnIndex(TOperations.COLUMN_FROM_SOURCE)),
                        to_source_id = getLong(getColumnIndex(TOperations.COLUMN_TO_SOURCE)),
                        plan_id = getLong(getColumnIndex(TOperations.COLUMN_PLAN_ID)),
                        category_id = getLong(getColumnIndex(TOperations.COLUMN_CLASS)),
                        comment = getString(getColumnIndex(TOperations.COLUMN_COMMENT))
                    )
                    App.db.operationsDao().insert(operation)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        if (App.LOG_ENABLED) Log.d(App.DEBUG_TAG, "Operations table imported.")
    }

    private fun transferPlansTable(db: SQLiteDatabase) {
        App.db.plansDao().deleteAll()
        val cursor = db.query(
            TPlans.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) {
            cursor.run {
                do {
                    val plan = PlanTable(
                        id = getLong(getColumnIndex(TPlans._ID)),
                        type = getInt(getColumnIndex(TPlans.COLUMN_CATEGORY)),
                        sum = getLong(getColumnIndex(TPlans.COLUMN_SUM)),
                        name = getString(getColumnIndex(TPlans.COLUMN_NAME)),
                        operation_id = getLong(getColumnIndex(TPlans.COLUMN_OPERATION_ID)),
                        planning_date = getLong(getColumnIndex(TPlans.COLUMN_PLANNING_DATE))
                    )
                    App.db.plansDao().insert(plan)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        if (App.LOG_ENABLED) Log.d(App.DEBUG_TAG, "Plans table imported.")
    }

    private fun transferCategoriesTable(db: SQLiteDatabase) {
        App.db.classesDao().deleteAll()
        val cursor = db.query(
            TClasses.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) {
            cursor.run {
                do {
                    val category = Class(
                        _id = getLong(getColumnIndex(TClasses._ID)),
                        category = getInt(getColumnIndex(TClasses.COLUMN_CATEGORY)),
                        name = getString(getColumnIndex(TClasses.COLUMN_NAME))
                    )
                    App.db.classesDao().insert(category)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        if (App.LOG_ENABLED) Log.d(App.DEBUG_TAG, "Categories table imported.")
    }

    private fun deleteLegacyDb(context: Context, db: SQLiteDatabase) {
        db.close()
        val legacyDb = context.getDatabasePath(LegacyDbHelper.DATABASE_NAME)
        legacyDb.delete()
        if (App.LOG_ENABLED) Log.d(App.DEBUG_TAG, "Legacy database deleted.")
    }

    enum class State { WORKING, SUCCESS, ERROR }

    @Suppress("UNCHECKED_CAST")
    class DatabaseVMFactory(
        private val mainVM: MainViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: java.lang.Class<T>): T {
            return DatabaseViewModel(mainVM) as T
        }
    }

}