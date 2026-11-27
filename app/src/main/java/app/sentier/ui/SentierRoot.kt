/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Map
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Alignment
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRail
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navArgument
import app.sentier.ui.backup.BackupScreen
import app.sentier.ui.edit.TaskEditScreen
import app.sentier.ui.habits.HabitEditScreen
import app.sentier.ui.habits.HabitNoteScreen
import java.time.LocalDate
import app.sentier.ui.habits.HabitsScreen
import app.sentier.ui.journey.JourneyScreen
import app.sentier.ui.journey.PassportScreen
import app.sentier.ui.journey.TreasureDialog
import app.sentier.ui.celebrate.CelebrationOverlay
import app.sentier.ui.celebrate.Confetti
import app.sentier.ui.stats.StatsScreen
import app.sentier.ui.review.WeeklyReviewScreen
import app.sentier.notify.Notifier
import app.sentier.ui.progress.CreditsScreen
import app.sentier.ui.progress.ProgressScreen
import app.sentier.ui.reminders.RemindersScreen
import app.sentier.ui.tasks.TasksScreen
import app.sentier.ui.today.TodayScreen

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    TODAY("today", "Aujourd'hui", Icons.Filled.WbSunny),
    TASKS("tasks", "Tâches", Icons.Filled.Checklist),
    HABITS("habits", "Habitudes", Icons.Filled.SelfImprovement),
    JOURNEY("journey", "Voyage", Icons.Filled.Map),
    PROGRESS("progress", "Progrès", Icons.Filled.EmojiEvents),
}

/** Largeur à partir de laquelle la navigation passe sur le côté. */
private const val WIDE_SCREEN_DP = 600

