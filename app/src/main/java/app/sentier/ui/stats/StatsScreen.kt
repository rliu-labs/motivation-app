/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.stats

import app.sentier.domain.tr
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sentier.data.Stats
import app.sentier.data.StatsPeriod
import app.sentier.data.YearActivity
import app.sentier.ui.common.SectionHeader
import app.sentier.ui.progress.GoldColor
import java.text.NumberFormat
import java.util.Locale

private val Fr: NumberFormat get() = NumberFormat.getIntegerInstance(app.sentier.domain.Lang.locale)
private fun fmt(n: Long) = Fr.format(n)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onBack: () -> Unit, vm: StatsViewModel = viewModel(factory = StatsViewModel.Factory)) {
    val period by vm.period.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Statistiques", "Statistics")) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, tr("Retour", "Back")) } },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                StatsPeriod.entries.forEachIndexed { i, p ->
                    SegmentedButton(
                        selected = p == period,
                        onClick = { vm.select(p) },
                        shape = SegmentedButtonDefaults.itemShape(i, StatsPeriod.entries.size),
                    ) { Text(p.label) }
                }
            }
            StatsContent(stats, goal = vm.goal.collectAsStateWithLifecycle().value, year = vm.year.collectAsStateWithLifecycle().value)
        }
    }
}

@Composable
private fun StatsContent(s: Stats, goal: Int, year: YearActivity?) {
    val per = if (s.period == StatsPeriod.YEAR) tr("par mois", "per month") else tr("par jour", "per day")
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Tile(tr("Jours actifs", "Active days"), "${s.activeDays} / ${s.days}", Modifier.weight(1f))
        Tile(tr("Tâches", "Tasks"), fmt(s.tasksDone.toLong()), Modifier.weight(1f))
        Tile(tr("Habitudes", "Habits"), fmt(s.habitsDone.toLong()), Modifier.weight(1f))
        Tile("XP", fmt(s.xp.toLong()), Modifier.weight(1f))
    }

    if (year != null) {
        SectionHeader(tr("Ton année", "Your year"))
        Card(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            YearHeatmap(year, Modifier.padding(16.dp))
        }
    }

    SectionHeader(tr("Pas", "Steps"))
    ChartCard(
        summary = if (s.totalSteps == 0L) {
            tr("Aucun pas enregistré sur la période.", "No steps recorded over the period.")
        } else {
            tr("${fmt(s.totalSteps)} pas au total · ${fmt(s.averageSteps)} en moyenne par jour marché · record ${fmt(s.bestDaySteps)} · objectif atteint ${s.goalDays} fois", "${fmt(s.totalSteps)} steps in total · ${fmt(s.averageSteps)} on average per walking day · best ${fmt(s.bestDaySteps)} · goal reached ${s.goalDays} times")
        },
        values = s.buckets.map { it.steps.toFloat() },
        labels = s.buckets.map { it.label },
        color = MaterialTheme.colorScheme.primary,
        reference = if (s.period == StatsPeriod.YEAR) null else goal.toFloat(),
        description = tr("Graphique des pas $per", "Steps chart $per"),
    )

    SectionHeader(tr("Habitudes réussies", "Habits done"))
    ChartCard(
        summary = tr("${s.habitsDone} habitudes réussies sur la période.", "${s.habitsDone} habits done over the period."),
        values = s.buckets.map { it.habits.toFloat() },
        labels = s.buckets.map { it.label },
        color = MaterialTheme.colorScheme.tertiary,
        reference = null,
        description = tr("Graphique des habitudes réussies $per", "Habits done chart $per"),
    )
    if (s.habitRates.isNotEmpty()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (h in s.habitRates) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(Color(h.color)))
                    Text(h.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp).weight(1f))
                    Text("${Math.round(h.rate * 100)} %", style = MaterialTheme.typography.labelLarge)
                }
                LinearProgressIndicator(
                    progress = { h.rate },
                    color = Color(h.color),
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                )
            }
        }
    }

    SectionHeader(tr("Expérience gagnée", "Experience earned"))
    ChartCard(
        summary = tr("${fmt(s.xp.toLong())} XP et ${s.tasksDone} tâches terminées sur la période.", "${fmt(s.xp.toLong())} XP and ${s.tasksDone} tasks finished over the period."),
        values = s.buckets.map { it.xp.toFloat() },
        labels = s.buckets.map { it.label },
        color = GoldColor,
        reference = null,
        description = tr("Graphique de l'expérience gagnée $per", "Experience earned chart $per"),
    )
    androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))
}

@Composable
private fun Tile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

@Composable
private fun ChartCard(
    summary: String,
    values: List<Float>,
    labels: List<String>,
    color: Color,
    reference: Float?,
    description: String,
) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(summary, style = MaterialTheme.typography.bodyMedium)
            BarChart(values, labels, color, reference, Modifier.semantics { contentDescription = description })
        }
    }
}

/** Graphique en barres dessiné par l'appli, avec une ligne d'objectif en pointillés. */
@Composable
fun BarChart(values: List<Float>, labels: List<String>, color: Color, reference: Float?, modifier: Modifier = Modifier) {
    val grow = remember(values) { Animatable(0f) }
    LaunchedEffect(values) { grow.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
    val track = MaterialTheme.colorScheme.surfaceVariant
    val line = MaterialTheme.colorScheme.outline
    val max = maxOf(values.maxOrNull() ?: 0f, reference ?: 0f).coerceAtLeast(1f)
    // Au plus 7 étiquettes sous le graphique, pour qu'elles restent lisibles.
    val every = ((labels.size + 6) / 7).coerceAtLeast(1)
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(120.dp)) {
            val n = values.size.coerceAtLeast(1)
            val slot = size.width / n
            val bar = (slot * 0.62f).coerceAtMost(28.dp.toPx())
            val radius = CornerRadius(bar / 3, bar / 3)
            values.forEachIndexed { i, v ->
                val x = slot * i + (slot - bar) / 2
                drawRoundRect(track, Offset(x, 0f), Size(bar, size.height), radius)
                val h = size.height * (v / max) * grow.value
                if (h > 0f) drawRoundRect(color, Offset(x, size.height - h), Size(bar, h), radius)
            }
            reference?.let { r ->
                val y = size.height * (1 - r / max)
                drawLine(
                    line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            labels.forEachIndexed { i, l ->
                Text(
                    if (i % every == 0 || i == labels.lastIndex && labels.size <= 12) l else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
