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
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ViewDay
import androidx.compose.material.icons.outlined.ViewTimeline
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
import com.example.data.model.Categories
import com.example.data.model.TaskItem
import com.example.data.model.TaskStatus
import com.example.ui.components.DatePickerStrip
import com.example.ui.components.TaskCard
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.SoftGold
import com.example.ui.viewmodel.ProductivityUiState
import com.example.ui.viewmodel.RoutineViewMode

@Composable
fun RoutineScreen(
    uiState: ProductivityUiState,
    onDateSelected: (String) -> Unit,
    onViewModeChange: (RoutineViewMode) -> Unit,
    onStatusChange: (Long, TaskStatus) -> Unit,
    onStartTimer: (TaskItem) -> Unit,
    onEditTask: (TaskItem) -> Unit,
    onDuplicateTask: (Long) -> Unit,
    onRescheduleTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onAddNewTask: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val filteredTasks = remember(uiState.tasks, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) {
            uiState.tasks
        } else {
            uiState.tasks.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddNewTask(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("routine_fab_add_task")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task to Routine")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Date Picker Strip
            DatePickerStrip(
                selectedDateStr = uiState.selectedDate,
                todayDateStr = uiState.todayDate,
                onDateSelected = onDateSelected
            )

            // View Mode & Category Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Slots vs Timeline Switcher
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        val isSlots = uiState.routineViewMode == RoutineViewMode.TIME_SLOTS
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSlots) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable { onViewModeChange(RoutineViewMode.TIME_SLOTS) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("view_mode_slots"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.ViewDay,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isSlots) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Slots",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSlots) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSlots) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val isTimeline = uiState.routineViewMode == RoutineViewMode.TIMELINE
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isTimeline) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable { onViewModeChange(RoutineViewMode.TIMELINE) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("view_mode_timeline"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.ViewTimeline,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isTimeline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Timeline",
                                    fontSize = 11.sp,
                                    fontWeight = if (isTimeline) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isTimeline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "${filteredTasks.size} blocks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Category Filter Horizontal Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All Categories", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_cat_all")
                    )
                }
                items(Categories.ALL) { cat ->
                    val isSel = selectedCategoryFilter.equals(cat.name, ignoreCase = true)
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            selectedCategoryFilter = if (isSel) null else cat.name
                        },
                        label = { Text(cat.name, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = cat.color.copy(alpha = 0.2f),
                            selectedLabelColor = cat.color
                        ),
                        modifier = Modifier.testTag("filter_cat_${cat.id}")
                    )
                }
            }

            // Routine Content List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = SoftGold,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No routine blocks scheduled",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + below to build your schedule for ${uiState.selectedDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (uiState.routineViewMode == RoutineViewMode.TIME_SLOTS) {
                // Grouped by Slots: Morning (05-11), Afternoon (12-16), Evening (17-21), Night (22-04)
                val slots = listOf("Morning", "Afternoon", "Evening", "Night")
                val groupedTasks = filteredTasks.groupBy { it.getTimeSlot() }

                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    slots.forEach { slot ->
                        val tasksInSlot = groupedTasks[slot].orEmpty()
                        if (tasksInSlot.isNotEmpty()) {
                            item(key = "header_$slot") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$slot Routine",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    TextButton(
                                        onClick = {
                                            val initialTime = when (slot) {
                                                "Morning" -> "09:00"
                                                "Afternoon" -> "13:00"
                                                "Evening" -> "18:00"
                                                else -> "21:00"
                                            }
                                            onAddNewTask(initialTime)
                                        }
                                    ) {
                                        Text("+ Add to $slot", fontSize = 11.sp, color = SoftGold)
                                    }
                                }
                            }

                            items(tasksInSlot, key = { it.id }) { task ->
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
            } else {
                // Chronological Timeline Format
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Timeline Time Rail
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(52.dp)
                                    .padding(top = 4.dp)
                            ) {
                                Text(
                                    text = task.startTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(48.dp)
                                        .background(SoftGold.copy(alpha = 0.4f))
                                )
                                Text(
                                    text = task.endTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Task Card
                            Box(modifier = Modifier.weight(1f)) {
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
            }
        }
    }
}
