package digital.fact.saver.domain.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "TEMPLATES")
data class Template (
    @PrimaryKey
    val _id: Int,
    val category: Int,
    val name: String,
    val sum: Int,
    val from_source: Int,
    val to_source: Int,
    val `class`: Int
){
    constructor(category: Int, name: String, sum: Int, from_source: Int, to_source: Int, `class`: Int) : this(
            0, category, name, sum, from_source, to_source, `class`
    )
}