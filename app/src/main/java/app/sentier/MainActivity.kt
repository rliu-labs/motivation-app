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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import app.sentier.data.ThemeMode
import app.sentier.notify.Reminders
import app.sentier.ui.SentierRoot
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
        // Reprogramme les rappels actifs s'ils ont été perdus (mise à jour de l'appli, arrêt forcé…).
        lifecycleScope.launch { runCatching { Reminders.ensureScheduled(application as SentierApplication) } }
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
                SentierRoot()
            }
        }
    }
}
