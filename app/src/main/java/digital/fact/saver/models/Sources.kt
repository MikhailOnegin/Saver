package digital.fact.saver.models

import digital.fact.saver.domain.models.Operation.OperationType
import digital.fact.saver.domain.models.Source

data class Sources(
    val id: Long,
    val name: String,
    val type: Int,
    val startSum: Long = 0L,
    val addingDate: Long,
    val aimSum: Long = 0,
    val sortOrder: Int,
    val currentSum: Long,
    val visibility: Int
) : SourceItem(
    itemId = id, itemType = checkType(type),
    isVisible = checkVisibility(visibility)
) {
    companion object {
        const val TYPE_COUNT_ACTIVE = 1
        const val TYPE_SOURCE_ACTIVE = 2
        const val TYPE_SAVER = 3
        const val TYPE_BUTTON_SHOW = 6
        const val TYPE_COUNT_INACTIVE = 4
        const val TYPE_SOURCE_INACTIVE = 5
        const val TYPE_BUTTON_ADD = 7

        const val ID_COUNT_ACTIVE = -1L
        const val ID_BUTTON_SHOW = -2L
        const val ID_COUNT_INACTIVE = -3L
        const val ID_BUTTON_ADD = -4L

        enum class Destination { ADD_WALLET, ADD_SAVER }
    }
}

fun checkType(category: Int): Int {
    return when (category) {
        Source.SourceCategory.WALLET_ACTIVE.value -> Sources.TYPE_SOURCE_ACTIVE
        Source.SourceCategory.WALLET_INACTIVE.value -> Sources.TYPE_SOURCE_INACTIVE
        else -> Sources.TYPE_SAVER
    }
}

fun checkVisibility(visibility: Int): Boolean {
    return visibility == Source.SourceVisibility.VISIBLE.value
}

data class SourcesActiveCount(
    val activeWalletsSum: Long
) : SourceItem(itemId = Sources.ID_COUNT_ACTIVE, itemType = Sources.TYPE_COUNT_ACTIVE)

data class SourcesShowHidedWallets(
    var isHidedShowed: Boolean,
    val destinationSource: Enum<Sources.Companion.Destination>
) : SourceItem(itemId = Sources.ID_BUTTON_SHOW, itemType = Sources.TYPE_BUTTON_SHOW)

data class SourcesInactiveCount(
    val inactiveWalletsSum: Long
) : SourceItem(itemId = Sources.ID_COUNT_INACTIVE, itemType = Sources.TYPE_COUNT_INACTIVE)

data class SourcesAddNewWallet(
    val destinationSource: Enum<Sources.Companion.Destination>
) :
    SourceItem(itemId = Sources.ID_BUTTON_ADD, itemType = Sources.TYPE_BUTTON_ADD)

sealed class SourceItem(
    val itemId: Long,
    val itemType: Int,
    val isVisible: Boolean? = null
)

fun List<Source>.toSources(
    operations: List<Operation>?,
    isShowed: Boolean,
    onlySavers: Boolean = false
): List<SourceItem> {
    val result = mutableListOf<SourceItem>()
    val activeSources = mutableListOf<Sources>()
    val inactiveSources = mutableListOf<Sources>()
    val saversSources = mutableListOf<Sources>()
    val invisibleCount =
        this.count { it.visibility == Source.SourceVisibility.INVISIBLE.value && it.type != Source.SourceCategory.SAVER.value }
    val invisibleSaversCount =
        this.count { it.visibility == Source.SourceVisibility.INVISIBLE.value && it.type == Source.SourceCategory.SAVER.value }
    var activeSum = 0L
    var inactiveSum = 0L
    var inactive = false
    var active = false
    var savers = false

    for (it in this) {
        val currentSum = countCurrentWalletSum(operations, it._id)
        if (it.type == Source.SourceCategory.WALLET_ACTIVE.value) {
            activeSum += if (currentSum == 0L) it.start_sum else currentSum
        } else {
            inactiveSum += if (currentSum == 0L) it.start_sum else currentSum
        }
        if (!isShowed && it.visibility == Source.SourceVisibility.INVISIBLE.value) continue
        when (it.type) {
            Source.SourceCategory.WALLET_ACTIVE.value, Source.SourceCategory.WALLET_INACTIVE.value -> {
                if (it.type == Source.SourceCategory.WALLET_ACTIVE.value) active = true
                else inactive = true
                activeSources.add(
                    Sources(
                        id = it._id,
                        name = it.name,
                        type = it.type,
                        startSum = it.start_sum,
                        addingDate = it.adding_date,
                        aimSum = it.aim_sum,
                        sortOrder = it.sort_order,
                        currentSum = currentSum,
                        visibility = it.visibility
                    )
                )
            }
            Source.SourceCategory.SAVER.value -> {
                savers = true
                saversSources.add(
                    Sources(
                        id = it._id,
                        name = it.name,
                        type = it.type,
                        startSum = it.start_sum,
                        addingDate = it.adding_date,
                        aimSum = it.aim_sum,
                        sortOrder = it.sort_order,
                        currentSum = currentSum,
                        visibility = it.visibility
                    )
                )
            }
        }
    }
    if (onlySavers) {
        result.addAll(saversSources.sortedBy { it.visibility })
        if (invisibleSaversCount != 0 || savers) result.add(SourcesAddNewWallet(Sources.Companion.Destination.ADD_SAVER))
        if (invisibleSaversCount != 0) result.add(
            SourcesShowHidedWallets(
                isHidedShowed = isShowed,
                destinationSource = Sources.Companion.Destination.ADD_SAVER
            )
        )
    } else {
        if (active) result.add(SourcesActiveCount(activeSum))
        if (inactive) result.add(SourcesInactiveCount(inactiveSum))
        result.addAll(activeSources.sortedBy { it.visibility })
        result.addAll(inactiveSources.sortedBy { it.visibility })
        if (invisibleCount != 0 || active || inactive) result.add(SourcesAddNewWallet(Sources.Companion.Destination.ADD_WALLET))
        if (invisibleCount != 0) result.add(
            SourcesShowHidedWallets(
                isHidedShowed = isShowed,
                destinationSource = Sources.Companion.Destination.ADD_WALLET
            )
        )
    }
    return result.sortedBy { it.itemType }
}

fun countCurrentWalletSum(operations: List<Operation>?, id: Long): Long {
    var currentSum = 0L
    val bindedOperations = operations?.filter { it.fromSourceId == id || it.toSourceId == id }
    bindedOperations?.forEach {
        when (it.type) {
            OperationType.EXPENSES.value, OperationType.PLANNED_EXPENSES.value -> currentSum -= it.sum
            OperationType.INCOME.value, OperationType.PLANNED_INCOME.value -> currentSum += it.sum
            OperationType.TRANSFER.value -> if (it.fromSourceId == id) {
                currentSum -= it.sum
            } else if (it.toSourceId == id) {
                currentSum += it.sum
            }
        }
    }
    return currentSum
}