package digital.fact.saver.domain.models

data class Template(
    val id: Long,
    val operationType: Int,
    val operationName: String,
    val operationSum: Long,
    val sourceId: Long
)