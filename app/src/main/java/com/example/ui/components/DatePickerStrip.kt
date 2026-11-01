package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.SoftGold
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DatePickerStrip(
    selectedDateStr: String,
    todayDateStr: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val selectedDate = try {
        LocalDate.parse(selectedDateStr, dateFormatter)
    } catch (e: Exception) {
        LocalDate.now()
    }

    // Generate 15 days window (e.g. 5 days back, 9 days forward)
    val today = LocalDate.now()
    val dates = remember {
        (-5..9).map { offset -> today.plusDays(offset.toLong()) }
    }

    val listState = rememberLazyListState()

    // Scroll near the selected date
    LaunchedEffect(selectedDateStr) {
        val index = dates.indexOfFirst { it.format(dateFormatter) == selectedDateStr }
        if (index >= 0) {
            val target = (index - 2).coerceAtLeast(0)
            listState.animateScrollToItem(target)
        }
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(dates) { date ->
            val dateStr = date.format(dateFormatter)
            val isSelected = dateStr == selectedDateStr
            val isToday = dateStr == todayDateStr
            val dayOfWeekLetter = date.dayOfWeek.name.take(3)
            val dayNumber = date.dayOfMonth.toString()

            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(68.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isToday -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.surface
                        }
                    )
                    .border(
                        width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                        color = if (isToday && !isSelected) SoftGold else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onDateSelected(dateStr) }
                    .testTag("date_chip_$dateStr"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dayOfWeekLetter,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) SoftGold else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayNumber,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    if (isToday) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) SoftGold else MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}
