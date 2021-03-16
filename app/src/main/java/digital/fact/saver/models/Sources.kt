package digital.fact.saver.models

import digital.fact.saver.domain.models.Source

data class Sources(
    val _id: Int,
    val name: String,
    val category: Int,
    val start_sum: Int = 0,
    val adding_date: Long,
    val order_number: Int,
    val current_sum: Int,
    val visibility: Int
) : SourceItem(id = _id, type = Sources.TYPE_SOURCE_ACTIVE) {

    companion object {
        const val TYPE_COUNT_ACTIVE = 1
        const val TYPE_SOURCE_ACTIVE = 2
        const val TYPE_BUTTON_SHOW = 5
        const val TYPE_COUNT_INACTIVE = 3
        const val TYPE_SOURCE_INACTIVE = 4
        const val TYPE_BUTTON_ADD = 6

        const val ID_COUNT_ACTIVE = -1
        const val ID_BUTTON_SHOW = -2
        const val ID_COUNT_INACTIVE = -3
        const val ID_BUTTON_ADD = -4
    }
}

data class SourcesActiveCount(
    val activeWalletsSum: Int
) : SourceItem(id = Sources.ID_COUNT_ACTIVE, type = Sources.TYPE_COUNT_ACTIVE)

data class SourcesShowHidedWallets(
    var isHidedShowed: Boolean = false
) : SourceItem(id = Sources.ID_BUTTON_SHOW, type = Sources.TYPE_BUTTON_SHOW)

data class SourcesInactiveCount(
    val inactiveWalletsSum: Int
) : SourceItem(id = Sources.ID_COUNT_INACTIVE, type = Sources.TYPE_COUNT_INACTIVE)

data class SourcesInactive(
    val _id: Int,
    val name: String,
    val category: Int,
    val start_sum: Int = 0,
    val adding_date: Long,
    val order_number: Int,
    val current_sum: Int,
    val visibility: Int
) : SourceItem(id = _id, type = Sources.TYPE_SOURCE_INACTIVE)

object SourcesAddNewWallet : SourceItem(id = Sources.ID_BUTTON_ADD, type = Sources.TYPE_BUTTON_ADD)

sealed class SourceItem(
    val id: Int,
    val type: Int
)

fun List<Source>.toSources(operations: List<Operations>, isShowed: Boolean): List<SourceItem> {
    val result = mutableListOf<SourceItem>()
    val invisibleCount = this.count { it.visibility == Source.SourceVisibility.INVISIBLE.value }
    var activeSum = 0
    var inactiveSum = 0

    for (it in this) {
        val currentSum = countCurrentWalletSum(operations, it._id)
        if (it.category == Source.SourceCategory.WALLET_ACTIVE.value) {
            activeSum += currentSum
        } else {
            inactiveSum += currentSum
        }
        if (!isShowed && it.visibility == Source.SourceVisibility.INVISIBLE.value) continue
        when (it.category) {
            Source.SourceCategory.WALLET_ACTIVE.value -> {
                result.add(
                    Sources(
                        _id = it._id,
                        name = it.name,
                        category = it.category,
                        start_sum = it.start_sum,
                        adding_date = it.adding_date,
                        order_number = it.order_number,
                        current_sum = currentSum,
                        visibility = it.visibility
                    )
                )
            }
            Source.SourceCategory.WALLET_INACTIVE.value -> {
                result.add(
                    SourcesInactive(
                        _id = it._id,
                        name = it.name,
                        category = it.category,
                        start_sum = it.start_sum,
                        adding_date = it.adding_date,
                        order_number = it.order_number,
                        current_sum = currentSum,
                        visibility = it.visibility
                    )
                )
            }
            Source.SourceCategory.SAVER.value -> {
            }
        }
    }
    result.add(SourcesActiveCount(activeSum))
    if (invisibleCount != 0) result.add(SourcesShowHidedWallets(isShowed))
    result.add(SourcesAddNewWallet)
    if (isShowed) result.add(SourcesInactiveCount(inactiveSum))
    return result.sortedBy { it.type }

}

fun countCurrentWalletSum(operations: List<Operations>, id: Int): Int {
    var currrentSum = 0
    val bindedOperations = operations.filter { it.from_source == id || it.to_source == id }
    bindedOperations.forEach {
        when (it.category) {
            Operations.MINUS, Operations.PLAN_MINUS -> currrentSum -= it.sum
            Operations.PLUS, Operations.PLAN_PLUS -> currrentSum += it.sum
            Operations.TRANSPORT -> if (it.from_source == id) {
                currrentSum -= it.sum
            } else if (it.to_source == id) {
                currrentSum += it.sum
            }
        }
    }
    return currrentSum
}