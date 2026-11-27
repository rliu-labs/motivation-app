/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.today

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.LifecycleResumeEffect
import app.sentier.data.Habit
import app.sentier.data.HabitWithStats
import app.sentier.ui.habits.HabitIcons
import androidx.compose.ui.text.style.TextOverflow
import app.sentier.ui.habits.HabitsViewModel
import app.sentier.ui.habits.NumericValueDialog
import app.sentier.ui.habits.StepsMiniCard
import app.sentier.ui.habits.StrengthRing
import app.sentier.ui.habits.formatValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sentier.ui.common.rememberTick
import app.sentier.ui.common.QuickAddBar
import app.sentier.ui.progress.GameSummary
import app.sentier.ui.progress.ProgressViewModel
import app.sentier.ui.common.SectionHeader
import app.sentier.ui.common.TaskRow
import app.sentier.ui.common.friendlyDate
import app.sentier.ui.common.longDate

@Composable
fun TodayScreen(
    snackbar: SnackbarHostState,
    onOpenTask: (Long) -> Unit,
    onOpenHabits: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenReminders: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenNote: (Long) -> Unit = {},
    vm: TodayViewModel = viewModel(factory = TodayViewModel.Factory),
    habitsVm: HabitsViewModel = viewModel(factory = HabitsViewModel.Factory),
    gameVm: ProgressViewModel = viewModel(factory = ProgressViewModel.Factory),
) {
    val game by gameVm.state.collectAsStateWithLifecycle()
    val state by vm.state.collectAsStateWithLifecycle()
    val habits by habitsVm.state.collectAsStateWithLifecycle()
    var editingHabit by remember { mutableStateOf<Habit?>(null) }
    val notes by habitsVm.notes.collectAsStateWithLifecycle()

    LifecycleResumeEffect(habitsVm) {
        habitsVm.refreshSteps()
        onPauseOrDispose { }
    }

    LaunchedEffect(vm) {
        vm.completions.collect { result ->
            val base = if (result.nextDue != null) {
                "Bravo ! Prochaine fois : ${friendlyDate(result.nextDue, state.today).lowercase()}"
            } else {
                "Bravo, « ${result.before.title} » est faite !"
            }
            val message = result.gain?.let { "$base +${it.xp} XP, +${it.gold} or" } ?: base
            val action = snackbar.showSnackbar(message, actionLabel = "Annuler", duration = SnackbarDuration.Short)
            if (action == SnackbarResult.ActionPerformed) vm.undo(result)
        }
    }

    Column(modifier.fillMaxSize().imePadding()) {
        LazyColumn(Modifier.weight(1f)) {
            item {
                Box(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)) {
                        Text("Aujourd'hui", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(longDate(state.today), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        GameSummary(game, onClick = onOpenProgress, modifier = Modifier.padding(top = 12.dp))
                    }
                    IconButton(onClick = onOpenReminders, modifier = Modifier.align(Alignment.TopEnd).padding(top = 20.dp, end = 8.dp)) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Rappels", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item { ProgressCard(done = state.done.size, total = state.total) }
            item { StepsMiniCard(habits.steps, onClick = onOpenHabits) }
            if (habits.habits.isNotEmpty()) {
                item { SectionHeader("Habitudes du jour") }
                items(habits.habits, key = { "h-${it.habit.id}" }) { h ->
                    TodayHabitRow(
                        h = h,
                        today = habits.today.toEpochDay(),
                        note = notes[h.habit.id],
                        onClick = { if (h.habit.isNumeric) editingHabit = h.habit else habitsVm.toggle(h.habit) },
                        onNote = { onOpenNote(h.habit.id) },
                    )
                }
            }

            if (state.todo.isNotEmpty()) {
                item { SectionHeader("À faire") }
                items(state.todo, key = { it.id }) { task ->
                    TaskRow(
                        task = task, checked = false, list = task.listId?.let(state.lists::get), today = state.today,
                        onToggle = { vm.toggle(task, isDone = false) }, onClick = { onOpenTask(task.id) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
            if (state.done.isNotEmpty()) {
                item { SectionHeader("Fait aujourd'hui") }
                items(state.done, key = { it.id }) { task ->
                    TaskRow(
                        task = task, checked = true, list = task.listId?.let(state.lists::get), today = state.today,
                        onToggle = { vm.toggle(task, isDone = true) }, onClick = { onOpenTask(task.id) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
            item { Spacer(Modifier.size(16.dp)) }
        }
        QuickAddBar(placeholder = "Ajouter une tâche pour aujourd'hui", onAdd = vm::add)
    }

    editingHabit?.let { habit ->
        val current = habits.habits.firstOrNull { it.habit.id == habit.id }?.valueOn(habits.today.toEpochDay()) ?: 0.0
        NumericValueDialog(
            habit = habit, day = habits.today, today = habits.today, initial = current,
            onDismiss = { editingHabit = null },
            onSave = { v -> habitsVm.setValue(habit, habits.today, v); editingHabit = null },
        )
    }
}

/** Une habitude sur l'écran Aujourd'hui : un geste pour la cocher. */
@Composable
private fun TodayHabitRow(h: HabitWithStats, today: Long, note: String?, onClick: () -> Unit, onNote: () -> Unit) {
    val color = Color(h.habit.color)
    val done = h.isDoneOn(today)
    val tick = rememberTick()
    Row(
        Modifier.fillMaxWidth().clickable { if (!done && !h.habit.isNumeric) tick(); onClick() }.padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StrengthRing(h.strength, color, HabitIcons.of(h.habit.icon), size = 36)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(h.habit.name, style = MaterialTheme.typography.bodyLarge)
            if (note != null) {
                Text(
                    note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        IconButton(onClick = onNote) {
            Icon(
                if (note != null) Icons.Filled.EditNote else Icons.Outlined.EditNote,
                contentDescription = if (note != null) "Modifier la note : ${h.habit.name}" else "Note du jour : ${h.habit.name}",
                tint = if (note != null) color else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (h.habit.isNumeric) {
            Text(
                "${formatValue(h.valueOn(today))} / ${formatValue(h.habit.target)} ${h.habit.unit}",
                color = if (done) color else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (done) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (done) color else Color.Transparent)
                    .border(2.dp, if (done) color else MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .semantics { contentDescription = if (done) "${h.habit.name} : fait" else "Cocher ${h.habit.name}" },
                contentAlignment = Alignment.Center,
            ) {
                if (done) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/** Anneau de progression du jour et message encourageant. */
@Composable
private fun ProgressCard(done: Int, total: Int) {
    val target = if (total == 0) 0f else done.toFloat() / total
    val progress by animateFloatAsState(target, label = "progress")
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(72.dp),
                    strokeWidth = 8.dp,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                )
                Text("$done/$total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    encouragement(done, total),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (total > 0 && done < total) {
                    val left = total - done
                    Text(
                        if (left == 1) "Plus qu'une tâche." else "Encore $left tâches.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

internal fun encouragement(done: Int, total: Int): String = when {
    total == 0 -> "Rien de prévu. Ajoute une petite tâche pour bien démarrer !"
    done == 0 -> "Un petit pas suffit pour commencer."
    done == total -> "Tout est fait, bravo ! 🎉"
    done * 2 >= total -> "Bien joué, déjà $done sur $total !"
    else -> "C'est parti, continue comme ça !"
}
