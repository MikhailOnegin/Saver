package digital.fact.saver.domain.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "SOURCES")
data class Source(
    @PrimaryKey(autoGenerate = true)
    val _id: Int = 0,
    val name: String,
    val type: Int,
    val start_sum: Long = 0,
    val adding_date: Long,
    val aim_sum: Long = 0,
    val sort_order: Int,
    val visibility: Int
) {

    enum class SourceCategory(val value: Int) {
        WALLET_ACTIVE(0),
        WALLET_INACTIVE(1),
        SAVER(2)
    }

    enum class SourceVisibility(val value: Int) {
        VISIBLE(0),
        INVISIBLE(1)
    }
}