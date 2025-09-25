package com.todoapp.modern.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.todoapp.modern.data.local.dao.TaskDao
import com.todoapp.modern.data.local.entities.Task
import com.todoapp.modern.data.local.database.Converters

/**
 * Room database for the Todo application
 * Handles all local data storage operations
 */
@Database(
    entities = [Task::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TodoDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    
    companion object {
        const val DATABASE_NAME = "todo_database"
        
        @Volatile
        private var INSTANCE: TodoDatabase? = null
        
        fun getDatabase(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
