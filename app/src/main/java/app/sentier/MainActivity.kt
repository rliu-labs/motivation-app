/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import app.sentier.data.AutoBackup
import app.sentier.data.ThemeMode
import app.sentier.notify.HabitReminders
import app.sentier.notify.Reminders
import app.sentier.ui.SentierRoot
import app.sentier.ui.onboarding.OnboardingScreen
import app.sentier.ui.onboarding.OnboardingViewModel
import app.sentier.ui.theme.SentierTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val settings = (application as SentierApplication).settings
        // Lu tout de suite pour que l'appli s'ouvre directement dans le bon thème.
        val initialTheme = runCatching { runBlocking { settings.themeMode.first() } }.getOrDefault(ThemeMode.SYSTEM)
        // L'accueil ne s'affiche qu'au tout premier lancement (et revient si l'écran tourne avant la fin).
        val firstLaunch = runCatching { runBlocking { OnboardingViewModel.needed(application as SentierApplication) } }.getOrDefault(false)
        // Reprogramme les rappels actifs s'ils ont été perdus (mise à jour de l'appli, arrêt forcé…).
        lifecycleScope.launch {
            runCatching { Reminders.ensureScheduled(application as SentierApplication) }
            runCatching { HabitReminders.ensureScheduled(application as SentierApplication) }
            runCatching { AutoBackup.ensureScheduled(application as SentierApplication) }
        }
        setContent {
            val mode by settings.themeMode.collectAsStateWithLifecycle(initialTheme)
            val dark = when (mode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            // Les icônes de la barre d'état suivent le thème choisi, pas celui du téléphone.
            LaunchedEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            SentierTheme(darkTheme = dark) {
                var showOnboarding by remember { mutableStateOf(firstLaunch) }
                if (showOnboarding) {
                    OnboardingScreen(onDone = { showOnboarding = false })
                } else {
                    SentierRoot()
                }
            }
        }
    }
}
