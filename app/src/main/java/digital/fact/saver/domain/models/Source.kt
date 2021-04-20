package digital.fact.saver.domain.models

import digital.fact.saver.data.database.dto.DbSource
import digital.fact.saver.utils.UniqueIdGenerator
import java.lang.IllegalArgumentException

sealed class SourceItem(val itemId: Long)

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
) : SourceItem(itemId = UniqueIdGenerator.nextId())

data class SourcesHeaderActive(
    val activeWalletsSum: Long
) : SourceItem(itemId = UniqueIdGenerator.nextId())

data class SourcesHeaderInactive(
    val inactiveWalletsSum: Long
) : SourceItem(itemId = UniqueIdGenerator.nextId())

data class SourcesVisibilitySwitcher(
    var invisibleAreShown: Boolean
) : SourceItem(itemId = UniqueIdGenerator.nextId())

fun List<Source>.toSourceItemsList(showInvisible: Boolean, type: Int): List<SourceItem> {
    val result = mutableListOf<SourceItem>()
    val filteredList = when (type) {
        DbSource.Type.ACTIVE.value, DbSource.Type.INACTIVE.value ->
            this.filter { it.type == type }
                .sortedWith(compareBy({ it.visibility }, { it.sortOrder }))
        DbSource.Type.SAVER.value ->
            this.sortedWith(compareBy({ it.visibility }, { -it.aimSum }, { it.sortOrder }))
        else -> throw IllegalArgumentException("Wrong source type.")
    }
    for (source in filteredList) {
        if (!showInvisible && source.visibility == DbSource.Visibility.INVISIBLE.value) continue
        result.add(source)
    }
    if (this.firstOrNull { it.visibility == DbSource.Visibility.INVISIBLE.value } != null) {
        result.add(
            result.indexOfLast {
                (it as Source).visibility == DbSource.Visibility.VISIBLE.value
            } + 1,
            SourcesVisibilitySwitcher(showInvisible)
        )
    }
    when (type) {
        DbSource.Type.ACTIVE.value -> result.add(0, SourcesHeaderActive(
            filteredList.filter { it.visibility == DbSource.Visibility.VISIBLE.value }
                .sumOf { it.currentSum }
        ))
        DbSource.Type.INACTIVE.value -> result.add(0, SourcesHeaderInactive(
            filteredList.filter { it.visibility == DbSource.Visibility.VISIBLE.value }
                .sumOf { it.currentSum }
        ))
    }
    return result
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