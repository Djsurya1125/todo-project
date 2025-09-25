package com.todoapp.modern.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
// Removed R import - using string-based resource references
import com.todoapp.modern.presentation.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.todoapp.modern.TodoApplication

class TodoWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each widget instance
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_TASK_TOGGLE -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
                if (taskId != -1L) {
                    val app = context.applicationContext as TodoApplication
                    CoroutineScope(Dispatchers.IO).launch {
                        app.appContainer.widgetRepository.toggleTaskCompletion(taskId)
                        updateAllWidgets(context)
                    }
                }
            }
            ACTION_ADD_TASK -> {
                // Open app to add new task
                val appIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigate_to", "add_task")
                }
                context.startActivity(appIntent)
            }
            ACTION_REFRESH -> {
                updateAllWidgets(context)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as TodoApplication
                val tasks = app.appContainer.widgetRepository.getActiveTasks()
                val layoutId = context.resources.getIdentifier("widget_todo_list", "layout", context.packageName)
                if (layoutId == 0) {
                    // Fallback if resource not found
                    return@launch
                }
                val views = RemoteViews(context.packageName, layoutId)
                
                // Set up the widget header
                val titleId = context.resources.getIdentifier("widget_title", "id", context.packageName)
                val countId = context.resources.getIdentifier("widget_task_count", "id", context.packageName)
                val containerId = context.resources.getIdentifier("widget_task_container", "id", context.packageName)
                
                if (titleId != 0) views.setTextViewText(titleId, "Today's Tasks")
                if (countId != 0) views.setTextViewText(countId, "${tasks.size} tasks")
                
                // Clear existing task views
                views.removeAllViews(containerId)
                
                // Add task items (limit to 5 for widget space)
                val displayTasks = tasks.take(5)
                for (task in displayTasks) {
                    val taskView = RemoteViews(context.packageName, context.resources.getIdentifier("widget_task_item", "layout", context.packageName))
                    
                    val taskTitleId = context.resources.getIdentifier("widget_task_title", "id", context.packageName)
                    val taskCategoryId = context.resources.getIdentifier("widget_task_category", "id", context.packageName)
                    val priorityIndicatorId = context.resources.getIdentifier("widget_priority_indicator", "id", context.packageName)
                    
                    taskView.setTextViewText(taskTitleId, task.title)
                    taskView.setTextViewText(taskCategoryId, task.category.displayName)
                    
                    // Set priority indicator color
                    val priorityColorName = when (task.priority) {
                        com.todoapp.modern.data.local.entities.TaskPriority.LOW -> "priority_low"
                        com.todoapp.modern.data.local.entities.TaskPriority.MEDIUM -> "priority_medium"
                        com.todoapp.modern.data.local.entities.TaskPriority.HIGH -> "priority_high"
                        com.todoapp.modern.data.local.entities.TaskPriority.URGENT -> "priority_urgent"
                    }
                    val priorityColor = context.resources.getIdentifier(priorityColorName, "color", context.packageName)
                    taskView.setInt(priorityIndicatorId, "setBackgroundResource", priorityColor)
                    
                    // Set up toggle task completion intent
                    val toggleIntent = Intent(context, TodoWidgetProvider::class.java).apply {
                        action = ACTION_TASK_TOGGLE
                        putExtra(EXTRA_TASK_ID, task.id)
                    }
                    val togglePendingIntent = PendingIntent.getBroadcast(
                        context, 
                        task.id.toInt(), 
                        toggleIntent, 
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val checkboxId = context.resources.getIdentifier("widget_task_checkbox", "id", context.packageName)
                    taskView.setOnClickPendingIntent(checkboxId, togglePendingIntent)
                    
                    views.addView(containerId, taskView)
                }
                
                // Show "No tasks" message if empty
                if (tasks.isEmpty()) {
                    val emptyView = RemoteViews(context.packageName, context.resources.getIdentifier("widget_empty_state", "layout", context.packageName))
                    views.addView(containerId, emptyView)
                }
                
                // Set up add task button intent
                val addTaskIntent = Intent(context, TodoWidgetProvider::class.java).apply {
                    action = ACTION_ADD_TASK
                }
                val addTaskPendingIntent = PendingIntent.getBroadcast(
                    context, 
                    0, 
                    addTaskIntent, 
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val addButtonId = context.resources.getIdentifier("widget_add_button", "id", context.packageName)
                views.setOnClickPendingIntent(addButtonId, addTaskPendingIntent)
                
                // Set up refresh button intent
                val refreshIntent = Intent(context, TodoWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context, 
                    1, 
                    refreshIntent, 
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val refreshButtonId = context.resources.getIdentifier("widget_refresh_button", "id", context.packageName)
                views.setOnClickPendingIntent(refreshButtonId, refreshPendingIntent)
                
                // Set up main app launch intent
                val appIntent = Intent(context, MainActivity::class.java)
                val appPendingIntent = PendingIntent.getActivity(
                    context, 
                    0, 
                    appIntent, 
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val headerId = context.resources.getIdentifier("widget_header", "id", context.packageName)
                views.setOnClickPendingIntent(headerId, appPendingIntent)
                
                // Update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
                
            } catch (e: Exception) {
                // Handle error - show error state
                val errorViews = RemoteViews(context.packageName, context.resources.getIdentifier("widget_error_state", "layout", context.packageName))
                appWidgetManager.updateAppWidget(appWidgetId, errorViews)
            }
        }
    }
    
    private fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            android.content.ComponentName(context, TodoWidgetProvider::class.java)
        )
        onUpdate(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        const val ACTION_TASK_TOGGLE = "com.todoapp.modern.widget.TASK_TOGGLE"
        const val ACTION_ADD_TASK = "com.todoapp.modern.widget.ADD_TASK"
        const val ACTION_REFRESH = "com.todoapp.modern.widget.REFRESH"
        const val EXTRA_TASK_ID = "task_id"
        
        fun updateWidgets(context: Context) {
            val intent = Intent(context, TodoWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
}
