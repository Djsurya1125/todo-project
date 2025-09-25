package com.todoapp.modern.data.local.database

import androidx.room.TypeConverter
import com.todoapp.modern.data.local.entities.TaskCategory
import com.todoapp.modern.data.local.entities.TaskPriority
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Type converters for Room database
 * Handles conversion of enums and other complex types
 */
class Converters {
    
    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String {
        return priority.name
    }
    
    @TypeConverter
    fun toTaskPriority(priority: String): TaskPriority {
        return TaskPriority.valueOf(priority)
    }
    
    @TypeConverter
    fun fromTaskCategory(category: TaskCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toTaskCategory(category: String): TaskCategory {
        return TaskCategory.valueOf(category)
    }
    
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }
}
