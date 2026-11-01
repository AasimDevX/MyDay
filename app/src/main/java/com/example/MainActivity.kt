package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.model.TaskStatus
import com.example.data.repository.ProductivityRepository
import com.example.notification.NotificationHelper
import com.example.ui.components.BottomNavBar
import com.example.ui.components.TopHeaderBar
import com.example.ui.dialogs.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val database = remember { AppDatabase.getDatabase(context) }
            val repository = remember {
                ProductivityRepository(
                    taskDao = database.taskDao(),
                    habitDao = database.habitDao(),
                    dailyReviewDao = database.dailyReviewDao(),
                    settingsDao = database.settingsDao()
                )
            }
            val notificationHelper = remember { NotificationHelper(context) }

            val viewModel: ProductivityViewModel = viewModel(
                factory = ProductivityViewModelFactory(repository, notificationHelper)
            )

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val isDarkTheme = when (uiState.settings.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppContent(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: ProductivityViewModel,
    uiState: ProductivityUiState
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showCalendarPicker by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    // Handle user feedback message
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // BackHandler for secondary tabs
    if (uiState.currentNav != NavItem.HOME && !uiState.isPinLocked) {
        BackHandler {
            viewModel.setNav(NavItem.HOME)
        }
    }

    // Pin Lock Gate
    if (uiState.isPinLocked) {
        PinAuthScreen(
            onUnlock = { pin -> viewModel.unlockPin(pin) }
        )
        return
    }

    Scaffold(
        topBar = {
            TopHeaderBar(
                userName = uiState.settings.userName,
                selectedDateStr = uiState.selectedDate,
                todayDateStr = uiState.todayDate,
                onTodayClick = { viewModel.selectDate(uiState.todayDate) },
                onCalendarClick = { showCalendarPicker = true },
                onNotificationClick = { showNotificationDialog = true }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedItem = uiState.currentNav,
                onItemSelected = { nav -> viewModel.setNav(nav) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = uiState.currentNav, label = "navigation_crossfade") { screen ->
                when (screen) {
                    NavItem.HOME -> HomeScreen(
                        uiState = uiState,
                        onStatusChange = { id, status -> viewModel.updateTaskStatus(id, status) },
                        onStartTimer = { task -> viewModel.startTimerForTask(task) },
                        onEditTask = { task -> viewModel.openEditTaskDialog(task) },
                        onDuplicateTask = { id -> viewModel.duplicateTask(id) },
                        onRescheduleTask = { id ->
                            val nextDay = java.time.LocalDate.parse(uiState.selectedDate).plusDays(1).toString()
                            viewModel.rescheduleTask(id, nextDay)
                        },
                        onDeleteTask = { id -> viewModel.deleteTask(id) },
                        onToggleHabit = { habitId -> viewModel.toggleHabit(habitId) },
                        onAddNewTask = { viewModel.openNewTaskDialog() },
                        onOpenDailyReview = { viewModel.openDailyReviewDialog() },
                        onNavigate = { nav -> viewModel.setNav(nav) }
                    )

                    NavItem.ROUTINE -> RoutineScreen(
                        uiState = uiState,
                        onDateSelected = { date -> viewModel.selectDate(date) },
                        onViewModeChange = { mode -> viewModel.setRoutineViewMode(mode) },
                        onStatusChange = { id, status -> viewModel.updateTaskStatus(id, status) },
                        onStartTimer = { task -> viewModel.startTimerForTask(task) },
                        onEditTask = { task -> viewModel.openEditTaskDialog(task) },
                        onDuplicateTask = { id -> viewModel.duplicateTask(id) },
                        onRescheduleTask = { id ->
                            val nextDay = java.time.LocalDate.parse(uiState.selectedDate).plusDays(1).toString()
                            viewModel.rescheduleTask(id, nextDay)
                        },
                        onDeleteTask = { id -> viewModel.deleteTask(id) },
                        onAddNewTask = { initialTime -> viewModel.openNewTaskDialog(initialTime) }
                    )

                    NavItem.TASKS -> TasksScreen(
                        uiState = uiState,
                        onSearchChange = { q -> viewModel.setSearchQuery(q) },
                        onStatusFilterChange = { st -> viewModel.setStatusFilter(st) },
                        onPriorityFilterChange = { prio -> viewModel.setPriorityFilter(prio) },
                        onCategoryFilterChange = { cat -> viewModel.setCategoryFilter(cat) },
                        onStatusChange = { id, status -> viewModel.updateTaskStatus(id, status) },
                        onStartTimer = { task -> viewModel.startTimerForTask(task) },
                        onEditTask = { task -> viewModel.openEditTaskDialog(task) },
                        onDuplicateTask = { id -> viewModel.duplicateTask(id) },
                        onRescheduleTask = { id ->
                            val nextDay = java.time.LocalDate.parse(uiState.selectedDate).plusDays(1).toString()
                            viewModel.rescheduleTask(id, nextDay)
                        },
                        onDeleteTask = { id -> viewModel.deleteTask(id) },
                        onAddNewTask = { viewModel.openNewTaskDialog() }
                    )

                    NavItem.ANALYTICS -> AnalyticsScreen(
                        uiState = uiState
                    )

                    NavItem.PROFILE -> ProfileScreen(
                        uiState = uiState,
                        onUpdateSettings = { settings -> viewModel.updateSettings(settings) },
                        onOpenDailyReview = { viewModel.openDailyReviewDialog() },
                        onOpenHabitDialog = { habit -> viewModel.openHabitDialog(habit) },
                        onToggleHabit = { habitId -> viewModel.toggleHabit(habitId) },
                        onLockApp = { viewModel.lockApp() }
                    )
                }
            }
        }
    }

    // Task Create / Edit Dialog
    if (uiState.isTaskDialogOpen && uiState.taskDialogData != null) {
        TaskDetailDialog(
            task = uiState.taskDialogData!!,
            isNew = uiState.isNewTaskDialog,
            onDismiss = { viewModel.closeTaskDialog() },
            onSave = { updated -> viewModel.saveTask(updated) }
        )
    }

    // Active Timer / Stopwatch Dialog
    if (uiState.activeTimerTask != null) {
        TaskTimerDialog(
            task = uiState.activeTimerTask!!,
            elapsedSeconds = uiState.timerElapsedSeconds,
            isRunning = uiState.isTimerRunning,
            onTogglePause = { viewModel.toggleTimerPause() },
            onFinish = { asCompleted, notes -> viewModel.finishActiveTimer(asCompleted, notes) },
            onDismiss = { viewModel.dismissTimer() }
        )
    }

    // Daily Review & Reflection Dialog
    if (uiState.showDailyReviewDialog) {
        DailyReviewDialog(
            dateStr = uiState.selectedDate,
            stats = uiState.dayStats,
            existingReview = uiState.dailyReview,
            onRescheduleMissed = { viewModel.rescheduleAllMissedToTomorrow() },
            onSaveReview = { reflection, improvement -> viewModel.saveDailyReview(reflection, improvement) },
            onDismiss = { viewModel.closeDailyReviewDialog() }
        )
    }

    // Habit Create / Edit Dialog
    if (uiState.showHabitDialog) {
        HabitManagementDialog(
            habitToEdit = uiState.habitToEdit,
            onSave = { habit -> viewModel.saveHabit(habit) },
            onDelete = { habit -> viewModel.deleteHabit(habit) },
            onDismiss = { viewModel.closeHabitDialog() }
        )
    }

    // Full Month Calendar Dialog
    if (showCalendarPicker) {
        CalendarPickerDialog(
            selectedDateStr = uiState.selectedDate,
            todayDateStr = uiState.todayDate,
            tasks = uiState.allTasks,
            onDateSelected = { date -> viewModel.selectDate(date) },
            onDismiss = { showCalendarPicker = false }
        )
    }

    // Quick Notifications Preview Dialog
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Smart Notifications") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Active Reminders for ${uiState.selectedDate}:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("• Task start alerts configured (${uiState.settings.reminderLeadMinutes}m prior)")
                    Text("• Evening reflection prompt at ${uiState.settings.dayEndTime}")
                    Text("• Overdue task notices active")
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}
