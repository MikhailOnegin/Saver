package digital.fact.saver.domain.models

import digital.fact.saver.data.database.dto.DbPlan
import digital.fact.saver.utils.UniqueIdGenerator

data class Plan (
        val id: Long,
        val type: Int,
        val sum: Long,
        val name: String,
        val operation_id: Long,
        val planning_date: Long,
        var sum_fact: Long = 0,
        var inPeriod: Boolean = false
): PlanItem(itemId = UniqueIdGenerator.nextId())


class SeparatorPlans: PlanItem(itemId = UniqueIdGenerator.nextId())

sealed class PlanItem(
    val itemId:Long
)

fun List<DbPlan>.toPlans(): List<Plan>{
    val result = mutableListOf<Plan>()
    for (item in this){
        result.add(
                Plan(
                        item.id,
                        item.type,
                        item.sum,
                        item.name,
                        item.operation_id,
                        item.planning_date
                )
        )
    }
    return result
}

fun List<DbPlan>.toPlansDoneOutside(): List<Plan>{
    val result = mutableListOf<Plan>()
    for (item in this){
        result.add(
                Plan(
                        item.id,
                        item.type,
                        item.sum,
                        item.name,
                        item.operation_id,
                        item.planning_date
                )
        )
    }
    return result
}

fun toPlansItems(plans1: List<Plan>, plans2: List<Plan>): List<PlanItem> {
    val plansItems = mutableListOf<PlanItem>()
    plansItems.addAll(plans1)
    if (plans2.isNotEmpty()) plansItems.add(SeparatorPlans())
    plansItems.addAll(plans2)
    return plansItems
}
