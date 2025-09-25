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

@HiltWorker
class DailySummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskUseCases: TaskUseCases,
    private val notificationService: TaskNotificationService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Get task statistics
            val statistics = taskUseCases.getTaskStatistics()
            
            // Get tasks due today
            val dueTodayTasks = taskUseCases.getTasks(
                com.todoapp.modern.domain.use_case.TaskFilter.DUE_TODAY
            ).first()
            
            // Show daily summary notification
            notificationService.showDailySummary(
                totalTasks = statistics.totalTasks,
                completedTasks = statistics.completedTasks,
                dueTodayTasks = dueTodayTasks.size
            )
            
            // Update widgets
            TodoWidgetProvider.updateWidgets(applicationContext)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "daily_summary"
    }
}
