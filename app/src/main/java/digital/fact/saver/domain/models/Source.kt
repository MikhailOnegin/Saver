package digital.fact.saver.domain.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "SOURCES")
data class Source(
    @PrimaryKey(autoGenerate = true)
    val _id: Int,
    val name: String,
    val category: Int,
    val start_sum: Int = 0,
    val adding_date: Int,
    val order_number: Int,
    val visibility: Int
){
    @Ignore
    constructor(
        name: String,
        category: SourceCategory,
        start_sum: Int,
        adding_date: Int,
        order_number: Int,
        visibility: SourceVisibility
    ) : this(
        0, name, category.value, start_sum, adding_date, order_number, visibility.value
    )

    enum class SourceCategory(val value: Int) {
        WALLET_ACTIVE(0),
        WALLET_INACTIVE(1),
        SAVER(2)
    }

    enum class SourceVisibility(val value: Int) {
        VISIBLE(0),
        INVISIBLE(1)
    }

    companion object {
        const val TYPE_COUNT_ACTIVE = 1
        const val TYPE_SOURCE_ACTIVE = 2
        const val TYPE_BUTTON_SHOW = 3
        const val TYPE_COUNT_INACTIVE = 4
        const val TYPE_SOURCE_INACTIVE = 5
        const val TYPE_BUTTON_ADD = 6

        const val ID_COUNT_ACTIVE = -1
        const val ID_BUTTON_SHOW = -2
        const val ID_COUNT_INACTIVE = -3
        const val ID_BUTTON_ADD = -4

    }
}

data class SourceActiveCount(
    val activeWalletsSum: Int
) : SourceItem(id = Source.ID_COUNT_ACTIVE, type = Source.TYPE_COUNT_ACTIVE)

object SourceShowInactiveWallets : SourceItem(id = Source.ID_BUTTON_SHOW, type = Source.TYPE_BUTTON_SHOW)

data class SourceInactiveCount(
    val inactiveWalletsSum: Int
) : SourceItem(id = Source.ID_COUNT_INACTIVE, type = Source.TYPE_COUNT_INACTIVE)

data class SourceInactive(
    val _id: Int,
    val name: String,
    val category: Int,
    val start_sum: Int = 0,
    val adding_date: Int,
    val order_number: Int,
    val visibility: Int
) : SourceItem(id = _id, type = Source.TYPE_SOURCE_INACTIVE)

data class SourceActive(
    val _id: Int,
    val name: String,
    val category: Int,
    val start_sum: Int = 0,
    val adding_date: Int,
    val order_number: Int,
    val visibility: Int
) : SourceItem(id = _id, type = Source.TYPE_SOURCE_ACTIVE)

object SourceAddNewWallet : SourceItem(id = Source.ID_BUTTON_ADD, type = Source.TYPE_BUTTON_ADD)

sealed class SourceItem(
    val id: Int,
    val type: Int
)

fun List<Source>.toSources(): List<SourceItem> {
    val result = mutableListOf<SourceItem>()
    var activeSum = 0
    var inactiveSum = 0
    result.addAll(this.map {
        if (it.visibility == Source.SourceVisibility.VISIBLE.value) {
            activeSum = +it.start_sum
            SourceActive(
                _id = it._id,
                name = it.name,
                category = it.category,
                start_sum = it.start_sum,
                adding_date = it.adding_date,
                order_number = it.order_number,
                visibility = it.visibility
            )
        } else {
            inactiveSum = +it.start_sum
            SourceInactive(
                _id = it._id,
                name = it.name,
                category = it.category,
                start_sum = it.start_sum,
                adding_date = it.adding_date,
                order_number = it.order_number,
                visibility = it.visibility
            )
        }
    })
    result.add(SourceActiveCount(activeSum))
    result.add(SourceInactiveCount(inactiveSum))
    result.add(SourceShowInactiveWallets)
    result.add(SourceAddNewWallet)
    return result.sortedBy { it.type }
}