package digital.fact.saver.domain.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "CLASSES1")
data class Class (
    @PrimaryKey
    val _id: Int,
    val category: Int,
    val name: String
)