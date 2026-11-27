/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.stats

import app.sentier.domain.tr
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.sentier.data.YearActivity
import app.sentier.ui.common.longDate
import java.time.LocalDate
import java.time.format.TextStyle as DateStyle
import java.util.Locale

/** Plus petite case lisible ; en dessous, le calendrier défile (et s'ouvre sur les semaines récentes). */
private val MIN_CELL = 11.dp
private val GAP = 2.dp
private val DAY_LABELS = 16.dp
private val MONTH_LABELS = 14.dp

/** Mots pour une intensité, pour la légende et les lecteurs d'écran. */
internal fun actionsText(n: Int): String = when (n) {
    0 -> tr("aucune action", "no actions")
    1 -> tr("1 action", "1 action")
    else -> tr("$n actions", "$n actions")
}

/**
 * Calendrier de chaleur : une colonne par semaine, une ligne par jour (lundi en haut).
 * Plus la case est foncée, plus la journée a été remplie. Toucher une case affiche son détail.
 */
@Composable
fun YearHeatmap(year: YearActivity, modifier: Modifier = Modifier) {
    var selected by remember(year.today) { mutableStateOf<Long?>(null) }
    val base = MaterialTheme.colorScheme.primary
    val empty = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.onSurface
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall
    val measurer = rememberTextMeasurer()
    val levels = remember(base, empty) {
        listOf(empty, base.copy(alpha = 0.3f), base.copy(alpha = 0.5f), base.copy(alpha = 0.75f), base)
    }
    val weeks = year.weeks
    val startDay = year.start.toEpochDay()
    val todayDay = year.today.toEpochDay()

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            tr("${year.activeDays} jours actifs sur l'année · ${actionsText(year.total)} · plus longue série ${year.bestRun} j", "${year.activeDays} active days this year · ${actionsText(year.total)} · longest streak ${year.bestRun} d"),
            style = MaterialTheme.typography.bodyMedium,
        )
        BoxWithConstraints {
            val fit: Dp = (maxWidth - DAY_LABELS) / weeks - GAP
            val cell = if (fit < MIN_CELL) MIN_CELL else fit.coerceAtMost(18.dp)
            val step = cell + GAP
            val width = DAY_LABELS + step * weeks
            val scroll = rememberScrollState(Int.MAX_VALUE)
            Canvas(
                Modifier
                    .horizontalScroll(scroll)
                    .width(width)
                    .height(MONTH_LABELS + step * 7)
                    .testTag("yearHeatmap")
                    .semantics {
                        contentDescription = tr("Calendrier de l'année : ${year.activeDays} jours actifs, ", "Year calendar: ${year.activeDays} active days, ") +
                            tr("${actionsText(year.total)}, plus longue série de ${year.bestRun} jours", "${actionsText(year.total)}, longest streak of ${year.bestRun} days")
                    }
                    .pointerInput(year) {
                        detectTapGestures { p ->
                            val col = ((p.x - DAY_LABELS.toPx()) / step.toPx()).toInt()
                            val row = ((p.y - MONTH_LABELS.toPx()) / step.toPx()).toInt()
                            val day = startDay + col * 7L + row
                            selected = if (p.x >= DAY_LABELS.toPx() && p.y >= MONTH_LABELS.toPx() &&
                                col in 0 until weeks && row in 0..6 && day <= todayDay
                            ) day.takeIf { it != selected } else null
                        }
                    },
            ) {
                val left = DAY_LABELS.toPx()
                val top = MONTH_LABELS.toPx()
                val s = step.toPx()
                val c = cell.toPx()
                val radius = CornerRadius(c / 4, c / 4)
                // Initiales des jours, une ligne sur deux.
                for ((row, label) in listOf(0 to tr("L", "M"), 2 to tr("M", "W"), 4 to tr("V", "F"))) {
                    val text = measurer.measure(label, TextStyle(color = labelColor, fontSize = labelStyle.fontSize))
                    drawText(text, topLeft = Offset(0f, top + row * s + (c - text.size.height) / 2))
                }
                var lastMonth = -1
                for (col in 0 until weeks) {
                    val weekStart = LocalDate.ofEpochDay(startDay + col * 7L)
                    if (weekStart.monthValue != lastMonth && col < weeks - 2) {
                        if (lastMonth != -1 || weekStart.dayOfMonth <= 7) {
                            val name = weekStart.month.getDisplayName(DateStyle.SHORT, app.sentier.domain.Lang.locale).trimEnd('.')
                            val text = measurer.measure(name, TextStyle(color = labelColor, fontSize = labelStyle.fontSize))
                            drawText(text, topLeft = Offset(left + col * s, 0f))
                        }
                        lastMonth = weekStart.monthValue
                    }
                    for (row in 0..6) {
                        val day = startDay + col * 7L + row
                        if (day > todayDay) break
                        val topLeft = Offset(left + col * s, top + row * s)
                        drawRoundRect(levels[year.levelOn(day)], topLeft, Size(c, c), radius)
                        if (day == selected || (selected == null && day == todayDay)) {
                            drawRoundRect(outline, topLeft, Size(c, c), radius, style = Stroke(1.5.dp.toPx()))
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            val day = selected
            Text(
                if (day == null) {
                    tr("Touche une case pour voir le détail d'une journée.", "Tap a square to see a day's details.")
                } else {
                    "${longDate(LocalDate.ofEpochDay(day))} : ${actionsText(year.countOn(day))}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = labelColor,
                modifier = Modifier.weight(1f),
            )
            Text(tr("Moins", "Less"), style = labelStyle, color = labelColor)
            for (color in levels) {
                Canvas(Modifier.padding(horizontal = 1.dp).size(10.dp)) {
                    drawRoundRect(color, cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()))
                }
            }
            Text(tr("Plus", "More"), style = labelStyle, color = labelColor)
        }
    }
}

