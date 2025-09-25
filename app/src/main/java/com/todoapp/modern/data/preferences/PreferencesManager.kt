package com.todoapp.modern.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )
    
    private val _isDarkMode = MutableStateFlow(getDarkMode())
    val isDarkMode: Flow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _notificationsEnabled = MutableStateFlow(getNotificationsEnabled())
    val notificationsEnabled: Flow<Boolean> = _notificationsEnabled.asStateFlow()
    
    private val _dailySummaryEnabled = MutableStateFlow(getDailySummaryEnabled())
    val dailySummaryEnabled: Flow<Boolean> = _dailySummaryEnabled.asStateFlow()
    
    private val _autoArchiveEnabled = MutableStateFlow(getAutoArchiveEnabled())
    val autoArchiveEnabled: Flow<Boolean> = _autoArchiveEnabled.asStateFlow()
    
    private val _isFirstLaunch = MutableStateFlow(getIsFirstLaunch())
    val isFirstLaunch: Flow<Boolean> = _isFirstLaunch.asStateFlow()
    
    // Dark Mode
    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_DARK_MODE, enabled)
        }
        _isDarkMode.value = enabled
    }
    
    private fun getDarkMode(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false)
    }
    
    // Notifications
    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
        }
        _notificationsEnabled.value = enabled
    }
    
    private fun getNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
    
    // Daily Summary
    fun setDailySummaryEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_DAILY_SUMMARY_ENABLED, enabled)
        }
        _dailySummaryEnabled.value = enabled
    }
    
    private fun getDailySummaryEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_DAILY_SUMMARY_ENABLED, false)
    }
    
    fun setDailySummaryTime(hour: Int, minute: Int) {
        sharedPreferences.edit {
            putInt(KEY_DAILY_SUMMARY_HOUR, hour)
            putInt(KEY_DAILY_SUMMARY_MINUTE, minute)
        }
    }
    
    fun getDailySummaryTime(): Pair<Int, Int> {
        val hour = sharedPreferences.getInt(KEY_DAILY_SUMMARY_HOUR, 9)
        val minute = sharedPreferences.getInt(KEY_DAILY_SUMMARY_MINUTE, 0)
        return Pair(hour, minute)
    }
    
    // Auto Archive
    fun setAutoArchiveEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_AUTO_ARCHIVE_ENABLED, enabled)
        }
        _autoArchiveEnabled.value = enabled
    }
    
    private fun getAutoArchiveEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_AUTO_ARCHIVE_ENABLED, true)
    }
    
    // First Launch
    fun setFirstLaunchCompleted() {
        sharedPreferences.edit {
            putBoolean(KEY_FIRST_LAUNCH, false)
        }
        _isFirstLaunch.value = false
    }
    
    private fun getIsFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    // Widget Settings
    fun setWidgetEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_WIDGET_ENABLED, enabled)
        }
    }
    
    fun isWidgetEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_WIDGET_ENABLED, true)
    }
    
    // Last Widget Update
    fun setLastWidgetUpdate(timestamp: Long) {
        sharedPreferences.edit {
            putLong(KEY_LAST_WIDGET_UPDATE, timestamp)
        }
    }
    
    fun getLastWidgetUpdate(): Long {
        return sharedPreferences.getLong(KEY_LAST_WIDGET_UPDATE, 0L)
    }
    
    // Task Completion Sound
    fun setTaskCompletionSoundEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_TASK_COMPLETION_SOUND, enabled)
        }
    }
    
    fun isTaskCompletionSoundEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_TASK_COMPLETION_SOUND, true)
    }
    
    companion object {
        private const val PREFERENCES_NAME = "todo_app_preferences"
        
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_DAILY_SUMMARY_ENABLED = "daily_summary_enabled"
        private const val KEY_DAILY_SUMMARY_HOUR = "daily_summary_hour"
        private const val KEY_DAILY_SUMMARY_MINUTE = "daily_summary_minute"
        private const val KEY_AUTO_ARCHIVE_ENABLED = "auto_archive_enabled"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_WIDGET_ENABLED = "widget_enabled"
        private const val KEY_LAST_WIDGET_UPDATE = "last_widget_update"
        private const val KEY_TASK_COMPLETION_SOUND = "task_completion_sound"
    }
}
