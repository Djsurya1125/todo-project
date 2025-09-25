package com.todoapp.modern.data.local.dao

import androidx.room.*
import com.todoapp.modern.data.local.entities.Task
import com.todoapp.modern.data.local.entities.TaskCategory
import com.todoapp.modern.data.local.entities.TaskPriority
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for Task operations
 * Provides all database operations for tasks
 */
@Dao
interface TaskDao {
    
    // Basic CRUD operations
    @Query("SELECT * FROM tasks WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isArchived = 0 AND isCompleted = 0 ORDER BY priority DESC, dueDateTime ASC")
    fun getActiveTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isArchived = 0 AND isCompleted = 1 ORDER BY updatedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long
    
    @Update
    suspend fun updateTask(task: Task)
    
    @Delete
    suspend fun deleteTask(task: Task)
    
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
    
    // Filtered queries
    @Query("SELECT * FROM tasks WHERE isArchived = 0 AND category = :category ORDER BY priority DESC, dueDateTime ASC")
    fun getTasksByCategory(category: TaskCategory): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isArchived = 0 AND priority = :priority ORDER BY dueDateTime ASC")
    fun getTasksByPriority(priority: TaskPriority): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isArchived = 0 AND dueDateTime IS NOT NULL AND date(dueDateTime) = date('now', 'localtime') ORDER BY priority DESC")
    fun getTasksDueToday(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isArchived = 0 AND dueDateTime IS NOT NULL AND date(dueDateTime) < date('now', 'localtime') AND isCompleted = 0 ORDER BY dueDateTime ASC")
    fun getOverdueTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isArchived = 0 AND hasReminder = 1 AND reminderDateTime IS NOT NULL ORDER BY reminderDateTime ASC")
    fun getTasksWithReminders(): Flow<List<Task>>
    
    // Search functionality
    @Query("""
        SELECT * FROM tasks 
        WHERE isArchived = 0 AND (
            title LIKE '%' || :query || '%' OR 
            description LIKE '%' || :query || '%'
        ) 
        ORDER BY 
            CASE WHEN title LIKE '%' || :query || '%' THEN 1 ELSE 2 END,
            priority DESC, 
            dueDateTime ASC
    """)
    fun searchTasks(query: String): Flow<List<Task>>
    
    // Bulk operations
    @Query("UPDATE tasks SET isCompleted = :completed, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTaskCompletion(id: Long, completed: Boolean, updatedAt: LocalDateTime)
    
    @Query("UPDATE tasks SET isArchived = :archived, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTaskArchiveStatus(id: Long, archived: Boolean, updatedAt: LocalDateTime)
    
    @Query("UPDATE tasks SET isArchived = 1, updatedAt = :updatedAt WHERE isCompleted = 1")
    suspend fun archiveAllCompletedTasks(updatedAt: LocalDateTime)
    
    @Query("DELETE FROM tasks WHERE isArchived = 1")
    suspend fun deleteAllArchivedTasks()
    
    // Statistics
    @Query("SELECT COUNT(*) FROM tasks WHERE isArchived = 0")
    suspend fun getTotalTasksCount(): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE isArchived = 0 AND isCompleted = 1")
    suspend fun getCompletedTasksCount(): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE isArchived = 0 AND isCompleted = 0")
    suspend fun getActiveTasksCount(): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE isArchived = 0 AND dueDateTime IS NOT NULL AND date(dueDateTime) < date('now', 'localtime') AND isCompleted = 0")
    suspend fun getOverdueTasksCount(): Int
}
