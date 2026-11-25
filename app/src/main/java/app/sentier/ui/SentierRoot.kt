/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.sentier.ui.edit.TaskEditScreen
import app.sentier.ui.habits.HabitEditScreen
import app.sentier.ui.habits.HabitsScreen
import app.sentier.ui.progress.CreditsScreen
import app.sentier.ui.progress.ProgressScreen
import app.sentier.ui.tasks.TasksScreen
import app.sentier.ui.today.TodayScreen

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    TODAY("today", "Aujourd'hui", Icons.Filled.WbSunny),
    TASKS("tasks", "Tâches", Icons.Filled.Checklist),
    HABITS("habits", "Habitudes", Icons.Filled.SelfImprovement),
    PROGRESS("progress", "Progrès", Icons.Filled.EmojiEvents),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentierRoot(nav: NavHostController = rememberNavController()) {
    val snackbar = remember { SnackbarHostState() }
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val tabRoute = Tab.entries.firstOrNull { it.route == route }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (tabRoute != null) {
                NavigationBar {
                    for (tab in Tab.entries) {
                        NavigationBarItem(
                            selected = tab == tabRoute,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        val openTask = { id: Long -> nav.navigate("task/$id") }
        val openHabit = { id: Long -> nav.navigate("habit/$id") }
        val openHabitsTab = {
            nav.navigate(Tab.HABITS.route) {
                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        val tabModifier = Modifier.padding(padding).consumeWindowInsets(padding)
        NavHost(nav, startDestination = Tab.TODAY.route) {
            composable(Tab.TODAY.route) {
                TodayScreen(
                    snackbar, onOpenTask = openTask, onOpenHabits = openHabitsTab, modifier = tabModifier,
                )
            }
            composable(Tab.TASKS.route) {
                TasksScreen(snackbar, onOpenTask = openTask, modifier = tabModifier)
            }
            composable(Tab.HABITS.route) {
                HabitsScreen(onOpenHabit = openHabit, modifier = tabModifier)
            }
            composable(Tab.PROGRESS.route) {
                Box(tabModifier) {
                    ProgressScreen(onOpenCredits = { nav.navigate("credits") })
                }
            }
            composable("task/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
                TaskEditScreen(taskId = entry.arguments?.getLong("id") ?: 0L, onBack = { nav.popBackStack() })
            }
            composable("habit/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
                HabitEditScreen(habitId = entry.arguments?.getLong("id") ?: 0L, onBack = { nav.popBackStack() })
            }
            composable("credits") {
                CreditsScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}
