package digital.fact.saver.models

import digital.fact.saver.domain.models.Operation

data class Operations(
    val _id: Int,
    val category: Int,
    val name: String,
    val date: Int,
    val adding_date: Int,
    val sum: Int,
    val from_source: Int,
    val to_source: Int,
    val plain_id: Int,
    val _class: Int,
    val comment: String
) {
    companion object {
        const val MINUS = 0
        const val PLUS = 1
        const val TRANSPORT = 2
        const val PLAN_MINUS = 3
        const val PLAN_PLUS = 4
        const val BANK_MINUS = 5
        const val BANK_PLUS = 6
    }
}

fun List<Operation>.toOperations(): List<Operations> {
    val result = mutableListOf<Operations>()
    result.addAll(
        this.map {
            Operations(
                _id = it._id,
                category = it.category,
                name = it.name,
                date = it.date,
                adding_date = it.adding_date,
                sum = it.sum,
                from_source = it.from_source,
                to_source = it.to_source,
                plain_id = it.plain_id,
                _class = it._class,
                comment = it.comment
            )
        }
    )
    return result
}