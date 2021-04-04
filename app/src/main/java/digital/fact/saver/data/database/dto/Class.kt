package digital.fact.saver.data.database.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CLASSES")
data class Class (
    @PrimaryKey(autoGenerate = true)
    val _id: Long,
    val category: Int,
    val name: String
)
