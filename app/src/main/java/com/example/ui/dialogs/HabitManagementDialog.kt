package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.HabitItem
import com.example.ui.theme.*

@Composable
fun HabitManagementDialog(
    habitToEdit: HabitItem?,
    onSave: (HabitItem) -> Unit,
    onDelete: (HabitItem) -> Unit,
    onDismiss: () -> Unit
) {
    val isNew = habitToEdit == null
    var title by remember { mutableStateOf(habitToEdit?.title ?: "") }
    var description by remember { mutableStateOf(habitToEdit?.description ?: "") }
    var category by remember { mutableStateOf(habitToEdit?.category ?: "Health & Fitness") }
    var frequencyType by remember { mutableStateOf(habitToEdit?.frequencyType ?: "DAILY") }
    var selectedColorHex by remember { mutableStateOf(habitToEdit?.colorHex ?: "#C9A227") }
    var titleError by remember { mutableStateOf(false) }

    val colorOptions = listOf(
        "#C9A227", // Soft Gold
        "#10B981", // Emerald
        "#3B82F6", // Blue
        "#8B5CF6", // Purple
        "#EC4899", // Pink
        "#F97316"  // Orange
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNew) "Create Habit" else "Edit Habit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_habit_dialog")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Habit Name *") },
                    isError = titleError,
                    supportingText = if (titleError) { { Text("Title cannot be empty") } } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_title_input")
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Frequency & Target (e.g. 10 mins daily)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_desc_input")
                )

                // Frequency chips
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("DAILY" to "Daily", "WEEKDAYS" to "Weekdays", "CUSTOM" to "3x / Week").forEach { (type, label) ->
                        val sel = frequencyType == type
                        FilterChip(
                            selected = sel,
                            onClick = { frequencyType = type },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.testTag("habit_freq_$type")
                        )
                    }
                }

                // Accent Color Picker
                Text(
                    text = "Habit Color",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colorOptions.forEach { hex ->
                        val c = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColorHex == hex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                                .testTag("color_picker_$hex")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isNew && habitToEdit != null) {
                        IconButton(
                            onClick = { onDelete(habitToEdit) },
                            modifier = Modifier.testTag("delete_habit_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PriorityHigh)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (title.isBlank()) {
                                    titleError = true
                                    return@Button
                                }
                                val item = (habitToEdit ?: HabitItem(title = "")).copy(
                                    title = title.trim(),
                                    description = description.trim(),
                                    category = category,
                                    frequencyType = frequencyType,
                                    colorHex = selectedColorHex
                                )
                                onSave(item)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("save_habit_btn")
                        ) {
                            Text(if (isNew) "Create Habit" else "Save")
                        }
                    }
                }
            }
        }
    }
}
