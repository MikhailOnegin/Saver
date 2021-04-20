package digital.fact.saver.data.database.classes

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import digital.fact.saver.data.database.dao.*
import digital.fact.saver.data.database.dto.*

@Database(entities = [DbCategory::class, DbOperation::class, DbPlan::class, DbSource::class], version = 2, exportSchema = false)
abstract class MainDatabase: RoomDatabase() {

    abstract fun sourcesDao():SourcesDao

    abstract fun operationsDao(): OperationsDao

    abstract fun plansDao():PlansDao

    abstract fun classesDao(): CategoriesDao

    companion object {

        const val dbName = "main.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE TEMPLATES")
            }
        }

    }

}