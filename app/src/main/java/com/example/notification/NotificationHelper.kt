package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_TASKS = "myday_tasks_channel"
        const val CHANNEL_REMINDERS = "myday_reminders_channel"
        const val CHANNEL_REVIEWS = "myday_reviews_channel"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val taskChannel = NotificationChannel(
                CHANNEL_TASKS,
                "Task Schedules & Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when tasks start or become overdue"
                enableVibration(true)
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Routine & Upcoming Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming tasks and daily routines"
            }

            val reviewChannel = NotificationChannel(
                CHANNEL_REVIEWS,
                "Daily Reviews & Summaries",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Evening reflections and morning summaries"
            }

            notificationManager.createNotificationChannels(
                listOf(taskChannel, reminderChannel, reviewChannel)
            )
        }
    }

    fun showTaskStartNotification(taskId: Long, title: String, timeSlot: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_TASKS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Task Starting: $title")
            .setContentText("Scheduled for $timeSlot. Ready to focus?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(taskId.toInt() + 1000, notification)
        } catch (e: SecurityException) {
            // Notification permission might not be granted yet
        }
    }

    fun showOverdueNotification(taskId: Long, title: String, scheduledTime: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_TASKS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Task Overdue: $title")
            .setContentText("Was scheduled for $scheduledTime. Tap to complete or reschedule.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(taskId.toInt() + 2000, notification)
        } catch (e: SecurityException) {
            // Ignored if permission not granted
        }
    }

    fun showDailyReviewReminder(completionRate: Int, completedCount: Int, totalCount: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REVIEWS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Daily Review Ready")
            .setContentText("You completed $completedCount of $totalCount tasks ($completionRate%). Log your reflections!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(9999, notification)
        } catch (e: SecurityException) {
            // Ignored
        }
    }
}
