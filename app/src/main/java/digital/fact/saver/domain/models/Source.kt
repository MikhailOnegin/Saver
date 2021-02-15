package digital.fact.saver.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import digital.fact.saver.ustils.enums.source.SourceVisibility

@Entity(tableName = "SOURCES")
data class Source (
    @PrimaryKey(autoGenerate = true)
    val _id: Int,
    val name: String,
    val category: Int,
    val start_sum: Int = 0,
    val adding_date: Int,
    val order_number: Int,
    val visibility: Int
)