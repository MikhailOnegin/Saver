package digital.fact.saver.domain.models

import androidx.room.Entity
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
    val `class`: Int
)