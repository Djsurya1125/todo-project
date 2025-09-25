package com.todoapp.modern.data.local.entities

/**
 * Enum representing different categories of tasks
 */
enum class TaskCategory(
    val displayName: String,
    val icon: String
) {
    PERSONAL("Personal", "person"),
    WORK("Work", "work"),
    SHOPPING("Shopping", "shopping_cart"),
    HEALTH("Health", "favorite"),
    LEARNING("Learning", "school"),
    EDUCATION("Education", "school"),
    FINANCE("Finance", "attach_money"),
    TRAVEL("Travel", "flight"),
    HOME("Home", "home"),
    OTHER("Other", "category");

    companion object {
        fun fromDisplayName(displayName: String): TaskCategory {
            return values().find { it.displayName == displayName } ?: OTHER
        }
    }
}
