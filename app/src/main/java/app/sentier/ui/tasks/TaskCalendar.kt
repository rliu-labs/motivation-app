/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.tasks

import app.sentier.domain.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.sentier.data.TaskList
import app.sentier.data.TaskWithCounts
import app.sentier.ui.common.longDate
import app.sentier.ui.theme.PriorityColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** Jours affichés pour [month] : semaines complètes du lundi au dimanche (null hors du mois). */
fun calendarDays(month: YearMonth): List<LocalDate?> {
    val first = month.atDay(1)
    val lead = first.dayOfWeek.value - DayOfWeek.MONDAY.value
    val days = List<LocalDate?>(lead) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
    val trail = (7 - days.size % 7) % 7
    return days + List(trail) { null }
}

/** Calendrier du mois : un point par tâche à faire ce jour-là (trois au plus), un jour choisi. */
@Composable
fun TaskCalendar(
    month: YearMonth,
    today: LocalDate,
    selected: LocalDate,
    dueByDay: Map<Long, List<TaskWithCounts>>,
    listsById: Map<Long, TaskList>,
    onMonth: (YearMonth) -> Unit,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().widthIn(max = 520.dp).padding(horizontal = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onMonth(month.minusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = tr("Mois précédent", "Previous month"))
            }
            val title = month.month.getDisplayName(TextStyle.FULL_STANDALONE, app.sentier.domain.Lang.locale)
                .replaceFirstChar { it.titlecase(app.sentier.domain.Lang.locale) } + " " + month.year
            Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            IconButton(onClick = { onMonth(month.plusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = tr("Mois suivant", "Next month"))
            }
        }
        if (YearMonth.from(today) != month || selected != today) {
            TextButton(onClick = { onSelect(today) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(tr("Revenir à aujourd'hui", "Back to today"))
            }
        }
        Row(Modifier.fillMaxWidth()) {
            for (d in DayOfWeek.entries) {
                Text(
                    d.getDisplayName(TextStyle.NARROW, app.sentier.domain.Lang.locale).uppercase(app.sentier.domain.Lang.locale),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        for (week in calendarDays(month).chunked(7)) {
            Row(Modifier.fillMaxWidth()) {
                for (day in week) {
                    Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (day != null) {
                            DayCell(
                                day = day,
                                today = today,
                                selected = day == selected,
                                tasks = dueByDay[day.toEpochDay()].orEmpty(),
                                listsById = listsById,
                                onClick = { onSelect(day) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: LocalDate,
    today: LocalDate,
    selected: Boolean,
    tasks: List<TaskWithCounts>,
    listsById: Map<Long, TaskList>,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val late = day.isBefore(today) && tasks.isNotEmpty()
    val count = when (tasks.size) {
        0 -> tr("aucune tâche", "no tasks")
        1 -> tr("1 tâche", "1 task")
        else -> tr("${tasks.size} tâches", "${tasks.size} tasks")
    }
    Column(
        Modifier
            .padding(2.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(if (selected) colors.primary else Color.Transparent)
            .then(if (day == today && !selected) Modifier.border(1.5.dp, colors.primary, CircleShape) else Modifier)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "${longDate(day)}, $count"
                this.selected = selected
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            day.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (day == today) FontWeight.Bold else FontWeight.Normal,
            color = when {
                selected -> colors.onPrimary
                late -> colors.error
                else -> colors.onSurface
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.height(6.dp)) {
            for (t in tasks.take(3)) {
                val color = t.listId?.let(listsById::get)?.let { Color(it.color) } ?: PriorityColors.of(t.priority)
                Box(Modifier.size(5.dp).clip(CircleShape).background(if (selected) colors.onPrimary else color))
            }
        }
    }
}
