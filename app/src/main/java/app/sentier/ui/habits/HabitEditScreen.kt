/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.habits

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import app.sentier.ui.common.DifficultySelector
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sentier.data.Habit
import app.sentier.domain.HabitTemplates
import app.sentier.notify.Notifier
import app.sentier.notify.ReminderContent
import app.sentier.ui.reminders.TimeDialog
import app.sentier.ui.theme.ListPalette
import java.time.LocalDate
import kotlin.math.roundToInt

/** Heure proposée pour un nouveau rappel d'habitude : 9 h. */
private const val DEFAULT_HABIT_REMINDER = 9 * 60

private enum class FreqMode(val label: String) { DAILY("Chaque jour"), WEEK("Par semaine"), MONTH("Par mois") }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitEditScreen(
    habitId: Long,
    onBack: () -> Unit,
    onOpenNote: (Long, LocalDate) -> Unit = { _, _ -> },
    vm: HabitEditViewModel = viewModel(key = "habit-$habitId", factory = HabitEditViewModel.factory(habitId)),
) {
    val draft by vm.draft.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val notes by vm.notes.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    var pickingTime by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var canPost by remember { mutableStateOf(Notifier.canPost(context)) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        canPost = Notifier.canPost(context)
    }
    fun askNotifications() {
        if (Build.VERSION.SDK_INT >= 33 && !Notifier.canPost(context)) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (vm.isNew) "Nouvelle habitude" else "Modifier l'habitude") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour") } },
                actions = {
                    if (!vm.isNew) {
                        IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, "Supprimer l'habitude") }
                    }
                },
            )
        },
    ) { padding ->
        val h = draft ?: return@Scaffold
        Column(
            Modifier.padding(padding).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            stats?.let { s ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        StrengthRing(s.strength, Color(s.habit.color), HabitIcons.of(s.habit.icon), size = 56)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Force : ${(s.strength * 100).roundToInt()} %", style = MaterialTheme.typography.titleMedium)
                            val count = s.values.count { (d, _) -> s.isDoneOn(d) }
                            Text(
                                if (count == 1) "Réalisée 1 fois" else "Réalisée $count fois",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                "Un jour raté fait baisser la force doucement, sans tout remettre à zéro.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            if (stats != null) {
                HabitJournal(notes, vm.today, onWrite = { day -> onOpenNote(habitId, day) })
            }

            if (vm.isNew) {
                Text("Modèles", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (t in HabitTemplates) {
                        AssistChip(
                            onClick = { vm.applyTemplate(t) },
                            label = { Text(t.name) },
                            leadingIcon = { Icon(HabitIcons.of(t.icon), null, tint = Color(t.color), modifier = Modifier.size(18.dp)) },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = h.name,
                onValueChange = { v -> vm.edit { it.copy(name = v) } },
                label = { Text("Nom") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("habitName"),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )

            Text("Type", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !h.isNumeric,
                    onClick = { vm.edit { it.copy(type = Habit.TYPE_BOOLEAN) } },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text("Oui / non") }
                SegmentedButton(
                    selected = h.isNumeric,
                    onClick = { vm.edit { it.copy(type = Habit.TYPE_NUMERIC, target = if (it.target <= 1.0) 8.0 else it.target) } },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text("Chiffrée") }
            }
            if (h.isNumeric) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    var targetText by remember(h.id, h.type) { mutableStateOf(formatValue(h.target)) }
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { v ->
                            targetText = v
                            v.replace(',', '.').toDoubleOrNull()?.let { t -> vm.edit { it.copy(target = t) } }
                        },
                        label = { Text("Objectif par jour") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = h.unit,
                        onValueChange = { v -> vm.edit { it.copy(unit = v) } },
                        label = { Text("Unité") },
                        placeholder = { Text("verres") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Text("Fréquence", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val mode = when {
                h.freqNum >= h.freqDen -> FreqMode.DAILY
                h.freqDen == 7 -> FreqMode.WEEK
                else -> FreqMode.MONTH
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (m in FreqMode.entries) {
                    FilterChip(
                        selected = m == mode,
                        onClick = {
                            vm.edit {
                                when (m) {
                                    FreqMode.DAILY -> it.copy(freqNum = 1, freqDen = 1)
                                    FreqMode.WEEK -> it.copy(freqNum = 3, freqDen = 7)
                                    FreqMode.MONTH -> it.copy(freqNum = 10, freqDen = 30)
                                }
                            }
                        },
                        label = { Text(m.label) },
                    )
                }
            }
            if (mode != FreqMode.DAILY) {
                val max = if (mode == FreqMode.WEEK) 6 else 29
                Stepper(
                    label = if (mode == FreqMode.WEEK) "Fois par semaine" else "Fois par mois",
                    value = "${h.freqNum}",
                    onMinus = { vm.edit { it.copy(freqNum = (it.freqNum - 1).coerceAtLeast(1)) } },
                    onPlus = { vm.edit { it.copy(freqNum = (it.freqNum + 1).coerceAtMost(max)) } },
                )
            }

            Text("Rappel", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Me le rappeler chaque jour", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Seulement si elle n'est pas encore faite.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = h.reminderMinute != null,
                    onCheckedChange = { on ->
                        vm.edit { it.copy(reminderMinute = if (on) DEFAULT_HABIT_REMINDER else null) }
                        if (on) askNotifications()
                    },
                    modifier = Modifier.semantics { contentDescription = "Rappel de l'habitude" },
                )
            }
            h.reminderMinute?.let { minute ->
                Row(
                    Modifier.clickable { pickingTime = true }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "À ${ReminderContent.formatMinute(minute)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                if (!canPost) {
                    Text(
                        "Les notifications de Sentier sont bloquées : autorise-les pour recevoir ce rappel.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Text("Difficulté", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            DifficultySelector(h.difficulty) { d -> vm.edit { it.copy(difficulty = d) } }

            Text("Couleur", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (c in ListPalette) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(c))
                            .then(if (c == h.color) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                            .clickable { vm.edit { it.copy(color = c) } }
                            .semantics { contentDescription = "Couleur" },
                    )
                }
            }

            Text("Icône", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for ((name, icon) in HabitIcons.all) {
                    val selected = name == h.icon
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (selected) Color(h.color).copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { vm.edit { it.copy(icon = name) } },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = name, tint = if (selected) Color(h.color) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Button(
                onClick = { vm.save(onBack) },
                enabled = h.name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            ) { Text("Enregistrer") }
            Spacer(Modifier.size(24.dp))
        }

        if (pickingTime) {
            TimeDialog(
                initial = h.reminderMinute ?: DEFAULT_HABIT_REMINDER,
                onDismiss = { pickingTime = false },
                onConfirm = { m ->
                    vm.edit { it.copy(reminderMinute = m) }
                    askNotifications()
                    pickingTime = false
                },
            )
        }

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text("Supprimer cette habitude ?") },
                text = { Text("« ${h.name} » et tout son historique seront supprimés.") },
                confirmButton = { TextButton(onClick = { confirmDelete = false; vm.delete(onBack) }) { Text("Supprimer") } },
                dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Garder") } },
            )
        }
    }
}
