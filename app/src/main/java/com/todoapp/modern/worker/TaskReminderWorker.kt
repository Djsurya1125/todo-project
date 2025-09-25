package com.todoapp.modern.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.todoapp.modern.domain.use_case.TaskUseCases
import com.todoapp.modern.notification.TaskNotificationService
import com.todoapp.modern.widget.TodoWidgetProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskUseCases: TaskUseCases,
    private val notificationService: TaskNotificationService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Check for tasks with reminders due now
            val taskId = inputData.getLong(KEY_TASK_ID, -1L)
            
            if (taskId != -1L) {
                // Single task reminder
                val task = taskUseCases.getTask(taskId)
                if (task != null && !task.isCompleted && task.hasReminder) {
                    notificationService.showTaskReminder(task)
                }
            } else {
                // Check for overdue tasks
                val overdueTasks = taskUseCases.getTasks(
                    com.todoapp.modern.domain.use_case.TaskFilter.OVERDUE
                ).first()
                
                if (overdueTasks.isNotEmpty()) {
                    notificationService.showOverdueTaskNotification(overdueTasks)
                }
            }
            
            // Update widgets
            TodoWidgetProvider.updateWidgets(applicationContext)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val WORK_NAME_REMINDER = "task_reminder"
        const val WORK_NAME_OVERDUE_CHECK = "overdue_check"
    }
}
