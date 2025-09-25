package com.todoapp.modern.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todoapp.modern.data.local.entities.Task
import com.todoapp.modern.domain.use_case.TaskUseCases
import com.todoapp.modern.domain.use_case.TaskFilter
import com.todoapp.modern.domain.use_case.TaskStatistics
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
// Removed Hilt imports - using manual DI

/**
 * ViewModel for the Home screen
 * Manages task list state and operations
 */
class HomeViewModel(
    private val taskUseCases: TaskUseCases
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow(TaskFilter.ACTIVE)
    val selectedFilter: StateFlow<TaskFilter> = _selectedFilter.asStateFlow()
    
    // Combine search and filter to get filtered tasks
    val tasks: StateFlow<List<Task>> = combine(
        _searchQuery,
        _selectedFilter
    ) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        if (query.isBlank()) {
            taskUseCases.getTasks(filter)
        } else {
            taskUseCases.searchTasks(query)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        loadTaskStatistics()
    }
    
    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.ToggleTaskCompletion -> {
                viewModelScope.launch {
                    taskUseCases.toggleTaskCompletion(event.taskId)
                    loadTaskStatistics()
                }
            }
            is HomeEvent.DeleteTask -> {
                viewModelScope.launch {
                    taskUseCases.deleteTask(event.taskId)
                    loadTaskStatistics()
                }
            }
            is HomeEvent.ArchiveTask -> {
                viewModelScope.launch {
                    taskUseCases.archiveTask(event.taskId, true)
                    loadTaskStatistics()
                }
            }
            is HomeEvent.SearchTasks -> {
                _searchQuery.value = event.query
            }
            is HomeEvent.FilterTasks -> {
                _selectedFilter.value = event.filter
            }
            is HomeEvent.ClearSearch -> {
                _searchQuery.value = ""
            }
            is HomeEvent.ArchiveAllCompleted -> {
                viewModelScope.launch {
                    taskUseCases.archiveTask.archiveAllCompleted()
                    loadTaskStatistics()
                }
            }
            is HomeEvent.ShowError -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = event.message
                )
            }
            is HomeEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = null
                )
            }
            is HomeEvent.RefreshTasks -> {
                loadTaskStatistics()
            }
        }
    }
    
    private fun loadTaskStatistics() {
        viewModelScope.launch {
            try {
                val statistics = taskUseCases.getTaskStatistics()
                _uiState.value = _uiState.value.copy(
                    statistics = statistics,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Unknown error occurred",
                    isLoading = false
                )
            }
        }
    }
}

/**
 * UI state for the Home screen
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val statistics: TaskStatistics = TaskStatistics(0, 0, 0, 0),
    val errorMessage: String? = null
)

/**
 * Events that can be triggered from the Home screen
 */
sealed class HomeEvent {
    data class ToggleTaskCompletion(val taskId: Long) : HomeEvent()
    data class DeleteTask(val taskId: Long) : HomeEvent()
    data class ArchiveTask(val taskId: Long) : HomeEvent()
    data class SearchTasks(val query: String) : HomeEvent()
    data class FilterTasks(val filter: TaskFilter) : HomeEvent()
    object ClearSearch : HomeEvent()
    object ArchiveAllCompleted : HomeEvent()
    data class ShowError(val message: String) : HomeEvent()
    object ClearError : HomeEvent()
    object RefreshTasks : HomeEvent()
}
