package com.todoapp.modern.presentation.screens.add_edit_task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
// Removed stringResource import
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Removed hiltViewModel import - using manual DI
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todoapp.modern.R
import com.todoapp.modern.data.local.entities.TaskCategory
import com.todoapp.modern.data.local.entities.TaskPriority
import com.todoapp.modern.presentation.components.DatePickerDialog
import com.todoapp.modern.presentation.components.TimePickerDialog
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    viewModel: AddEditTaskViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.isTaskSaved) {
        if (uiState.isTaskSaved) {
            onNavigateBack()
        }
    }
    
    if (uiState.errorMessage != null) {
        LaunchedEffect(uiState.errorMessage) {
            // Show snackbar or handle error
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Text(
                text = if (uiState.isEditMode) "Edit Task" else "Add Task",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(
                onClick = { viewModel.onEvent(AddEditTaskEvent.SaveTask) },
                enabled = uiState.title.isNotBlank()
            ) {
                Text("Save")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title Input
        OutlinedTextField(
            value = uiState.title,
            onValueChange = { viewModel.onEvent(AddEditTaskEvent.TitleChanged(it)) },
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.titleError != null
        )
        
        uiState.titleError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description Input
        OutlinedTextField(
            value = uiState.description,
            onValueChange = { viewModel.onEvent(AddEditTaskEvent.DescriptionChanged(it)) },
            label = { Text("Description (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Category Selection
        Text(
            text = "Category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskCategory.values().forEach { category ->
                FilterChip(
                    onClick = { viewModel.onEvent(AddEditTaskEvent.CategoryChanged(category)) },
                    label = { Text(category.displayName) },
                    selected = uiState.category == category,
                    leadingIcon = {
                        Icon(
                            imageVector = category.iconVector,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Priority Selection
        Text(
            text = "Priority",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskPriority.values().forEach { priority ->
                FilterChip(
                    onClick = { viewModel.onEvent(AddEditTaskEvent.PriorityChanged(priority)) },
                    label = { Text(priority.displayName) },
                    selected = uiState.priority == priority,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = priority.color.copy(alpha = 0.2f),
                        selectedLabelColor = priority.color
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Due Date Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showDatePicker = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = uiState.dueDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                            ?: "No due date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    if (uiState.dueDate != null) {
                        IconButton(
                            onClick = { viewModel.onEvent(AddEditTaskEvent.DueDateChanged(null)) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear due date"
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Due Time Section (only show if date is selected)
        if (uiState.dueDate != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showTimePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Due Time",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = uiState.dueTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))
                                ?: "No specific time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row {
                        if (uiState.dueTime != null) {
                            IconButton(
                                onClick = { viewModel.onEvent(AddEditTaskEvent.DueTimeChanged(null)) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear due time"
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Select time"
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Reminder Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Set Reminder",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Get notified when task is due",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = uiState.hasReminder,
                onCheckedChange = { viewModel.onEvent(AddEditTaskEvent.ReminderChanged(it)) },
                enabled = uiState.dueDate != null
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Delete Button (only in edit mode)
        if (uiState.isEditMode) {
            OutlinedButton(
                onClick = { viewModel.onEvent(AddEditTaskEvent.DeleteTask) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Task")
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                viewModel.onEvent(AddEditTaskEvent.DueDateChanged(date))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            initialDate = uiState.dueDate
        )
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { time ->
                viewModel.onEvent(AddEditTaskEvent.DueTimeChanged(time))
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            initialTime = uiState.dueTime
        )
    }
}

// Extension properties for icons
private val TaskCategory.iconVector
    get() = when (this) {
        TaskCategory.PERSONAL -> Icons.Default.Person
        TaskCategory.WORK -> Icons.Default.Work
        TaskCategory.SHOPPING -> Icons.Default.ShoppingCart
        TaskCategory.HEALTH -> Icons.Default.FavoriteBorder
        TaskCategory.LEARNING -> Icons.Default.School
        TaskCategory.EDUCATION -> Icons.Default.School
        TaskCategory.FINANCE -> Icons.Default.AttachMoney
        TaskCategory.TRAVEL -> Icons.Default.Flight
        TaskCategory.HOME -> Icons.Default.Home
        TaskCategory.OTHER -> Icons.Default.Category
    }
