package digital.fact.saver.domain.models

import digital.fact.saver.data.database.dto.Operation.OperationType
import digital.fact.saver.data.database.dto.Source
import kotlin.random.Random

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
    itemId = id, itemType = checkType(type, visibility)
) {
    companion object {
        const val TYPE_COUNT_ACTIVE = 1
        const val TYPE_COUNT_INACTIVE = 2
        const val TYPE_SOURCE_ACTIVE = 3
        const val TYPE_SOURCE_INACTIVE = 4
        const val TYPE_SAVER = 5
        const val TYPE_BUTTON_SHOW = 6
        const val TYPE_SOURCE_ACTIVE_HIDED = 7
        const val TYPE_SOURCE_INACTIVE_HIDED = 8
        const val TYPE_SAVER_HIDED = 9

        const val ID_COUNT_ACTIVE = -1L
        const val ID_BUTTON_SHOW = -2L
        const val ID_COUNT_INACTIVE = -3L

        enum class Destination { WALLETS_ACTIVE, WALLETS_INACTIVE, SAVERS }

        fun getTestSourcesList(): List<Sources> {
            val result = mutableListOf<Sources>()
            for (i in 0 until 5) {
                result.add(
                    Sources(
                        id = i + 1L,
                        name = listOf("Альфа-банк", "Сбербанк", "Наличные")[Random.nextInt(3)],
                        type = Source.Type.ACTIVE.value,
                        startSum = 0L,
                        addingDate = 0L,
                        aimSum = 0L,
                        sortOrder = 0,
                        currentSum = listOf(
                            20000000L,
                            5698676L,
                            120300L,
                            10050000000000L
                        )[Random.nextInt(4)],
                        visibility = Source.Visibility.VISIBLE.value
                    )
                )
            }
            return result
        }

    }
}

fun checkType(category: Int, visibility: Int): Int {
    return when (category) {
        Source.Type.ACTIVE.value -> {
            if (visibility == Source.Visibility.VISIBLE.value) Sources.TYPE_SOURCE_ACTIVE
            else Sources.TYPE_SOURCE_ACTIVE_HIDED
        }
        Source.Type.INACTIVE.value -> {
            if (visibility == Source.Visibility.VISIBLE.value) Sources.TYPE_SOURCE_INACTIVE
            else Sources.TYPE_SOURCE_INACTIVE_HIDED
        }
        else -> {
            if (visibility == Source.Visibility.VISIBLE.value) Sources.TYPE_SAVER
            else Sources.TYPE_SAVER_HIDED
        }
    }
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

sealed class SourceItem(
    val itemId: Long,
    val itemType: Int
)

fun List<Source>.toActiveSources(
    operations: List<Operation>?,
    isHidedForShow: Boolean = false,
    needOther: Boolean = true
): List<SourceItem> {
    val activeSources = mutableListOf<SourceItem>()
    var activeSummary = 0L
    for (item in this) {
        if (item.type == Source.Type.ACTIVE.value) {
            val itemSum = countCurrentWalletSum(operations, item._id)
            activeSummary += if (itemSum != 0L) itemSum else item.start_sum
            if (!isHidedForShow && item.visibility == Source.Visibility.INVISIBLE.value) continue
            activeSources.add(
                Sources(
                    id = item._id,
                    name = item.name,
                    type = item.type,
                    startSum = item.start_sum,
                    addingDate = item.adding_date,
                    aimSum = item.aim_sum,
                    sortOrder = item.sort_order,
                    currentSum = itemSum,
                    visibility = item.visibility
                )
            )
        }
    }
    if (needOther) activeSources.add(SourcesActiveCount(activeSummary))
    if (this.any {
            it.visibility == Source.Visibility.INVISIBLE.value &&
                    it.type == Source.Type.ACTIVE.value
        }) activeSources.add(
        SourcesShowHidedWallets(
            isHidedForShow,
            Sources.Companion.Destination.WALLETS_ACTIVE
        )
    )
    return activeSources.sortedBy { it.itemType }
}

fun List<Source>.toInactiveSources(
    operations: List<Operation>?,
    isHidedForShow: Boolean = false,
    needOther: Boolean = true
): List<SourceItem> {
    val inactiveSources = mutableListOf<SourceItem>()
    var inactiveSummary = 0L
    for (item in this) {
        if (item.type == Source.Type.INACTIVE.value) {
            if (!isHidedForShow && item.visibility == Source.Visibility.INVISIBLE.value) continue
            val itemSum = countCurrentWalletSum(operations, item._id)
            inactiveSummary += if (itemSum != 0L) itemSum else item.start_sum

            inactiveSources.add(
                Sources(
                    id = item._id,
                    name = item.name,
                    type = item.type,
                    startSum = item.start_sum,
                    addingDate = item.adding_date,
                    aimSum = item.aim_sum,
                    sortOrder = item.sort_order,
                    currentSum = itemSum,
                    visibility = item.visibility
                )
            )
        }
    }
    if (needOther) inactiveSources.add(SourcesInactiveCount(inactiveSummary))
    if (this.any {
            it.visibility == Source.Visibility.INVISIBLE.value &&
                    it.type == Source.Type.INACTIVE.value
        }) inactiveSources.add(
        SourcesShowHidedWallets(
            isHidedForShow,
            Sources.Companion.Destination.WALLETS_INACTIVE
        )
    )
    return inactiveSources.sortedBy { it.itemType }
}

fun List<Source>.toSavers(
    operations: List<Operation>?,
    isHidedForShow: Boolean = false
): List<SourceItem> {
    val savers = mutableListOf<SourceItem>()
    var saversSummary = 0L
    for (item in this) {
        if (item.type == Source.Type.SAVER.value) {
            if (!isHidedForShow && item.visibility == Source.Visibility.INVISIBLE.value) continue
            var itemSum = countCurrentWalletSum(operations, item._id)
            if (itemSum != 0L) saversSummary += itemSum
            else itemSum = item.start_sum

            savers.add(
                Sources(
                    id = item._id,
                    name = item.name,
                    type = item.type,
                    startSum = item.start_sum,
                    addingDate = item.adding_date,
                    aimSum = item.aim_sum,
                    sortOrder = item.sort_order,
                    currentSum = itemSum,
                    visibility = item.visibility
                )
            )
        }
    }
    if (this.any {
            it.visibility == Source.Visibility.INVISIBLE.value &&
                    it.type == Source.Type.SAVER.value
        }) savers.add(
        SourcesShowHidedWallets(
            isHidedForShow,
            Sources.Companion.Destination.SAVERS
        )
    )
    return savers.sortedBy { it.itemType }
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