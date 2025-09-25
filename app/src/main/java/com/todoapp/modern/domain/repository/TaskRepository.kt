package com.todoapp.modern.domain.repository

import com.todoapp.modern.data.local.entities.Task
import com.todoapp.modern.data.local.entities.TaskCategory
import com.todoapp.modern.data.local.entities.TaskPriority
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for task operations
 * Defines the contract for data operations
 */
interface TaskRepository {
    
    // Basic CRUD operations
    fun getAllTasks(): Flow<List<Task>>
    fun getActiveTasks(): Flow<List<Task>>
    fun getCompletedTasks(): Flow<List<Task>>
    fun getArchivedTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: Long): Task?
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun deleteTaskById(id: Long)
    
    // Filtered operations
    fun getTasksByCategory(category: TaskCategory): Flow<List<Task>>
    fun getTasksByPriority(priority: TaskPriority): Flow<List<Task>>
    fun getTasksDueToday(): Flow<List<Task>>
    fun getOverdueTasks(): Flow<List<Task>>
    fun getTasksWithReminders(): Flow<List<Task>>
    
    // Search
    fun searchTasks(query: String): Flow<List<Task>>
    
    // Bulk operations
    suspend fun toggleTaskCompletion(id: Long)
    suspend fun archiveTask(id: Long)
    suspend fun unarchiveTask(id: Long)
    suspend fun archiveAllCompletedTasks()
    suspend fun deleteAllArchivedTasks()
    
    // Statistics
    suspend fun getTotalTasksCount(): Int
    suspend fun getCompletedTasksCount(): Int
    suspend fun getActiveTasksCount(): Int
    suspend fun getOverdueTasksCount(): Int
}
