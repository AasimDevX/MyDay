package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.SoftGold
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailDialog(
    task: TaskItem,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (TaskItem) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var category by remember { mutableStateOf(task.category) }
    var priority by remember { mutableStateOf(task.priority) }
    var scheduledDate by remember { mutableStateOf(task.scheduledDate) }
    var startTime by remember { mutableStateOf(task.startTime) }
    var endTime by remember { mutableStateOf(task.endTime) }
    var status by remember { mutableStateOf(task.status) }
    var plannedDuration by remember { mutableStateOf(task.plannedDurationMinutes.toString()) }
    var actualDuration by remember { mutableStateOf(task.actualDurationMinutes.toString()) }
    var completionPercent by remember { mutableStateOf(task.completionPercentage.toFloat()) }
    var personalNotes by remember { mutableStateOf(task.personalNotes) }
    var isRecurring by remember { mutableStateOf(task.isRecurring) }
    var recurrenceType by remember { mutableStateOf(task.recurrenceType) }

    var titleError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar in Dialog
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNew) "Schedule Daily Task" else "Edit Task Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_task_dialog")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                // Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (it.isNotBlank()) titleError = false
                        },
                        label = { Text("Task Title *") },
                        isError = titleError,
                        supportingText = if (titleError) { { Text("Title cannot be empty") } } else null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_title_input")
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Objectives (optional)") },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_description_input")
                    )

                    // Category Selection
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Categories.ALL.take(4).forEach { cat ->
                            val isSelected = category.equals(cat.name, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = cat.name },
                                label = { Text(cat.name, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = cat.color.copy(alpha = 0.2f),
                                    selectedLabelColor = cat.color
                                ),
                                modifier = Modifier.testTag("category_chip_${cat.name}")
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Categories.ALL.drop(4).forEach { cat ->
                            val isSelected = category.equals(cat.name, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = cat.name },
                                label = { Text(cat.name, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = cat.color.copy(alpha = 0.2f),
                                    selectedLabelColor = cat.color
                                ),
                                modifier = Modifier.testTag("category_chip_${cat.name}")
                            )
                        }
                    }

                    // Priority
                    Text(
                        text = "Priority Level",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TaskPriority.values().forEach { prio ->
                            val selected = priority == prio
                            OutlinedButton(
                                onClick = { priority = prio },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selected) prio.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                    contentColor = if (selected) prio.color else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("priority_btn_${prio.name}")
                            ) {
                                Text(prio.displayName, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    // Time Schedule (Start & End)
                    Text(
                        text = "Schedule Time & Duration",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start (HH:mm)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("task_start_time_input")
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("End (HH:mm)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("task_end_time_input")
                        )
                    }

                    // Planned Duration & Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = scheduledDate,
                            onValueChange = { scheduledDate = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("task_date_input")
                        )
                        OutlinedTextField(
                            value = plannedDuration,
                            onValueChange = { plannedDuration = it },
                            label = { Text("Planned (min)") },
                            modifier = Modifier
                                .weight(0.8f)
                                .testTag("task_planned_duration_input")
                        )
                    }

                    // Recurrence Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Recurring Routine",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Repeat automatically on future days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it },
                            modifier = Modifier.testTag("recurrence_switch")
                        )
                    }

                    if (isRecurring) {
                        Text(
                            text = "Repeat Pattern",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(RecurrenceType.DAILY, RecurrenceType.WEEKDAYS, RecurrenceType.WEEKENDS).forEach { rec ->
                                val sel = recurrenceType == rec
                                FilterChip(
                                    selected = sel,
                                    onClick = { recurrenceType = rec },
                                    label = { Text(rec.displayName, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("recurrence_${rec.name}")
                                )
                            }
                        }
                    }

                    // Task Status
                    Text(
                        text = "Current Status",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TaskStatus.values().forEach { st ->
                            val sel = status == st
                            FilterChip(
                                selected = sel,
                                onClick = {
                                    status = st
                                    if (st == TaskStatus.COMPLETED) completionPercent = 100f
                                    else if (st == TaskStatus.NOT_STARTED) completionPercent = 0f
                                },
                                label = { Text(st.displayName, fontSize = 11.sp) },
                                modifier = Modifier.testTag("dialog_status_${st.name}")
                            )
                        }
                    }

                    // Completion percentage slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Completion Progress", style = MaterialTheme.typography.labelSmall)
                            Text("${completionPercent.toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = completionPercent,
                            onValueChange = { completionPercent = it },
                            valueRange = 0f..100f,
                            steps = 19,
                            modifier = Modifier.testTag("completion_slider")
                        )
                    }

                    // Personal Notes
                    OutlinedTextField(
                        value = personalNotes,
                        onValueChange = { personalNotes = it },
                        label = { Text("Personal Reflections / Notes") },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_notes_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                HorizontalDivider()

                // Bottom Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_task_button")) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                                return@Button
                            }
                            val plannedMin = plannedDuration.toIntOrNull() ?: 60
                            val actualMin = actualDuration.toIntOrNull() ?: 0

                            val updated = task.copy(
                                title = title.trim(),
                                description = description.trim(),
                                category = category,
                                priority = priority,
                                scheduledDate = scheduledDate.trim(),
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                status = status,
                                plannedDurationMinutes = plannedMin,
                                actualDurationMinutes = actualMin,
                                completionPercentage = completionPercent.toInt(),
                                personalNotes = personalNotes.trim(),
                                isRecurring = isRecurring,
                                recurrenceType = if (isRecurring) recurrenceType else RecurrenceType.NONE,
                                updatedAt = System.currentTimeMillis()
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("save_task_button")
                    ) {
                        Text(if (isNew) "Add to Routine" else "Save Changes")
                    }
                }
            }
        }
    }
}
