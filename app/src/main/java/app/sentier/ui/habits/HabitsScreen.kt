/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import app.sentier.ui.common.rememberTick
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sentier.data.Habit
import app.sentier.data.HabitWithStats
import app.sentier.domain.HabitTemplates
import app.sentier.domain.describeFrequency
import app.sentier.domain.letterFr
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HabitsScreen(
    onOpenHabit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    vm: HabitsViewModel = viewModel(factory = HabitsViewModel.Factory),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var editing by remember { mutableStateOf<Pair<Habit, LocalDate>?>(null) }

    LifecycleResumeEffect(vm) {
        vm.refreshSteps()
        onPauseOrDispose { }
    }

    Box(modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                Text(
                    "Habitudes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
                )
            }
            item {
                StepsCard(
                    state = state.steps,
                    onUseHealthConnect = { vm.useHealthConnect(context) },
                    onUseSensor = { vm.useSensor(context) },
                    onSetGoal = vm::setGoal,
                )
            }
            if (!state.loading && state.habits.isEmpty()) {
                item { EmptyHabits(onPick = vm::addTemplate) }
            } else if (state.habits.isNotEmpty()) {
                item { DaysHeader(state.days, state.today) }
                items(state.habits, key = { it.habit.id }) { h ->
                    HabitRow(
                        h = h,
                        days = state.days,
                        onOpen = { onOpenHabit(h.habit.id) },
                        onCell = { day ->
                            if (h.habit.isNumeric) editing = h.habit to day else vm.toggle(h.habit, day)
                        },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
            item { Spacer(Modifier.size(96.dp)) }
        }
        ExtendedFloatingActionButton(
            onClick = { onOpenHabit(0L) },
            // Le texte d'un bouton étendu est masqué aux lecteurs d'écran : la description porte le libellé.
            icon = { Icon(Icons.Filled.Add, contentDescription = "Nouvelle habitude") },
            text = { Text("Nouvelle habitude") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    editing?.let { (habit, day) ->
        val current = state.habits.firstOrNull { it.habit.id == habit.id }?.valueOn(day.toEpochDay()) ?: 0.0
        NumericValueDialog(
            habit = habit,
            day = day,
            today = state.today,
            initial = current,
            onDismiss = { editing = null },
            onSave = { v -> vm.setValue(habit, day, v); editing = null },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmptyHabits(onPick: (app.sentier.domain.HabitTemplate) -> Unit) {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ajoute ta première habitude", style = MaterialTheme.typography.titleMedium)
        Text(
            "Commence par une ou deux. Touche un modèle pour l'ajouter, ou crée la tienne avec le bouton en bas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (t in HabitTemplates) {
                AssistChip(
                    onClick = { onPick(t) },
                    label = { Text(t.name) },
                    leadingIcon = { Icon(HabitIcons.of(t.icon), null, tint = Color(t.color), modifier = Modifier.size(18.dp)) },
                )
            }
        }
    }
}

private val CellWidth = 44.dp

@Composable
private fun DaysHeader(days: List<LocalDate>, today: LocalDate) {
    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 16.dp), verticalAlignment = Alignment.Bottom) {
        Text("Mes habitudes", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
        for (d in days) {
            Column(Modifier.width(CellWidth), horizontalAlignment = Alignment.CenterHorizontally) {
                val bold = if (d == today) FontWeight.Bold else FontWeight.Normal
                Text(d.dayOfWeek.letterFr(), style = MaterialTheme.typography.labelSmall, fontWeight = bold)
                Text("${d.dayOfMonth}", style = MaterialTheme.typography.labelSmall, fontWeight = bold)
            }
        }
    }
}

@Composable
private fun HabitRow(
    h: HabitWithStats,
    days: List<LocalDate>,
    onOpen: () -> Unit,
    onCell: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = Color(h.habit.color)
    val tick = rememberTick()
    Row(
        modifier.fillMaxWidth().clickable(onClick = onOpen).padding(start = 16.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StrengthRing(h.strength, color, HabitIcons.of(h.habit.icon))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(h.habit.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                subtitle(h.habit),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        for (d in days) {
            HabitCell(h, d, color, onClick = { if (!h.habit.isNumeric && !h.isDoneOn(d.toEpochDay())) tick(); onCell(d) })
        }
    }
}

fun subtitle(habit: Habit): String {
    val freq = describeFrequency(habit.freqNum, habit.freqDen)
    return if (habit.isNumeric) "${formatValue(habit.target)} ${habit.unit} · $freq".replace("  ", " ") else freq
}

fun formatValue(v: Double): String =
    if (v == v.roundToInt().toDouble()) v.roundToInt().toString() else String.format(Locale.FRANCE, "%.1f", v)

/** Anneau de force de l'habitude (à la Loop), avec son icône. */
@Composable
fun StrengthRing(strength: Double, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, size: Int = 40) {
    Box(
        Modifier.size(size.dp).semantics { contentDescription = "Force ${(strength * 100).roundToInt()} %" },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { strength.toFloat() },
            modifier = Modifier.size(size.dp),
            color = color,
            strokeWidth = 4.dp,
            trackColor = color.copy(alpha = 0.18f),
        )
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size((size / 2).dp))
    }
}

@Composable
private fun HabitCell(h: HabitWithStats, day: LocalDate, color: Color, onClick: () -> Unit) {
    val epoch = day.toEpochDay()
    val done = h.isDoneOn(epoch)
    val value = h.valueOn(epoch)
    val label = "${h.habit.name}, ${day.dayOfMonth}"
    Box(
        Modifier
            .size(CellWidth)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = if (done) "$label : fait" else "$label : à faire" }
            .testTag("cell-${h.habit.id}-$epoch"),
        contentAlignment = Alignment.Center,
    ) {
        if (h.habit.isNumeric) {
            Text(
                if (value > 0) formatValue(value) else "·",
                color = if (done) color else if (value > 0) color.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline,
                fontWeight = if (done) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else if (done) {
            Box(Modifier.size(30.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        } else {
            Box(
                Modifier.size(30.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Close, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(14.dp))
            }
        }
    }
}

/** Saisie d'une valeur chiffrée (ex. nombre de verres d'eau). */
@Composable
fun NumericValueDialog(
    habit: Habit,
    day: LocalDate,
    today: LocalDate,
    initial: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit,
) {
    var text by remember { mutableStateOf(if (initial > 0) formatValue(initial) else "") }
    val value = text.replace(',', '.').toDoubleOrNull() ?: 0.0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(habit.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (day == today) "Aujourd'hui" else app.sentier.ui.common.friendlyDate(day, today),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Stepper(
                    label = habit.unit.ifBlank { "Valeur" },
                    value = formatValue(value),
                    onMinus = { text = formatValue((value - 1).coerceAtLeast(0.0)) },
                    onPlus = { text = formatValue(value + 1) },
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Objectif : ${formatValue(habit.target)} ${habit.unit}") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.testTag("habitValue"),
                )
                TextButton(onClick = { text = formatValue(habit.target) }) { Text("Objectif atteint") }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(value) }) { Text("Enregistrer") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}
