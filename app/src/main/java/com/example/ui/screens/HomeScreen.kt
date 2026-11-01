package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Categories
import com.example.data.model.TaskItem
import com.example.data.model.TaskStatus
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.NavItem
import com.example.ui.viewmodel.ProductivityUiState

@Composable
fun HomeScreen(
    uiState: ProductivityUiState,
    onStatusChange: (Long, TaskStatus) -> Unit,
    onStartTimer: (TaskItem) -> Unit,
    onEditTask: (TaskItem) -> Unit,
    onDuplicateTask: (Long) -> Unit,
    onRescheduleTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onToggleHabit: (Long) -> Unit,
    onAddNewTask: () -> Unit,
    onOpenDailyReview: () -> Unit,
    onNavigate: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val stats = uiState.dayStats
    val isToday = uiState.selectedDate == uiState.todayDate

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Productivity Overview Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("productivity_hero_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isToday) "Today's Productivity" else "Routine for ${uiState.selectedDate}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${stats.completedTasks} of ${stats.totalTasks} tasks completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = SoftGold,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = DeepNavy,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${stats.formatProductiveHours()} focus",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                }
                            }
                        }
                    }

                    ProductivityRing(
                        percentage = stats.productivityPercentage,
                        targetPercentage = uiState.settings.targetCompletionRate,
                        sizeDp = 110,
                        strokeWidthDp = 10
                    )
                }
            }
        }

        // 4 Key Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Scheduled",
                    value = "${stats.totalTasks}",
                    subtitle = "total",
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Outlined.Schedule,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_scheduled"
                )
                StatCard(
                    title = "Completed",
                    value = "${stats.completedTasks}",
                    subtitle = "finished",
                    color = StatusCompleted,
                    icon = Icons.Outlined.CheckCircle,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_completed"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Pending",
                    value = "${stats.pendingTasks}",
                    subtitle = "in queue",
                    color = StatusInProgress,
                    icon = Icons.Default.PendingActions,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_pending"
                )
                StatCard(
                    title = "Missed",
                    value = "${stats.missedTasks}",
                    subtitle = "past schedule",
                    color = if (stats.missedTasks > 0) PriorityHigh else MaterialTheme.colorScheme.onSurfaceVariant,
                    icon = Icons.Default.WarningAmber,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_missed"
                )
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddNewTask,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quick_add_task_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Task", fontSize = 13.sp)
                }

                FilledTonalButton(
                    onClick = { onNavigate(NavItem.ROUTINE) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("view_routine_btn")
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Routine", fontSize = 13.sp)
                }

                IconButton(
                    onClick = onOpenDailyReview,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("open_review_fab")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RateReview,
                        contentDescription = "Daily Review",
                        tint = SoftGold
                    )
                }
            }
        }

        // Upcoming Task Section
        item {
            Text(
                text = "Next Upcoming Focus",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (stats.upcomingTask != null) {
            item {
                val upTask = stats.upcomingTask
                val category = Categories.getCategory(upTask.category)

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upcoming_task_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(category.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = category.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${upTask.startTime} - ${upTask.endTime} • ${category.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftGold,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = upTask.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { onStartTimer(upTask) },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SoftGold)
                                .testTag("start_upcoming_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Start Timer",
                                tint = DeepNavy,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "All scheduled tasks for today are completed! 🎉",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Habit Streak Ticker
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Habit Streaks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { onNavigate(NavItem.PROFILE) }) {
                    Text("Manage", color = SoftGold, fontSize = 12.sp)
                }
            }
        }

        if (uiState.habits.isNotEmpty()) {
            items(uiState.habits) { habitWithProgress ->
                HabitCard(
                    habitWithProgress = habitWithProgress,
                    onToggle = { onToggleHabit(habitWithProgress.habit.id) },
                    onEdit = { }
                )
            }
        }

        // Today's Routine Tasks Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scheduled Routine (${uiState.tasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { onNavigate(NavItem.ROUTINE) }) {
                    Text("Timeline", color = SoftGold, fontSize = 12.sp)
                }
            }
        }

        if (uiState.tasks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tasks scheduled for this day",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add blocks to your daily routine to stay productive",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.tasks.take(4)) { task ->
                TaskCard(
                    task = task,
                    onStatusChange = { newStatus -> onStatusChange(task.id, newStatus) },
                    onStartTimer = { onStartTimer(task) },
                    onEdit = { onEditTask(task) },
                    onDuplicate = { onDuplicateTask(task.id) },
                    onReschedule = { onRescheduleTask(task.id) },
                    onDelete = { onDeleteTask(task.id) }
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}
