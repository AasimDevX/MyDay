package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class TaskPriority(val displayName: String, val color: Color) {
    LOW("Low", PriorityLow),
    MEDIUM("Medium", PriorityMedium),
    HIGH("High", PriorityHigh)
}

enum class TaskStatus(val displayName: String, val color: Color) {
    NOT_STARTED("Not Started", StatusNotStarted),
    IN_PROGRESS("In Progress", StatusInProgress),
    COMPLETED("Completed", StatusCompleted),
    PARTIALLY_COMPLETED("Partially Completed", StatusPartiallyCompleted),
    SKIPPED("Skipped", StatusSkipped)
}

enum class RecurrenceType(val displayName: String) {
    NONE("Does not repeat"),
    DAILY("Every day"),
    WEEKDAYS("Weekdays (Mon-Fri)"),
    WEEKENDS("Weekends (Sat-Sun)"),
    CUSTOM("Custom days")
}

data class CategoryInfo(
    val id: String,
    val name: String,
    val color: Color,
    val iconName: String
)

object Categories {
    val DEEP_WORK = CategoryInfo("deep_work", "Deep Work", CategoryDeepWork, "work")
    val ROUTINE = CategoryInfo("routine", "Routine", CategoryRoutine, "schedule")
    val HEALTH = CategoryInfo("health", "Health & Fitness", CategoryHealth, "fitness_center")
    val MEETINGS = CategoryInfo("meetings", "Meetings", CategoryMeetings, "groups")
    val LEARNING = CategoryInfo("learning", "Learning", CategoryLearning, "school")
    val PERSONAL = CategoryInfo("personal", "Personal", CategoryPersonal, "person")
    val ERRANDS = CategoryInfo("errands", "Errands", CategoryErrands, "shopping_bag")

    val ALL = listOf(DEEP_WORK, ROUTINE, HEALTH, MEETINGS, LEARNING, PERSONAL, ERRANDS)

    fun getCategory(name: String): CategoryInfo {
        return ALL.find { it.name.equals(name, ignoreCase = true) || it.id.equals(name, ignoreCase = true) }
            ?: CategoryInfo(name, name, CategoryDeepWork, "task")
    }
}
