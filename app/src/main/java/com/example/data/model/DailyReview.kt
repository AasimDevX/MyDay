package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_reviews",
    indices = [
        Index(value = ["dateString"], unique = true)
    ]
)
data class DailyReview(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // "YYYY-MM-DD"
    val totalTasksPlanned: Int = 0,
    val totalTasksCompleted: Int = 0,
    val totalTasksMissed: Int = 0,
    val completionPercentage: Int = 0,
    val totalProductiveMinutes: Int = 0,
    val mostProductiveCategory: String = "None",
    val tasksToReschedule: String = "", // Comma-separated or JSON list of task IDs/titles
    val areasNeedingImprovement: String = "",
    val personalReflection: String = "",
    val completedAt: Long = System.currentTimeMillis()
) {
    fun formatProductiveTime(): String {
        val h = totalProductiveMinutes / 60
        val m = totalProductiveMinutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
    }
}
