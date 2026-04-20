package com.mirea.module4.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mirea.module4.ui.home.HomeScreen
import com.mirea.module4.ui.task3.Task3Screen
import com.mirea.module4.ui.task4.Task4Screen
import com.mirea.module4.ui.task5.Task5Screen
import com.mirea.module4.ui.task6.Task6Screen
import com.mirea.module4.ui.task7.Task7Screen
import com.mirea.module4.ui.task8.Task8Screen
import com.mirea.module4.ui.task9.Task9Screen
import com.mirea.module4.ui.task10.Task10Screen
import com.mirea.module4.ui.task11.Task11Screen
import com.mirea.module4.ui.task12.Task12Screen
import com.mirea.module4.ui.task13.Task13Screen
import com.mirea.module4.ui.task14.Task14Screen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Task3 : Screen("task3")
    data object Task4 : Screen("task4")
    data object Task5 : Screen("task5")
    data object Task6 : Screen("task6")
    data object Task7 : Screen("task7")
    data object Task8 : Screen("task8")
    data object Task9 : Screen("task9")
    data object Task10 : Screen("task10")
    data object Task11 : Screen("task11")
    data object Task12 : Screen("task12")
    data object Task13 : Screen("task13")
    data object Task14 : Screen("task14")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Task3.route) { Task3Screen(navController) }
        composable(Screen.Task4.route) { Task4Screen(navController) }
        composable(Screen.Task5.route) { Task5Screen(navController) }
        composable(Screen.Task6.route) { Task6Screen(navController) }
        composable(Screen.Task7.route) { Task7Screen(navController) }
        composable(Screen.Task8.route) { Task8Screen(navController) }
        composable(Screen.Task9.route) { Task9Screen(navController) }
        composable(Screen.Task10.route) { Task10Screen(navController) }
        composable(Screen.Task11.route) { Task11Screen(navController) }
        composable(Screen.Task12.route) { Task12Screen(navController) }
        composable(Screen.Task13.route) { Task13Screen(navController) }
        composable(Screen.Task14.route) { Task14Screen(navController) }
    }
}
