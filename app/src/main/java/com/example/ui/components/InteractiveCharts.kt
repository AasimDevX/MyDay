package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Categories
import com.example.ui.theme.*
import com.example.ui.viewmodel.CategoryTimeDistribution
import com.example.ui.viewmodel.WeeklyAnalyticsItem
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProductivityRing(
    percentage: Int,
    targetPercentage: Int = 85,
    sizeDp: Int = 140,
    strokeWidthDp: Int = 14,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "progress_ring"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(sizeDp.dp)
            .testTag("productivity_ring")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidthDp.dp.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)

            // Background Track
            drawArc(
                color = SoftGold.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Progress Arc
            if (animatedProgress > 0) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to SoftGold,
                        0.5f to SoftGoldLight,
                        1.0f to SoftGold
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Target: $targetPercentage%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WeeklyBarChart(
    weeklyData: List<WeeklyAnalyticsItem>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_bar_chart")
    ) {
        if (selectedIndex != null && selectedIndex!! < weeklyData.size) {
            val item = weeklyData[selectedIndex!!]
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "${item.dayLabel}: ${item.completedCount}/${item.totalCount} completed (${item.productiveHours}h productive)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SoftGold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val maxCount = weeklyData.maxOfOrNull { it.totalCount }?.coerceAtLeast(5) ?: 5
            val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val barWidth = size.width / weeklyData.size
                            val index = (offset.x / barWidth).toInt().coerceIn(0, weeklyData.size - 1)
                            selectedIndex = index
                        }
                    }
            ) {
                val count = weeklyData.size
                if (count == 0) return@Canvas
                val barSlotWidth = size.width / count
                val barWidth = barSlotWidth * 0.45f
                val chartHeight = size.height - 24.dp.toPx()

                weeklyData.forEachIndexed { i, item ->
                    val x = i * barSlotWidth + (barSlotWidth - barWidth) / 2
                    val isSelected = selectedIndex == i

                    // Total planned bar (background track)
                    val totalBarHeight = if (item.totalCount > 0) (item.totalCount.toFloat() / maxCount) * chartHeight else 4.dp.toPx()
                    val totalY = chartHeight - totalBarHeight

                    drawRoundRect(
                        color = trackColor,
                        topLeft = Offset(x, totalY),
                        size = Size(barWidth, totalBarHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )

                    // Completed bar
                    val completedHeight = if (maxCount > 0) (item.completedCount.toFloat() / maxCount) * chartHeight else 0f
                    val completedY = chartHeight - completedHeight

                    if (completedHeight > 0) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(
                                    if (isSelected) SoftGoldLight else SoftGold,
                                    if (isSelected) SoftGold else SoftGoldDark
                                )
                            ),
                            topLeft = Offset(x, completedY),
                            size = Size(barWidth, completedHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                        )
                    }
                }
            }
        }

        // Days labels row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeklyData.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                Text(
                    text = item.dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) SoftGold else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CategoryDonutChart(
    distributions: List<CategoryTimeDistribution>,
    modifier: Modifier = Modifier
) {
    if (distributions.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No category data for this period",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_donut_chart"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Donut Canvas
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(140.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 18.dp.toPx()
                val diameter = size.minDimension - stroke
                val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                val arcSize = Size(diameter, diameter)

                var currentAngle = -90f
                distributions.forEach { item ->
                    val cat = Categories.getCategory(item.category)
                    val sweep = (item.percentage / 100f) * 360f
                    if (sweep > 0) {
                        drawArc(
                            color = cat.color,
                            startAngle = currentAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke)
                        )
                        currentAngle += sweep
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${distributions.sumOf { it.minutes } / 60}h",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total Time",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Legend list
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            distributions.take(5).forEach { item ->
                val cat = Categories.getCategory(item.category)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(cat.color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }

                    Text(
                        text = "${item.percentage}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PlannedVsActualChart(
    plannedHours: Float,
    actualHours: Float,
    modifier: Modifier = Modifier
) {
    val maxH = maxOf(plannedHours, actualHours, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("planned_vs_actual_chart")
    ) {
        // Planned Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Planned",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(70.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = (plannedHours / maxH).coerceIn(0.05f, 1f))
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format("%.1fh", plannedHours),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Actual Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Actual",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(70.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = (actualHours / maxH).coerceIn(0.05f, 1f))
                        .clip(RoundedCornerShape(9.dp))
                        .background(SoftGold)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format("%.1fh", actualHours),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = SoftGold,
                modifier = Modifier.width(42.dp)
            )
        }
    }
}
