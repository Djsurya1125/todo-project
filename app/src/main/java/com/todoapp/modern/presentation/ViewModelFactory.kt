package com.todoapp.modern.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.todoapp.modern.di.AppContainer
import com.todoapp.modern.presentation.screens.add_edit_task.AddEditTaskViewModel
import com.todoapp.modern.presentation.screens.home.HomeViewModel
import com.todoapp.modern.presentation.screens.settings.SettingsViewModel

/**
 * Factory for creating ViewModels with manual dependency injection
 */
class ViewModelFactory(private val appContainer: AppContainer) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            HomeViewModel::class.java -> {
                HomeViewModel(appContainer.taskUseCases) as T
            }
            AddEditTaskViewModel::class.java -> {
                AddEditTaskViewModel(appContainer.taskUseCases, SavedStateHandle()) as T
            }
            SettingsViewModel::class.java -> {
                SettingsViewModel(
                    appContainer.taskUseCases,
                    appContainer.preferencesManager,
                    appContainer.workManagerScheduler
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
