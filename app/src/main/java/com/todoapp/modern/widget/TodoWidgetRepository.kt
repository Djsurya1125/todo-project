package com.todoapp.modern.widget

import com.todoapp.modern.data.local.entities.Task
import com.todoapp.modern.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoWidgetRepository @Inject constructor(
    private val taskRepository: TaskRepository
) {
    
    suspend fun getActiveTasks(): List<Task> {
        return try {
            taskRepository.getActiveTasks().first()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getTasksDueToday(): List<Task> {
        return try {
            taskRepository.getTasksDueToday().first()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun toggleTaskCompletion(taskId: Long) {
        try {
            taskRepository.toggleTaskCompletion(taskId)
        } catch (e: Exception) {
            // Handle error silently for widget
        }
    }
    
    suspend fun getTaskStatistics(): Triple<Int, Int, Int> {
        return try {
            val totalTasks = taskRepository.getTotalTasksCount()
            val completedTasks = taskRepository.getCompletedTasksCount()
            val activeTasks = taskRepository.getActiveTasksCount()
            Triple(totalTasks, completedTasks, activeTasks)
        } catch (e: Exception) {
            Triple(0, 0, 0)
        }
    }
}
