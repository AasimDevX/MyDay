package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ProductivityRepository(
    private val taskDao: TaskDao,
    private val habitDao: HabitDao,
    private val dailyReviewDao: DailyReviewDao,
    private val settingsDao: SettingsDao
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun checkSeedIfEmpty() {
        if (taskDao.countAllTasks() == 0) {
            // In case db existed without seeds
            settingsDao.insertOrUpdateSettings(UserSettings())
        }
    }

    // Tasks for a specific date, including recurring tasks that apply to this day
    fun getTasksForDate(dateStr: String): Flow<List<TaskItem>> {
        return taskDao.getTasksForDateFlow(dateStr).map { tasks ->
            val date = try {
                LocalDate.parse(dateStr, dateFormatter)
            } catch (e: Exception) {
                LocalDate.now()
            }
            val dayOfWeek = date.dayOfWeek.value // 1 (Mon) - 7 (Sun)
            val isWeekend = dayOfWeek == DayOfWeek.SATURDAY.value || dayOfWeek == DayOfWeek.SUNDAY.value

            tasks.filter { task ->
                if (task.scheduledDate == dateStr) {
                    true
                } else if (task.isRecurring) {
                    when (task.recurrenceType) {
                        RecurrenceType.DAILY -> true
                        RecurrenceType.WEEKDAYS -> !isWeekend
                        RecurrenceType.WEEKENDS -> isWeekend
                        RecurrenceType.CUSTOM -> {
                            val days = task.recurrenceDaysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
                            days.contains(dayOfWeek)
                        }
                        RecurrenceType.NONE -> false
                    }
                } else {
                    false
                }
            }.sortedBy { it.startTime }
        }
    }

    fun getAllTasksFlow(): Flow<List<TaskItem>> = taskDao.getAllTasksFlow()

    fun getTasksBetweenDates(startDate: String, endDate: String): Flow<List<TaskItem>> =
        taskDao.getTasksBetweenDatesFlow(startDate, endDate)

    suspend fun getTaskById(id: Long): TaskItem? = taskDao.getTaskById(id)

    suspend fun addTask(task: TaskItem): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskItem) = taskDao.updateTask(task)

    suspend fun deleteTask(id: Long) = taskDao.deleteTaskById(id)

    suspend fun duplicateTask(id: Long, targetDate: String): Long {
        val original = taskDao.getTaskById(id) ?: return -1
        val copy = original.copy(
            id = 0,
            scheduledDate = targetDate,
            status = TaskStatus.NOT_STARTED,
            actualStartTime = null,
            actualEndTime = null,
            actualDurationMinutes = 0,
            completionPercentage = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return taskDao.insertTask(copy)
    }

    suspend fun rescheduleTask(id: Long, newDate: String, newStart: String? = null, newEnd: String? = null) {
        val original = taskDao.getTaskById(id) ?: return
        val updated = original.copy(
            scheduledDate = newDate,
            startTime = newStart ?: original.startTime,
            endTime = newEnd ?: original.endTime,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.updateTask(updated)
    }

    suspend fun updateTaskStatus(
        taskId: Long,
        status: TaskStatus,
        completionPercentage: Int = if (status == TaskStatus.COMPLETED) 100 else 0,
        actualDuration: Int? = null
    ) {
        val task = taskDao.getTaskById(taskId) ?: return
        val nowTimeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val duration = actualDuration ?: if (status == TaskStatus.COMPLETED && task.actualDurationMinutes == 0) {
            task.plannedDurationMinutes
        } else {
            task.actualDurationMinutes
        }

        val updated = task.copy(
            status = status,
            completionPercentage = completionPercentage,
            actualDurationMinutes = duration,
            actualEndTime = if (status == TaskStatus.COMPLETED || status == TaskStatus.PARTIALLY_COMPLETED) nowTimeStr else task.actualEndTime,
            actualStartTime = if (task.actualStartTime == null && (status == TaskStatus.IN_PROGRESS || status == TaskStatus.COMPLETED)) nowTimeStr else task.actualStartTime,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.updateTask(updated)
    }

    suspend fun startTaskTimer(taskId: Long) {
        val task = taskDao.getTaskById(taskId) ?: return
        val nowTimeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val updated = task.copy(
            status = TaskStatus.IN_PROGRESS,
            actualStartTime = task.actualStartTime ?: nowTimeStr,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.updateTask(updated)
    }

    suspend fun recordTaskProgress(
        taskId: Long,
        actualDurationMinutes: Int,
        completionPercentage: Int,
        status: TaskStatus,
        notes: String? = null
    ) {
        val task = taskDao.getTaskById(taskId) ?: return
        val nowTimeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val updated = task.copy(
            status = status,
            actualDurationMinutes = actualDurationMinutes,
            completionPercentage = completionPercentage,
            personalNotes = notes ?: task.personalNotes,
            actualEndTime = if (status == TaskStatus.COMPLETED) nowTimeStr else task.actualEndTime,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.updateTask(updated)
    }

    // Habits & Streaks
    fun getHabitsWithProgressFlow(currentDateStr: String): Flow<List<HabitWithProgress>> {
        val date = try {
            LocalDate.parse(currentDateStr, dateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }

        // Days of current week (Monday to Sunday)
        val monday = date.with(DayOfWeek.MONDAY)
        val weekDates = (0..6).map { monday.plusDays(it.toLong()).format(dateFormatter) }
        val currentMonthPrefix = currentDateStr.substring(0, 7) // "YYYY-MM"

        return combine(
            habitDao.getAllHabitsFlow(),
            habitDao.getAllCompletionsFlow()
        ) { habits, completions ->
            val completionsByHabit = completions.groupBy { it.habitId }

            habits.map { habit ->
                val habitCompletions = completionsByHabit[habit.id].orEmpty()
                val completedDatesSet = habitCompletions.map { it.dateString }.toSet()

                val isCompletedToday = completedDatesSet.contains(currentDateStr)

                // Calculate current streak
                var currentStreak = 0
                var checkDate = if (isCompletedToday) date else date.minusDays(1)
                while (completedDatesSet.contains(checkDate.format(dateFormatter))) {
                    currentStreak++
                    checkDate = checkDate.minusDays(1)
                }

                // Longest streak calculation
                val sortedDates = completedDatesSet.mapNotNull {
                    try { LocalDate.parse(it, dateFormatter) } catch (e: Exception) { null }
                }.sorted()

                var longestStreak = 0
                var tempStreak = 0
                var prevDate: LocalDate? = null

                for (d in sortedDates) {
                    if (prevDate == null || d == prevDate.plusDays(1)) {
                        tempStreak++
                    } else if (d != prevDate) {
                        tempStreak = 1
                    }
                    if (tempStreak > longestStreak) longestStreak = tempStreak
                    prevDate = d
                }
                if (currentStreak > longestStreak) longestStreak = currentStreak

                val completedThisWeek = weekDates.filter { completedDatesSet.contains(it) }
                val completedThisMonthCount = habitCompletions.count { it.dateString.startsWith(currentMonthPrefix) }

                HabitWithProgress(
                    habit = habit,
                    isCompletedToday = isCompletedToday,
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    completedDatesThisWeek = completedThisWeek,
                    completionCountThisMonth = completedThisMonthCount
                )
            }
        }
    }

    suspend fun toggleHabitCompletion(habitId: Long, dateString: String) {
        val existing = habitDao.getCompletionsForHabit(habitId).find { it.dateString == dateString }
        if (existing != null) {
            habitDao.deleteCompletion(habitId, dateString)
        } else {
            habitDao.insertCompletion(HabitCompletion(habitId = habitId, dateString = dateString))
        }
    }

    suspend fun addHabit(habit: HabitItem): Long = habitDao.insertHabit(habit)
    suspend fun updateHabit(habit: HabitItem) = habitDao.updateHabit(habit)
    suspend fun deleteHabit(habit: HabitItem) = habitDao.deleteHabit(habit)

    // Daily Review
    fun getDailyReviewFlow(date: String): Flow<DailyReview?> = dailyReviewDao.getReviewForDateFlow(date)
    fun getAllReviewsFlow(): Flow<List<DailyReview>> = dailyReviewDao.getAllReviewsFlow()
    suspend fun saveDailyReview(review: DailyReview): Long = dailyReviewDao.insertOrUpdateReview(review)

    // User Settings
    fun getSettingsFlow(): Flow<UserSettings> = settingsDao.getSettingsFlow().map { it ?: UserSettings() }
    suspend fun updateSettings(settings: UserSettings) = settingsDao.insertOrUpdateSettings(settings)
}
