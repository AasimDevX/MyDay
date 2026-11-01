package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Health & Fitness",
    val iconName: String = "star",
    val colorHex: String = "#C9A227",
    val frequencyType: String = "DAILY", // "DAILY", "WEEKDAYS", "CUSTOM"
    val targetDaysOfWeek: String = "1,2,3,4,5,6,7", // Monday=1 ... Sunday=7
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = HabitItem::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId", "dateString"], unique = true)
    ]
)
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val dateString: String, // "YYYY-MM-DD"
    val completedAt: Long = System.currentTimeMillis(),
    val note: String? = null
)

data class HabitWithProgress(
    val habit: HabitItem,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val longestStreak: Int,
    val completedDatesThisWeek: List<String>,
    val completionCountThisMonth: Int
)
