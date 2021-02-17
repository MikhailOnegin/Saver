package digital.fact.saver.utils.enums.source

enum class OperationCategory(value: Int) {
    CONSUMPTION(0),
    ADMISSION(1),
    MOVEMENT(2),
    PLANNED_EXPENDITURE(3),
    SCHEDULED_ADMISSION(4),
    REMOVING_FROM_SAVER(5),
    REPLENISHMENT_SAVER(6)
}