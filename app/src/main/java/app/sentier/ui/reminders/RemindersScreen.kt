/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.reminders

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sentier.notify.Notifier
import app.sentier.notify.ReminderContent
import app.sentier.notify.ReminderKind

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(onBack: () -> Unit, vm: RemindersViewModel = viewModel(factory = RemindersViewModel.Factory)) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val preview by vm.preview.collectAsStateWithLifecycle()
    var canPost by remember { mutableStateOf(Notifier.canPost(context)) }
    var picking by remember { mutableStateOf<ReminderKind?>(null) }

    LifecycleResumeEffect(Unit) {
        canPost = Notifier.canPost(context)
        onPauseOrDispose { }
    }

    fun openSystemSettings() {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        canPost = Notifier.canPost(context)
        if (!granted) vm.showHint("Sans autorisation, Android bloque les rappels. Tu peux l'accorder dans les réglages.")
    }

    fun askIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && !canPost) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rappels") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Un petit signe le matin pour lancer la journée, un autre le soir pour ne rien oublier. " +
                    "Les rappels sont calculés sur ton téléphone, rien n'est envoyé ailleurs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            )

            ReminderCard(
                title = "Le matin",
                description = "Le programme de la journée : tâches et habitudes à faire.",
                on = settings.morningOn,
                minute = settings.morningMinute,
                onToggle = { on -> vm.update(settings.copy(morningOn = on)); if (on) askIfNeeded() },
                onPickTime = { picking = ReminderKind.MORNING },
            )
            ReminderCard(
                title = "Le soir",
                description = "Ce qui reste à faire, avec un bouton « Fait » pour valider sans ouvrir l'appli. " +
                    "Si tout est fait, Sentier ne dit rien.",
                on = settings.eveningOn,
                minute = settings.eveningMinute,
                onToggle = { on -> vm.update(settings.copy(eveningOn = on)); if (on) askIfNeeded() },
                onPickTime = { picking = ReminderKind.EVENING },
            )

            if (settings.anyOn && !canPost) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.NotificationsOff, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text(
                                "Les notifications de Sentier sont bloquées",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        Button(
                            onClick = { if (Build.VERSION.SDK_INT >= 33) askIfNeeded() else openSystemSettings() },
                            modifier = Modifier.padding(top = 8.dp),
                        ) { Text("Autoriser") }
                        TextButton(onClick = ::openSystemSettings) { Text("Ouvrir les réglages d'Android") }
                    }
                }
            }

            OutlinedButton(
                onClick = { vm.sendPreview(); askIfNeeded() },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("M'envoyer le rappel du soir maintenant") }
            preview?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    picking?.let { kind ->
        val minute = if (kind == ReminderKind.MORNING) settings.morningMinute else settings.eveningMinute
        TimeDialog(
            initial = minute,
            onDismiss = { picking = null },
            onConfirm = { m ->
                vm.update(
                    if (kind == ReminderKind.MORNING) settings.copy(morningMinute = m, morningOn = true)
                    else settings.copy(eveningMinute = m, eveningOn = true)
                )
                askIfNeeded()
                picking = null
            },
        )
    }
}

@Composable
private fun ReminderCard(
    title: String,
    description: String,
    on: Boolean,
    minute: Int,
    onToggle: (Boolean) -> Unit,
    onPickTime: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(
                    checked = on,
                    onCheckedChange = onToggle,
                    modifier = Modifier.semantics { contentDescription = "Rappel : ${title.lowercase()}" },
                )
            }
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(
                Modifier.padding(top = 8.dp).clickable(onClick = onPickTime).padding(vertical = 8.dp),
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimeDialog(initial: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = initial / 60, initialMinute = initial % 60, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Heure du rappel") },
        text = { TimePicker(state = state) },
        confirmButton = { TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}

