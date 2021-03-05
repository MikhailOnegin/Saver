package digital.fact.saver.domain.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "TEMPLATES")
data class Template (
    @PrimaryKey(autoGenerate = true)
    val _id: Int,
    val category: Int,
    val name: String,
    val sum: Int,
    val from_source: Int,
    val to_source: Int,
    val _class: Int
){
    @Ignore
    constructor(category: TemplateCategory, name: String, sum: Int, from_source: Int, to_source: Int, _class: Int
    ):this(
        0, category.value, name, sum, from_source, to_source, _class
    )
    enum class TemplateCategory(val value:Int) {
        CONSUMPTION (0),
        INCOME(1),
        TRANSACTION(2),
        PLANNED_CONSUMPTION(3),
        PLANNED_INCOME(4),
        REMOVAL_FROM_BANK(5),
        DEPOSIT_TO_BANK(6)
    }
}