package digital.fact.saver.domain.models

import androidx.room.PrimaryKey
import digital.fact.saver.data.database.dto.PlanTable

data class Plan (
        val id: Long,
        val type: Int,
        val sum: Long,
        val name: String,
        val operation_id: Int,
        val planning_date: Long
): PlanItem(_id = id)

sealed class PlanItem(
        val _id:Long
)


data class PlanDoneOutside(
        val id: Long,
        val type: Int,
        val sum: Long,
        val sumPlanned: Long,
        val name: String,
        val operation_id: Int,
        val planning_date: Long
): PlanItem(_id = id)

class SeparatorPlans: PlanItem(2)

fun List<PlanTable>.toPlans(): List<Plan>{
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

fun List<PlanTable>.toPlansDoneOutside(): List<PlanDoneOutside>{
    val result = mutableListOf<PlanDoneOutside>()
    for (item in this){
        result.add(
                PlanDoneOutside(
                        item.id,
                        item.type,
                        item.sum,
                        0,
                        item.name,
                        item.operation_id,
                        item.planning_date
                )
        )
    }
    return result
}

fun toPlansItems(plans1: List<Plan>, plans2: List<PlanDoneOutside>): List<PlanItem>{
    val plansItems = mutableListOf<PlanItem>()
    plansItems.addAll(plans1)
    plansItems.add(SeparatorPlans())
    plansItems.addAll(plans2)
    return plansItems
}