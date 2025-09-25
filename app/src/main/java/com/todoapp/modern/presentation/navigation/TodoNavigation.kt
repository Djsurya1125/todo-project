package com.todoapp.modern.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.todoapp.modern.presentation.screens.home.HomeScreen
import com.todoapp.modern.presentation.screens.add_edit_task.AddEditTaskScreen
import com.todoapp.modern.presentation.screens.onboarding.OnboardingScreen
import com.todoapp.modern.presentation.screens.settings.SettingsScreen

/**
 * Main navigation component for the Todo app
 * Handles navigation between different screens
 */
@Composable
fun TodoNavigation(
    navController: NavHostController,
    appContainer: com.todoapp.modern.di.AppContainer
) {
    val viewModelFactory = com.todoapp.modern.presentation.ViewModelFactory(appContainer)
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel(factory = viewModelFactory),
                onNavigateToAddTask = {
                    navController.navigate(Screen.AddEditTask.route)
                },
                onNavigateToEditTask = { taskId ->
                    navController.navigate(Screen.AddEditTask.createRoute(taskId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route)
                }
            )
        }
        
        composable(
            route = Screen.AddEditTask.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            AddEditTaskScreen(
                viewModel = viewModel(factory = viewModelFactory),
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel(factory = viewModelFactory),
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.popBackStack(Screen.Home.route, false)
                }
            )
        }
    }
}

/**
 * Sealed class representing all screens in the app
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddEditTask : Screen("add_edit_task?taskId={taskId}") {
        fun createRoute(taskId: Long) = "add_edit_task?taskId=$taskId"
    }
    object Settings : Screen("settings")
    object Onboarding : Screen("onboarding")
}
