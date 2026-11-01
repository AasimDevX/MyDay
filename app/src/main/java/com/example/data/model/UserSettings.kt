package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "Alex Vance",
    val userRole: String = "Product Architect",
    val userEmail: String = "alex.vance@workplace.com",
    val dayStartTime: String = "06:00",
    val dayEndTime: String = "23:00",
    val workStartTime: String = "09:00",
    val workEndTime: String = "18:00",
    val weekStartDay: String = "Monday", // "Monday" or "Sunday"
    val targetProductiveHours: Float = 6.0f,
    val targetCompletionRate: Int = 85,
    val isPinLockEnabled: Boolean = false,
    val pinCode: String = "",
    val notifyTaskStart: Boolean = true,
    val notifyUpcoming: Boolean = true,
    val notifyOverdue: Boolean = true,
    val notifyDailySummary: Boolean = true,
    val notifyEndOfDayReview: Boolean = true,
    val reminderLeadMinutes: Int = 10,
    val themeMode: String = "SYSTEM" // "LIGHT", "DARK", "SYSTEM"
)
