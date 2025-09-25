package com.todoapp.modern.data.repository

import com.todoapp.modern.data.local.dao.TaskDao
import com.todoapp.modern.data.local.entities.Task
import com.todoapp.modern.data.local.entities.TaskCategory
import com.todoapp.modern.data.local.entities.TaskPriority
import com.todoapp.modern.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TaskRepository
 * Handles all data operations through Room database
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {
    
    override fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()
    
    override fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks()
    
    override fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()
    
    override fun getArchivedTasks(): Flow<List<Task>> = taskDao.getArchivedTasks()
    
    override suspend fun getTaskById(id: Long): Task? = taskDao.getTaskById(id)
    
    override suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)
    
    override suspend fun updateTask(task: Task) {
        val updatedTask = task.copy(
            updatedAt = LocalDateTime.now()
        )
        taskDao.updateTask(updatedTask)
    }
    
    override suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    
    override suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)
    
    override fun getTasksByCategory(category: TaskCategory): Flow<List<Task>> = 
        taskDao.getTasksByCategory(category)
    
    override fun getTasksByPriority(priority: TaskPriority): Flow<List<Task>> = 
        taskDao.getTasksByPriority(priority)
    
    override fun getTasksDueToday(): Flow<List<Task>> = taskDao.getTasksDueToday()
    
    override fun getOverdueTasks(): Flow<List<Task>> = taskDao.getOverdueTasks()
    
    override fun getTasksWithReminders(): Flow<List<Task>> = taskDao.getTasksWithReminders()
    
    override fun searchTasks(query: String): Flow<List<Task>> = taskDao.searchTasks(query)
    
    override suspend fun toggleTaskCompletion(id: Long) {
        val task = taskDao.getTaskById(id)
        task?.let {
            val updatedAt = LocalDateTime.now()
            taskDao.updateTaskCompletion(id, !it.isCompleted, updatedAt)
        }
    }
    
    override suspend fun archiveTask(id: Long) {
        val updatedAt = LocalDateTime.now()
        taskDao.updateTaskArchiveStatus(id, true, updatedAt)
    }
    
    override suspend fun unarchiveTask(id: Long) {
        val updatedAt = LocalDateTime.now()
        taskDao.updateTaskArchiveStatus(id, false, updatedAt)
    }
    
    override suspend fun archiveAllCompletedTasks() {
        val updatedAt = LocalDateTime.now()
        taskDao.archiveAllCompletedTasks(updatedAt)
    }
    
    override suspend fun deleteAllArchivedTasks() = taskDao.deleteAllArchivedTasks()
    
    override suspend fun getTotalTasksCount(): Int = taskDao.getTotalTasksCount()
    
    override suspend fun getCompletedTasksCount(): Int = taskDao.getCompletedTasksCount()
    
    override suspend fun getActiveTasksCount(): Int = taskDao.getActiveTasksCount()
    
    override suspend fun getOverdueTasksCount(): Int = taskDao.getOverdueTasksCount()
}
