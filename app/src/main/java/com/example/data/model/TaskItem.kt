package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Routine",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val scheduledDate: String, // "YYYY-MM-DD"
    val startTime: String, // "09:00" (24h)
    val endTime: String, // "10:00" (24h)
    val status: TaskStatus = TaskStatus.NOT_STARTED,
    val actualStartTime: String? = null,
    val actualEndTime: String? = null,
    val plannedDurationMinutes: Int = 60,
    val actualDurationMinutes: Int = 0,
    val completionPercentage: Int = 0,
    val personalNotes: String = "",
    val isRecurring: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceDaysOfWeek: String = "", // e.g. "1,2,3,4,5" (1=Monday ... 7=Sunday)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Helper to get duration string like "1h 30m"
    fun formatPlannedDuration(): String {
        val h = plannedDurationMinutes / 60
        val m = plannedDurationMinutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
    }

    fun formatActualDuration(): String {
        val h = actualDurationMinutes / 60
        val m = actualDurationMinutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
    }

    // Time slot categorization
    fun getTimeSlot(): String {
        val hour = startTime.split(":").firstOrNull()?.toIntOrNull() ?: 9
        return when {
            hour in 5..11 -> "Morning"
            hour in 12..16 -> "Afternoon"
            hour in 17..21 -> "Evening"
            else -> "Night"
        }
    }
}
