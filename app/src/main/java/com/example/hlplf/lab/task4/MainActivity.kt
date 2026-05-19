package com.example.hlplf.lab.task4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hlplf.lab.task4.data.database.UniversityDatabase
import com.example.hlplf.lab.task4.data.repository.UniversityRepository
import com.example.hlplf.lab.task4.ui.navigation.AppNavHost
import com.example.hlplf.lab.task4.ui.navigation.Screen
import com.example.hlplf.lab.task4.ui.theme.HLPLFLabTask4Theme

class MainActivity : ComponentActivity() {

    private val repository by lazy {
        UniversityRepository(UniversityDatabase.getInstance(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HLPLFLabTask4Theme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val topLevelRoutes = listOf(Screen.Students.route, Screen.Courses.route)
                val showBottomBar = currentRoute in topLevelRoutes

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Students.route,
                                    onClick = {
                                        navController.navigate(Screen.Students.route) {
                                            popUpTo(Screen.Students.route) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Students") },
                                    label = { Text("Students") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Courses.route,
                                    onClick = {
                                        navController.navigate(Screen.Courses.route) {
                                            popUpTo(Screen.Students.route)
                                            launchSingleTop = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.List, contentDescription = "Courses") },
                                    label = { Text("Courses") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        repository = repository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
