package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.presentation.LauncherUiState
import com.example.presentation.LauncherViewModel
import com.example.presentation.analytics.AnalyticsScreen
import com.example.presentation.focusmode.FocusScreen
import com.example.presentation.launcher.HomeScreen
import com.example.presentation.search.SearchScreen
import com.example.presentation.settings.SettingsScreen

object LauncherDestinations {
    const val HOME = "home"
    const val APPS = "apps"
    const val ANALYTICS = "analytics"
    const val FOCUS = "focus"
    const val SETTINGS = "settings"
}

@Composable
fun LauncherNavigation(
    navController: NavHostController,
    state: LauncherUiState,
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = LauncherDestinations.HOME,
        modifier = modifier
    ) {
        composable(LauncherDestinations.HOME) {
            HomeScreen(
                state = state,
                viewModel = viewModel,
                onNavigateToApps = { navController.navigate(LauncherDestinations.APPS) },
                onNavigateToFocus = { navController.navigate(LauncherDestinations.FOCUS) },
                onNavigateToAnalytics = { navController.navigate(LauncherDestinations.ANALYTICS) },
                onNavigateToSettings = { navController.navigate(LauncherDestinations.SETTINGS) }
            )
        }
        
        composable(LauncherDestinations.APPS) {
            SearchScreen(
                state = state,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(LauncherDestinations.ANALYTICS) {
            AnalyticsScreen(
                state = state,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(LauncherDestinations.FOCUS) {
            FocusScreen(
                state = state,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(LauncherDestinations.SETTINGS) {
            SettingsScreen(
                state = state,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
