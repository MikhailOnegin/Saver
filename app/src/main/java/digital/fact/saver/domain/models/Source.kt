package digital.fact.saver.domain.models

import digital.fact.saver.data.database.dto.DbOperation.OperationType
import digital.fact.saver.data.database.dto.DbSource

data class Source(
        val id: Long,
        val name: String,
        val type: Int,
        val startSum: Long,
        val addingDate: Long,
        val aimSum: Long,
        val aimDate: Long,
        val sortOrder: Int,
        var currentSum: Long,
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

        enum class SourceType { WALLET, SAVER }

        enum class Destination { WALLETS_ACTIVE, WALLETS_INACTIVE, SAVERS }

    }
}

fun checkType(category: Int, visibility: Int): Int {
    return when (category) {
        DbSource.Type.ACTIVE.value -> {
            if (visibility == DbSource.Visibility.VISIBLE.value) Source.TYPE_SOURCE_ACTIVE
            else Source.TYPE_SOURCE_ACTIVE_HIDED
        }
        DbSource.Type.INACTIVE.value -> {
            if (visibility == DbSource.Visibility.VISIBLE.value) Source.TYPE_SOURCE_INACTIVE
            else Source.TYPE_SOURCE_INACTIVE_HIDED
        }
        else -> {
            if (visibility == DbSource.Visibility.VISIBLE.value) Source.TYPE_SAVER
            else Source.TYPE_SAVER_HIDED
        }
    }
}

data class SourcesActiveCount(
        val activeWalletsSum: Long
) : SourceItem(itemId = Source.ID_COUNT_ACTIVE, itemType = Source.TYPE_COUNT_ACTIVE)

data class SourcesShowHidedWallets(
        var isHidedShowed: Boolean,
        val destinationSource: Enum<Source.Companion.Destination>
) : SourceItem(itemId = Source.ID_BUTTON_SHOW, itemType = Source.TYPE_BUTTON_SHOW)

data class SourcesInactiveCount(
        val inactiveWalletsSum: Long
) : SourceItem(itemId = Source.ID_COUNT_INACTIVE, itemType = Source.TYPE_COUNT_INACTIVE)

sealed class SourceItem(
        val itemId: Long,
        val itemType: Int
)

fun List<DbSource>.toActiveSources(
        operations: List<Operation>?,
        isHidedForShow: Boolean = false,
        needOther: Boolean = true
): List<SourceItem> {
    val activeSources = mutableListOf<SourceItem>()
    var activeSummary = 0L
    for (item in this) {
        if (item.type == DbSource.Type.ACTIVE.value) {
            val itemSum = countCurrentWalletSum(operations, item._id) + item.start_sum
            activeSummary += itemSum
            if (!isHidedForShow && item.visibility == DbSource.Visibility.INVISIBLE.value) continue
            activeSources.add(
                    Source(
                            id = item._id,
                            name = item.name,
                            type = item.type,
                            startSum = item.start_sum,
                            addingDate = item.adding_date,
                            aimSum = item.aim_sum,
                            sortOrder = item.sort_order,
                            currentSum = itemSum,
                            visibility = item.visibility,
                            aimDate = item.aim_date
                    )
            )
        }
    }
    if (needOther) activeSources.add(SourcesActiveCount(activeSummary))
    if (this.any {
                it.visibility == DbSource.Visibility.INVISIBLE.value &&
                        it.type == DbSource.Type.ACTIVE.value
            }) activeSources.add(
            SourcesShowHidedWallets(
                    isHidedForShow,
                    Source.Companion.Destination.WALLETS_ACTIVE
            )
    )
    return activeSources.sortedBy { it.itemType }
}

