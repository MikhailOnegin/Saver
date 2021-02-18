package digital.fact.saver.domain.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "SOURCES")
data class Source (
    @PrimaryKey
    val _id: Int,
    val name: String,
    val category: Int,
    val start_sum: Int = 0,
    val adding_date: Int,
    val order_number: Int,
    val visibility: Int
){
    @Ignore
    constructor(name: String, category: SourceCategory, start_sum: Int, adding_date: Int, order_number: Int, visibility: SourceVisibility
    ):this(
        0, name, category.value, start_sum, adding_date, order_number, visibility.value
    )

    enum class SourceCategory(val value:Int) {
        WALLET_ACTIVE(0),
        WALLET_INACTIVE(1),
        SAVER(2)
    }
    enum class SourceVisibility(val value:Int) {
        VISIBLE (0),
        INVISIBLE (1)
    }
}