/** Largeur maximale du contenu des écrans. */
private val MAX_CONTENT_WIDTH = 840.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentierRoot(
    nav: NavHostController = rememberNavController(),
    /** Écran demandé par une notification (le bilan du dimanche), ouvert une fois puis oublié. */
    openRequest: String? = null,
    onOpenHandled: () -> Unit = {},
) {
    val snackbar = remember { SnackbarHostState() }
    val rewards: RewardsViewModel = viewModel(factory = RewardsViewModel.Factory)
    val newTreasures by rewards.newTreasures.collectAsStateWithLifecycle()
    val celebration by rewards.celebration.collectAsStateWithLifecycle()
    var confetti by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(rewards) {
        rewards.challengeDone.collect { c ->
            confetti = "defi-${c.index}-${System.currentTimeMillis()}"
            snackbar.showSnackbar("Défi relevé : ${c.title} ! +${c.xp} XP, +${c.gold} or")
        }
    }
    LaunchedEffect(openRequest) {
        if (openRequest == Notifier.OPEN_REVIEW) {
            nav.navigate("review") { launchSingleTop = true }
            onOpenHandled()
        }
    }
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val tabRoute = Tab.entries.firstOrNull { it.route == route }
    // Rail de navigation sur le côté dès que l'écran est assez large (tablette, paysage, pliable ouvert).
    val wide = LocalConfiguration.current.screenWidthDp >= WIDE_SCREEN_DP
    val goTo = { tab: Tab ->
        nav.navigate(tab.route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                if (tabRoute != null && !wide) {
                    NavigationBar {
                        for (tab in Tab.entries) {
                            NavigationBarItem(
                                selected = tab == tabRoute,
                                onClick = { goTo(tab) },
                                icon = { Icon(tab.icon, contentDescription = null) },
                                label = { Text(tab.label) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Row(Modifier.fillMaxSize()) {
                if (tabRoute != null && wide) {
                    NavigationRail(Modifier.fillMaxHeight()) {
                        Spacer(Modifier.weight(1f))
                        for (tab in Tab.entries) {
                            NavigationRailItem(
                                selected = tab == tabRoute,
                                onClick = { goTo(tab) },
                                icon = { Icon(tab.icon, contentDescription = null) },
                                label = { Text(tab.label) },
                            )
                        }
                        Spacer(Modifier.weight(1f))
                    }
                }
                // Sur tablette ou en paysage, le contenu reste centré et lisible au lieu de s'étirer.
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                    Box(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxSize()) {
                        val openTask = { id: Long -> nav.navigate("task/$id") }
                        val openHabit = { id: Long -> nav.navigate("habit/$id") }
                        val openHabitsTab = { goTo(Tab.HABITS) }
                        val openProgressTab = { goTo(Tab.PROGRESS) }
                        val tabModifier = Modifier.padding(padding).consumeWindowInsets(padding)
                        NavHost(nav, startDestination = Tab.TODAY.route) {
                            composable(Tab.TODAY.route) {
                                TodayScreen(
                                    snackbar, onOpenTask = openTask, onOpenHabits = openHabitsTab, onOpenProgress = openProgressTab,
                                    onOpenReminders = { nav.navigate("reminders") }, modifier = tabModifier,
                                    onOpenNote = { id -> nav.navigate("note/$id/${LocalDate.now().toEpochDay()}") },
                                )
                            }
                            composable(Tab.TASKS.route) {
                                TasksScreen(snackbar, onOpenTask = openTask, modifier = tabModifier)
                            }
                            composable(Tab.HABITS.route) {
                                HabitsScreen(onOpenHabit = openHabit, modifier = tabModifier)
                            }
                            composable(Tab.JOURNEY.route) {
                                JourneyScreen(modifier = tabModifier, onOpenPassport = { nav.navigate("passport") })
                            }
                            composable(Tab.PROGRESS.route) {
                                Box(tabModifier) {
                                    ProgressScreen(
                                        onOpenCredits = { nav.navigate("credits") },
                                        onOpenBackup = { nav.navigate("backup") },
                                        onOpenStats = { nav.navigate("stats") },
                                        onOpenReview = { nav.navigate("review") },
                                    )
                                }
                            }
                            composable("task/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
                                TaskEditScreen(taskId = entry.arguments?.getLong("id") ?: 0L, onBack = { nav.popBackStack() })
                            }
                            composable("habit/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
                                HabitEditScreen(
                                    habitId = entry.arguments?.getLong("id") ?: 0L,
                                    onBack = { nav.popBackStack() },
                                    onOpenNote = { id, day -> nav.navigate("note/$id/${day.toEpochDay()}") },
                                )
                            }
                            composable(
                                "note/{id}/{day}",
                                arguments = listOf(navArgument("id") { type = NavType.LongType }, navArgument("day") { type = NavType.LongType }),
                            ) { entry ->
                                HabitNoteScreen(
                                    habitId = entry.arguments?.getLong("id") ?: 0L,
                                    day = LocalDate.ofEpochDay(entry.arguments?.getLong("day") ?: LocalDate.now().toEpochDay()),
                                    onBack = { nav.popBackStack() },
                                )
                            }
                            composable("reminders") {
                                RemindersScreen(onBack = { nav.popBackStack() })
                            }
                            composable("stats") {
                                StatsScreen(onBack = { nav.popBackStack() })
                            }
                            composable("review") {
                                WeeklyReviewScreen(onBack = { nav.popBackStack() })
                            }
                            composable("backup") {
                                BackupScreen(onBack = { nav.popBackStack() })
                            }
                            composable("passport") {
                                PassportScreen(onBack = { nav.popBackStack() })
                            }
                            composable("credits") {
                                CreditsScreen(onBack = { nav.popBackStack() })
                            }
                        }
                    }
                }
            }
        }

        confetti?.let { Confetti(it) }

        val current = celebration
        if (current != null) {
            CelebrationOverlay(current, onDismiss = rewards::dismissCelebration)
        } else if (newTreasures.isNotEmpty()) {
            val best = newTreasures.maxBy { it.rarity.ordinal }
            val others = newTreasures.size - 1
            TreasureDialog(
                best,
                title = "Nouveau trésor : ${best.name}",
                footer = when (others) {
                    0 -> "Il rejoint ton carnet de voyage, dans l'onglet Voyage."
                    1 -> "Avec 1 autre trésor, il rejoint ton carnet de voyage, dans l'onglet Voyage."
                    else -> "Avec $others autres trésors, il rejoint ton carnet de voyage, dans l'onglet Voyage."
                },
                onDismiss = { rewards.markSeen(newTreasures) },
            )
            Confetti(newTreasures.joinToString { it.id })
        }
    }
}
