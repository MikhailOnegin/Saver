package digital.fact.saver.domain.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "OPERATIONS")
data class Operation(
    @PrimaryKey(autoGenerate = true)
    val _id: Int,
    val category: Int,
    val name: String,
    val date: Int,
    val adding_date: Int,
    val sum: Int,
    val from_source: Int,
    val to_source: Int,
    val plain_id: Int,
    val _class: Int,
    val comment: String
) {
    @Ignore
    constructor(
        category: OperationCategory,
        name: String,
        date: Int,
        adding_date: Int,
        sum: Int,
        from_source: Int,
        to_source: Int,
        plain_id: Int,
        _class: Int,
        comment: String
    ) : this(
        0,
        category.value,
        name,
        date,
        adding_date,
        sum,
        from_source,
        to_source,
        plain_id,
        _class,
        comment
    )

    enum class OperationCategory(val value: Int) {
        CONSUMPTION(0),
        ADMISSION(1),
        MOVEMENT(2),
        PLANNED_EXPENDITURE(3),
        SCHEDULED_ADMISSION(4),
        REMOVING_FROM_SAVER(5),
        REPLENISHMENT_SAVER(6)
    }
}