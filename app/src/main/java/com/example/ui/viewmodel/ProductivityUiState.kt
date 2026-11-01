package com.example.ui.viewmodel

import com.example.data.model.*

enum class NavItem(val label: String, val iconName: String) {
    HOME("Home", "home"),
    ROUTINE("Routine", "schedule"),
    TASKS("Tasks", "task_alt"),
    ANALYTICS("Analytics", "bar_chart"),
    PROFILE("Profile", "account_circle")
}

enum class RoutineViewMode {
    TIME_SLOTS,
    TIMELINE
}

data class DayStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val missedTasks: Int = 0,
    val inProgressTasks: Int = 0,
    val productivityPercentage: Int = 0,
    val totalProductiveMinutes: Int = 0,
    val plannedMinutes: Int = 0,
    val upcomingTask: TaskItem? = null
) {
    fun formatProductiveHours(): String {
        val h = totalProductiveMinutes / 60
        val m = totalProductiveMinutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    fun formatPlannedHours(): String {
        val h = plannedMinutes / 60
        val m = plannedMinutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }
}

data class WeeklyAnalyticsItem(
    val dayLabel: String,
    val dateStr: String,
    val completedCount: Int,
    val totalCount: Int,
    val productiveHours: Float,
    val completionPercentage: Int
)

data class CategoryTimeDistribution(
    val category: String,
    val minutes: Int,
    val percentage: Int,
    val taskCount: Int
)

data class ProductivityUiState(
    val isLoading: Boolean = false,
    val selectedDate: String, // "YYYY-MM-DD"
    val todayDate: String, // "YYYY-MM-DD"
    val tasks: List<TaskItem> = emptyList(),
    val allTasks: List<TaskItem> = emptyList(),
    val habits: List<HabitWithProgress> = emptyList(),
    val dailyReview: DailyReview? = null,
    val settings: UserSettings = UserSettings(),
    val dayStats: DayStats = DayStats(),
    val isPinLocked: Boolean = false,
    val currentNav: NavItem = NavItem.HOME,
    val routineViewMode: RoutineViewMode = RoutineViewMode.TIME_SLOTS,
    // Filters & Search
    val searchQuery: String = "",
    val selectedCategoryFilter: String? = null,
    val selectedStatusFilter: TaskStatus? = null,
    val selectedPriorityFilter: TaskPriority? = null,
    // Active Timer
    val activeTimerTask: TaskItem? = null,
    val isTimerRunning: Boolean = false,
    val timerElapsedSeconds: Long = 0,
    // Dialogs
    val taskDialogData: TaskItem? = null,
    val isTaskDialogOpen: Boolean = false,
    val isNewTaskDialog: Boolean = true,
    val showDailyReviewDialog: Boolean = false,
    val showHabitDialog: Boolean = false,
    val habitToEdit: HabitItem? = null,
    val userMessage: String? = null
)
