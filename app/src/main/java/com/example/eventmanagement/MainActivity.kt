package com.example.eventmanagement

import ProfileScreen
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eventmanagement.ui.screens.CreateEventScreen
import com.example.eventmanagement.ui.screens.EventDetailScreen
import com.example.eventmanagement.ui.screens.EventsByDateScreen
import com.example.eventmanagement.ui.screens.HomeScreen
import com.example.eventmanagement.ui.theme.EventManagementTheme
import com.example.eventmanagement.ui.viewmodel.EventViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: EventViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventManagementTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EventManagementApp(viewModel)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventManagementApp(viewModel: EventViewModel) {
    val navController = rememberNavController()

    Scaffold(
        // Hapus bottomBar karena navigasi sudah dipindah ke top bar (Menu untuk Detail, Person untuk Profil)
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }


            composable(Screen.CreateEvent.route) {
                CreateEventScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.EventDetail.route,
                arguments = listOf(
                    navArgument("eventId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                EventDetailScreen(
                    eventId = eventId,
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.EventsByDate.route) {
                EventsByDateScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
    }
}

/**
 * Navigation Routes
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Profile : Screen("profile")
    object CreateEvent : Screen("create_event")
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object EventsByDate : Screen("events_by_date")
}