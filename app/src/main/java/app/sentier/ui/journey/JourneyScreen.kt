/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.journey

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sentier.data.JourneyState
import app.sentier.domain.Stop
import app.sentier.domain.Treasure
import app.sentier.domain.WorldTour
import app.sentier.ui.common.SectionHeader
import java.text.NumberFormat
import java.util.Locale

@Composable
fun JourneyScreen(modifier: Modifier = Modifier, vm: JourneyViewModel = viewModel(factory = JourneyViewModel.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    val treasures by vm.treasures.collectAsStateWithLifecycle()
    var opened by remember { mutableStateOf<Stop?>(null) }
    var openedTreasure by remember { mutableStateOf<Treasure?>(null) }

    LifecycleResumeEffect(vm) {
        vm.refresh()
        onPauseOrDispose { }
    }

    val p = state.progress
    LazyColumn(modifier.fillMaxSize()) {
        item {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)) {
                Text("Tour du monde", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Chaque pas te rapproche de la prochaine escale.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { ProgressCard(state) }
        item {
            SectionHeader(if (p.currentIndex == 0 && !p.done) "Point de départ" else "Dernière escale")
            StopCard(p.current, Modifier.padding(horizontal = 16.dp))
        }
        item {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp)) {
                Text("Carnet de voyage", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    "Les souvenirs rapportés de chaque escale, et les trésors rares gagnés avec tes niveaux, tes séries, tes badges et tes défis.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            val owned = p.reached.size
            SectionHeader("Souvenirs ($owned sur ${WorldTour.stops.size})")
            SouvenirGrid(p.currentIndex, onOpen = { opened = it })
        }
        item {
            SectionHeader("Trésors rares (${treasures.count { it.owned }} sur ${treasures.size})")
            TreasureGrid(treasures, onOpen = { openedTreasure = it })
        }
        item { SectionHeader("Itinéraire") }
        itemsIndexed(WorldTour.stops, key = { i, _ -> "stop-$i" }) { i, stop ->
            val reached = i <= p.currentIndex
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
                headlineContent = { Text(stop.city) },
                supportingContent = {
                    Text(
                        when {
                            reached -> "${stop.country} · ${stop.souvenir.name}"
                            isNext -> "${stop.country} · dans ${formatKm(p.kmToNext)} km"
                            else -> stop.country
                        }
                    )
                },
                trailingContent = { Text("${formatKm(stop.km)} km", style = MaterialTheme.typography.labelMedium) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                modifier = if (reached) Modifier.clickable { opened = stop } else Modifier,
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }

    openedTreasure?.let { t ->
        TreasureDialog(t, title = t.name, footer = null, onDismiss = { openedTreasure = null })
    }

    opened?.let { stop ->
        StopDialog(stop, title = "${stop.city}, ${stop.country}", footer = null, onDismiss = { opened = null })
    }

    if (state.unseen.isNotEmpty() && opened == null && openedTreasure == null) {
        val latest = state.unseen.last().stop
        val n = state.unseen.size
        val xp = WorldTour.STOP_XP * n
        val footer = if (n > 1) "+$xp XP, +${xp / 5} or pour $n escales et leurs souvenirs" else "+$xp XP, +${xp / 5} or"
        StopDialog(latest, title = "Escale : ${latest.city}", footer = footer, onDismiss = { vm.dismissNews() })
    }
}

@Composable
private fun ProgressCard(state: JourneyState) {
    val p = state.progress
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        WorldMap(p, Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 12.dp))
        Column(Modifier.padding(16.dp)) {
            LinearProgressIndicator(
                progress = { p.fraction },
                strokeCap = StrokeCap.Round,
                modifier = Modifier.fillMaxWidth().height(8.dp),
            )
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${formatKm(p.km)} km sur ${formatKm(WorldTour.lengthKm)}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "Escale ${p.currentIndex} sur ${WorldTour.stops.lastIndex}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val next = p.nextStop
            Text(
                when {
                    p.done -> "Tour du monde bouclé : ${formatKm(p.km)} km parcourus. Chapeau !"
                    p.km == 0.0 -> "Active le comptage des pas dans l'onglet Habitudes pour te mettre en route."
                    next != null -> "Prochaine escale : ${next.city} (${next.country}), dans ${formatKm(p.kmToNext)} km, " +
                        "environ ${formatSteps(p.kmToNext, state.stepLengthCm)} pas."
                    else -> ""
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** Fiche d'une escale : architecture, culture, anecdote et souvenir. */
@Composable
private fun StopCard(stop: Stop, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            StopHeader(stop)
            StopDetails(stop, Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun StopHeader(stop: Stop) {
    Text(stop.country.uppercase(Locale.FRANCE), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    Text(stop.city, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun StopDetails(stop: Stop, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Topic(Icons.Filled.AccountBalance, "Architecture", stop.architecture)
        Topic(Icons.Filled.Celebration, "Culture", stop.culture)
        Topic(Icons.Filled.Lightbulb, "Le savais-tu ?", stop.anecdote)
        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            SouvenirBadge(stop, owned = true, size = 44)
            Column(Modifier.padding(start = 12.dp)) {
                Text("Souvenir : ${stop.souvenir.name}", style = MaterialTheme.typography.titleSmall)
                Text(stop.souvenir.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Topic(icon: ImageVector, title: String, text: String) {
    Row {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Column(Modifier.padding(start = 10.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SouvenirBadge(stop: Stop, owned: Boolean, size: Int) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(if (owned) colors.primaryContainer else colors.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (owned) SouvenirIcons.of(stop.souvenir.icon) else Icons.Filled.Lock,
            contentDescription = null,
            tint = if (owned) colors.onPrimaryContainer else colors.outline,
            modifier = Modifier.size((size * 0.5).dp),
        )
    }
}

/** Les souvenirs, 4 par ligne : ceux rapportés, et ceux qui attendent (avec leur pays). */
@Composable
private fun SouvenirGrid(currentIndex: Int, onOpen: (Stop) -> Unit) {
    Column(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WorldTour.stops.withIndex().toList().chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { (i, stop) ->
                    val owned = i <= currentIndex
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(MaterialTheme.shapes.medium)
                            .then(if (owned) Modifier.clickable { onOpen(stop) } else Modifier)
                            .padding(vertical = 4.dp)
                            .semantics { contentDescription = if (owned) "Souvenir : ${stop.souvenir.name}" else "Souvenir à découvrir : ${stop.country}" },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        SouvenirBadge(stop, owned, size = 48)
                        Text(
                            if (owned) stop.souvenir.name else stop.country,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (owned) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp, start = 2.dp, end = 2.dp),
                        )
                    }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun StopDialog(stop: Stop, title: String, footer: String?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Place, contentDescription = null) },
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                StopDetails(stop)
                footer?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(if (footer != null) "Continuer" else "Fermer") } },
    )
}

/** Kilomètres à la française, sans décimale au-delà de 100 km (ex. « 3,2 », « 245 »). */
fun formatKm(km: Double): String =
    if (km >= 100 || km == Math.floor(km)) "%.0f".format(Locale.FRANCE, km) else "%.1f".format(Locale.FRANCE, km)

private fun formatSteps(km: Double, stepLengthCm: Int): String =
    NumberFormat.getIntegerInstance(Locale.FRANCE).format(Math.round(km * 100_000 / stepLengthCm.coerceAtLeast(1)))
