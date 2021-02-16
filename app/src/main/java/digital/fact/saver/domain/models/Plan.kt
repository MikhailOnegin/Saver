package digital.fact.saver.domain.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "PLANS")
data class Plan (
        @PrimaryKey
        val _id: Int,
        val category: Int,
        val sum: Int,
        val operation_id: Int,
        val state: Int,
        val planning_date: Int = 0
)