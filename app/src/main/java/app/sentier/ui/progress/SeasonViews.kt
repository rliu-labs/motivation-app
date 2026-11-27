/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.sentier.domain.Badge
import app.sentier.domain.Season
import app.sentier.domain.SeasonMetric
import app.sentier.domain.SeasonalChallenge
import app.sentier.domain.Seasons
import app.sentier.domain.SeasonsState
import app.sentier.ui.common.SectionHeader
import java.text.NumberFormat
import java.time.LocalDate
import java.time.MonthDay
import java.time.format.TextStyle
import java.util.Locale

/** Couleur de chaque défi saisonnier. */
fun seasonColor(id: String): Color = when (id) {
    "nouvel_an" -> Color(0xFF7E57C2)
    "saint_valentin" -> Color(0xFFE91E63)
    "printemps" -> Color(0xFFEC407A)
    "ete" -> Color(0xFFFFA000)
    "vacances" -> Color(0xFF00ACC1)
    "automne" -> Color(0xFFE65100)
    "halloween" -> Color(0xFFFF6F00)
    "novembre" -> Color(0xFF5D4037)
    "avent" -> Color(0xFF2E7D32)
    "fetes" -> Color(0xFF1E88E5)
    else -> Color(0xFF8E24AA)
}

private val Fr = Locale.FRANCE

/** « 1er décembre », « 24 décembre ». */
fun dayMonthFr(d: MonthDay): String =
    (if (d.dayOfMonth == 1) "1er" else "${d.dayOfMonth}") + " " + d.month.getDisplayName(TextStyle.FULL, Fr)

/** « du 1er au 24 décembre », « du 20 mars au 5 avril ». */
fun periodFr(s: Season): String =
    if (s.start.month == s.end.month) {
        "du ${if (s.start.dayOfMonth == 1) "1er" else s.start.dayOfMonth} au ${dayMonthFr(s.end)}"
    } else {
        "du ${dayMonthFr(s.start)} au ${dayMonthFr(s.end)}"
    }

private fun count(n: Long): String = NumberFormat.getIntegerInstance(Fr).format(n)

@Composable
fun SeasonSection(state: SeasonsState, today: LocalDate = LocalDate.now()) {
    SectionHeader("Défi saisonnier")
    val active = state.active
    if (active != null) {
        ActiveSeasonCard(active)
    } else if (state.next != null && state.nextStart != null) {
        NextSeasonCard(state.next, state.nextStart, today)
    }
}

@Composable
private fun ActiveSeasonCard(c: SeasonalChallenge) {
    val s = c.season
    val color = seasonColor(s.id)
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = (if (c.done) "Défi saisonnier réussi : " else "Défi saisonnier : ") + "${s.title}, ${s.goalText}"
            },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(Modifier.background(Brush.linearGradient(listOf(color.copy(alpha = 0.30f), color.copy(alpha = 0.08f))))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).background(color),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(if (c.done) Icons.Filled.Check else GameIcons.of(s.icon), contentDescription = null, tint = Color.White)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(s.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Défi limité · ${periodFr(s)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(s.goalText, style = MaterialTheme.typography.bodyLarge)
                Text(
                    s.flavor,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LinearProgressIndicator(
                    progress = { c.progress },
                    color = color,
                    trackColor = color.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(8.dp).clip(CircleShape),
                )
                Text(
                    when {
                        c.done -> "Réussi ! Bonus versé et badge rare « ${s.badgeTitle} » gagné."
                        c.hint != null -> c.hint
                        else -> "${count(c.current)} / ${count(s.goal)}${unitOf(s.metric)} · " + when (c.daysLeft) {
                            0 -> "dernier jour !"
                            1 -> "encore 2 jours, aujourd'hui compris"
                            else -> "encore ${c.daysLeft + 1} jours, aujourd'hui compris"
                        }
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (c.done) color else MaterialTheme.colorScheme.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("+${s.xp} XP", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.Paid, contentDescription = null, tint = GoldColor, modifier = Modifier.size(16.dp))
                    Text(" ${s.gold} or", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "· badge rare « ${s.badgeTitle} »",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun unitOf(m: SeasonMetric): String = when (m) {
    SeasonMetric.STEPS -> " pas"
    SeasonMetric.ACTIVE_DAYS, SeasonMetric.SPORT_DAYS -> " jours"
    SeasonMetric.HABITS -> " habitudes"
    SeasonMetric.TASKS -> " tâches"
}

@Composable
private fun NextSeasonCard(s: Season, start: LocalDate, today: LocalDate) {
    val color = seasonColor(s.id)
    val days = java.time.temporal.ChronoUnit.DAYS.between(today, start)
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) { Icon(GameIcons.of(s.icon), contentDescription = null, tint = color) }
            Spacer(Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Prochain : ${s.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    when (days) {
                        1L -> "Commence demain, le ${dayMonthFr(s.start)}."
                        else -> "Commence dans $days jours, le ${dayMonthFr(s.start)}."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "${s.goalText}, pour un badge rare : « ${s.badgeTitle} ».",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Les badges rares des défis saisonniers. */
@Composable
fun RareBadgesSection(badges: List<Badge>) {
    SectionHeader("Badges rares (${badges.count { it.unlocked }} sur ${badges.size})")
    Text(
        "Un par défi saisonnier. Chaque défi revient une fois par an : c'est la seule occasion de gagner son badge.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp),
    )
    for (b in badges) RareBadgeRow(b)
}

@Composable
private fun RareBadgeRow(badge: Badge) {
    val season = Seasons.all.firstOrNull { "saison_${it.id}" == badge.id }
    val color = season?.let { seasonColor(it.id) } ?: GoldColor
    ListItem(
        modifier = Modifier.semantics {
            contentDescription = (if (badge.unlocked) "Badge rare obtenu : " else "Badge rare à obtenir : ") + badge.title
        },
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    badge.title,
                    fontWeight = if (badge.unlocked) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (badge.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (badge.current > 1) {
                    Text(
                        "  ×${badge.current}",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
        supportingContent = {
            Text(
                badge.description + (season?.let { " (${periodFr(it)})" } ?: ""),
            )
        },
        leadingContent = {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (badge.unlocked) color else MaterialTheme.colorScheme.surfaceContainerHighest)
                    .border(2.dp, if (badge.unlocked) GoldColor else MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (badge.unlocked) GameIcons.of(badge.icon) else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = if (badge.unlocked) Color.White else MaterialTheme.colorScheme.outline,
                )
            }
        },
    )
}
