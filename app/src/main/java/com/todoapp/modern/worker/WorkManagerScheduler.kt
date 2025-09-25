package com.todoapp.modern.worker

import android.content.Context
import androidx.work.*
import com.todoapp.modern.data.local.entities.Task
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerScheduler @Inject constructor(
    private val context: Context
) {
    
    private val workManager = WorkManager.getInstance(context)
    
    fun scheduleTaskReminder(task: Task) {
        if (!task.hasReminder || task.reminderDateTime == null || task.isCompleted) {
            return
        }
        
        val now = LocalDateTime.now()
        val reminderTime = task.reminderDateTime!!
        
        if (reminderTime.isBefore(now)) {
            return // Don't schedule past reminders
        }
        
        val delayInMinutes = ChronoUnit.MINUTES.between(now, reminderTime)
        
        val inputData = Data.Builder()
            .putLong(TaskReminderWorker.KEY_TASK_ID, task.id)
            .build()
        
        val reminderWork = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delayInMinutes, TimeUnit.MINUTES)
            .setInputData(inputData)
            .addTag("reminder_${task.id}")
            .build()
        
        workManager.enqueueUniqueWork(
            "reminder_${task.id}",
            ExistingWorkPolicy.REPLACE,
            reminderWork
        )
    }
    
    fun cancelTaskReminder(taskId: Long) {
        workManager.cancelUniqueWork("reminder_$taskId")
    }
    
    fun scheduleOverdueCheck() {
        val overdueCheckWork = PeriodicWorkRequestBuilder<TaskReminderWorker>(
            repeatInterval = 6, // Check every 6 hours
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .addTag(TaskReminderWorker.WORK_NAME_OVERDUE_CHECK)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            TaskReminderWorker.WORK_NAME_OVERDUE_CHECK,
            ExistingPeriodicWorkPolicy.KEEP,
            overdueCheckWork
        )
    }
    
    fun scheduleDailySummary(hour: Int = 9, minute: Int = 0) {
        // Calculate delay until next occurrence of the specified time
        val now = LocalDateTime.now()
        var nextSummaryTime = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        
        if (nextSummaryTime.isBefore(now) || nextSummaryTime.isEqual(now)) {
            nextSummaryTime = nextSummaryTime.plusDays(1)
        }
        
        val delayInMinutes = ChronoUnit.MINUTES.between(now, nextSummaryTime)
        
        val dailySummaryWork = PeriodicWorkRequestBuilder<DailySummaryWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(delayInMinutes, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .addTag(DailySummaryWorker.WORK_NAME)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            DailySummaryWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            dailySummaryWork
        )
    }
    
    fun cancelDailySummary() {
        workManager.cancelUniqueWork(DailySummaryWorker.WORK_NAME)
    }
    
    fun scheduleAutoArchive() {
        val autoArchiveWork = PeriodicWorkRequestBuilder<AutoArchiveWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .addTag(AutoArchiveWorker.WORK_NAME)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            AutoArchiveWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            autoArchiveWork
        )
    }
    
    fun cancelAutoArchive() {
        workManager.cancelUniqueWork(AutoArchiveWorker.WORK_NAME)
    }
    
    fun cancelAllWork() {
        workManager.cancelAllWork()
    }
}
