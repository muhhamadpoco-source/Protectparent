package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*

object Routes {
    const val ROLE_SELECTION = "role_selection"
    const val PARENT_SETUP = "parent_setup"
    const val PARENT_DASHBOARD = "parent_dashboard"
    const val CHILD_SETUP = "child_setup"
    const val NOTIFICATIONS_REVIEW = "notifications_review"
    const val SCREEN_TIME_REVIEW = "screen_time_review"
    const val CAMERA_CONTROL = "camera_control"
    const val ALL_FEATURES = "all_features"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.ROLE_SELECTION) {
        composable(Routes.ROLE_SELECTION) {
            RoleSelectionScreen(
                onSelectParent = { navController.navigate(Routes.PARENT_SETUP) },
                onSelectChild = { navController.navigate(Routes.CHILD_SETUP) }
            )
        }
        
        composable(Routes.PARENT_SETUP) {
            ParentSetupScreen(
                onPaired = {
                    navController.navigate(Routes.PARENT_DASHBOARD) {
                        popUpTo(Routes.ROLE_SELECTION) { inclusive = false }
                    }
                },
                onSkip = {
                    navController.navigate(Routes.PARENT_DASHBOARD) {
                        popUpTo(Routes.ROLE_SELECTION) { inclusive = false }
                    }
                }
            )
        }
        
        composable(Routes.CHILD_SETUP) {
            ChildSetupScreen()
        }
        
        composable(Routes.PARENT_DASHBOARD) {
            ParentDashboardScreen(
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS_REVIEW) },
                onNavigateToScreenTime = { navController.navigate(Routes.SCREEN_TIME_REVIEW) },
                onNavigateToCameraControl = { navController.navigate(Routes.CAMERA_CONTROL) },
                onNavigateToAllFeatures = { navController.navigate(Routes.ALL_FEATURES) },
                onNavigateToChildSetup = { navController.navigate(Routes.CHILD_SETUP) }
            )
        }

        composable(Routes.NOTIFICATIONS_REVIEW) {
            NotificationsReviewScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SCREEN_TIME_REVIEW) {
            ScreenTimeReviewScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.CAMERA_CONTROL) {
            CameraControlScreen(onBack = { navController.popBackStack() })
        }
        
        composable(Routes.ALL_FEATURES) {
            AllFeaturesScreen(onBack = { navController.popBackStack() })
        }
    }
}
