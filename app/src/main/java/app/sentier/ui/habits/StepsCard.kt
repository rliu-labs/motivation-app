/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.habits

import app.sentier.domain.tr
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import app.sentier.data.StepSource
import app.sentier.domain.letterFr
import app.sentier.steps.HealthConnectSource
import app.sentier.steps.StepsState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import java.text.NumberFormat
import java.util.Locale

private val FrNumber: NumberFormat get() = NumberFormat.getIntegerInstance(app.sentier.domain.Lang.locale)
fun formatSteps(n: Long): String = FrNumber.format(n)
fun formatKm(km: Double): String = String.format(app.sentier.domain.Lang.locale, if (km < 10) "%.1f km" else "%.0f km", km)

/** Carte des pas : anneau d'objectif, distance, 7 derniers jours. */
@Composable
fun StepsCard(
    state: StepsState,
    onUseHealthConnect: () -> Unit,
    onUseSensor: () -> Unit,
    onSetGoal: (Int, Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editGoal by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.connected) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StepsRing(state, size = 96)
                    Spacer(Modifier.width(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            tr("${formatSteps(state.todaySteps)} pas", "${formatSteps(state.todaySteps)} steps"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(tr("${formatKm(state.todayKm)} aujourd'hui", "${formatKm(state.todayKm)} today"), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (state.goalReached) tr("Objectif atteint, bravo ! 🎉", "Goal reached, well done! 🎉")
                            else tr("Encore ${formatSteps(state.settings.goal - state.todaySteps)} pas pour l'objectif", "${formatSteps(state.settings.goal - state.todaySteps)} steps to go"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        TextButton(onClick = { editGoal = true }, modifier = Modifier.padding(start = 0.dp)) {
                            Text(
                                (if (state.settings.adaptive) tr("Objectif adaptatif : ", "Adaptive goal: ") else tr("Objectif : ", "Goal: ")) +
                                    tr("${formatSteps(state.settings.goal.toLong())} pas", "${formatSteps(state.settings.goal.toLong())} steps")
                            )
                        }
                    }
                }
                WeekBars(state)
                Text(
                    tr("Distance totale parcourue : ${formatKm(state.totalKm)}", "Total distance walked: ${formatKm(state.totalKm)}"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                if (state.settings.source == StepSource.HEALTH_CONNECT && state.week.all { it.second == 0L }) {
                    Text(
                        tr("Aucun pas trouvé dans Health Connect. Vérifie qu'une appli (Google Fit, Samsung Health…) y enregistre tes pas, ou utilise le capteur du téléphone.", "No steps found in Health Connect. Check that an app (Google Fit, Samsung Health…) records your steps there, or use the phone's sensor."),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (state.availability.sensorAvailable) {
                        OutlinedButton(onClick = onUseSensor) { Text(tr("Utiliser le capteur du téléphone", "Use the phone's sensor")) }
                    }
                }
            } else {
                ConnectSteps(state, onUseHealthConnect, onUseSensor)
            }
        }
    }
    if (editGoal) {
        GoalDialog(
            goal = state.settings.goal,
            stepLengthCm = state.settings.stepLengthCm,
            adaptive = state.settings.adaptive,
            onDismiss = { editGoal = false },
            onSave = { g, l, a -> onSetGoal(g, l, a); editGoal = false },
        )
    }
}

@Composable
fun StepsRing(state: StepsState, size: Int, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(state.progress, label = "steps")
    Box(modifier.size(size.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(size.dp),
            strokeWidth = (size / 10).dp,
            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        )
        Icon(
            Icons.AutoMirrored.Filled.DirectionsWalk,
            contentDescription = null,
            modifier = Modifier.size((size / 3).dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun WeekBars(state: StepsState) {
    val max = maxOf(state.settings.goal.toLong(), state.week.maxOfOrNull { it.second } ?: 0L).coerceAtLeast(1L)
    Row(
        Modifier.fillMaxWidth().height(90.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        for ((day, n) in state.week) {
            val reached = n >= state.settings.goal
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxHeight()) {
                Box(Modifier.weight(1f).width(18.dp), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight((n.toFloat() / max).coerceIn(0.03f, 1f))
                            .background(
                                if (reached) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                            )
                    )
                }
                Text(
                    day.dayOfWeek.letterFr(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (day == state.today) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun ConnectSteps(state: StepsState, onUseHealthConnect: () -> Unit, onUseSensor: () -> Unit) {
    val context = LocalContext.current
    val hcLauncher = rememberLauncherForActivityResult(PermissionController.createRequestPermissionResultContract()) {
        onUseHealthConnect()
    }
    val sensorLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onUseSensor()
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(12.dp))
        Text(tr("Compte tes pas", "Count your steps"), style = MaterialTheme.typography.titleLarge)
    }
    val hc = state.availability.healthConnect
    Text(
        when {
            state.settings.source == StepSource.HEALTH_CONNECT ->
                tr("Sentier n'a pas encore l'autorisation de lire tes pas dans Health Connect.", "Sentier doesn't have permission to read your steps in Health Connect yet.")
            hc == HealthConnectSource.Status.AVAILABLE ->
                tr("Sentier lit tes pas dans Health Connect, l'espace santé d'Android. Rien ne quitte ton téléphone.", "Sentier reads your steps in Health Connect, Android's health space. Nothing leaves your phone.")
            hc == HealthConnectSource.Status.NEEDS_UPDATE ->
                tr("Health Connect doit être mis à jour pour que Sentier puisse lire tes pas.", "Health Connect needs an update so Sentier can read your steps.")
            else -> tr("Health Connect n'est pas installé. Tu peux l'installer, ou utiliser directement le capteur de pas du téléphone.", "Health Connect isn't installed. You can install it, or use the phone's step sensor directly.")
        },
        style = MaterialTheme.typography.bodyMedium,
    )
    when (hc) {
        HealthConnectSource.Status.AVAILABLE ->
            Button(onClick = { hcLauncher.launch(HealthConnectSource.PERMISSIONS) }) {
                Text(if (state.settings.source == StepSource.HEALTH_CONNECT) tr("Autoriser l'accès aux pas", "Allow access to steps") else tr("Connecter Health Connect", "Connect Health Connect"))
            }
        else -> if (state.availability.checked) {
            Button(onClick = { openHealthConnectStore(context) }) {
                Text(if (hc == HealthConnectSource.Status.NEEDS_UPDATE) tr("Mettre à jour Health Connect", "Update Health Connect") else tr("Installer Health Connect", "Install Health Connect"))
            }
        }
    }
    if (state.availability.sensorAvailable) {
        OutlinedButton(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                sensorLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            } else {
                onUseSensor()
            }
        }) { Text(tr("Utiliser le capteur du téléphone", "Use the phone's sensor")) }
    }
}

private fun openHealthConnectStore(context: Context) {
    val pkg = HealthConnectSource.PROVIDER_PACKAGE
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg&url=healthconnect%3A%2F%2Fonboarding"))
    try {
        context.startActivity(market)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))
    }
}

@Composable
private fun GoalDialog(goal: Int, stepLengthCm: Int, adaptive: Boolean, onDismiss: () -> Unit, onSave: (Int, Int, Boolean) -> Unit) {
    var g by remember { mutableIntStateOf(goal) }
    var l by remember { mutableIntStateOf(stepLengthCm) }
    var a by remember { mutableStateOf(adaptive) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Objectif de pas", "Step goal")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(tr("Objectif adaptatif", "Adaptive goal"))
                        Text(
                            tr("Chaque jour, l'objectif suit tes pas des 2 dernières semaines, plus 10 %, pour rester atteignable.", "Every day, the goal follows your steps of the last 2 weeks, plus 10%, so it stays within reach."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = a, onCheckedChange = { a = it })
                }
                if (!a) {
                    Stepper(tr("Pas par jour", "Steps per day"), formatSteps(g.toLong()), onMinus = { g = (g - 500).coerceAtLeast(500) }, onPlus = { g = (g + 500).coerceAtMost(50_000) })
                }
                Stepper(tr("Longueur d'un pas", "Step length"), "$l cm", onMinus = { l = (l - 5).coerceAtLeast(30) }, onPlus = { l = (l + 5).coerceAtMost(150) })
                Text(
                    tr("La longueur du pas sert à calculer les kilomètres parcourus.", "Step length is used to compute the distance walked."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(g, l, a) }) { Text(tr("Enregistrer", "Save")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr("Annuler", "Cancel")) } },
    )
}

@Composable
fun Stepper(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        IconButton(onClick = onMinus) { Icon(Icons.Filled.Remove, tr("Moins", "Less")) }
        Text(value, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onPlus) { Icon(Icons.Filled.Add, tr("Plus", "More")) }
    }
}

/** Version compacte pour l'écran Aujourd'hui. */
@Composable
fun StepsMiniCard(state: StepsState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            StepsRing(state, size = 52)
            Spacer(Modifier.width(16.dp))
            Column {
                if (state.connected) {
                    Text(
                        tr("${formatSteps(state.todaySteps)} / ${formatSteps(state.settings.goal.toLong())} pas", "${formatSteps(state.todaySteps)} / ${formatSteps(state.settings.goal.toLong())} steps"),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        if (state.goalReached) tr("Objectif atteint !", "Goal reached!") else formatKm(state.todayKm),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Text(tr("Compte tes pas", "Count your steps"), style = MaterialTheme.typography.titleMedium)
                    Text(tr("Touche ici pour connecter le podomètre", "Tap here to connect the pedometer"), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
