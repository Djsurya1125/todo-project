package com.todoapp.modern.data.local.entities

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Task entity for Room database
 * Represents a single todo task with all its properties
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: TaskCategory = TaskCategory.PERSONAL,
    val dueDateTime: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isArchived: Boolean = false,
    val hasReminder: Boolean = false,
    val reminderDateTime: LocalDateTime? = null
) {
    fun isOverdue(): Boolean {
        return dueDateTime != null && dueDateTime.isBefore(LocalDateTime.now()) && !isCompleted
    }
    
    fun isDueToday(): Boolean {
        val today = LocalDateTime.now().toLocalDate()
        return dueDateTime?.toLocalDate() == today
    }
}


