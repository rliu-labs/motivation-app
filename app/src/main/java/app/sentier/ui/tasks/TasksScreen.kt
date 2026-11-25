/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sentier.data.TaskList
import app.sentier.ui.common.QuickAddBar
import app.sentier.ui.common.SectionHeader
import app.sentier.ui.common.TaskRow
import app.sentier.ui.common.friendlyDate
import app.sentier.ui.theme.ListPalette

@Composable
fun TasksScreen(
    snackbar: SnackbarHostState,
    onOpenTask: (Long) -> Unit,
    modifier: Modifier = Modifier,
    vm: TasksViewModel = viewModel(factory = TasksViewModel.Factory),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var editingList by remember { mutableStateOf<TaskList?>(null) }
    var creatingList by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(vm) {
        vm.completions.collect { result ->
            val message = if (result.nextDue != null) {
                "Bravo ! Prochaine fois : ${friendlyDate(result.nextDue, state.today).lowercase()}"
            } else {
                "Bravo, tâche terminée !"
            }
            val action = snackbar.showSnackbar(message, actionLabel = "Annuler", duration = SnackbarDuration.Short)
            if (action == SnackbarResult.ActionPerformed) vm.undo(result)
        }
    }

    Column(modifier.fillMaxSize().imePadding()) {
        Text(
            "Tâches",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(selected = state.selectedListId == null, onClick = { vm.selectList(null) }, label = { Text("Toutes") })
            }
            items(state.lists, key = { it.id }) { list ->
                FilterChip(
                    selected = state.selectedListId == list.id,
                    onClick = { vm.selectList(list.id) },
                    label = { Text(list.name) },
                    leadingIcon = { Box(Modifier.size(10.dp).clip(CircleShape).background(Color(list.color))) },
                )
            }
            item {
                FilterChip(
                    selected = false,
                    onClick = { creatingList = true },
                    label = { Text("Liste") },
                    leadingIcon = { Icon(Icons.Filled.Add, null, Modifier.size(18.dp)) },
                )
            }
        }
        state.selectedListId?.let { id ->
            state.listsById[id]?.let { list ->
                TextButton(onClick = { editingList = list }, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text("Modifier la liste « ${list.name} »")
                }
            }
        }

        LazyColumn(Modifier.weight(1f)) {
            if (!state.loading && state.isEmpty) {
                item {
                    Text(
                        "Aucune tâche ici. Ajoute la première juste en dessous !",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
            for (group in state.groups) {
                item(key = "h-${group.title}") {
                    SectionHeader(
                        group.title,
                        color = if (group.late) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    )
                }
                items(group.tasks, key = { it.id }) { task ->
                    TaskRow(
                        task = task, checked = false,
                        list = if (state.selectedListId == null) task.listId?.let(state.listsById::get) else null,
                        today = state.today,
                        onToggle = { vm.toggle(task) }, onClick = { onOpenTask(task.id) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
            if (state.completed.isNotEmpty()) {
                item(key = "h-done") {
                    Row(
                        Modifier.fillMaxWidth().clickable { vm.toggleShowCompleted() }.padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SectionHeader("Terminées (${state.completed.size})", Modifier.weight(1f))
                        Icon(if (state.showCompleted) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
                    }
                }
                if (state.showCompleted) {
                    items(state.completed, key = { it.id }) { task ->
                        TaskRow(
                            task = task, checked = true, list = null, today = state.today,
                            onToggle = { vm.toggle(task) }, onClick = { onOpenTask(task.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
            item { Spacer(Modifier.size(16.dp)) }
        }
        val listName = state.selectedListId?.let { state.listsById[it]?.name }
        QuickAddBar(
            placeholder = if (listName != null) "Ajouter à « $listName »" else "Ajouter une tâche",
            onAdd = vm::add,
        )
    }

    if (creatingList) {
        ListDialog(
            initial = null,
            onDismiss = { creatingList = false },
            onSave = { name, color -> vm.addList(name, color); creatingList = false },
            onDelete = null,
        )
    }
    editingList?.let { list ->
        ListDialog(
            initial = list,
            onDismiss = { editingList = null },
            onSave = { name, color -> vm.updateList(list.copy(name = name.trim(), color = color)); editingList = null },
            onDelete = { vm.deleteList(list); editingList = null },
        )
    }
}

/** Création ou modification d'une liste : nom et couleur. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ListDialog(
    initial: TaskList?,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by rememberSaveable { mutableStateOf(initial?.name ?: "") }
    var color by rememberSaveable { mutableStateOf(initial?.color ?: ListPalette.first()) }
    var confirmDelete by remember { mutableStateOf(false) }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Supprimer la liste ?") },
            text = { Text("Les tâches de cette liste ne sont pas supprimées : elles restent dans « Toutes ».") },
            confirmButton = { TextButton(onClick = onDelete) { Text("Supprimer") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Garder") } },
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nouvelle liste" else "Modifier la liste") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") }, singleLine = true)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (c in ListPalette) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .then(if (c == color) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                                .clickable { color = c }
                                .semantics { contentDescription = "Couleur" },
                        )
                    }
                }
                if (onDelete != null) {
                    TextButton(onClick = { confirmDelete = true }) {
                        Text("Supprimer cette liste", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, color) }, enabled = name.isNotBlank()) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}
