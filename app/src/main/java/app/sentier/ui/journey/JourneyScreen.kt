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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import app.sentier.domain.Journey
import app.sentier.domain.Journeys
import app.sentier.domain.Stop
import app.sentier.domain.TourProgress
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
    // Un voyage fini qu'on regarde à nouveau (null : le voyage en cours).
    var viewingId by rememberSaveable { mutableStateOf<String?>(null) }
    var choosing by remember { mutableStateOf(false) }

    LifecycleResumeEffect(vm) {
        vm.refresh()
        onPauseOrDispose { }
    }

    val viewing = Journeys.byId(viewingId)?.takeIf { it.id in state.done }
    val journey = viewing ?: state.journey
    val p = viewing?.progress(viewing.lengthKm) ?: state.progress
    LazyColumn(modifier.fillMaxSize()) {
        item {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)) {
                Text(journey.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (viewing != null) "Voyage terminé : retrouve ses escales et ses souvenirs."
                    else "Chaque pas te rapproche de la prochaine escale.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (viewing != null) {
                    TextButton(onClick = { viewingId = null }, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Revenir à « ${state.journey.title} »", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
        item { ProgressCard(p, state.stepLengthCm, next = if (viewing == null) state.next else null, onChoose = { choosing = true }) }
        item {
            SectionHeader(if (p.currentIndex == 0 && !p.done) "Point de départ" else "Dernière escale")
            StopCard(journey, p.currentIndex, p.current, Modifier.padding(horizontal = 16.dp))
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
            SectionHeader("Souvenirs ($owned sur ${journey.stops.size})")
            SouvenirGrid(journey, p.currentIndex, onOpen = { opened = it })
        }
        item {
            SectionHeader("Trésors rares (${treasures.count { it.owned }} sur ${treasures.size})")
            TreasureGrid(treasures, onOpen = { openedTreasure = it })
        }
        item { SectionHeader("Itinéraire") }
        itemsIndexed(journey.stops, key = { i, _ -> "stop-${journey.id}-$i" }) { i, stop ->
            val reached = i <= p.currentIndex
            val isNext = i == p.currentIndex + 1
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
        item { SectionHeader("Mes voyages") }
        items(Journeys.all, key = { "journey-${it.id}" }) { j ->
            JourneyRow(
                j, state,
                viewing = j.id == journey.id,
                onOpen = {
                    when {
                        j.id == state.journey.id -> viewingId = null
                        j.id in state.done -> viewingId = j.id
                        state.next.any { it.id == j.id } -> choosing = true
                    }
                },
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }

    openedTreasure?.let { t ->
        TreasureDialog(t, title = t.name, footer = null, onDismiss = { openedTreasure = null })
    }

    opened?.let { stop ->
        StopDialog(journey, stop, title = "${stop.city}, ${stop.country}", footer = null, onDismiss = { opened = null })
    }

    if (choosing && state.next.isNotEmpty()) {
        ChooseJourneyDialog(state.next, onChoose = { vm.start(it.id); viewingId = null; choosing = false }, onDismiss = { choosing = false })
    }

    if (state.unseen.isNotEmpty() && opened == null && openedTreasure == null && !choosing) {
        val latest = state.unseen.last()
        val n = state.unseen.size
        val xp = WorldTour.STOP_XP * n
        val gain = if (n > 1) "+$xp XP, +${xp / 5} or pour $n escales et leurs souvenirs" else "+$xp XP, +${xp / 5} or"
        val footer = if (latest.index == latest.journey.stops.lastIndex) {
            "$gain\n${latest.journey.title} bouclé ! Choisis ton prochain voyage sous la carte."
        } else gain
        StopDialog(latest.journey, latest.stop, title = "Escale : ${latest.stop.city}", footer = footer, onDismiss = { vm.dismissNews() })
    }
}

@Composable
private fun ProgressCard(p: TourProgress, stepLengthCm: Int, next: List<Journey>?, onChoose: () -> Unit) {
    val journey = p.journey
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        JourneyMap(p, Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 12.dp))
        Column(Modifier.padding(16.dp)) {
            LinearProgressIndicator(
                progress = { p.fraction },
                strokeCap = StrokeCap.Round,
                modifier = Modifier.fillMaxWidth().height(8.dp),
            )
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${formatKm(p.km.coerceAtMost(journey.lengthKm))} km sur ${formatKm(journey.lengthKm)}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "Escale ${p.currentIndex} sur ${p.stops.lastIndex}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val nextStop = p.nextStop
            Text(
                when {
                    p.done && next != null && next.isEmpty() ->
                        "${journey.title} bouclé. Tu as fait tous les voyages proposés pour l'instant : d'autres arrivent bientôt !"
                    p.done -> "${journey.title} bouclé : ${formatKm(journey.lengthKm)} km parcourus. Chapeau !"
                    p.km == 0.0 && journey.id == WorldTour.journey.id ->
                        "Active le comptage des pas dans l'onglet Habitudes pour te mettre en route."
                    nextStop != null -> "Prochaine escale : ${nextStop.city} (${nextStop.country}), dans ${formatKm(p.kmToNext)} km, " +
                        "environ ${formatSteps(p.kmToNext, stepLengthCm)} pas."
                    else -> ""
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (p.done && !next.isNullOrEmpty()) {
                Button(onClick = onChoose, modifier = Modifier.padding(top = 12.dp)) {
                    Icon(Icons.Filled.Flight, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Choisir le prochain voyage", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

/** Un voyage de la liste « Mes voyages » : fini, en cours, prêt à partir ou encore verrouillé. */
@Composable
private fun JourneyRow(j: Journey, state: JourneyState, viewing: Boolean, onOpen: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val current = j.id == state.journey.id
    val ready = state.next.any { it.id == j.id }
    val finished = state.isDone(j)
    val (icon, tint) = when {
        finished -> Icons.Filled.CheckCircle to colors.primary
        current || ready -> Icons.Filled.Place to colors.tertiary
        else -> Icons.Filled.Lock to colors.outline
    }
    val status = when {
        current && finished -> "Bouclé · ${formatKm(j.lengthKm)} km"
        current -> "En cours · ${formatKm(state.progress.km)} km sur ${formatKm(j.lengthKm)}"
        finished -> "Bouclé · ${j.stops.size - 1} souvenirs rapportés"
        ready -> "Prêt à partir · ${j.stops.size - 1} escales, ${formatKm(j.lengthKm)} km"
        else -> "Après : ${Journeys.prerequisite(j)?.title ?: "le voyage précédent"}"
    }
    ListItem(
        leadingContent = { Icon(icon, contentDescription = null, tint = tint) },
        headlineContent = { Text(j.title, fontWeight = if (viewing) FontWeight.SemiBold else null) },
        supportingContent = { Text(status) },
        colors = ListItemDefaults.colors(containerColor = if (viewing) colors.surfaceContainerLow else colors.background),
        modifier = if (current || finished || ready) Modifier.clickable(onClick = onOpen) else Modifier,
    )
}

/** Choix du voyage suivant, une fois le précédent bouclé. */
@Composable
private fun ChooseJourneyDialog(options: List<Journey>, onChoose: (Journey) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Flight, contentDescription = null) },
        title = { Text("Prochain voyage") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Les pas faits depuis la fin de ton dernier voyage comptent déjà pour le suivant.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                options.forEach { j ->
                    ListItem(
                        headlineContent = { Text(j.title, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("${j.tagline}\n${j.stops.size - 1} escales · ${formatKm(j.lengthKm)} km") },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onChoose(j) },
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Plus tard") } },
    )
}

/** Fiche d'une escale : architecture, culture, anecdote et souvenir. */
@Composable
private fun StopCard(journey: Journey, index: Int, stop: Stop, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier.fillMaxWidth(),
    ) {
        StopIllustration(journey, index)
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
private fun SouvenirGrid(journey: Journey, currentIndex: Int, onOpen: (Stop) -> Unit) {
    Column(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        journey.stops.withIndex().toList().chunked(4).forEach { row ->
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
private fun StopDialog(journey: Journey, stop: Stop, title: String, footer: String?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Place, contentDescription = null) },
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                StopIllustration(
                    journey,
                    journey.stops.indexOf(stop),
                    Modifier.padding(bottom = 12.dp).clip(MaterialTheme.shapes.medium),
                )
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
