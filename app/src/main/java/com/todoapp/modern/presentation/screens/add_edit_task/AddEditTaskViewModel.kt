package com.todoapp.modern.presentation.screens.add_edit_task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todoapp.modern.data.local.entities.Task
import com.todoapp.modern.data.local.entities.TaskCategory
import com.todoapp.modern.data.local.entities.TaskPriority
import com.todoapp.modern.domain.use_case.TaskUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AddEditTaskViewModel(
    private val taskUseCases: TaskUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: -1L
    
    private val _uiState = MutableStateFlow(AddEditTaskUiState())
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()
    
    init {
        if (taskId != -1L) {
            loadTask(taskId)
        }
    }
    
    fun onEvent(event: AddEditTaskEvent) {
        when (event) {
            is AddEditTaskEvent.TitleChanged -> {
                _uiState.value = _uiState.value.copy(
                    title = event.title,
                    titleError = if (event.title.isBlank()) "Title cannot be empty" else null
                )
            }
            is AddEditTaskEvent.DescriptionChanged -> {
                _uiState.value = _uiState.value.copy(description = event.description)
            }
            is AddEditTaskEvent.CategoryChanged -> {
                _uiState.value = _uiState.value.copy(category = event.category)
            }
            is AddEditTaskEvent.PriorityChanged -> {
                _uiState.value = _uiState.value.copy(priority = event.priority)
            }
            is AddEditTaskEvent.DueDateChanged -> {
                _uiState.value = _uiState.value.copy(
                    dueDate = event.date,
                    // Clear time if date is cleared
                    dueTime = if (event.date == null) null else _uiState.value.dueTime,
                    // Disable reminder if no date
                    hasReminder = if (event.date == null) false else _uiState.value.hasReminder
                )
            }
            is AddEditTaskEvent.DueTimeChanged -> {
                _uiState.value = _uiState.value.copy(dueTime = event.time)
            }
            is AddEditTaskEvent.ReminderChanged -> {
                _uiState.value = _uiState.value.copy(hasReminder = event.hasReminder)
            }
            AddEditTaskEvent.SaveTask -> {
                saveTask()
            }
            AddEditTaskEvent.DeleteTask -> {
                deleteTask()
            }
        }
    }
    
    private fun loadTask(id: Long) {
        viewModelScope.launch {
            try {
                val task = taskUseCases.getTask(id)
                if (task != null) {
                    _uiState.value = _uiState.value.copy(
                        title = task.title,
                        description = task.description,
                        category = task.category,
                        priority = task.priority,
                        dueDate = task.dueDateTime?.toLocalDate(),
                        dueTime = task.dueDateTime?.toLocalTime(),
                        hasReminder = task.hasReminder,
                        isEditMode = true,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Task not found",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to load task",
                    isLoading = false
                )
            }
        }
    }
    
    private fun saveTask() {
        val currentState = _uiState.value
        
        // Validate input
        if (currentState.title.isBlank()) {
            _uiState.value = currentState.copy(titleError = "Title cannot be empty")
            return
        }
        
        viewModelScope.launch {
            try {
                val dueDateTime = if (currentState.dueDate != null) {
                    val time = currentState.dueTime ?: LocalTime.of(23, 59)
                    LocalDateTime.of(currentState.dueDate, time)
                } else null
                
                val task = if (currentState.isEditMode) {
                    // Update existing task
                    val existingTask = taskUseCases.getTask(taskId)
                    existingTask?.copy(
                        title = currentState.title.trim(),
                        description = currentState.description.trim(),
                        category = currentState.category,
                        priority = currentState.priority,
                        dueDateTime = dueDateTime,
                        hasReminder = currentState.hasReminder,
                        updatedAt = LocalDateTime.now()
                    )
                } else {
                    // Create new task
                    Task(
                        title = currentState.title.trim(),
                        description = currentState.description.trim(),
                        category = currentState.category,
                        priority = currentState.priority,
                        dueDateTime = dueDateTime,
                        hasReminder = currentState.hasReminder,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                }
                
                if (task != null) {
                    if (currentState.isEditMode) {
                        taskUseCases.updateTask(task)
                    } else {
                        taskUseCases.addTask(task)
                    }
                    
                    _uiState.value = currentState.copy(isTaskSaved = true)
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    errorMessage = e.message ?: "Failed to save task"
                )
            }
        }
    }
    
    private fun deleteTask() {
        if (taskId != -1L) {
            viewModelScope.launch {
                try {
                    taskUseCases.deleteTask(taskId)
                    _uiState.value = _uiState.value.copy(isTaskSaved = true)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = e.message ?: "Failed to delete task"
                    )
                }
            }
        }
    }
}

/**
 * UI state for the Add/Edit Task screen
 */
data class AddEditTaskUiState(
    val title: String = "",
    val description: String = "",
    val category: TaskCategory = TaskCategory.PERSONAL,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val hasReminder: Boolean = false,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isTaskSaved: Boolean = false,
    val titleError: String? = null,
    val errorMessage: String? = null
)

/**
 * Events that can be triggered from the Add/Edit Task screen
 */
sealed class AddEditTaskEvent {
    data class TitleChanged(val title: String) : AddEditTaskEvent()
    data class DescriptionChanged(val description: String) : AddEditTaskEvent()
    data class CategoryChanged(val category: TaskCategory) : AddEditTaskEvent()
    data class PriorityChanged(val priority: TaskPriority) : AddEditTaskEvent()
    data class DueDateChanged(val date: LocalDate?) : AddEditTaskEvent()
    data class DueTimeChanged(val time: LocalTime?) : AddEditTaskEvent()
    data class ReminderChanged(val hasReminder: Boolean) : AddEditTaskEvent()
    object SaveTask : AddEditTaskEvent()
    object DeleteTask : AddEditTaskEvent()
}
