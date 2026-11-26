/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.journey

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import app.sentier.domain.Stop
import app.sentier.domain.TourProgress
import app.sentier.domain.WorldMask
import app.sentier.domain.WorldTour

/**
 * Carte du monde en pointillés, dessinée par l'appli (aucun service de cartes) :
 * les trajets entre escales en arcs, le chemin parcouru en couleur et la position actuelle.
 */
@Composable
fun WorldMap(progress: TourProgress, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelMedium.copy(color = colors.onSurface, fontWeight = FontWeight.SemiBold)
    val stops = WorldTour.stops
    val km = progress.km
    val legIndex = progress.currentIndex
    val next = progress.nextStop

    val description = "Carte du tour du monde : ${formatKm(km)} km parcourus, dernière escale ${progress.current.city}" +
        (next?.let { ", prochaine escale ${it.city}" } ?: "")

    Box(
        modifier
            .aspectRatio(WorldMask.COLS.toFloat() / WorldMask.ROWS)
            .semantics { contentDescription = description }
            .drawWithCache {
                val cell = size.width / WorldMask.COLS
                val dots = Path()
                val r = cell * 0.34f
                for (row in 0 until WorldMask.ROWS) for (col in 0 until WorldMask.COLS) {
                    if (WorldMask.isLand(col, row)) {
                        val c = Offset((col + 0.5f) * cell, (row + 0.5f) * cell)
                        dots.addOval(Rect(c, r))
                    }
                }
                val legs = (0 until stops.lastIndex).map { i -> legPoints(stops[i], stops[i + 1], size) }
                val dash = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
                val line = 2.dp.toPx()

                onDrawBehind {
                    drawPath(dots, colors.onSurfaceVariant.copy(alpha = 0.22f))
                    clipRect {
                        legs.forEachIndexed { i, pts ->
                            when {
                                i < legIndex || progress.done -> drawLeg(pts, colors.primary, Stroke(line, cap = StrokeCap.Round))
                                i == legIndex -> {
                                    drawLeg(pts, colors.outline, Stroke(line, pathEffect = dash))
                                    val t = legFraction(stops[i], stops[i + 1], km)
                                    drawLeg(pts.take((t * (pts.size - 1)).toInt() + 1) + pointAt(pts, t), colors.primary, Stroke(line, cap = StrokeCap.Round))
                                }
                                else -> drawLeg(pts, colors.outline.copy(alpha = 0.5f), Stroke(line, pathEffect = dash))
                            }
                        }
                    }

                    // Escales.
                    stops.forEachIndexed { i, s ->
                        val p = project(s.lat, s.lon, size)
                        when {
                            s.km <= km -> drawCircle(colors.primary, 3.dp.toPx(), p)
                            s == next -> drawCircle(colors.tertiary, 4.dp.toPx(), p, style = Stroke(1.5.dp.toPx()))
                            i != stops.lastIndex -> drawCircle(colors.outline, 2.dp.toPx(), p, style = Stroke(1.dp.toPx()))
                        }
                    }

                    // Position actuelle.
                    val here = if (progress.done || next == null) {
                        project(stops.last().lat, stops.last().lon, size)
                    } else {
                        wrapX(pointAt(legs[legIndex], legFraction(stops[legIndex], next, km)), size.width)
                    }
                    drawCircle(colors.tertiary.copy(alpha = 0.25f), 11.dp.toPx(), here)
                    drawCircle(Color.White, 6.dp.toPx(), here)
                    drawCircle(colors.tertiary, 4.5.dp.toPx(), here)

                    // Nom de la prochaine escale, sur une pastille.
                    next?.let { n ->
                        val p = project(n.lat, n.lon, size)
                        val text = measurer.measure(n.city, labelStyle)
                        val pad = 4.dp.toPx()
                        val w = text.size.width + 2 * pad
                        val h = text.size.height.toFloat()
                        val gap = 8.dp.toPx()
                        val x = if (p.x + gap + w <= size.width) p.x + gap else (p.x - gap - w).coerceAtLeast(0f)
                        val y = (p.y - h / 2).coerceIn(0f, (size.height - h).coerceAtLeast(0f))
                        drawRoundRect(colors.surface.copy(alpha = 0.9f), Offset(x, y), Size(w, h), CornerRadius(h / 2))
                        drawText(text, topLeft = Offset(x + pad, y))
                    }
                }
            }
    )
}

/** Projection équirectangulaire alignée sur la grille des terres. */
private fun project(lat: Double, lon: Double, size: Size): Offset = Offset(
    ((lon + 180.0) / 360.0 * size.width).toFloat(),
    ((WorldMask.LAT_TOP - lat) / (WorldMask.LAT_TOP - WorldMask.LAT_BOTTOM) * size.height).toFloat(),
)

/**
 * Points d'un trajet en arc entre deux escales. Un trajet qui traverse le Pacifique
 * passe par le bord de la carte au lieu de la traverser.
 */
private fun legPoints(a: Stop, b: Stop, size: Size): List<Offset> {
    var lonB = b.lon
    if (lonB - a.lon > 180) lonB -= 360
    if (lonB - a.lon < -180) lonB += 360
    val pa = project(a.lat, a.lon, size)
    val pb = project(b.lat, lonB, size)
    val mid = Offset((pa.x + pb.x) / 2, (pa.y + pb.y) / 2)
    val dx = pb.x - pa.x
    val dy = pb.y - pa.y
    val len = kotlin.math.sqrt(dx * dx + dy * dy)
    // Arc bombé vers le haut de la carte, comme une route aérienne.
    var nx = dy / (len.coerceAtLeast(1f))
    var ny = -dx / (len.coerceAtLeast(1f))
    if (ny > 0) { nx = -nx; ny = -ny }
    val control = Offset(mid.x + nx * len * 0.18f, mid.y + ny * len * 0.18f)
    return (0..24).map { k ->
        val t = k / 24f
        val u = 1 - t
        Offset(u * u * pa.x + 2 * u * t * control.x + t * t * pb.x, u * u * pa.y + 2 * u * t * control.y + t * t * pb.y)
    }
}

private fun legFraction(a: Stop, b: Stop, km: Double): Float =
    ((km - a.km) / (b.km - a.km)).toFloat().coerceIn(0f, 1f)

private fun pointAt(pts: List<Offset>, t: Float): Offset {
    val f = t * (pts.size - 1)
    val i = f.toInt().coerceIn(0, pts.size - 2)
    val r = f - i
    return Offset(pts[i].x + (pts[i + 1].x - pts[i].x) * r, pts[i].y + (pts[i + 1].y - pts[i].y) * r)
}

private fun wrapX(p: Offset, width: Float): Offset = when {
    p.x < 0 -> p.copy(x = p.x + width)
    p.x > width -> p.copy(x = p.x - width)
    else -> p
}

/** Dessine un trajet, et sa copie décalée d'une largeur s'il dépasse un bord de la carte. */
private fun DrawScope.drawLeg(pts: List<Offset>, color: Color, stroke: Stroke) {
    if (pts.size < 2) return
    val path = Path().apply {
        moveTo(pts[0].x, pts[0].y)
        pts.drop(1).forEach { lineTo(it.x, it.y) }
    }
    drawPath(path, color, style = stroke)
    val minX = pts.minOf { it.x }
    val maxX = pts.maxOf { it.x }
    if (maxX > size.width) translate(left = -size.width) { drawPath(path, color, style = stroke) }
    if (minX < 0) translate(left = size.width) { drawPath(path, color, style = stroke) }
}
