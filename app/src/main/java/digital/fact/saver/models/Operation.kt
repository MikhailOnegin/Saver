package digital.fact.saver.models

import digital.fact.saver.domain.models.Operation.OperationType
import java.lang.IllegalArgumentException
import java.util.*

data class Operation(
        val id: Long,
        val type: Int,
        val name: String,
        val operationDate: Long,
        val addingDate: Long,
        val sum: Long,
        val fromSourceId: Long,
        val fromSourceSum: Long,
        val toSourceId: Long,
        val toSourceSum: Long,
        val planId: Long,
        val planSum: Long,
        val categoryId: Long,
        val comment: String
){

    companion object {

        fun getTestList(): List<Operation> {
            val result = mutableListOf<Operation>()
            for (i in 0..6) {
                result.add(Operation(
                        id = i+1L,
                        type = when (i){
                            0 -> OperationType.EXPENSES.value
                            1 -> OperationType.INCOME.value
                            2 -> OperationType.TRANSFER.value
                            3 -> OperationType.PLANNED_EXPENSES.value
                            4 -> OperationType.PLANNED_INCOME.value
                            5 -> OperationType.SAVER_EXPENSES.value
                            6 -> OperationType.SAVER_INCOME.value
                            else -> throw IllegalArgumentException()
                        },
                        "Название операции",
                        Date().time,
                        Date().time,
                        1500000L,
                        3,
                        20000000,
                        4,
                        3400000,
                        if (i > 4) i.toLong() else 0L,
                        2300000L,
                        0,
                        "Chop-chop!")
                )
            }
            return result
        }

    }

}

fun List<digital.fact.saver.domain.models.Operation>.toOperations(): List<Operation> {
    return map {
        Operation(
                id = it.id,
                type = it.type,
                name = it.name,
                operationDate = it.operation_date,
                addingDate = it.adding_date,
                sum = it.sum,
                fromSourceId = it.from_source_id,
                fromSourceSum = 0L,
                toSourceId = it.to_source_id,
                toSourceSum = 0L,
                planId = it.plan_id,
                planSum = 0L,
                categoryId = it.category_id,
                comment = it.comment
        )
    }
}