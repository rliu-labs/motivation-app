/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import app.sentier.notify.Reminders
import app.sentier.ui.SentierRoot
import app.sentier.ui.theme.SentierTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Reprogramme les rappels actifs s'ils ont été perdus (mise à jour de l'appli, arrêt forcé…).
        lifecycleScope.launch { runCatching { Reminders.ensureScheduled(application as SentierApplication) } }
        setContent {
            SentierTheme {
                SentierRoot()
            }
        }
    }
}
