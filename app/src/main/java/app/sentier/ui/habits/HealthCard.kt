/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.habits

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import app.sentier.steps.HealthConnectSource
import app.sentier.steps.HealthState

/** « 7 h 25 », « 45 min ». */
fun formatDuration(minutes: Long): String = when {
    minutes < 60 -> "$minutes min"
    minutes % 60 == 0L -> "${minutes / 60} h"
    else -> "${minutes / 60} h ${"%02d".format(minutes % 60)}"
}

/**
 * Carte « Sommeil et sport automatiques » : lit les nuits et les séances de sport dans Health Connect
 * pour cocher toutes seules les habitudes de sommeil et de sport.
 */
@Composable
fun HealthCard(state: HealthState, onSetOn: (Boolean) -> Unit, onAddTemplate: (String) -> Unit, modifier: Modifier = Modifier) {
    var refused by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
        val ok = granted.containsAll(HealthConnectSource.HEALTH_PERMISSIONS)
        refused = !ok
        if (ok) onSetOn(true)
    }
    Card(
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.MonitorHeart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Sommeil et sport automatiques",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = state.on,
                    onCheckedChange = { on ->
                        if (on) launcher.launch(HealthConnectSource.HEALTH_PERMISSIONS) else onSetOn(false)
                    },
                    modifier = Modifier.semantics { contentDescription = "Sommeil et sport automatiques" },
                )
            }
            if (!state.on) {
                Text(
                    "Sentier peut lire tes nuits et tes séances de sport dans Health Connect (enregistrées par ta montre, " +
                        "Google Fit, Samsung Health…) et cocher tout seul tes habitudes de sommeil et de sport. " +
                        "Rien ne quitte ton téléphone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (refused) {
                    Text(
                        "Sentier n'a pas reçu l'autorisation de lire le sommeil et le sport.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            } else if (state.read && !state.granted) {
                Text(
                    "L'autorisation de lire le sommeil et le sport a été retirée dans Health Connect.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = { launcher.launch(HealthConnectSource.HEALTH_PERMISSIONS) }) { Text("Autoriser l'accès") }
            } else {
                Linked(state, onAddTemplate)
            }
        }
    }
}

@Composable
private fun Linked(state: HealthState, onAddTemplate: (String) -> Unit) {
    Line(
        Icons.Filled.Bedtime,
        state.lastNightMinutes?.let { "Dernière nuit : ${formatDuration(it)}" } ?: "Pas de nuit enregistrée cette nuit",
        state.sleepHabit?.let { "Reliée à « ${it.name} »" },
    )
    if (state.sleepHabit == null) {
        OutlinedButton(onClick = { onAddTemplate("Dormir 7 heures") }) { Text("Ajouter l'habitude « Dormir 7 heures »") }
    }
    Line(
        Icons.Filled.FitnessCenter,
        when (state.sportTodaySessions) {
            0 -> "Pas encore de séance de sport aujourd'hui"
            1 -> "Sport aujourd'hui : ${formatDuration(state.sportTodayMinutes)}"
            else -> "Sport aujourd'hui : ${formatDuration(state.sportTodayMinutes)} en ${state.sportTodaySessions} séances"
        },
        state.sportHabit?.let { "Reliée à « ${it.name} »" },
    )
    if (state.sportHabit == null) {
        OutlinedButton(onClick = { onAddTemplate("Faire du sport") }) { Text("Ajouter l'habitude « Faire du sport »") }
    }
    Text(
        "Les 7 derniers jours sont mis à jour à chaque ouverture de l'appli. Une valeur saisie à la main n'est jamais baissée.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun Line(icon: ImageVector, title: String, subtitle: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
