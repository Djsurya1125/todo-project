package com.todoapp.modern.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.todoapp.modern.data.local.entities.Task
import com.todoapp.modern.data.local.entities.TaskCategory
import com.todoapp.modern.data.local.entities.TaskPriority
import com.todoapp.modern.presentation.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Individual task item component with swipe actions
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    task: Task,
    onTaskClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onDeleteTask: () -> Unit,
    onArchiveTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .combinedClickable(
                onClick = onTaskClick,
                onLongClick = { showMenu = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Completion checkbox
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Task content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Title
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Description
                    if (task.description.isNotEmpty()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .alpha(if (task.isCompleted) 0.6f else 1f)
                        )
                    }
                    
                    // Task metadata row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Priority indicator
                        PriorityChip(priority = task.priority)
                        
                        // Category indicator
                        CategoryChip(category = task.category)
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Due date
                        task.dueDateTime?.let { dueDateTime ->
                            DueDateChip(
                                dueDate = dueDateTime,
                                isOverdue = task.isOverdue(),
                                isDueToday = task.isDueToday()
                            )
                        }
                    }
                }
                
                // Menu button
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onTaskClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text(if (task.isCompleted) "Mark as incomplete" else "Mark as complete") },
                            onClick = {
                                showMenu = false
                                onToggleComplete()
                            },
                            leadingIcon = {
                                Icon(
                                    if (task.isCompleted) Icons.Outlined.RadioButtonUnchecked 
                                    else Icons.Default.CheckCircle,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Archive") },
                            onClick = {
                                showMenu = false
                                onArchiveTask()
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Archive, contentDescription = null)
                            }
                        )
                        
                        Divider()
                        
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDeleteTask()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityChip(
    priority: TaskPriority,
    modifier: Modifier = Modifier
) {
    val priorityColor = Color(priority.colorValue)
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = priorityColor.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(priorityColor)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = priority.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = priorityColor
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: TaskCategory,
    modifier: Modifier = Modifier
) {
    val categoryColor = getCategoryColor(category)
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = categoryColor.copy(alpha = 0.1f)
    ) {
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = categoryColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DueDateChip(
    dueDate: LocalDateTime?,
    isOverdue: Boolean,
    isDueToday: Boolean,
    modifier: Modifier = Modifier
) {
    if (dueDate == null) return
    
    val backgroundColor = when {
        isOverdue -> MaterialTheme.colorScheme.errorContainer
        isDueToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = when {
        isOverdue -> MaterialTheme.colorScheme.onErrorContainer
        isDueToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    isOverdue -> Icons.Default.Warning
                    isDueToday -> Icons.Default.CalendarToday
                    else -> Icons.Default.AccessTime
                },
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = when {
                    isDueToday -> "Today"
                    isOverdue -> "Overdue"
                    else -> dueDate.format(DateTimeFormatter.ofPattern("MMM dd"))
                },
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}

private fun getCategoryColor(category: TaskCategory): Color {
    return when (category) {
        TaskCategory.PERSONAL -> Color(0xFF6366F1)
        TaskCategory.WORK -> Color(0xFF8B5CF6)
        TaskCategory.SHOPPING -> Color(0xFF06B6D4)
        TaskCategory.HEALTH -> Color(0xFFEF4444)
        TaskCategory.LEARNING -> Color(0xFF10B981)
        TaskCategory.EDUCATION -> Color(0xFF10B981)
        TaskCategory.FINANCE -> Color(0xFFF59E0B)
        TaskCategory.TRAVEL -> Color(0xFF3B82F6)
        TaskCategory.HOME -> Color(0xFF84CC16)
        TaskCategory.OTHER -> Color(0xFF6B7280)
    }
}
