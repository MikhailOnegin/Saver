package digital.fact.saver.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "OPERATIONS")
data class Class (
    @PrimaryKey(autoGenerate = true)
    val _id: Int,
    val category: Int,
    val name: String
)