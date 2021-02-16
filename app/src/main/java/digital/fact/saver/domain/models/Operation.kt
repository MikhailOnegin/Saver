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
        val `class`: Int,
        val comment: String
) {
        constructor(category: Int, name: String, date: Int, adding_date: Int, sum: Int, from_source: Int,
                    to_source: Int, plain_id: Int, `class`: Int, comment: String
                    ) : this(
                0, category, name, date, adding_date, sum, from_source,
                to_source, plain_id, `class`, comment
        )
}