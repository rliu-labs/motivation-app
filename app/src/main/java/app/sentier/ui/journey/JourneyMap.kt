/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.journey

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import app.sentier.domain.Journey
import app.sentier.domain.Stop
import kotlin.math.cos

/**
 * Carte du voyage dessinée à partir des coordonnées des étapes (aucune carte en ligne) :
 * le chemin à venir en pointillés, le chemin parcouru en couleur, et sa position.
 */
@Composable
fun JourneyMap(journey: Journey, km: Double, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelMedium.copy(color = colors.onSurface, fontWeight = FontWeight.Medium)
    val position = positionOf(journey, km)
    val nextIndex = journey.stops.indexOfFirst { it.km > km }

    Canvas(
        modifier.semantics {
            contentDescription = "Carte du voyage ${journey.title}, ${formatKm(km)} km parcourus sur ${formatKm(journey.lengthKm)}"
        }
    ) {
        val pad = 36.dp.toPx()
        val points = project(journey.stops, size, pad)
        val line = 5.dp.toPx()

        // Chemin complet, en pointillés.
        drawPath(
            pathOf(points),
            color = colors.outline.copy(alpha = 0.6f),
            style = Stroke(line, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.5f * line, 2f * line))),
        )

        // Chemin parcouru, jusqu'à la position.
        val (segment, t) = position
        val here = if (segment >= points.lastIndex) points.last() else lerp(points[segment], points[segment + 1], t)
        drawPath(
            pathOf(points.take(segment + 1) + here),
            color = colors.primary,
            style = Stroke(line, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        // Étapes.
        journey.stops.forEachIndexed { i, stop ->
            val p = points[i]
            if (stop.km <= km) {
                drawCircle(colors.primary, radius = 6.dp.toPx(), center = p)
                drawCircle(colors.onPrimary, radius = 2.5.dp.toPx(), center = p)
            } else {
                drawCircle(colors.surface, radius = 6.dp.toPx(), center = p)
                drawCircle(if (i == nextIndex) colors.tertiary else colors.outline, radius = 6.dp.toPx(), center = p, style = Stroke(2.dp.toPx()))
            }
        }

        // Position actuelle.
        drawCircle(Color.White, radius = 11.dp.toPx(), center = here)
        drawCircle(colors.tertiary, radius = 8.dp.toPx(), center = here)

        // Noms : départ, arrivée (si différente) et prochaine étape.
        val labelled = buildList {
            add(0)
            if (journey.stops.last().name != journey.stops.first().name) add(journey.stops.lastIndex)
            if (nextIndex > 0 && nextIndex !in this && journey.stops[nextIndex].name != journey.stops.first().name) add(nextIndex)
        }
        for (i in labelled) {
            val text = measurer.measure(journey.stops[i].name, labelStyle)
            val p = points[i]
            val w = text.size.width.toFloat()
            val h = text.size.height.toFloat()
            val gap = 10.dp.toPx()
            val x = if (p.x + gap + w <= size.width) p.x + gap else (p.x - gap - w).coerceAtLeast(0f)
            val y = (p.y - h / 2).coerceIn(0f, size.height - h)
            drawText(text, topLeft = Offset(x, y))
        }
    }
}

/** Segment du chemin où l'on se trouve et avancement (0 à 1) sur ce segment. */
internal fun positionOf(journey: Journey, km: Double): Pair<Int, Float> {
    val stops = journey.stops
    if (km >= journey.lengthKm) return stops.lastIndex to 0f
    val i = stops.indexOfLast { it.km <= km }.coerceAtLeast(0)
    val span = stops[i + 1].km - stops[i].km
    return i to if (span <= 0) 0f else ((km - stops[i].km) / span).toFloat().coerceIn(0f, 1f)
}

/** Projection simple des coordonnées dans le cadre (suffisante à l'échelle d'une région). */
private fun project(stops: List<Stop>, size: Size, pad: Float): List<Offset> {
    val meanLat = stops.map { it.lat }.average()
    val k = cos(Math.toRadians(meanLat))
    val xs = stops.map { it.lon * k }
    val ys = stops.map { -it.lat }
    val minX = xs.min(); val maxX = xs.max(); val minY = ys.min(); val maxY = ys.max()
    val dx = (maxX - minX).coerceAtLeast(1e-6)
    val dy = (maxY - minY).coerceAtLeast(1e-6)
    val scale = minOf((size.width - 2 * pad) / dx, (size.height - 2 * pad) / dy)
    val offX = (size.width - dx * scale) / 2
    val offY = (size.height - dy * scale) / 2
    return xs.indices.map { i -> Offset((offX + (xs[i] - minX) * scale).toFloat(), (offY + (ys[i] - minY) * scale).toFloat()) }
}

private fun pathOf(points: List<Offset>) = Path().apply {
    points.firstOrNull()?.let { moveTo(it.x, it.y) }
    points.drop(1).forEach { lineTo(it.x, it.y) }
}

private fun lerp(a: Offset, b: Offset, t: Float) = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
