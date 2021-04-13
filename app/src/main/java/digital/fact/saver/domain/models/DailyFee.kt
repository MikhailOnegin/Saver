package digital.fact.saver.domain.models

data class DailyFee(
    val id: Long,
    val saverId: Long,
    val saverName: String,
    val fee: Long,
    val daysLeft: Long
)