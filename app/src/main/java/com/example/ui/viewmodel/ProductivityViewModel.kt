package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ProductivityRepository
import com.example.notification.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ProductivityViewModel(
    private val repository: ProductivityRepository,
    private val notificationHelper: NotificationHelper? = null
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val todayStr = LocalDate.now().format(dateFormatter)

    private val _selectedDate = MutableStateFlow(todayStr)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _uiState = MutableStateFlow(
        ProductivityUiState(
            selectedDate = todayStr,
            todayDate = todayStr
        )
    )
    val uiState: StateFlow<ProductivityUiState> = _uiState.asStateFlow()

    // Timer Job
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.checkSeedIfEmpty()
        }

        // Combine selectedDate, tasks, habits, review, settings into state
        viewModelScope.launch {
            _selectedDate.flatMapLatest { date ->
                combine(
                    repository.getTasksForDate(date),
                    repository.getAllTasksFlow(),
                    repository.getHabitsWithProgressFlow(date),
                    repository.getDailyReviewFlow(date),
                    repository.getSettingsFlow()
                ) { tasksForDate, allTasks, habits, review, settings ->
                    val stats = calculateDayStats(date, tasksForDate)
                    val isLocked = settings.isPinLockEnabled && _uiState.value.isPinLocked

                    _uiState.value.copy(
                        selectedDate = date,
                        todayDate = todayStr,
                        tasks = tasksForDate,
                        allTasks = allTasks,
                        habits = habits,
                        dailyReview = review,
                        settings = settings,
                        dayStats = stats,
                        isPinLocked = isLocked
                    )
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun calculateDayStats(dateStr: String, tasks: List<TaskItem>): DayStats {
        val total = tasks.size
        val completed = tasks.count { it.status == TaskStatus.COMPLETED }
        val inProgress = tasks.count { it.status == TaskStatus.IN_PROGRESS }
        val skipped = tasks.count { it.status == TaskStatus.SKIPPED }
        val notStarted = tasks.count { it.status == TaskStatus.NOT_STARTED }

        val nowTime = LocalTime.now()
        val isToday = dateStr == todayStr
        val isPastDay = try {
            LocalDate.parse(dateStr, dateFormatter).isBefore(LocalDate.now())
        } catch (e: Exception) {
            false
        }

        // Missed tasks: tasks that ended in the past or on past days and not completed or skipped
        val missed = tasks.count { task ->
            if (task.status == TaskStatus.COMPLETED || task.status == TaskStatus.SKIPPED) {
                false
            } else if (isPastDay) {
                true
            } else if (isToday) {
                val endHourMin = try {
                    LocalTime.parse(task.endTime, timeFormatter)
                } catch (e: Exception) {
                    null
                }
                endHourMin != null && endHourMin.isBefore(nowTime)
            } else {
                false
            }
        }

        val pending = if (isToday) {
            tasks.count { task ->
                if (task.status != TaskStatus.NOT_STARTED) return@count false
                val endHourMin = try { LocalTime.parse(task.endTime, timeFormatter) } catch (e: Exception) { null }
                endHourMin == null || !endHourMin.isBefore(nowTime)
            }
        } else if (isPastDay) {
            0
        } else {
            notStarted
        }

        // Productivity percentage: weights completed 100%, partially completed %
        val weightedSum = tasks.sumOf {
            when (it.status) {
                TaskStatus.COMPLETED -> 100
                TaskStatus.PARTIALLY_COMPLETED -> it.completionPercentage.coerceIn(0, 99)
                TaskStatus.IN_PROGRESS -> (it.completionPercentage).coerceAtLeast(30)
                else -> 0
            }
        }
        val percentage = if (total > 0) (weightedSum / total).coerceIn(0, 100) else 0

        // Productive minutes
        val productiveMinutes = tasks.sumOf {
            if (it.status == TaskStatus.COMPLETED) {
                if (it.actualDurationMinutes > 0) it.actualDurationMinutes else it.plannedDurationMinutes
            } else if (it.status == TaskStatus.PARTIALLY_COMPLETED || it.status == TaskStatus.IN_PROGRESS) {
                it.actualDurationMinutes
            } else {
                0
            }
        }

        val plannedMinutes = tasks.sumOf { it.plannedDurationMinutes }

        // Upcoming task
        val upcoming = if (isToday) {
            tasks.filter { it.status == TaskStatus.NOT_STARTED || it.status == TaskStatus.IN_PROGRESS }
                .firstOrNull { task ->
                    val startHourMin = try { LocalTime.parse(task.startTime, timeFormatter) } catch (e: Exception) { null }
                    startHourMin == null || !startHourMin.isBefore(nowTime.minusMinutes(10))
                } ?: tasks.firstOrNull { it.status == TaskStatus.NOT_STARTED }
        } else {
            tasks.firstOrNull { it.status == TaskStatus.NOT_STARTED }
        }

        return DayStats(
            totalTasks = total,
            completedTasks = completed,
            pendingTasks = pending,
            missedTasks = missed,
            inProgressTasks = inProgress,
            productivityPercentage = percentage,
            totalProductiveMinutes = productiveMinutes,
            plannedMinutes = plannedMinutes,
            upcomingTask = upcoming
        )
    }

    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun setNav(nav: NavItem) {
        _uiState.update { it.copy(currentNav = nav) }
    }

    fun setRoutineViewMode(mode: RoutineViewMode) {
        _uiState.update { it.copy(routineViewMode = mode) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setCategoryFilter(category: String?) {
        _uiState.update { it.copy(selectedCategoryFilter = category) }
    }

    fun setStatusFilter(status: TaskStatus?) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
    }

    fun setPriorityFilter(priority: TaskPriority?) {
        _uiState.update { it.copy(selectedPriorityFilter = priority) }
    }

    // Task CRUD & Actions
    fun openNewTaskDialog(initialSlot: String? = null) {
        val defaultStart = initialSlot ?: "09:00"
        val startLocal = try { LocalTime.parse(defaultStart, timeFormatter) } catch (e: Exception) { LocalTime.of(9, 0) }
        val endLocal = startLocal.plusHours(1)
        val defaultEnd = endLocal.format(timeFormatter)

        val newTask = TaskItem(
            title = "",
            description = "",
            category = "Deep Work",
            priority = TaskPriority.MEDIUM,
            scheduledDate = _selectedDate.value,
            startTime = defaultStart,
            endTime = defaultEnd,
            plannedDurationMinutes = 60
        )
        _uiState.update {
            it.copy(
                taskDialogData = newTask,
                isTaskDialogOpen = true,
                isNewTaskDialog = true
            )
        }
    }

    fun openEditTaskDialog(task: TaskItem) {
        _uiState.update {
            it.copy(
                taskDialogData = task,
                isTaskDialogOpen = true,
                isNewTaskDialog = false
            )
        }
    }

    fun closeTaskDialog() {
        _uiState.update { it.copy(isTaskDialogOpen = false, taskDialogData = null) }
    }

    fun saveTask(task: TaskItem) {
        viewModelScope.launch {
            if (_uiState.value.isNewTaskDialog || task.id == 0L) {
                val newId = repository.addTask(task)
                _uiState.update { it.copy(userMessage = "Task scheduled successfully") }
                if (_uiState.value.settings.notifyTaskStart) {
                    notificationHelper?.showTaskStartNotification(newId, task.title, "${task.startTime} - ${task.endTime}")
                }
            } else {
                repository.updateTask(task)
                _uiState.update { it.copy(userMessage = "Task updated") }
            }
            closeTaskDialog()
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            _uiState.update { it.copy(userMessage = "Task removed") }
        }
    }

    fun duplicateTask(taskId: Long, targetDate: String? = null) {
        viewModelScope.launch {
            val date = targetDate ?: _selectedDate.value
            repository.duplicateTask(taskId, date)
            _uiState.update { it.copy(userMessage = "Task duplicated for $date") }
        }
    }

    fun rescheduleTask(taskId: Long, newDate: String, newStartTime: String? = null, newEndTime: String? = null) {
        viewModelScope.launch {
            repository.rescheduleTask(taskId, newDate, newStartTime, newEndTime)
            _uiState.update { it.copy(userMessage = "Task rescheduled to $newDate") }
        }
    }

    fun updateTaskStatus(taskId: Long, status: TaskStatus, percentage: Int = 100) {
        viewModelScope.launch {
            repository.updateTaskStatus(taskId, status, percentage)
            val msg = when (status) {
                TaskStatus.COMPLETED -> "Task marked as completed! 🎉"
                TaskStatus.IN_PROGRESS -> "Task in progress ⏱️"
                TaskStatus.SKIPPED -> "Task skipped"
                TaskStatus.PARTIALLY_COMPLETED -> "Task marked partially complete"
                TaskStatus.NOT_STARTED -> "Task reset to not started"
            }
            _uiState.update { it.copy(userMessage = msg) }
        }
    }

    // Active Timer / Stopwatch for Tasks
    fun startTimerForTask(task: TaskItem) {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                activeTimerTask = task,
                isTimerRunning = true,
                timerElapsedSeconds = task.actualDurationMinutes.toLong() * 60
            )
        }
        viewModelScope.launch {
            repository.startTaskTimer(task.id)
        }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update {
                    if (it.isTimerRunning) {
                        it.copy(timerElapsedSeconds = it.timerElapsedSeconds + 1)
                    } else {
                        it
                    }
                }
            }
        }
    }

    fun toggleTimerPause() {
        _uiState.update { it.copy(isTimerRunning = !it.isTimerRunning) }
    }

    fun finishActiveTimer(asCompleted: Boolean = true, notes: String = "") {
        val active = _uiState.value.activeTimerTask ?: return
        val elapsedMinutes = (_uiState.value.timerElapsedSeconds / 60).toInt().coerceAtLeast(1)
        timerJob?.cancel()
        timerJob = null
        viewModelScope.launch {
            repository.recordTaskProgress(
                taskId = active.id,
                actualDurationMinutes = elapsedMinutes,
                completionPercentage = if (asCompleted) 100 else 50,
                status = if (asCompleted) TaskStatus.COMPLETED else TaskStatus.PARTIALLY_COMPLETED,
                notes = if (notes.isNotEmpty()) notes else active.personalNotes
            )
            _uiState.update {
                it.copy(
                    activeTimerTask = null,
                    isTimerRunning = false,
                    timerElapsedSeconds = 0,
                    userMessage = if (asCompleted) "Task completed in ${elapsedMinutes}m!" else "Progress saved: ${elapsedMinutes}m"
                )
            }
        }
    }

    fun dismissTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(activeTimerTask = null, isTimerRunning = false, timerElapsedSeconds = 0) }
    }

    // Habits
    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, _selectedDate.value)
        }
    }

    fun openHabitDialog(habit: HabitItem? = null) {
        _uiState.update { it.copy(showHabitDialog = true, habitToEdit = habit) }
    }

    fun closeHabitDialog() {
        _uiState.update { it.copy(showHabitDialog = false, habitToEdit = null) }
    }

    fun saveHabit(habit: HabitItem) {
        viewModelScope.launch {
            if (habit.id == 0L) {
                repository.addHabit(habit)
                _uiState.update { it.copy(userMessage = "New habit created") }
            } else {
                repository.updateHabit(habit)
                _uiState.update { it.copy(userMessage = "Habit updated") }
            }
            closeHabitDialog()
        }
    }

    fun deleteHabit(habit: HabitItem) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            closeHabitDialog()
            _uiState.update { it.copy(userMessage = "Habit deleted") }
        }
    }

    // Daily Review System
    fun openDailyReviewDialog() {
        _uiState.update { it.copy(showDailyReviewDialog = true) }
    }

    fun closeDailyReviewDialog() {
        _uiState.update { it.copy(showDailyReviewDialog = false) }
    }

    fun saveDailyReview(reflection: String, improvementArea: String) {
        viewModelScope.launch {
            val stats = _uiState.value.dayStats
            val tasks = _uiState.value.tasks
            val mostProdCat = tasks.filter { it.status == TaskStatus.COMPLETED }
                .groupBy { it.category }
                .maxByOrNull { entry -> entry.value.sumOf { it.actualDurationMinutes.coerceAtLeast(it.plannedDurationMinutes) } }
                ?.key ?: "Routine"

            val missedTitles = tasks.filter {
                it.status == TaskStatus.NOT_STARTED || it.status == TaskStatus.PARTIALLY_COMPLETED
            }.joinToString(", ") { it.title }

            val review = DailyReview(
                dateString = _selectedDate.value,
                totalTasksPlanned = stats.totalTasks,
                totalTasksCompleted = stats.completedTasks,
                totalTasksMissed = stats.missedTasks,
                completionPercentage = stats.productivityPercentage,
                totalProductiveMinutes = stats.totalProductiveMinutes,
                mostProductiveCategory = mostProdCat,
                tasksToReschedule = missedTitles,
                areasNeedingImprovement = improvementArea,
                personalReflection = reflection
            )
            repository.saveDailyReview(review)
            closeDailyReviewDialog()
            _uiState.update { it.copy(userMessage = "Daily reflection saved! Great job today.") }
        }
    }

    fun rescheduleAllMissedToTomorrow() {
        viewModelScope.launch {
            val tomorrow = LocalDate.now().plusDays(1).format(dateFormatter)
            val missed = _uiState.value.tasks.filter {
                it.status != TaskStatus.COMPLETED && it.status != TaskStatus.SKIPPED
            }
            missed.forEach { task ->
                repository.rescheduleTask(task.id, tomorrow)
            }
            _uiState.update { it.copy(userMessage = "${missed.size} incomplete tasks rescheduled to tomorrow") }
        }
    }

    // Settings
    fun updateSettings(settings: UserSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            _uiState.update { it.copy(settings = settings, userMessage = "Settings updated") }
        }
    }

    fun unlockPin(pin: String): Boolean {
        return if (pin == _uiState.value.settings.pinCode) {
            _uiState.update { it.copy(isPinLocked = false) }
            true
        } else {
            false
        }
    }

    fun lockApp() {
        if (_uiState.value.settings.isPinLockEnabled) {
            _uiState.update { it.copy(isPinLocked = true) }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}

class ProductivityViewModelFactory(
    private val repository: ProductivityRepository,
    private val notificationHelper: NotificationHelper?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProductivityViewModel(repository, notificationHelper) as T
    }
}
