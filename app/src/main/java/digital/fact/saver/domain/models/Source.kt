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
    constructor(name: String, category: Int, start_sum: Int, adding_date: Int, order_number: Int, visibility: Int) : this(
            0, name, category, start_sum, adding_date, order_number, visibility
    )
}