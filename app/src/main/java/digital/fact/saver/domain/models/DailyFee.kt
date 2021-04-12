package digital.fact.saver.domain.models

import kotlin.random.Random

data class DailyFee(
    val id: Long,
    val saverId: Long,
    val saverName: String,
    val fee: Long,
    val daysLeft: Long
) {

    companion object {

        fun getTestList(): List<DailyFee> {
            val result = mutableListOf<DailyFee>()
            for (i in 0L until 5L) {
                result.add(
                    DailyFee(
                        id = i + 1L,
                        saverId = 0L,
                        saverName = arrayOf("SVEN MC-20", "Свободная касса", "Фонд семейного благосостояния")[Random.nextInt(3)],
                        fee = arrayOf(20000L, 150000, 1200000)[Random.nextInt(3)],
                        daysLeft = arrayOf(1L, 23L, 175L)[Random.nextInt(3)],
                        )
                )
            }
            return result
        }

    }

}