package com.todoapp.modern.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todoapp.modern.data.preferences.PreferencesManager
import com.todoapp.modern.domain.use_case.TaskUseCases
import com.todoapp.modern.worker.WorkManagerScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val taskUseCases: TaskUseCases,
    private val preferencesManager: PreferencesManager,
    private val workManagerScheduler: WorkManagerScheduler
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = combine(
        _uiState,
        preferencesManager.isDarkMode,
        preferencesManager.notificationsEnabled,
        preferencesManager.dailySummaryEnabled,
        preferencesManager.autoArchiveEnabled
    ) { state, isDarkMode, notificationsEnabled, dailySummaryEnabled, autoArchiveEnabled ->
        state.copy(
            isDarkMode = isDarkMode,
            notificationsEnabled = notificationsEnabled,
            dailySummaryEnabled = dailySummaryEnabled,
            autoArchiveEnabled = autoArchiveEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )
    
    init {
        loadStatistics()
        loadSettings()
    }
    
    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ToggleDarkMode -> {
                preferencesManager.setDarkMode(event.enabled)
            }
            is SettingsEvent.ToggleNotifications -> {
                preferencesManager.setNotificationsEnabled(event.enabled)
                if (event.enabled) {
                    workManagerScheduler.scheduleOverdueCheck()
                } else {
                    workManagerScheduler.cancelAllWork()
                }
            }
            is SettingsEvent.ToggleDailySummary -> {
                preferencesManager.setDailySummaryEnabled(event.enabled)
                if (event.enabled) {
                    val (hour, minute) = preferencesManager.getDailySummaryTime()
                    workManagerScheduler.scheduleDailySummary(hour, minute)
                } else {
                    workManagerScheduler.cancelDailySummary()
                }
            }
            is SettingsEvent.ToggleAutoArchive -> {
                preferencesManager.setAutoArchiveEnabled(event.enabled)
                if (event.enabled) {
                    workManagerScheduler.scheduleAutoArchive()
                } else {
                    workManagerScheduler.cancelAutoArchive()
                }
            }
            SettingsEvent.ClearArchivedTasks -> {
                clearArchivedTasks()
            }
        }
    }
    
    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                val statistics = taskUseCases.getTaskStatistics()
                _uiState.value = _uiState.value.copy(
                    totalTasks = statistics.totalTasks,
                    completedTasks = statistics.completedTasks
                )
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
    }
    
    private fun loadSettings() {
        val (hour, minute) = preferencesManager.getDailySummaryTime()
        val timeString = String.format("%d:%02d %s", 
            if (hour == 0) 12 else if (hour > 12) hour - 12 else hour,
            minute,
            if (hour < 12) "AM" else "PM"
        )
        
        _uiState.value = _uiState.value.copy(
            dailySummaryTime = timeString
        )
    }
    
    private fun clearArchivedTasks() {
        viewModelScope.launch {
            try {
                taskUseCases.archiveTask.deleteAllArchived()
                loadStatistics() // Refresh statistics
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to clear archived tasks: ${e.message}"
                )
            }
        }
    }
}

/**
 * UI state for the Settings screen
 */
data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val dailySummaryEnabled: Boolean = false,
    val autoArchiveEnabled: Boolean = true,
    val dailySummaryTime: String = "9:00 AM",
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Events that can be triggered from the Settings screen
 */
sealed class SettingsEvent {
    data class ToggleDarkMode(val enabled: Boolean) : SettingsEvent()
    data class ToggleNotifications(val enabled: Boolean) : SettingsEvent()
    data class ToggleDailySummary(val enabled: Boolean) : SettingsEvent()
    data class ToggleAutoArchive(val enabled: Boolean) : SettingsEvent()
    object ClearArchivedTasks : SettingsEvent()
}
