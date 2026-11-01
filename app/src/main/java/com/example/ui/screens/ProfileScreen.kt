package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitItem
import com.example.data.model.UserSettings
import com.example.ui.components.HabitCard
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.SoftGold
import com.example.ui.theme.StatusCompleted
import com.example.ui.viewmodel.ProductivityUiState

@Composable
fun ProfileScreen(
    uiState: ProductivityUiState,
    onUpdateSettings: (UserSettings) -> Unit,
    onOpenDailyReview: () -> Unit,
    onOpenHabitDialog: (HabitItem?) -> Unit,
    onToggleHabit: (Long) -> Unit,
    onLockApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings

    var userName by remember(settings.userName) { mutableStateOf(settings.userName) }
    var userRole by remember(settings.userRole) { mutableStateOf(settings.userRole) }
    var dayStartTime by remember(settings.dayStartTime) { mutableStateOf(settings.dayStartTime) }
    var dayEndTime by remember(settings.dayEndTime) { mutableStateOf(settings.dayEndTime) }
    var workStartTime by remember(settings.workStartTime) { mutableStateOf(settings.workStartTime) }
    var workEndTime by remember(settings.workEndTime) { mutableStateOf(settings.workEndTime) }
    var weekStartDay by remember(settings.weekStartDay) { mutableStateOf(settings.weekStartDay) }
    var targetHours by remember(settings.targetProductiveHours) { mutableStateOf(settings.targetProductiveHours) }
    var targetRate by remember(settings.targetCompletionRate) { mutableStateOf(settings.targetCompletionRate.toFloat()) }
    var notifyTaskStart by remember(settings.notifyTaskStart) { mutableStateOf(settings.notifyTaskStart) }
    var notifyUpcoming by remember(settings.notifyUpcoming) { mutableStateOf(settings.notifyUpcoming) }
    var notifyOverdue by remember(settings.notifyOverdue) { mutableStateOf(settings.notifyOverdue) }
    var notifyDailySummary by remember(settings.notifyDailySummary) { mutableStateOf(settings.notifyDailySummary) }
    var notifyEndOfDayReview by remember(settings.notifyEndOfDayReview) { mutableStateOf(settings.notifyEndOfDayReview) }
    var isPinLockEnabled by remember(settings.isPinLockEnabled) { mutableStateOf(settings.isPinLockEnabled) }
    var pinCode by remember(settings.pinCode) { mutableStateOf(settings.pinCode) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp)
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SoftGold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = userRole,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = settings.userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Profile edit fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_name_input")
                    )
                    OutlinedTextField(
                        value = userRole,
                        onValueChange = { userRole = it },
                        label = { Text("Title / Focus") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_role_input")
                    )
                }
            }
        }

        // Daily Review & Reflection Launcher Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily Performance Review",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Summarize today's planned vs actual hours, wins, and reflections",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onOpenDailyReview,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = DeepNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("launch_daily_review_btn")
                ) {
                    Text("Review", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Habit Management Section
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Habits & Consistency",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Track daily behaviors and streaks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { onOpenHabitDialog(null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_new_habit_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Habit", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                uiState.habits.forEach { habitWithProgress ->
                    HabitCard(
                        habitWithProgress = habitWithProgress,
                        onToggle = { onToggleHabit(habitWithProgress.habit.id) },
                        onEdit = { onOpenHabitDialog(habitWithProgress.habit) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Daily Time Boundaries & Working Hours
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Daily Schedule & Hours",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = dayStartTime,
                        onValueChange = { dayStartTime = it },
                        label = { Text("Day Starts") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dayEndTime,
                        onValueChange = { dayEndTime = it },
                        label = { Text("Day Ends") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = workStartTime,
                        onValueChange = { workStartTime = it },
                        label = { Text("Work Starts") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = workEndTime,
                        onValueChange = { workEndTime = it },
                        label = { Text("Work Ends") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Week Starts On",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Monday", "Sunday").forEach { day ->
                        val sel = weekStartDay == day
                        FilterChip(
                            selected = sel,
                            onClick = { weekStartDay = day },
                            label = { Text(day) },
                            modifier = Modifier.testTag("week_start_$day")
                        )
                    }
                }
            }
        }

        // Personal Productivity Goals
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Personal Productivity Goals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Target Focus Hours / Day", style = MaterialTheme.typography.bodySmall)
                        Text(String.format("%.1fh", targetHours), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SoftGold)
                    }
                    Slider(
                        value = targetHours,
                        onValueChange = { targetHours = it },
                        valueRange = 2f..12f,
                        steps = 19
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Target Task Completion Rate", style = MaterialTheme.typography.bodySmall)
                        Text("${targetRate.toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SoftGold)
                    }
                    Slider(
                        value = targetRate,
                        onValueChange = { targetRate = it },
                        valueRange = 50f..100f,
                        steps = 9
                    )
                }
            }
        }

        // Smart Notification Settings
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Smart Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                SettingToggleRow(
                    title = "Task Start Reminders",
                    subtitle = "Alert when a planned task schedule begins",
                    checked = notifyTaskStart,
                    onCheckedChange = { notifyTaskStart = it }
                )
                SettingToggleRow(
                    title = "Upcoming Task Alerts",
                    subtitle = "Gentle nudge 10 minutes prior to scheduled slot",
                    checked = notifyUpcoming,
                    onCheckedChange = { notifyUpcoming = it }
                )
                SettingToggleRow(
                    title = "Overdue Task Warnings",
                    subtitle = "Alert when tasks exceed their planned time window",
                    checked = notifyOverdue,
                    onCheckedChange = { notifyOverdue = it }
                )
                SettingToggleRow(
                    title = "Daily Routine Morning Preview",
                    subtitle = "Overview of the day's commitments",
                    checked = notifyDailySummary,
                    onCheckedChange = { notifyDailySummary = it }
                )
                SettingToggleRow(
                    title = "End-of-Day Review Prompt",
                    subtitle = "Evening reminder to log reflection & reschedule",
                    checked = notifyEndOfDayReview,
                    onCheckedChange = { notifyEndOfDayReview = it }
                )
            }
        }

        // Security & App Lock (PIN Protection)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Security & Privacy Protection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                SettingToggleRow(
                    title = "Enable PIN Passcode Lock",
                    subtitle = "Require 4-digit PIN to access schedule and reflections",
                    checked = isPinLockEnabled,
                    onCheckedChange = { isPinLockEnabled = it }
                )

                if (isPinLockEnabled) {
                    OutlinedTextField(
                        value = pinCode,
                        onValueChange = { if (it.length <= 4) pinCode = it },
                        label = { Text("4-Digit PIN Code") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_code_setting_input")
                    )

                    Button(
                        onClick = onLockApp,
                        colors = ButtonDefaults.buttonColors(containerColor = PriorityHigh),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("lock_app_now_btn")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lock App Now")
                    }
                }
            }
        }

        // Save Settings Action Button
        Button(
            onClick = {
                val updated = settings.copy(
                    userName = userName.trim(),
                    userRole = userRole.trim(),
                    dayStartTime = dayStartTime.trim(),
                    dayEndTime = dayEndTime.trim(),
                    workStartTime = workStartTime.trim(),
                    workEndTime = workEndTime.trim(),
                    weekStartDay = weekStartDay,
                    targetProductiveHours = targetHours,
                    targetCompletionRate = targetRate.toInt(),
                    notifyTaskStart = notifyTaskStart,
                    notifyUpcoming = notifyUpcoming,
                    notifyOverdue = notifyOverdue,
                    notifyDailySummary = notifyDailySummary,
                    notifyEndOfDayReview = notifyEndOfDayReview,
                    isPinLockEnabled = isPinLockEnabled,
                    pinCode = pinCode
                )
                onUpdateSettings(updated)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_settings_btn")
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Settings & Preferences", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
