package digital.fact.saver.domain.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "PLANS")
data class Plan(
        @PrimaryKey(autoGenerate = true)
        val _id: Int,
        val category: Int,
        val sum: Float,
        val name: String,
        val operation_id: Int,
        val state: Int,
        val planning_date: Long
) {
        @Ignore
        constructor(
                category: PlanCategory, sum: Float, name: String, operation_id: Int, state: Int, planning_date: Long
        ) : this(
                0, category.value, sum, name,  operation_id, state, planning_date
        )

        enum class PlanCategory(val value: Int) {
                SPENDING(0),
                INCOME(1)
        }

        enum class PlanState(val value:Int) {
                COMPLETED(0),
                NOT_DONE(1)
        }
}