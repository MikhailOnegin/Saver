package digital.fact.saver.data.database.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "OPERATIONS")
data class DbOperation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: Int,
    val name: String,
    val operation_date: Long,
    val adding_date: Long,
    val sum: Long,
    val from_source_id: Long,
    val to_source_id: Long,
    val plan_id: Long,
    val category_id: Long,
    val comment: String
) {

    @Suppress("unused")
    enum class OperationType(val value: Int) {
        EXPENSES(0),
        INCOME(1),
        TRANSFER(2),
        PLANNED_EXPENSES(3),
        PLANNED_INCOME(4),
        SAVER_EXPENSES(5),
        SAVER_INCOME(6)
    }

}