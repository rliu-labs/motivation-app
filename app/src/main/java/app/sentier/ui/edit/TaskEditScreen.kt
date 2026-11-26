/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import app.sentier.ui.common.DifficultySelector
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sentier.data.Task
import app.sentier.domain.Recurrence
import app.sentier.ui.common.PriorityCheckbox
import app.sentier.ui.common.friendlyDate
import app.sentier.ui.theme.PriorityColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditScreen(
    taskId: Long,
    onBack: () -> Unit,
    vm: TaskEditViewModel = viewModel(key = "task-$taskId", factory = TaskEditViewModel.factory(taskId)),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmDelete by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showRecurrence by remember { mutableStateOf(false) }

    LaunchedEffect(vm) {
        vm.completions.collect { result ->
            val message = if (result.nextDue != null) {
                "Bravo ! Prochaine fois : ${friendlyDate(result.nextDue, vm.today).lowercase()}"
            } else {
                "Bravo, tâche terminée !"
            }
            if (snackbar.showSnackbar(message, "Annuler", duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed) {
                vm.undo(result)
            }
        }
    }

    // Tâche supprimée ou introuvable : on revient en arrière.
    LaunchedEffect(state.loading, state.task) {
        if (!state.loading && state.task == null) onBack()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") }
                },
                actions = {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Supprimer la tâche")
                    }
                },
            )
        },
    ) { padding ->
        val task = state.task ?: return@Scaffold
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            TitleRow(task, onToggle = vm::toggleDone, onTitle = vm::setTitle)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Field(Icons.Filled.Event, "Échéance") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val due = task.dueDay?.let(LocalDate::ofEpochDay)
                    if (due != null) {
                        FilledTonalButton(onClick = { showDatePicker = true }) { Text(friendlyDate(due, vm.today)) }
                        if (task.recurrence == null) {
                            IconButton(onClick = { vm.setDueDate(null) }) { Icon(Icons.Filled.Close, "Retirer l'échéance") }
                        }
                    } else {
                        AssistChip(onClick = { vm.setDueDate(vm.today) }, label = { Text("Aujourd'hui") })
                        AssistChip(onClick = { vm.setDueDate(vm.today.plusDays(1)) }, label = { Text("Demain") })
                        AssistChip(onClick = { showDatePicker = true }, label = { Text("Choisir…") })
                    }
                }
            }

            Field(Icons.Filled.Repeat, "Répétition") {
                val rec = Recurrence.parse(task.recurrence)
                OutlinedButton(onClick = { showRecurrence = true }) {
                    Text(
                        when {
                            rec == null -> "Ne se répète pas"
                            task.repeatFromCompletion -> rec.describe() + ", après la fin"
                            else -> rec.describe()
                        }
                    )
                }
            }

            Field(Icons.Filled.Flag, "Priorité") {
                PrioritySelector(task.priority, vm::setPriority)
            }

            Field(Icons.Filled.Star, "Difficulté") {
                DifficultySelector(task.difficulty, vm::setDifficulty)
            }

            Field(Icons.AutoMirrored.Filled.List, "Liste") {
                ListSelector(task.listId, state.lists, vm::setList)
            }

            Field(Icons.AutoMirrored.Filled.Notes, "Notes") {
                var notes by remember(task.id) { mutableStateOf(task.notes) }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it; vm.setNotes(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ajouter une note") },
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            }

            if (task.parentId == null) {
                Field(Icons.Filled.Check, "Sous-tâches") {
                    Subtasks(
                        subtasks = state.subtasks,
                        onAdd = vm::addSubtask,
                        onToggle = vm::toggleSubtask,
                        onDelete = vm::deleteSubtask,
                    )
                }
            }

            if (task.completionCount > 0) {
                Text(
                    if (task.completionCount == 1) "Validée 1 fois" else "Validée ${task.completionCount} fois",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
        }

        if (showDatePicker) {
            DueDateDialog(
                initial = task.dueDay?.let(LocalDate::ofEpochDay) ?: vm.today,
                onDismiss = { showDatePicker = false },
                onPick = { vm.setDueDate(it); showDatePicker = false },
            )
        }
        if (showRecurrence) {
            RecurrenceDialog(
                initial = Recurrence.parse(task.recurrence),
                initialFromCompletion = task.repeatFromCompletion,
                onDismiss = { showRecurrence = false },
                onSave = { rec, fromCompletion -> vm.setRecurrence(rec, fromCompletion); showRecurrence = false },
            )
        }
        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text("Supprimer cette tâche ?") },
                text = { Text("« ${task.title} » et ses sous-tâches seront supprimées.") },
                confirmButton = { TextButton(onClick = { confirmDelete = false; vm.delete(onBack) }) { Text("Supprimer") } },
                dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Garder") } },
            )
        }
    }
}

