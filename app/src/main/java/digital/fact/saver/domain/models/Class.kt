package digital.fact.saver.domain.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "CLASSES")
data class Class (
    @PrimaryKey(autoGenerate = true)
    val _id: Int = 0,
    val category: Int,
    val name: String
){

    @Ignore
    constructor(category: ClassCategory, name: String):this(
        0,  category.value, name
    )
    enum class ClassCategory(val value: Int) {
        COSTS(0),
        INCOME(1)
    }
}
