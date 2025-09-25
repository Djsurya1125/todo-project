package com.todoapp.modern.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.todoapp.modern.domain.use_case.TaskUseCases
import com.todoapp.modern.widget.TodoWidgetProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

@HiltWorker
class AutoArchiveWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskUseCases: TaskUseCases
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Get all completed tasks
            val completedTasks = taskUseCases.getTasks(
                com.todoapp.modern.domain.use_case.TaskFilter.COMPLETED
            ).first()
            
            val sevenDaysAgo = LocalDateTime.now().minusDays(7)
            
            // Archive tasks completed more than 7 days ago
            completedTasks.forEach { task ->
                if (task.isCompleted && task.updatedAt.isBefore(sevenDaysAgo)) {
                    taskUseCases.archiveTask(task.id, true)
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
        const val WORK_NAME = "auto_archive"
    }
}