@Composable
private fun TitleRow(task: Task, onToggle: () -> Unit, onTitle: (String) -> Unit) {
    var title by remember(task.id) { mutableStateOf(task.title) }
    Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        PriorityCheckbox(checked = task.isCompleted, priority = task.priority, onClick = onToggle, label = task.title)
        TextField(
            value = title,
            onValueChange = { title = it; onTitle(it) },
            modifier = Modifier.weight(1f).testTag("title"),
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )
    }
}

@Composable
private fun Field(icon: ImageVector, label: String, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.size(4.dp))
            content()
        }
    }
}

private val PriorityLabels = listOf("Aucune", "Basse", "Moyenne", "Haute")

@Composable
private fun PrioritySelector(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        for (p in 3 downTo 0) {
            val color = PriorityColors.of(p)
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (p == selected) color else Color.Transparent)
                    .border(2.5.dp, color, CircleShape)
                    .clickable { onSelect(p) },
                contentAlignment = Alignment.Center,
            ) {
                if (p == selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
            }
        }
        Text(PriorityLabels[selected], style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ListSelector(selected: Long?, lists: List<app.sentier.data.TaskList>, onSelect: (Long?) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val current = lists.firstOrNull { it.id == selected }
    Box {
        OutlinedButton(onClick = { open = true }) {
            if (current != null) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(Color(current.color)))
                Spacer(Modifier.width(8.dp))
            }
            Text(current?.name ?: "Aucune liste")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text("Aucune liste") }, onClick = { onSelect(null); open = false })
            for (l in lists) {
                DropdownMenuItem(
                    text = { Text(l.name) },
                    leadingIcon = { Box(Modifier.size(12.dp).clip(CircleShape).background(Color(l.color))) },
                    onClick = { onSelect(l.id); open = false },
                )
            }
        }
    }
}

@Composable
private fun Subtasks(
    subtasks: List<Task>,
    onAdd: (String) -> Unit,
    onToggle: (Task) -> Unit,
    onDelete: (Task) -> Unit,
) {
    Column {
        for (sub in subtasks) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PriorityCheckbox(checked = sub.isCompleted, priority = 0, onClick = { onToggle(sub) }, label = sub.title)
                Text(
                    sub.title,
                    modifier = Modifier.weight(1f),
                    textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else null,
                    color = if (sub.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = { onDelete(sub) }) { Icon(Icons.Filled.Close, contentDescription = "Supprimer la sous-tâche") }
            }
        }
        var text by rememberSaveable { mutableStateOf("") }
        val submit = { if (text.isNotBlank()) { onAdd(text.trim()); text = "" } }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth().testTag("subtaskAdd"),
            placeholder = { Text("Ajouter une sous-tâche") },
            leadingIcon = { Icon(Icons.Filled.Add, null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDateDialog(initial: LocalDate, onDismiss: () -> Unit, onPick: (LocalDate) -> Unit) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let {
                    onPick(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                } ?: onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    ) {
        DatePicker(state = pickerState)
    }
}
