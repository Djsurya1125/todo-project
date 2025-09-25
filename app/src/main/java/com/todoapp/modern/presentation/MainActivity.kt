package com.todoapp.modern.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.todoapp.modern.TodoApplication
import com.todoapp.modern.presentation.navigation.TodoNavigation
import com.todoapp.modern.presentation.theme.TodoAppTheme
import com.todoapp.modern.presentation.theme.shouldUseDarkTheme

/**
 * Main Activity for the Todo application
 * Entry point with edge-to-edge display and navigation setup
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        setContent {
            val appContainer = (application as TodoApplication).appContainer
            TodoAppTheme(
                darkTheme = shouldUseDarkTheme(appContainer.preferencesManager)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TodoApp(
                        appContainer = appContainer,
                        navigateToAddTask = intent.getStringExtra("navigate_to") == "add_task"
                    )
                }
            }
        }
    }
}

@Composable
fun TodoApp(
    appContainer: com.todoapp.modern.di.AppContainer,
    navigateToAddTask: Boolean = false
) {
    val navController = rememberNavController()
    
    // Navigate to add task if requested from widget
    LaunchedEffect(navigateToAddTask) {
        if (navigateToAddTask) {
            navController.navigate("add_edit_task/-1L")
        }
    }
    
    TodoNavigation(
        navController = navController,
        appContainer = appContainer
    )
}
