package digital.fact.saver.models

data class Operation(
        val id: Long,
        val type: Int,
        val name: String,
        val operationDate: Long,
        val addingDate: Long,
        val sum: Long,
        val fromSourceId: Long,
        val toSourceId: Long,
        val planId: Long,
        val categoryId: Long,
        val comment: String
)

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
                toSourceId = it.to_source_id,
                planId = it.plan_id,
                categoryId = it.category_id,
                comment = it.comment
        )
    }
}