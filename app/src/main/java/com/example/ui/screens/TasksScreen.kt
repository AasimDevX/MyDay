package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Categories
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.ui.components.TaskCard
import com.example.ui.theme.SoftGold
import com.example.ui.viewmodel.ProductivityUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    uiState: ProductivityUiState,
    onSearchChange: (String) -> Unit,
    onStatusFilterChange: (TaskStatus?) -> Unit,
    onPriorityFilterChange: (TaskPriority?) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onStatusChange: (Long, TaskStatus) -> Unit,
    onStartTimer: (TaskItem) -> Unit,
    onEditTask: (TaskItem) -> Unit,
    onDuplicateTask: (Long) -> Unit,
    onRescheduleTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onAddNewTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter tasks based on search, status, priority, category
    val allTasks = uiState.allTasks
    val filtered = remember(
        allTasks,
        uiState.searchQuery,
        uiState.selectedStatusFilter,
        uiState.selectedPriorityFilter,
        uiState.selectedCategoryFilter
    ) {
        allTasks.filter { task ->
            val matchSearch = uiState.searchQuery.isBlank() ||
                    task.title.contains(uiState.searchQuery, ignoreCase = true) ||
                    task.description.contains(uiState.searchQuery, ignoreCase = true)
            val matchStatus = uiState.selectedStatusFilter == null || task.status == uiState.selectedStatusFilter
            val matchPriority = uiState.selectedPriorityFilter == null || task.priority == uiState.selectedPriorityFilter
            val matchCategory = uiState.selectedCategoryFilter == null || task.category.equals(uiState.selectedCategoryFilter, ignoreCase = true)

            matchSearch && matchStatus && matchPriority && matchCategory
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewTask,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("tasks_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search scheduled tasks or notes...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("tasks_search_input")
            )

            // Status Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedStatusFilter == null,
                        onClick = { onStatusFilterChange(null) },
                        label = { Text("All Statuses", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_status_all")
                    )
                }
                items(TaskStatus.values()) { st ->
                    val isSel = uiState.selectedStatusFilter == st
                    FilterChip(
                        selected = isSel,
                        onClick = { onStatusFilterChange(if (isSel) null else st) },
                        label = { Text(st.displayName, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = st.color.copy(alpha = 0.2f),
                            selectedLabelColor = st.color
                        ),
                        modifier = Modifier.testTag("filter_status_${st.name}")
                    )
                }
            }

            // Priority & Category Filters Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TaskPriority.values()) { prio ->
                    val isSel = uiState.selectedPriorityFilter == prio
                    FilterChip(
                        selected = isSel,
                        onClick = { onPriorityFilterChange(if (isSel) null else prio) },
                        label = { Text("${prio.displayName} Priority", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_priority_${prio.name}")
                    )
                }

                items(Categories.ALL) { cat ->
                    val isSel = uiState.selectedCategoryFilter.equals(cat.name, ignoreCase = true)
                    FilterChip(
                        selected = isSel,
                        onClick = { onCategoryFilterChange(if (isSel) null else cat.name) },
                        label = { Text(cat.name, fontSize = 11.sp) },
                        modifier = Modifier.testTag("tasks_filter_cat_${cat.id}")
                    )
                }
            }

            // Summary text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${filtered.size} tasks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (uiState.selectedStatusFilter != null || uiState.selectedPriorityFilter != null || uiState.selectedCategoryFilter != null) {
                    TextButton(
                        onClick = {
                            onStatusFilterChange(null)
                            onPriorityFilterChange(null)
                            onCategoryFilterChange(null)
                        }
                    ) {
                        Text("Reset Filters", fontSize = 11.sp, color = SoftGold)
                    }
                }
            }

            // Tasks List
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No tasks found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search or filters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { task ->
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
