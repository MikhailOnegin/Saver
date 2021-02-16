package digital.fact.saver.domain.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "OPERATIONS")
data class Operation(
        @PrimaryKey
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
)