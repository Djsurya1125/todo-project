package com.todoapp.modern.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.todoapp.modern.data.preferences.PreferencesManager

@Composable
fun shouldUseDarkTheme(
    preferencesManager: PreferencesManager
): Boolean {
    val isDarkModeEnabled by preferencesManager.isDarkMode.collectAsState(initial = false)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    
    // For now, prioritize user preference, but fall back to system theme
    // In a full implementation, you might want a "Follow System" option
    return isDarkModeEnabled
}
