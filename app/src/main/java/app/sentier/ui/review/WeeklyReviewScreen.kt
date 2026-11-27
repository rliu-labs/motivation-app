/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.review

import app.sentier.domain.tr
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sentier.domain.WeeklyReview
import app.sentier.ui.common.SectionHeader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DAY_MONTH: DateTimeFormatter get() = DateTimeFormatter.ofPattern("d MMMM", app.sentier.domain.Lang.locale)

/** « du 16 au 22 novembre », ou « du 30 novembre au 6 décembre ». */
internal fun weekRange(start: LocalDate, end: LocalDate): String =
    if (start.month == end.month) tr("du ${start.dayOfMonth} au ${end.format(DAY_MONTH)}", "from ${start.dayOfMonth} to ${end.format(DAY_MONTH)}")
    else tr("du ${start.format(DAY_MONTH)} au ${end.format(DAY_MONTH)}", "from ${start.format(DAY_MONTH)} to ${end.format(DAY_MONTH)}")

private fun iconOf(name: String): ImageVector = when (name) {
    "star" -> Icons.Filled.Star
    "trophy" -> Icons.Filled.EmojiEvents
    "sun" -> Icons.Filled.WbSunny
    "habit" -> Icons.Filled.SelfImprovement
    "up" -> Icons.AutoMirrored.Filled.TrendingUp
    "walk" -> Icons.AutoMirrored.Filled.DirectionsWalk
    else -> Icons.Filled.TaskAlt
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReviewScreen(onBack: () -> Unit, vm: WeeklyReviewViewModel = viewModel(factory = WeeklyReviewViewModel.Factory)) {
    val review by vm.review.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Bilan de la semaine", "Weekly review")) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, tr("Retour", "Back")) } },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            review?.let { ReviewContent(it) }
        }
    }
}

@Composable
private fun ReviewContent(r: WeeklyReview) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                tr("Semaine ${weekRange(r.start, r.end)}", "Week ${weekRange(r.start, r.end)}"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                r.headline,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(r.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }

    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Tile(tr("Jours actifs", "Active days"), "${r.week.activeDays} / 7", r.week.activeDays - r.previous.activeDays, Modifier.weight(1f))
        Tile(tr("Habitudes", "Habits"), "${r.week.habits}", r.week.habits - r.previous.habits, Modifier.weight(1f))
        Tile(tr("Tâches", "Tasks"), "${r.week.tasks}", r.week.tasks - r.previous.tasks, Modifier.weight(1f))
        Tile("XP", "${r.week.xp}", r.week.xp - r.previous.xp, Modifier.weight(1f))
    }

    SectionHeader(tr("Moments forts", "Highlights"))
    if (r.highlights.isEmpty()) {
        Text(
            tr("Pas encore de moment fort : coche une habitude ou termine une tâche, et ton prochain bilan aura de quoi raconter.", "No highlights yet: check a habit or finish a task, and your next review will have something to tell."),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (h in r.highlights) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(iconOf(h.icon), contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Column(Modifier.padding(start = 14.dp)) {
                        Text(h.title, style = MaterialTheme.typography.titleSmall)
                        Text(h.detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    val ongoing = r.ongoing
    SectionHeader(if (ongoing != null) tr("Objectif de cette semaine", "This week's goal") else tr("Objectif de la semaine prochaine", "Next week's goal"))
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                Text(
                    r.goal.text,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
            if (ongoing != null) {
                val done = r.goal.progressOf(ongoing).coerceAtMost(r.goal.target)
                LinearProgressIndicator(
                    progress = { done.toFloat() / r.goal.target },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    if (done >= r.goal.target) tr("Objectif atteint, bravo !", "Goal reached, well done!") else tr("$done sur ${r.goal.target} pour l'instant.", "$done of ${r.goal.target} so far."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            } else {
                Text(
                    tr("Juste un peu plus que cette semaine : un pas de plus, pas une montagne.", "Just a little more than this week: one more step, not a mountain."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun Tile(label: String, value: String, diff: Int, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1)
            Text(
                when {
                    diff > 0 -> "+$diff"
                    diff < 0 -> "$diff"
                    else -> "="
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (diff > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

