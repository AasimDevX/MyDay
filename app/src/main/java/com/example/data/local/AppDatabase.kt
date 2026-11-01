package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Database(
    entities = [
        TaskItem::class,
        HabitItem::class,
        HabitCompletion::class,
        DailyReview::class,
        UserSettings::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun dailyReviewDao(): DailyReviewDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myday_productivity.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial data asynchronously
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database)
                    }
                }
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            val taskDao = database.taskDao()
            val habitDao = database.habitDao()
            val settingsDao = database.settingsDao()

            // 1. Initial settings
            settingsDao.insertOrUpdateSettings(UserSettings())

            // 2. Starter habits
            val defaultHabits = listOf(
                HabitItem(
                    title = "Morning Meditation & Mindfulness",
                    description = "10 minutes of box breathing or calm focus",
                    category = "Health & Fitness",
                    iconName = "self_improvement",
                    colorHex = "#10B981",
                    frequencyType = "DAILY"
                ),
                HabitItem(
                    title = "Read 20 Pages",
                    description = "Non-fiction, architecture or leadership books",
                    category = "Learning",
                    iconName = "menu_book",
                    colorHex = "#EC4899",
                    frequencyType = "DAILY"
                ),
                HabitItem(
                    title = "Hydrate (2.5L Water)",
                    description = "Consistent hydration throughout the workday",
                    category = "Health & Fitness",
                    iconName = "water_drop",
                    colorHex = "#3B82F6",
                    frequencyType = "DAILY"
                ),
                HabitItem(
                    title = "Evening Routine & Wind Down",
                    description = "No blue light 45 minutes before sleep",
                    category = "Routine",
                    iconName = "bedtime",
                    colorHex = "#C9A227",
                    frequencyType = "DAILY"
                )
            )
            val habitIds = habitDao.insertHabits(defaultHabits)

            // Seed some habit completions for previous days to show realistic streaks
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            if (habitIds.isNotEmpty()) {
                val h1 = habitIds[0]
                val h2 = habitIds.getOrNull(1) ?: h1
                // complete for yesterday and day before
                habitDao.insertCompletion(HabitCompletion(habitId = h1, dateString = today.minusDays(2).format(formatter)))
                habitDao.insertCompletion(HabitCompletion(habitId = h1, dateString = today.minusDays(1).format(formatter)))
                habitDao.insertCompletion(HabitCompletion(habitId = h1, dateString = today.format(formatter)))
                habitDao.insertCompletion(HabitCompletion(habitId = h2, dateString = today.minusDays(1).format(formatter)))
                habitDao.insertCompletion(HabitCompletion(habitId = h2, dateString = today.format(formatter)))
            }

            // 3. Starter tasks for today
            val todayStr = today.format(formatter)
            val starterTasks = listOf(
                TaskItem(
                    title = "Daily Focus & Priority Planning",
                    description = "Review objectives, align top 3 priorities, and prep workspace",
                    category = "Routine",
                    priority = TaskPriority.HIGH,
                    scheduledDate = todayStr,
                    startTime = "08:00",
                    endTime = "08:30",
                    status = TaskStatus.COMPLETED,
                    actualStartTime = "08:05",
                    actualEndTime = "08:35",
                    plannedDurationMinutes = 30,
                    actualDurationMinutes = 30,
                    completionPercentage = 100,
                    personalNotes = "Sharp focus this morning, clear deliverables outlined.",
                    isRecurring = true,
                    recurrenceType = RecurrenceType.WEEKDAYS
                ),
                TaskItem(
                    title = "System Architecture Deep Work",
                    description = "Core module refactoring, interface decoupling and test suite",
                    category = "Deep Work",
                    priority = TaskPriority.HIGH,
                    scheduledDate = todayStr,
                    startTime = "09:00",
                    endTime = "11:00",
                    status = TaskStatus.COMPLETED,
                    actualStartTime = "09:00",
                    actualEndTime = "11:15",
                    plannedDurationMinutes = 120,
                    actualDurationMinutes = 135,
                    completionPercentage = 100,
                    personalNotes = "Completed decoupled data contracts and Room persistence integration.",
                    isRecurring = false
                ),
                TaskItem(
                    title = "Cross-Team Alignment Sync",
                    description = "Sprint goals review and design QA handoff",
                    category = "Meetings",
                    priority = TaskPriority.MEDIUM,
                    scheduledDate = todayStr,
                    startTime = "11:30",
                    endTime = "12:15",
                    status = TaskStatus.COMPLETED,
                    actualStartTime = "11:30",
                    actualEndTime = "12:15",
                    plannedDurationMinutes = 45,
                    actualDurationMinutes = 45,
                    completionPercentage = 100,
                    personalNotes = "Agreed on API spec changes.",
                    isRecurring = false
                ),
                TaskItem(
                    title = "Nutritious Lunch & Quick Walk",
                    description = "30-min walk outdoors for mental reset and energy replenishment",
                    category = "Health & Fitness",
                    priority = TaskPriority.LOW,
                    scheduledDate = todayStr,
                    startTime = "12:30",
                    endTime = "13:30",
                    status = TaskStatus.COMPLETED,
                    actualStartTime = "12:35",
                    actualEndTime = "13:25",
                    plannedDurationMinutes = 60,
                    actualDurationMinutes = 50,
                    completionPercentage = 100,
                    personalNotes = "Refreshed and ready for afternoon blocks.",
                    isRecurring = true,
                    recurrenceType = RecurrenceType.DAILY
                ),
                TaskItem(
                    title = "Productivity Metrics & Analytics Feature",
                    description = "Implement interactive performance charts and trends visualization",
                    category = "Deep Work",
                    priority = TaskPriority.HIGH,
                    scheduledDate = todayStr,
                    startTime = "14:00",
                    endTime = "16:00",
                    status = TaskStatus.IN_PROGRESS,
                    actualStartTime = "14:00",
                    actualEndTime = null,
                    plannedDurationMinutes = 120,
                    actualDurationMinutes = 55,
                    completionPercentage = 50,
                    personalNotes = "Canvas bar and donut components integrated smoothly.",
                    isRecurring = false
                ),
                TaskItem(
                    title = "Daily Review & Reflection",
                    description = "Evaluate planned vs actual hours, log key wins, and schedule tomorrow",
                    category = "Routine",
                    priority = TaskPriority.MEDIUM,
                    scheduledDate = todayStr,
                    startTime = "17:30",
                    endTime = "18:00",
                    status = TaskStatus.NOT_STARTED,
                    actualStartTime = null,
                    actualEndTime = null,
                    plannedDurationMinutes = 30,
                    actualDurationMinutes = 0,
                    completionPercentage = 0,
                    personalNotes = "",
                    isRecurring = true,
                    recurrenceType = RecurrenceType.WEEKDAYS
                )
            )
            taskDao.insertTasks(starterTasks)
        }
    }
}
