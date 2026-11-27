/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.habits

import app.sentier.domain.tr
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.sentier.data.HabitRepository
import app.sentier.ui.common.app
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

/** Écran pour écrire (ou effacer) la note d'une habitude pour un jour. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitNoteScreen(
    habitId: Long,
    day: LocalDate,
    onBack: () -> Unit,
    vm: HabitNoteViewModel = viewModel(key = "note-$habitId-$day", factory = HabitNoteViewModel.factory(habitId, day)),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (day == vm.today) tr("Note du jour", "Note of the day") else tr("Note · ${friendlyDate(day, vm.today).lowercase()}", "Note · ${friendlyDate(day, vm.today).lowercase()}")) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, tr("Retour", "Back")) } },
                actions = {
                    if (state.hadNote) {
                        IconButton(onClick = { vm.save("", onBack) }) { Icon(Icons.Filled.Delete, tr("Effacer la note", "Clear the note")) }
                    }
                },
            )
        },
    ) { padding ->
        val current = state
        if (current.loading) return@Scaffold
        var text by remember(current.initial) { mutableStateOf(current.initial) }
        Column(
            Modifier.padding(padding).fillMaxSize().imePadding().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(current.habitName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(HabitNote.MAX_LENGTH) },
                placeholder = { Text(tr("Comment ça s'est passé ? Ce que tu as ressenti, appris…", "How did it go? What you felt, what you learned…")) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(min = 160.dp).testTag("habitNote"),
            )
            Text(
                tr("${text.length} / ${HabitNote.MAX_LENGTH} · la note reste sur ton téléphone, comme le reste.", "${text.length} / ${HabitNote.MAX_LENGTH} · the note stays on your phone, like everything else."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = { vm.save(text, onBack) }, modifier = Modifier.fillMaxWidth()) { Text(tr("Enregistrer", "Save")) }
        }
    }
}

data class HabitNoteState(
    val habitName: String = "",
    val initial: String = "",
    val hadNote: Boolean = false,
    val loading: Boolean = true,
)

class HabitNoteViewModel(private val repo: HabitRepository, private val habitId: Long, private val day: LocalDate) : ViewModel() {
    private val _state = MutableStateFlow(HabitNoteState())
    val state: StateFlow<HabitNoteState> = _state

    val today: LocalDate get() = repo.today()

    init {
        viewModelScope.launch {
            val note = repo.getNote(habitId, day).orEmpty()
            _state.value = HabitNoteState(repo.getHabit(habitId)?.name.orEmpty(), note, note.isNotEmpty(), loading = false)
        }
    }

    fun save(text: String, onDone: () -> Unit) = viewModelScope.launch {
        repo.setNote(habitId, day, text)
        onDone()
    }

    companion object {
        fun factory(habitId: Long, day: LocalDate) = viewModelFactory {
            initializer { HabitNoteViewModel(app().habits, habitId, day) }
        }
    }
}

/** Journal d'une habitude : ses notes, la plus récente en premier. */
@Composable
fun HabitJournal(
    notes: List<HabitNote>,
    today: LocalDate,
    onWrite: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAll by remember { mutableStateOf(false) }
    val todayNote = notes.firstOrNull { it.day == today.toEpochDay() }
    Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(tr("Journal", "Journal"), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp).weight(1f))
                TextButton(onClick = { onWrite(today) }) {
                    Icon(Icons.Outlined.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(if (todayNote == null) tr("Note du jour", "Note of the day") else tr("Modifier", "Edit"), modifier = Modifier.padding(start = 4.dp))
                }
            }
            if (notes.isEmpty()) {
                Text(
                    tr("Une phrase par jour suffit : ce qui a aidé, ce qui a coincé. Tu verras le chemin parcouru.", "One sentence a day is enough: what helped, what got in the way. You'll see how far you've come."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val shown = if (showAll) notes else notes.take(PREVIEW_NOTES)
            shown.forEachIndexed { i, note ->
                if (i > 0) HorizontalDivider()
                val day = LocalDate.ofEpochDay(note.day)
                Column(Modifier.fillMaxWidth().clickable { onWrite(day) }.padding(vertical = 6.dp)) {
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
                    Text(if (showAll) tr("Voir moins", "Show less") else tr("Voir les ${notes.size} notes", "Show all ${notes.size} notes"))
                }
            }
        }
    }
}

private const val PREVIEW_NOTES = 5
