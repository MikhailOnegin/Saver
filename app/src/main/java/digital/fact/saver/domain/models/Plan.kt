package digital.fact.saver.domain.models

import digital.fact.saver.data.database.dto.DbPlan
import digital.fact.saver.utils.UniqueIdGenerator

data class Plan(
    val id: Long,
    val type: Int,
    val sum: Long,
    val name: String,
    val operation_id: Long,
    val planning_date: Long,
    var sum_fact: Long = 0,
    var inPeriod: Boolean = false,
    var status: PlanStatus? = null
) : PlanItem(itemId = UniqueIdGenerator.nextId())

enum class PlanStatus {
    CURRENT,
    DONE,
    DONE_OUTSIDE,
    OUTSIDE
}

class SeparatorPlans : PlanItem(itemId = UniqueIdGenerator.nextId())

sealed class PlanItem(
    val itemId: Long
)

fun List<DbPlan>.toPlans(): List<Plan> {
    val result = mutableListOf<Plan>()
    for (item in this) {
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

fun checkPlanStatus(plan: DbPlan, unixFrom: Long, unixTo: Long): PlanStatus? {
    return if (plan.operation_id == 0L && plan.planning_date in unixFrom  .. unixTo || plan.planning_date == 0L)
        PlanStatus.CURRENT
    else if (plan.operation_id != 0L && plan.planning_date in unixFrom  .. unixTo && plan.planning_date != 0L)
        PlanStatus.DONE
    else if (plan.operation_id != 0L && plan.planning_date !in unixFrom  .. unixTo && plan.planning_date != 0L)
        PlanStatus.DONE_OUTSIDE
    else if( plan.planning_date !in unixFrom  .. unixTo && plan.planning_date != 0L  || plan.planning_date != 0L)
        PlanStatus.OUTSIDE
    else null
}

