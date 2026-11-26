/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.journey

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sentier.data.JourneyState
import app.sentier.domain.Journeys
import app.sentier.ui.common.SectionHeader
import java.text.NumberFormat
import java.util.Locale

@Composable
fun JourneyScreen(modifier: Modifier = Modifier, vm: JourneyViewModel = viewModel(factory = JourneyViewModel.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(vm) {
        vm.refresh()
        onPauseOrDispose { }
    }

    val p = state.progress
    LazyColumn(modifier.fillMaxSize()) {
        item {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)) {
                Text("Voyage", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Tes pas te font avancer d'étape en étape.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { ProgressCard(state) }
        item { SectionHeader("Étapes") }
        itemsIndexed(p.journey.stops, key = { i, _ -> "stop-$i" }) { i, stop ->
            val reached = stop.km <= p.km
            val isNext = stop == p.nextStop
            ListItem(
                leadingContent = {
                    Icon(
                        when {
                            reached -> Icons.Filled.CheckCircle
                            isNext -> Icons.Filled.Place
                            else -> Icons.Filled.Lock
                        },
                        contentDescription = null,
                        tint = when {
                            reached -> MaterialTheme.colorScheme.primary
                            isNext -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.outline
                        },
                    )
                },
                headlineContent = {
                    Text(stop.name, color = if (reached || isNext) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                },
                supportingContent = {
                    Text(
                        when {
                            reached && i == 0 -> "Départ. ${stop.fact}"
                            reached -> stop.fact
                            isNext -> "Dans ${formatKm(p.kmToNext)} km"
                            else -> "À ${formatKm(stop.km)} km du départ"
                        }
                    )
                },
                trailingContent = { Text("${formatKm(stop.km)} km", style = MaterialTheme.typography.labelMedium) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
            )
        }
        item { SectionHeader("Tous les voyages") }
        itemsIndexed(Journeys.all, key = { _, j -> "journey-${j.id}" }) { i, journey ->
            val status = when {
                p.allDone || i < p.journeyIndex -> "Terminé"
                i == p.journeyIndex -> "En cours"
                else -> "À venir"
            }
            ListItem(
                leadingContent = {
                    Icon(
                        if (status == "Terminé") Icons.Filled.Flag else Icons.Outlined.Explore,
                        contentDescription = null,
                        tint = if (status == "À venir") MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                    )
                },
                headlineContent = { Text(journey.title) },
                supportingContent = { Text("${journey.from} → ${journey.to} · ${formatKm(journey.lengthKm)} km") },
                trailingContent = { Text(status, style = MaterialTheme.typography.labelMedium) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }

    if (state.unseen.isNotEmpty()) {
        val latest = state.unseen.last()
        val others = state.unseen.size - 1
        val gold = Journeys.STOP_XP / 5
        AlertDialog(
            onDismissRequest = { vm.dismissNews() },
            icon = { Icon(Icons.Filled.Flag, contentDescription = null) },
            title = { Text("Étape atteinte : ${latest.stop.name}") },
            text = {
                Column {
                    Text(latest.stop.fact)
                    Text(
                        if (others > 0) "+${Journeys.STOP_XP * state.unseen.size} XP, +${gold * state.unseen.size} or pour ${state.unseen.size} étapes"
                        else "+${Journeys.STOP_XP} XP, +$gold or",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            },
            confirmButton = { TextButton(onClick = { vm.dismissNews() }) { Text("Continuer") } },
        )
    }
}

@Composable
private fun ProgressCard(state: JourneyState) {
    val p = state.progress
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(p.journey.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "${p.journey.from} → ${p.journey.to} · ${p.journey.region}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            JourneyMap(p.journey, p.km, Modifier.fillMaxWidth().height(260.dp).padding(vertical = 8.dp))
            LinearProgressIndicator(
                progress = { p.fraction },
                strokeCap = StrokeCap.Round,
                modifier = Modifier.fillMaxWidth().height(8.dp),
            )
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${formatKm(p.km)} km sur ${formatKm(p.journey.lengthKm)}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "Voyage ${p.journeyIndex + 1} sur ${Journeys.all.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val next = p.nextStop
            Text(
                when {
                    p.allDone -> "Tous les voyages sont terminés : ${formatKm(p.totalKm)} km parcourus. Chapeau !"
                    p.totalKm == 0.0 -> "Active le comptage des pas dans l'onglet Habitudes pour te mettre en route."
                    next != null -> "Prochaine étape : ${next.name}, dans ${formatKm(p.kmToNext)} km " +
                        "(environ ${formatSteps(p.kmToNext, state.stepLengthCm)} pas)."
                    else -> ""
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
    Spacer(Modifier.size(4.dp))
}

/** Kilomètres à la française, sans décimale au-delà de 100 km (ex. « 3,2 », « 245 »). */
fun formatKm(km: Double): String =
    if (km >= 100 || km == Math.floor(km)) "%.0f".format(Locale.FRANCE, km) else "%.1f".format(Locale.FRANCE, km)

private fun formatSteps(km: Double, stepLengthCm: Int): String =
    NumberFormat.getIntegerInstance(Locale.FRANCE).format(Math.round(km * 100_000 / stepLengthCm.coerceAtLeast(1)))
