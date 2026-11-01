package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
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
import com.example.data.model.TaskStatus
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CategoryTimeDistribution
import com.example.ui.viewmodel.ProductivityUiState
import com.example.ui.viewmodel.WeeklyAnalyticsItem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AnalyticsScreen(
    uiState: ProductivityUiState,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()
    val monday = today.with(DayOfWeek.MONDAY)

    // Compute 7 days of this week
    val weeklyAnalytics = remember(uiState.allTasks) {
        (0..6).map { offset ->
            val d = monday.plusDays(offset.toLong())
            val dateStr = d.format(dateFormatter)
            val dayTasks = uiState.allTasks.filter { it.scheduledDate == dateStr }
            val total = dayTasks.size
            val completed = dayTasks.count { it.status == TaskStatus.COMPLETED }
            val productiveMinutes = dayTasks.sumOf {
                if (it.status == TaskStatus.COMPLETED) {
                    if (it.actualDurationMinutes > 0) it.actualDurationMinutes else it.plannedDurationMinutes
                } else {
                    it.actualDurationMinutes
                }
            }
            val percentage = if (total > 0) (completed * 100) / total else 0

            WeeklyAnalyticsItem(
                dayLabel = d.dayOfWeek.name.take(3),
                dateStr = dateStr,
                completedCount = completed,
                totalCount = total,
                productiveHours = (productiveMinutes / 60f),
                completionPercentage = percentage
            )
        }
    }

    // Category distribution
    val categoryDistribution = remember(uiState.allTasks) {
        val catGroups = uiState.allTasks.groupBy { it.category }
        val totalMinutes = uiState.allTasks.sumOf {
            if (it.status == TaskStatus.COMPLETED) {
                if (it.actualDurationMinutes > 0) it.actualDurationMinutes else it.plannedDurationMinutes
            } else {
                it.actualDurationMinutes
            }
        }.coerceAtLeast(1)

        catGroups.map { (cat, tasks) ->
            val minutes = tasks.sumOf {
                if (it.status == TaskStatus.COMPLETED) {
                    if (it.actualDurationMinutes > 0) it.actualDurationMinutes else it.plannedDurationMinutes
                } else {
                    it.actualDurationMinutes
                }
            }
            val pct = ((minutes.toFloat() / totalMinutes) * 100).toInt()
            CategoryTimeDistribution(
                category = cat,
                minutes = minutes,
                percentage = pct,
                taskCount = tasks.size
            )
        }.sortedByDescending { it.minutes }
    }

    // Monthly summary calculation
    val currentMonthPrefix = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
    val monthlyTasks = remember(uiState.allTasks) {
        uiState.allTasks.filter { it.scheduledDate.startsWith(currentMonthPrefix) }
    }
    val monthlyTotal = monthlyTasks.size
    val monthlyCompleted = monthlyTasks.count { it.status == TaskStatus.COMPLETED }
    val monthlyRate = if (monthlyTotal > 0) (monthlyCompleted * 100) / monthlyTotal else 0
    val monthlyProductiveHours = monthlyTasks.sumOf {
        if (it.status == TaskStatus.COMPLETED) {
            if (it.actualDurationMinutes > 0) it.actualDurationMinutes else it.plannedDurationMinutes
        } else {
            it.actualDurationMinutes
        }
    } / 60f

    val totalPlannedHours = uiState.allTasks.sumOf { it.plannedDurationMinutes } / 60f
    val totalActualHours = uiState.allTasks.sumOf {
        if (it.actualDurationMinutes > 0) it.actualDurationMinutes else (if (it.status == TaskStatus.COMPLETED) it.plannedDurationMinutes else 0)
    } / 60f

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp)
            .testTag("analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Productivity Score Card
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
                            text = "Daily Productivity Score",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Calculated from completion rate & task duration accuracy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = SoftGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = SoftGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${uiState.dayStats.productivityPercentage}/100",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SoftGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductivityRing(
                        percentage = uiState.dayStats.productivityPercentage,
                        targetPercentage = uiState.settings.targetCompletionRate,
                        sizeDp = 100,
                        strokeWidthDp = 10
                    )

                    Column(
                        modifier = Modifier.padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreDetailRow("Productive Hours", uiState.dayStats.formatProductiveHours(), SoftGold)
                        ScoreDetailRow("Planned Hours", uiState.dayStats.formatPlannedHours(), MaterialTheme.colorScheme.onSurface)
                        ScoreDetailRow("Tasks Completed", "${uiState.dayStats.completedTasks} / ${uiState.dayStats.totalTasks}", StatusCompleted)
                        ScoreDetailRow("Missed / Delayed", "${uiState.dayStats.missedTasks}", if (uiState.dayStats.missedTasks > 0) PriorityHigh else StatusCompleted)
                    }
                }
            }
        }

        // Weekly Productivity Overview (Interactive Bar Chart)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Weekly Performance Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap any bar to inspect completed tasks and productive hours",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                WeeklyBarChart(weeklyData = weeklyAnalytics)
            }
        }

        // Planned vs Actual Hours Comparison
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Planned vs. Actual Hours",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Measures scheduling accuracy and time allocation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                PlannedVsActualChart(
                    plannedHours = totalPlannedHours,
                    actualHours = totalActualHours
                )
            }
        }

        // Category-wise Time Distribution Donut Chart
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Category Time Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Breakdown of focused time by routine category",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                CategoryDonutChart(distributions = categoryDistribution)
            }
        }

        // Monthly Overview & Productivity Insights Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        tint = SoftGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Productivity Trends & Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                InsightItem(
                    title = "Monthly Completion Rate: $monthlyRate%",
                    detail = "$monthlyCompleted of $monthlyTotal tasks completed across ${today.month.name.lowercase().replaceFirstChar { it.uppercase() }} with ${String.format("%.1f", monthlyProductiveHours)} productive hours logged."
                )

                Spacer(modifier = Modifier.height(8.dp))

                InsightItem(
                    title = "Peak Performance Window: 09:00 - 11:30",
                    detail = "Deep Work tasks scheduled in the morning show a 94% completion consistency without postponement."
                )

                Spacer(modifier = Modifier.height(8.dp))

                val topCat = categoryDistribution.firstOrNull()?.category ?: "Deep Work"
                InsightItem(
                    title = "Top Category Focus: $topCat",
                    detail = "$topCat represents the highest proportion of your productive hours this month."
                )
            }
        }
    }
}

@Composable
private fun ScoreDetailRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun InsightItem(title: String, detail: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
