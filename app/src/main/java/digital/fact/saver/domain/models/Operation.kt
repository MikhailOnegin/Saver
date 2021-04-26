package digital.fact.saver.domain.models

data class Operation(
        val id: Long,
        val type: Int,
        val name: String,
        val operationDate: Long,
        val addingDate: Long,
        val sum: Long,
        val fromSourceId: Long,
        var fromSourceSum: Long,
        var fromSourceName: String,
        val toSourceId: Long,
        var toSourceSum: Long,
        var toSourceName: String,
        val planId: Long,
        var planSum: Long,
        val categoryId: Long,
        val comment: String,
        var sourceAimSum: Long = 0,
        var sourceAimDate: Long = 0
)

fun List<digital.fact.saver.data.database.dto.DbOperation>.toOperations(): List<Operation> {
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
                fromSourceName = "",
                toSourceId = it.to_source_id,
                toSourceSum = 0L,
                toSourceName = "",
                planId = it.plan_id,
                planSum = 0L,
                categoryId = it.category_id,
                comment = it.comment
        )
    }
}