fun List<DbSource>.toInactiveSources(
        operations: List<Operation>?,
        isHidedForShow: Boolean = false,
        needOther: Boolean = true
): List<SourceItem> {
    val inactiveSources = mutableListOf<SourceItem>()
    var inactiveSummary = 0L
    for (item in this) {
        if (item.type == DbSource.Type.INACTIVE.value) {
            val itemSum = countCurrentWalletSum(operations, item._id) + item.start_sum
            inactiveSummary += itemSum
            if (!isHidedForShow && item.visibility == DbSource.Visibility.INVISIBLE.value) continue
            inactiveSources.add(
                    Source(
                            id = item._id,
                            name = item.name,
                            type = item.type,
                            startSum = item.start_sum,
                            addingDate = item.adding_date,
                            aimSum = item.aim_sum,
                            sortOrder = item.sort_order,
                            currentSum = itemSum,
                            visibility = item.visibility,
                            aimDate = item.aim_date
                    )
            )
        }
    }
    if (needOther) inactiveSources.add(SourcesInactiveCount(inactiveSummary))
    if (this.any {
                it.visibility == DbSource.Visibility.INVISIBLE.value &&
                        it.type == DbSource.Type.INACTIVE.value
            }) inactiveSources.add(
            SourcesShowHidedWallets(
                    isHidedForShow,
                    Source.Companion.Destination.WALLETS_INACTIVE
            )
    )
    return inactiveSources.sortedBy { it.itemType }
}

fun List<DbSource>.toSavers(
        operations: List<Operation>?,
        isHidedForShow: Boolean = false
): List<SourceItem> {
    val savers = mutableListOf<SourceItem>()
    for (item in this) {
        if (item.type == DbSource.Type.SAVER.value) {
            if (!isHidedForShow && item.visibility == DbSource.Visibility.INVISIBLE.value) continue
            savers.add(
                    Source(
                            id = item._id,
                            name = item.name,
                            type = item.type,
                            startSum = item.start_sum,
                            addingDate = item.adding_date,
                            aimSum = item.aim_sum,
                            sortOrder = item.sort_order,
                            currentSum = countCurrentWalletSum(operations, item._id) + item.start_sum,
                            visibility = item.visibility,
                            aimDate = item.aim_date
                    )
            )
        }
    }
    if (this.any {
                it.visibility == DbSource.Visibility.INVISIBLE.value &&
                        it.type == DbSource.Type.SAVER.value
            }) savers.add(
            SourcesShowHidedWallets(
                    isHidedForShow,
                    Source.Companion.Destination.SAVERS
            )
    )
    return savers.sortedBy { it.itemType }
}

fun countCurrentWalletSum(operations: List<Operation>?, id: Long): Long {
    var currentSum = 0L
    val linkedOperations = operations?.filter { it.fromSourceId == id || it.toSourceId == id }
    linkedOperations?.forEach {
        when (it.type) {
            OperationType.EXPENSES.value,
            OperationType.PLANNED_EXPENSES.value,
            OperationType.SAVER_EXPENSES.value -> currentSum -= it.sum
            OperationType.INCOME.value,
            OperationType.PLANNED_INCOME.value,
            OperationType.SAVER_INCOME.value -> currentSum += it.sum
            OperationType.TRANSFER.value -> if (it.fromSourceId == id) {
                currentSum -= it.sum
            } else if (it.toSourceId == id) {
                currentSum += it.sum
            }
        }
    }
    return currentSum
}

fun List<DbSource>.toSources(): List<Source> {
    return this.map {
        Source(
                id = it._id,
                name = it.name,
                type = it.type,
                startSum = it.start_sum,
                addingDate = it.adding_date,
                aimSum = it.aim_sum,
                sortOrder = it.sort_order,
                currentSum = it.start_sum,
                visibility = it.visibility,
                aimDate = it.aim_date
        )
    }
}

fun DbSource.toSource(): Source {
    return Source(
        id = _id,
        name = name,
        type = type,
        startSum = start_sum,
        addingDate = adding_date,
        aimSum = aim_sum,
        sortOrder = sort_order,
        currentSum = start_sum,
        visibility = visibility,
        aimDate = aim_date
    )
}