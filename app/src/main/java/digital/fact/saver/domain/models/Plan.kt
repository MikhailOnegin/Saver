package digital.fact.saver.domain.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "PLANS")
data class Plan(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,
        val type: Int,
        val sum: Long,
        val name: String,
        val operation_id: Int,
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