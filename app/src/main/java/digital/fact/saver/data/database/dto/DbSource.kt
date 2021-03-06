package digital.fact.saver.data.database.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SOURCES")
data class DbSource(
    @PrimaryKey(autoGenerate = true)
    val _id: Long = 0L,
    val name: String,
    val type: Int,
    val start_sum: Long,
    val adding_date: Long,
    val aim_sum: Long,
    val aim_date: Long,
    val sort_order: Int,
    val visibility: Int
) {

    enum class Type(val value: Int) {
        ACTIVE(0),
        SAVER(1),
        INACTIVE(2)
    }

    enum class Visibility(val value: Int) {
        VISIBLE(0),
        INVISIBLE(1)
    }

    enum class SourceType {
        WALLET,
        SAVER
    }

}