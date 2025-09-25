package com.todoapp.modern.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Removed hiltViewModel import - using manual DI
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todoapp.modern.data.local.entities.Task
import com.todoapp.modern.domain.use_case.TaskFilter
import com.todoapp.modern.presentation.components.*

/**
 * Home screen displaying the main task list
 * Features search, filtering, and task management
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(HomeEvent.ClearError)
        }
    }
    
    Scaffold(
        topBar = {
            HomeTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.onEvent(HomeEvent.SearchTasks(it)) },
                onClearSearch = { viewModel.onEvent(HomeEvent.ClearSearch) },
                onNavigateToSettings = onNavigateToSettings,
                statistics = uiState.statistics
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddTask,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Task") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips
            FilterChipRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { viewModel.onEvent(HomeEvent.FilterTasks(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Task list
            AnimatedContent(
                targetState = tasks.isEmpty(),
                transitionSpec = {
                    fadeIn() with fadeOut()
                },
                label = "task_list_animation"
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyTasksContent(
                        filter = selectedFilter,
                        searchQuery = searchQuery,
                        onAddTask = onNavigateToAddTask,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = tasks,
                            key = { it.id }
                        ) { task ->
                            TaskItem(
                                task = task,
                                onTaskClick = { onNavigateToEditTask(task.id) },
                                onToggleComplete = { 
                                    viewModel.onEvent(HomeEvent.ToggleTaskCompletion(task.id))
                                },
                                onDeleteTask = {
                                    viewModel.onEvent(HomeEvent.DeleteTask(task.id))
                                },
                                onArchiveTask = {
                                    viewModel.onEvent(HomeEvent.ArchiveTask(task.id))
                                },
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .fillMaxWidth()
                            )
                        }
                        
                        // Add some bottom padding for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipRow(
    selectedFilter: TaskFilter,
    onFilterSelected: (TaskFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(TaskFilter.values()) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(getFilterDisplayName(filter)) },
                leadingIcon = if (selectedFilter == filter) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun EmptyTasksContent(
    filter: TaskFilter,
    searchQuery: String,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val (icon, title, subtitle) = when {
            searchQuery.isNotBlank() -> Triple(
                Icons.Default.Search,
                "No tasks found",
                "Try adjusting your search terms"
            )
            filter == TaskFilter.COMPLETED -> Triple(
                Icons.Default.CheckCircle,
                "No completed tasks",
                "Complete some tasks to see them here"
            )
            filter == TaskFilter.ARCHIVED -> Triple(
                Icons.Outlined.Archive,
                "No archived tasks",
                "Archive completed tasks to organize them"
            )
            else -> Triple(
                Icons.Default.List,
                "No tasks yet",
                "Add your first task to get started"
            )
        }
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        if (filter == TaskFilter.ACTIVE || filter == TaskFilter.ALL) {
            Button(
                onClick = onAddTask,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Task")
            }
        }
    }
}

private fun getFilterDisplayName(filter: TaskFilter): String {
    return when (filter) {
        TaskFilter.ALL -> "All"
        TaskFilter.ACTIVE -> "Active"
        TaskFilter.COMPLETED -> "Completed"
        TaskFilter.ARCHIVED -> "Archived"
        TaskFilter.DUE_TODAY -> "Due Today"
        TaskFilter.OVERDUE -> "Overdue"
    }
}
