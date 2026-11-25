/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.sentier.domain.Recurrence
import app.sentier.domain.letterFr
import java.time.DayOfWeek

private enum class Mode(val label: String) {
    NONE("Ne se répète pas"),
    DAILY("Chaque jour"),
    EVERY_N_DAYS("Tous les N jours"),
    WEEKDAYS("Certains jours de la semaine"),
    MONTHLY("Chaque mois"),
}

/** Choix de la répétition, version simple des règles de Tasks.org. */
@Composable
fun RecurrenceDialog(
    initial: Recurrence?,
    initialFromCompletion: Boolean,
    onDismiss: () -> Unit,
    onSave: (Recurrence?, Boolean) -> Unit,
) {
    var mode by remember {
        mutableStateOf(
            when {
                initial == null -> Mode.NONE
                initial.frequency == Recurrence.Frequency.MONTHLY -> Mode.MONTHLY
                initial.frequency == Recurrence.Frequency.WEEKLY -> Mode.WEEKDAYS
                initial.interval > 1 -> Mode.EVERY_N_DAYS
                else -> Mode.DAILY
            }
        )
    }
    var interval by remember { mutableIntStateOf(initial?.interval?.coerceAtLeast(2) ?: 2) }
    var days by remember { mutableStateOf(initial?.days?.takeIf { it.isNotEmpty() } ?: Recurrence.WEEKDAYS) }
    var fromCompletion by remember { mutableStateOf(initialFromCompletion) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Répétition") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                for (m in Mode.entries) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(selected = mode == m, onClick = { mode = m }, role = Role.RadioButton)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = mode == m, onClick = null)
                        Text(m.label, Modifier.padding(start = 8.dp))
                    }
                    if (m == Mode.EVERY_N_DAYS && mode == m) {
                        Row(Modifier.padding(start = 40.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Tous les")
                            IconButton(onClick = { if (interval > 2) interval-- }) { Icon(Icons.Filled.Remove, "Moins") }
                            Text("$interval", style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { if (interval < 365) interval++ }) { Icon(Icons.Filled.Add, "Plus") }
                            Text("jours")
                        }
                    }
                    if (m == Mode.WEEKDAYS && mode == m) {
                        Row(
                            Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            for (d in DayOfWeek.entries) {
                                FilledIconToggleButton(
                                    checked = d in days,
                                    onCheckedChange = { on -> days = if (on) days + d else (days - d).ifEmpty { days } },
                                    modifier = Modifier.size(36.dp),
                                    shape = CircleShape,
                                ) { Text(d.letterFr()) }
                            }
                        }
                    }
                }
                if (mode != Mode.NONE) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { fromCompletion = !fromCompletion }
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = fromCompletion, onCheckedChange = null)
                        Column(Modifier.padding(start = 8.dp)) {
                            Text("Compter à partir du jour où je la termine")
                            Text(
                                "Sinon, à partir de l'échéance prévue.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val rec = when (mode) {
                    Mode.NONE -> null
                    Mode.DAILY -> Recurrence(Recurrence.Frequency.DAILY)
                    Mode.EVERY_N_DAYS -> Recurrence(Recurrence.Frequency.DAILY, interval)
                    Mode.WEEKDAYS -> if (days.size == 7) Recurrence(Recurrence.Frequency.DAILY)
                        else Recurrence(Recurrence.Frequency.WEEKLY, days = days)
                    Mode.MONTHLY -> Recurrence(Recurrence.Frequency.MONTHLY)
                }
                onSave(rec, fromCompletion)
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}
