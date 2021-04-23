package digital.fact.saver.domain.models

import digital.fact.saver.data.database.dto.DbOperation

data class Template(
    val id: Long,
    val operationType: Int,
    val operationName: String,
    val operationSum: Long
) {

    companion object {

        fun getTestList(): List<Template> {
            return listOf(
                Template(1L, DbOperation.OperationType.EXPENSES.value, "Столовая", 30000L),
                Template(1L, DbOperation.OperationType.EXPENSES.value, "Стоянка", 300000L),
                Template(1L, DbOperation.OperationType.EXPENSES.value, "Магнит", 30000L),
                Template(1L, DbOperation.OperationType.INCOME.value, "Бабушка", 300000L),
                Template(1L, DbOperation.OperationType.INCOME.value, "Нашел", 30000L),
                Template(1L, DbOperation.OperationType.INCOME.value, "Мамуся", 300000L),
                Template(1L, DbOperation.OperationType.INCOME.value, "Продажи", 30000L),
                Template(1L, DbOperation.OperationType.EXPENSES.value, "Чоп-чоп", 30000L),
                Template(1L, DbOperation.OperationType.EXPENSES.value, "Чип-чип", 3000000L),
                Template(1L, DbOperation.OperationType.EXPENSES.value, "Чуп-чуп", 30000000L)
            )
        }

    }

}