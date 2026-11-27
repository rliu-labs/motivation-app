/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.journey

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.sentier.data.PassportRepository
import app.sentier.domain.PassportPage
import app.sentier.domain.PassportState
import app.sentier.domain.Stamp
import app.sentier.ui.common.app
import app.sentier.ui.common.longDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class PassportViewModel(passport: PassportRepository) : ViewModel() {
    val state: StateFlow<PassportState> = passport.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PassportState())

    companion object {
        val Factory = viewModelFactory { initializer { PassportViewModel(app().passport) } }
    }
}

/** Rouge bordeaux de la couverture et or des lettres. */
private val CoverColor = Color(0xFF6D1B2B)
private val CoverGold = Color(0xFFE8C46A)

/** Encres des tampons, choisies d'après le pays : lisibles sur fond clair comme sombre. */
private val Inks = listOf(
    Color(0xFF3F6FD8), Color(0xFFD84343), Color(0xFF2E9E5B), Color(0xFF8E5BD8),
    Color(0xFFD9822B), Color(0xFF1F9AAE), Color(0xFFC2457E), Color(0xFF7A8A2E),
)

fun inkOf(country: String): Color = Inks[abs(country.hashCode()) % Inks.size]

private val StampDate: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRANCE)

/** Accès au passeport depuis l'onglet Voyage. */
@Composable
fun PassportEntry(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(role = Role.Button, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CoverColor),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).border(2.dp, CoverGold, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Public, contentDescription = null, tint = CoverGold) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Mon passeport", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CoverGold)
                Text(
                    "Un tampon à chaque escale atteinte, tous voyages confondus",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = CoverGold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassportScreen(onBack: () -> Unit, vm: PassportViewModel = viewModel(factory = PassportViewModel.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    var opened by remember { mutableStateOf<Stamp?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon passeport") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour") } },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item { Cover(state) }
            if (!state.loading && state.stamps == 0) {
                item {
                    Text(
                        "Ton premier tampon t'attend à la prochaine escale. Marche, et le passeport se remplira page après page.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }
            }
            items(state.pages, key = { "page-${it.journey.id}" }) { page ->
                PageCard(page, number = state.pages.indexOf(page) + 1, onOpen = { opened = it })
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    opened?.let { s ->
        StopDialog(
            s.journey, s.stop,
            title = "${s.stop.city}, ${s.stop.country}",
            footer = s.day?.let { "Tamponné le ${longDate(LocalDate.ofEpochDay(it)).replaceFirstChar { c -> c.lowercase() }}" },
            onDismiss = { opened = null },
        )
    }
}

@Composable
private fun Cover(state: PassportState) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CoverColor),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "PASSEPORT DE VOYAGEUR",
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 3.sp,
                color = CoverGold,
            )
            Box(
                Modifier.size(72.dp).border(2.dp, CoverGold, CircleShape).padding(6.dp).border(1.dp, CoverGold.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Public, contentDescription = null, tint = CoverGold, modifier = Modifier.size(40.dp)) }
            Text("Sentier", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CoverGold)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                CoverStat(state.stamps, if (state.stamps > 1) "tampons" else "tampon")
                CoverStat(state.countries, "pays")
                CoverStat(state.journeysDone, if (state.journeysDone > 1) "voyages bouclés" else "voyage bouclé")
            }
            state.firstDay?.let {
                Text(
                    "Premier tampon le ${longDate(LocalDate.ofEpochDay(it)).replaceFirstChar { c -> c.lowercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun CoverStat(n: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "$n $label" },
    ) {
        Text("$n", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
    }
}

@Composable
private fun PageCard(page: PassportPage, number: Int, onOpen: (Stamp) -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "PAGE $number",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    page.journey.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${page.count} / ${page.stamps.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            LinearProgressIndicator(
                progress = { if (page.stamps.isEmpty()) 0f else page.count.toFloat() / page.stamps.size },
                strokeCap = StrokeCap.Round,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            )
            if (page.complete) {
                Text(
                    "Voyage bouclé : toutes les escales sont tamponnées.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            val next = page.stamps.firstOrNull { !it.stamped }
            for (row in page.stamps.chunked(3)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (s in row) {
                        StampView(s, isNext = s == next, onClick = { onOpen(s) }, modifier = Modifier.weight(1f))
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** Un tampon : rond ou rectangulaire, légèrement de travers, à l'encre du pays. */
@Composable
fun StampView(s: Stamp, isNext: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val ink = inkOf(s.stop.country)
    val round = s.index % 3 != 1
    // Un angle propre à chaque escale, toujours le même.
    val angle = ((abs((s.journey.id + s.index).hashCode()) % 25) - 12).toFloat()
    val outline = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .then(if (s.stamped) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .semantics(mergeDescendants = true) {
                contentDescription = if (s.stamped) {
                    "Tampon de ${s.stop.city}, ${s.stop.country}, le ${StampDate.format(LocalDate.ofEpochDay(s.day!!))}"
                } else {
                    "Escale à venir : ${s.stop.city}"
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (s.stamped) {
            Box(Modifier.fillMaxSize().rotate(angle), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = 2.5.dp.toPx()
                    if (round) {
                        val r = size.minDimension / 2 - w
                        drawCircle(ink, radius = r, style = Stroke(w))
                        drawCircle(ink, radius = r * 0.80f, style = Stroke(1.dp.toPx()))
                    } else {
                        val inset = size.minDimension * 0.08f
                        drawRoundRect(
                            ink, topLeft = Offset(inset, inset * 2.2f),
                            size = Size(size.width - 2 * inset, size.height - 4.4f * inset),
                            cornerRadius = CornerRadius(8.dp.toPx()), style = Stroke(w),
                        )
                        drawRoundRect(
                            ink, topLeft = Offset(inset * 1.9f, inset * 3.1f),
                            size = Size(size.width - 3.8f * inset, size.height - 6.2f * inset),
                            cornerRadius = CornerRadius(5.dp.toPx()), style = Stroke(1.dp.toPx()),
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 14.dp),
                ) {
                    Text(
                        s.stop.country.uppercase(Locale.FRANCE),
                        fontSize = 8.sp,
                        letterSpacing = 1.sp,
                        color = ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        s.stop.city,
                        fontSize = 12.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ink,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        StampDate.format(LocalDate.ofEpochDay(s.day!!)).uppercase(Locale.FRANCE),
                        fontSize = 8.sp,
                        color = ink,
                        maxLines = 1,
                    )
                }
            }
        } else {
            Canvas(Modifier.fillMaxSize()) {
                val w = 1.5.dp.toPx()
                drawCircle(
                    outline, radius = size.minDimension / 2 - w,
                    style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    s.stop.city,
                    fontSize = 11.sp,
                    lineHeight = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isNext) 0.9f else 0.55f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isNext) {
                    Text(
                        "prochaine",
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp),
                    )
                }
            }
        }
    }
}
