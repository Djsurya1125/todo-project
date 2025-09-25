package com.todoapp.modern.di

import android.content.Context
import com.todoapp.modern.notification.TaskNotificationService
import com.todoapp.modern.widget.TodoWidgetRepository
import com.todoapp.modern.worker.WorkManagerScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    
    @Provides
    @Singleton
    fun provideTaskNotificationService(
        @ApplicationContext context: Context
    ): TaskNotificationService {
        return TaskNotificationService(context)
    }
    
    @Provides
    @Singleton
    fun provideWorkManagerScheduler(
        @ApplicationContext context: Context
    ): WorkManagerScheduler {
        return WorkManagerScheduler(context)
    }
    
    @Provides
    @Singleton
    fun provideTodoWidgetRepository(
        taskRepository: com.todoapp.modern.domain.repository.TaskRepository
    ): TodoWidgetRepository {
        return TodoWidgetRepository(taskRepository)
    }
}
