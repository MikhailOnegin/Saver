package digital.fact.saver.data.database.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PLANS")
data class DbPlan(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,
        val type: Int,
        val sum: Long,
        val name: String,
        val operation_id: Long,
        val planning_date: Long
) {

    enum class PlanType(val value: Int) {
        EXPENSES(0),
        INCOME(1)
    }

}