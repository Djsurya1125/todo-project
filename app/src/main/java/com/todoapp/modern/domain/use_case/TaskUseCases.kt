package com.todoapp.modern.domain.use_case

import com.todoapp.modern.data.local.entities.Task
import com.todoapp.modern.data.local.entities.TaskCategory
import com.todoapp.modern.data.local.entities.TaskPriority
import com.todoapp.modern.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use cases for task operations
 * Encapsulates business logic and provides clean interface for ViewModels
 */
data class TaskUseCases @Inject constructor(
    val getTasks: GetTasksUseCase,
    val getTask: GetTaskUseCase,
    val addTask: AddTaskUseCase,
    val updateTask: UpdateTaskUseCase,
    val deleteTask: DeleteTaskUseCase,
    val toggleTaskCompletion: ToggleTaskCompletionUseCase,
    val searchTasks: SearchTasksUseCase,
    val getTasksByFilter: GetTasksByFilterUseCase,
    val archiveTask: ArchiveTaskUseCase,
    val getTaskStatistics: GetTaskStatisticsUseCase
)

class GetTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(filter: TaskFilter = TaskFilter.ALL): Flow<List<Task>> {
        return when (filter) {
            TaskFilter.ALL -> repository.getAllTasks()
            TaskFilter.ACTIVE -> repository.getActiveTasks()
            TaskFilter.COMPLETED -> repository.getCompletedTasks()
            TaskFilter.ARCHIVED -> repository.getArchivedTasks()
            TaskFilter.DUE_TODAY -> repository.getTasksDueToday()
            TaskFilter.OVERDUE -> repository.getOverdueTasks()
        }
    }
}

class GetTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(id: Long): Task? {
        return repository.getTaskById(id)
    }
}

class AddTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Long {
        return repository.insertTask(task)
    }
}

class UpdateTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        repository.updateTask(task)
    }
}

class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        repository.deleteTask(task)
    }
    
    suspend operator fun invoke(id: Long) {
        repository.deleteTaskById(id)
    }
}

class ToggleTaskCompletionUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.toggleTaskCompletion(id)
    }
}

class SearchTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(query: String): Flow<List<Task>> {
        return repository.searchTasks(query)
    }
}

class GetTasksByFilterUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(
        category: TaskCategory? = null,
        priority: TaskPriority? = null
    ): Flow<List<Task>> {
        return when {
            category != null -> repository.getTasksByCategory(category)
            priority != null -> repository.getTasksByPriority(priority)
            else -> repository.getAllTasks()
        }
    }
}

class ArchiveTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(id: Long, archive: Boolean = true) {
        if (archive) {
            repository.archiveTask(id)
        } else {
            repository.unarchiveTask(id)
        }
    }
    
    suspend fun archiveAllCompleted() {
        repository.archiveAllCompletedTasks()
    }
    
    suspend fun deleteAllArchived() {
        repository.deleteAllArchivedTasks()
    }
}

class GetTaskStatisticsUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(): TaskStatistics {
        return TaskStatistics(
            totalTasks = repository.getTotalTasksCount(),
            completedTasks = repository.getCompletedTasksCount(),
            activeTasks = repository.getActiveTasksCount(),
            overdueTasks = repository.getOverdueTasksCount()
        )
    }
}

/**
 * Task filter options
 */
enum class TaskFilter {
    ALL,
    ACTIVE,
    COMPLETED,
    ARCHIVED,
    DUE_TODAY,
    OVERDUE
}

/**
 * Task statistics data class
 */
data class TaskStatistics(
    val totalTasks: Int,
    val completedTasks: Int,
    val activeTasks: Int,
    val overdueTasks: Int
) {
    val completionRate: Float
        get() = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
}
