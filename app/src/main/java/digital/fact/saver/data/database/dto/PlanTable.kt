package digital.fact.saver.data.database.dto

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "PLANS")
data class PlanTable(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,
        val type: Int,
        val sum: Long,
        val name: String,
        val operation_id: Long,
        val planning_date: Long
) {

        enum class PlanType(val value: Int) {
                SPENDING(0),
                INCOME(1)
        }

        enum class PlanState(val value:Int) {
                COMPLETED(0),
                NOT_DONE(1)
        }
}