package com.todoapp.modern.data.local.entities

import androidx.compose.ui.graphics.Color

/**
 * Enum representing different priority levels for tasks
 */
enum class TaskPriority(
    val displayName: String,
    val value: Int,
    val colorValue: Long
) {
    LOW("Low", 1, 0xFF4CAF50),      // Green
    MEDIUM("Medium", 2, 0xFFFF9800), // Orange
    HIGH("High", 3, 0xFFF44336),     // Red
    URGENT("Urgent", 4, 0xFF9C27B0); // Purple

    val color: Color
        get() = Color(colorValue)

    companion object {
        fun fromValue(value: Int): TaskPriority {
            return values().find { it.value == value } ?: MEDIUM
        }

        fun fromDisplayName(displayName: String): TaskPriority {
            return values().find { it.displayName == displayName } ?: MEDIUM
        }
    }
}
