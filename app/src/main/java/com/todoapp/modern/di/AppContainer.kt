package com.todoapp.modern.di

import android.content.Context
import androidx.room.Room
import com.todoapp.modern.data.local.database.TodoDatabase
import com.todoapp.modern.data.preferences.PreferencesManager
import com.todoapp.modern.data.repository.TaskRepositoryImpl
import com.todoapp.modern.domain.repository.TaskRepository
import com.todoapp.modern.domain.use_case.*
import com.todoapp.modern.notification.TaskNotificationService
import com.todoapp.modern.widget.TodoWidgetRepository
import com.todoapp.modern.worker.WorkManagerScheduler

/**
 * Manual Dependency Injection Container
 * Provides all app dependencies without annotation processing
 */
class AppContainer(private val context: Context) {
    
    // Database
    val database: TodoDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            TodoDatabase::class.java,
            "todo_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    // Repository
    val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(database.taskDao())
    }
    
    // Use Cases
    val taskUseCases: TaskUseCases by lazy {
        TaskUseCases(
            getTasks = GetTasksUseCase(taskRepository),
            getTask = GetTaskUseCase(taskRepository),
            addTask = AddTaskUseCase(taskRepository),
            updateTask = UpdateTaskUseCase(taskRepository),
            deleteTask = DeleteTaskUseCase(taskRepository),
            toggleTaskCompletion = ToggleTaskCompletionUseCase(taskRepository),
            searchTasks = SearchTasksUseCase(taskRepository),
            getTasksByFilter = GetTasksByFilterUseCase(taskRepository),
            archiveTask = ArchiveTaskUseCase(taskRepository),
            getTaskStatistics = GetTaskStatisticsUseCase(taskRepository)
        )
    }
    
    // Preferences
    val preferencesManager: PreferencesManager by lazy {
        PreferencesManager(context)
    }
    
    // Notification Service
    val notificationService: TaskNotificationService by lazy {
        TaskNotificationService(context)
    }
    
    // Work Manager Scheduler
    val workManagerScheduler: WorkManagerScheduler by lazy {
        WorkManagerScheduler(context)
    }
    
    // Widget Repository
    val widgetRepository: TodoWidgetRepository by lazy {
        TodoWidgetRepository(taskRepository)
    }
}
