package com.todoapp.modern.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
// Removed R import - using dynamic resource resolution
import com.todoapp.modern.data.local.entities.Task
import com.todoapp.modern.presentation.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskNotificationService @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val CHANNEL_ID = "task_reminders"
        private const val CHANNEL_NAME = "Task Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for task reminders and due dates"
        private const val DAILY_SUMMARY_CHANNEL_ID = "daily_summary"
        private const val DAILY_SUMMARY_CHANNEL_NAME = "Daily Summary"
        private const val DAILY_SUMMARY_CHANNEL_DESCRIPTION = "Daily task summary notifications"
        private const val OVERDUE_NOTIFICATION_ID = 1001
        private const val DAILY_SUMMARY_NOTIFICATION_ID = 1002
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Task reminders channel
            val reminderChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Daily summary channel
            val summaryChannel = NotificationChannel(
                DAILY_SUMMARY_CHANNEL_ID,
                DAILY_SUMMARY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = DAILY_SUMMARY_CHANNEL_DESCRIPTION
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(summaryChannel)
        }
    }
    
    fun showTaskReminder(task: Task) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("task_id", task.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationIcon = context.resources.getIdentifier("ic_notification", "drawable", context.packageName)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(if (notificationIcon != 0) notificationIcon else android.R.drawable.ic_dialog_info)
            .setContentTitle("Task Reminder")
            .setContentText(task.title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(buildString {
                        append(task.title)
                        if (task.description.isNotBlank()) {
                            append("\n\n${task.description}")
                        }
                        if (task.dueDateTime != null) {
                            append("\n\nDue: ${formatDateTime(task.dueDateTime!!)}")
                        }
                    })
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(task.id.toInt(), notification)
        } catch (e: SecurityException) {
            // Handle notification permission not granted
        }
    }
    
    fun showOverdueTaskNotification(tasks: List<Task>) {
        if (tasks.isEmpty()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationIcon = context.resources.getIdentifier("ic_notification", "drawable", context.packageName)
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("Overdue Tasks")
        
        tasks.take(5).forEach { task ->
            inboxStyle.addLine(task.title)
        }
        if (tasks.size > 5) {
            inboxStyle.addLine("and ${tasks.size - 5} more...")
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(if (notificationIcon != 0) notificationIcon else android.R.drawable.ic_dialog_info)
            .setContentTitle("Overdue Tasks")
            .setContentText("You have ${tasks.size} overdue task${if (tasks.size > 1) "s" else ""}")
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(OVERDUE_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle notification permission not granted
        }
    }
    
    fun showDailySummary(totalTasks: Int, completedTasks: Int, dueTodayTasks: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val summaryText = buildString {
            append("$completedTasks of $totalTasks tasks completed")
            if (dueTodayTasks > 0) {
                append(" • $dueTodayTasks due today")
            }
        }
        
        val notificationIcon = context.resources.getIdentifier("ic_notification", "drawable", context.packageName)
        val notification = NotificationCompat.Builder(context, DAILY_SUMMARY_CHANNEL_ID)
            .setSmallIcon(if (notificationIcon != 0) notificationIcon else android.R.drawable.ic_dialog_info)
            .setContentTitle("Daily Summary")
            .setContentText(summaryText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(DAILY_SUMMARY_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle notification permission not granted
        }
    }
    
    fun cancelTaskReminder(taskId: Long) {
        NotificationManagerCompat.from(context).cancel(taskId.toInt())
    }
    
    fun cancelOverdueNotification() {
        NotificationManagerCompat.from(context).cancel(OVERDUE_NOTIFICATION_ID)
    }
    
    private fun formatDateTime(dateTime: java.time.LocalDateTime): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a")
        return dateTime.format(formatter)
    }
    

}
