package com.example

import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testTaskDurationFormatting() {
        val task = TaskItem(
            title = "Architecture Review",
            scheduledDate = "2026-10-31",
            startTime = "09:00",
            endTime = "10:30",
            plannedDurationMinutes = 90,
            actualDurationMinutes = 45
        )
        assertEquals("1h 30m", task.formatPlannedDuration())
        assertEquals("45m", task.formatActualDuration())
    }

    @Test
    fun testTaskTimeSlotCategorization() {
        val morningTask = TaskItem(
            title = "Morning Sync",
            scheduledDate = "2026-10-31",
            startTime = "08:30",
            endTime = "09:00"
        )
        val afternoonTask = TaskItem(
            title = "Deep Focus Work",
            scheduledDate = "2026-10-31",
            startTime = "14:00",
            endTime = "16:00"
        )
        val eveningTask = TaskItem(
            title = "Day Review",
            scheduledDate = "2026-10-31",
            startTime = "18:00",
            endTime = "18:30"
        )

        assertEquals("Morning", morningTask.getTimeSlot())
        assertEquals("Afternoon", afternoonTask.getTimeSlot())
        assertEquals("Evening", eveningTask.getTimeSlot())
    }
}
