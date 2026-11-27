/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.habits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.sentier.data.HabitNote
import app.sentier.ui.common.friendlyDate
import java.time.LocalDate

/** Fenêtre pour écrire (ou effacer) la note d'une habitude pour un jour. */
@Composable
fun HabitNoteDialog(
    habitName: String,
    day: LocalDate,
    today: LocalDate,
    initial: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var text by remember(habitName, day) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (day == today) "Note du jour" else "Note · ${friendlyDate(day, today).lowercase()}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(habitName, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(HabitNote.MAX_LENGTH) },
                    placeholder = { Text("Comment ça s'est passé ? Ce que tu as ressenti, appris…") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    // Hauteur fixe (environ 5 lignes) : le texte plus long défile dans le champ.
                    modifier = Modifier.fillMaxWidth().height(140.dp).testTag("habitNote"),
                )
                Text(
                    "Reste sur ton téléphone, comme le reste.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(text) }) { Text("Enregistrer") } },
        dismissButton = {
            if (initial.isNotEmpty()) {
                TextButton(onClick = { onSave("") }) { Text("Effacer") }
            } else {
                TextButton(onClick = onDismiss) { Text("Annuler") }
            }
        },
    )
}

/** Journal d'une habitude : ses notes, la plus récente en premier. */
@Composable
fun HabitJournal(
    notes: List<HabitNote>,
    today: LocalDate,
    onWrite: (LocalDate, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAll by remember { mutableStateOf(false) }
    val todayNote = notes.firstOrNull { it.day == today.toEpochDay() }
    Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Journal", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp).weight(1f))
                TextButton(onClick = { onWrite(today, todayNote?.text.orEmpty()) }) {
                    Icon(Icons.Outlined.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(if (todayNote == null) "Note du jour" else "Modifier", modifier = Modifier.padding(start = 4.dp))
                }
            }
            if (notes.isEmpty()) {
                Text(
                    "Une phrase par jour suffit : ce qui a aidé, ce qui a coincé. Tu verras le chemin parcouru.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val shown = if (showAll) notes else notes.take(PREVIEW_NOTES)
            shown.forEachIndexed { i, note ->
                if (i > 0) HorizontalDivider()
                val day = LocalDate.ofEpochDay(note.day)
                Column(Modifier.fillMaxWidth().clickable { onWrite(day, note.text) }.padding(vertical = 6.dp)) {
                    Text(
                        friendlyDate(day, today),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(note.text, style = MaterialTheme.typography.bodyMedium, maxLines = 6, overflow = TextOverflow.Ellipsis)
                }
            }
            if (notes.size > PREVIEW_NOTES) {
                TextButton(onClick = { showAll = !showAll }) {
                    Text(if (showAll) "Voir moins" else "Voir les ${notes.size} notes")
                }
            }
        }
    }
}

private const val PREVIEW_NOTES = 5
