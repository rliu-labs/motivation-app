/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.journey

import app.sentier.domain.tr
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
import app.sentier.domain.EuropeMask
import app.sentier.domain.Journey
import app.sentier.domain.Stop
import app.sentier.domain.TourProgress
import app.sentier.domain.WorldMask
import kotlin.math.cos

/**
 * Carte d'un voyage en pointillés, dessinée par l'appli (aucun service de cartes) : le monde entier,
 * ou la région du voyage pour un pays. Les trajets entre escales sont en arcs, le chemin parcouru
 * en couleur, avec la position actuelle.
 */
@Composable
fun JourneyMap(progress: TourProgress, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelMedium.copy(color = colors.onSurface, fontWeight = FontWeight.SemiBold)
    val stops = progress.stops
    val frame = MapFrame.of(progress.journey)
    val km = progress.km
    val legIndex = progress.currentIndex
    val next = progress.nextStop

    val description = tr("Carte du voyage ${progress.journey.title} : ${formatKm(km)} km parcourus, dernière escale ${progress.current.city}", "Map of the journey ${progress.journey.title}: ${formatKm(km)} km walked, latest stop ${progress.current.city}") +
        (next?.let { tr(", prochaine escale ${it.city}", ", next stop ${it.city}") } ?: "")

    Box(
        modifier
            .aspectRatio(frame.aspect)
            .semantics { contentDescription = description }
            .drawWithCache {
                val dots = frame.dots(size)
                val legs = (0 until stops.lastIndex).map { i -> legPoints(frame, stops[i], stops[i + 1], size) }
                val dash = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
                val line = 2.dp.toPx()

                onDrawBehind {
                    drawPath(dots, colors.onSurfaceVariant.copy(alpha = 0.22f))
                    clipRect {
                        legs.forEachIndexed { i, pts ->
                            when {
                                i < legIndex || progress.done -> drawLeg(pts, colors.primary, Stroke(line, cap = StrokeCap.Round), frame.wraps)
                                i == legIndex -> {
                                    drawLeg(pts, colors.outline, Stroke(line, pathEffect = dash), frame.wraps)
                                    val t = legFraction(stops[i], stops[i + 1], km)
                                    drawLeg(pts.take((t * (pts.size - 1)).toInt() + 1) + pointAt(pts, t), colors.primary, Stroke(line, cap = StrokeCap.Round), frame.wraps)
                                }
                                else -> drawLeg(pts, colors.outline.copy(alpha = 0.5f), Stroke(line, pathEffect = dash), frame.wraps)
                            }
                        }
                    }

                    // Escales.
                    stops.forEachIndexed { i, s ->
                        val p = frame.project(s.lat, s.lon, size)
                        when {
                            s.km <= km -> drawCircle(colors.primary, 3.dp.toPx(), p)
                            s == next -> drawCircle(colors.tertiary, 4.dp.toPx(), p, style = Stroke(1.5.dp.toPx()))
                            i != stops.lastIndex || stops.first().city != stops.last().city -> drawCircle(colors.outline, 2.dp.toPx(), p, style = Stroke(1.dp.toPx()))
                        }
                    }

                    // Position actuelle.
                    val here = if (progress.done || next == null) {
                        frame.project(stops.last().lat, stops.last().lon, size)
                    } else {
                        val p = pointAt(legs[legIndex], legFraction(stops[legIndex], next, km))
                        if (frame.wraps) wrapX(p, size.width) else p
                    }
                    drawCircle(colors.tertiary.copy(alpha = 0.25f), 11.dp.toPx(), here)
                    drawCircle(Color.White, 6.dp.toPx(), here)
                    drawCircle(colors.tertiary, 4.5.dp.toPx(), here)

                    // Nom de la prochaine escale, sur une pastille.
                    next?.let { n ->
                        val p = frame.project(n.lat, n.lon, size)
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

/**
 * Cadre d'une carte en projection équirectangulaire : le monde entier (qui se replie d'un bord à l'autre),
 * ou une région d'Europe, élargie pour garder des proportions agréables et corrigée de la latitude.
 */
internal class MapFrame private constructor(
    val latTop: Double,
    val latBottom: Double,
    val lonLeft: Double,
    val lonRight: Double,
    val wraps: Boolean,
    /** Largeur sur hauteur. */
    val aspect: Float,
) {
    fun project(lat: Double, lon: Double, size: Size): Offset = Offset(
        ((lon - lonLeft) / (lonRight - lonLeft) * size.width).toFloat(),
        ((latTop - lat) / (latTop - latBottom) * size.height).toFloat(),
    )

    /** Les points des terres émergées, à la taille de la carte. */
    fun dots(size: Size): Path {
        val dots = Path()
        if (wraps) {
            val cell = size.width / WorldMask.COLS
            val r = cell * 0.34f
            for (row in 0 until WorldMask.ROWS) for (col in 0 until WorldMask.COLS) {
                if (WorldMask.isLand(col, row)) dots.addOval(Rect(Offset((col + 0.5f) * cell, (row + 0.5f) * cell), r))
            }
        } else {
            val step = EuropeMask.STEP
            val cellH = (step / (latTop - latBottom) * size.height).toFloat()
            val cellW = (step / (lonRight - lonLeft) * size.width).toFloat()
            val r = minOf(cellW, cellH) * 0.36f
            val col0 = Math.floor((lonLeft - EuropeMask.LON_LEFT) / step).toInt()
            val col1 = Math.ceil((lonRight - EuropeMask.LON_LEFT) / step).toInt()
            val row0 = Math.floor((EuropeMask.LAT_TOP - latTop) / step).toInt()
            val row1 = Math.ceil((EuropeMask.LAT_TOP - latBottom) / step).toInt()
            for (row in row0..row1) for (col in col0..col1) {
                if (EuropeMask.isLand(col, row)) {
                    val lat = EuropeMask.LAT_TOP - (row + 0.5) * step
                    val lon = EuropeMask.LON_LEFT + (col + 0.5) * step
                    dots.addOval(Rect(project(lat, lon, size), r))
                }
            }
        }
        return dots
    }

    companion object {
        private const val MIN_ASPECT = 1.3
        private const val MAX_ASPECT = 1.9

        fun of(journey: Journey): MapFrame {
            val area = journey.area?.takeIf { EuropeMask.covers(it) }
                ?: return MapFrame(WorldMask.LAT_TOP, WorldMask.LAT_BOTTOM, -180.0, 180.0, wraps = true,
                    aspect = WorldMask.COLS.toFloat() / WorldMask.ROWS)
            var latTop = area.latTop
            var latBottom = area.latBottom
            var lonLeft = area.lonLeft
            var lonRight = area.lonRight
            val k = cos(Math.toRadians((latTop + latBottom) / 2))
            val ratio = (lonRight - lonLeft) * k / (latTop - latBottom)
            if (ratio < MIN_ASPECT) {
                val half = (latTop - latBottom) * MIN_ASPECT / k / 2
                val mid = (lonLeft + lonRight) / 2
                lonLeft = mid - half
                lonRight = mid + half
            } else if (ratio > MAX_ASPECT) {
                val half = (lonRight - lonLeft) * k / MAX_ASPECT / 2
                val mid = (latTop + latBottom) / 2
                latTop = mid + half
                latBottom = mid - half
            }
            // Reste dans la grille, en décalant le cadre si besoin.
            fun shift(lo: Double, hi: Double, min: Double, max: Double): Pair<Double, Double> = when {
                hi - lo >= max - min -> min to max
                lo < min -> min to hi + (min - lo)
                hi > max -> lo - (hi - max) to max
                else -> lo to hi
            }
            val (l, r) = shift(lonLeft, lonRight, EuropeMask.LON_LEFT, EuropeMask.LON_RIGHT)
            val (b, t) = shift(latBottom, latTop, EuropeMask.LAT_BOTTOM, EuropeMask.LAT_TOP)
            val aspect = ((r - l) * cos(Math.toRadians((t + b) / 2)) / (t - b)).toFloat()
            return MapFrame(t, b, l, r, wraps = false, aspect = aspect)
        }
    }
}

/**
 * Points d'un trajet en arc entre deux escales. Un trajet qui traverse le Pacifique
 * passe par le bord de la carte au lieu de la traverser.
 */
private fun legPoints(frame: MapFrame, a: Stop, b: Stop, size: Size): List<Offset> {
    var lonB = b.lon
    if (lonB - a.lon > 180) lonB -= 360
    if (lonB - a.lon < -180) lonB += 360
    val pa = frame.project(a.lat, a.lon, size)
    val pb = frame.project(b.lat, lonB, size)
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
private fun DrawScope.drawLeg(pts: List<Offset>, color: Color, stroke: Stroke, wraps: Boolean) {
    if (pts.size < 2) return
    val path = Path().apply {
        moveTo(pts[0].x, pts[0].y)
        pts.drop(1).forEach { lineTo(it.x, it.y) }
    }
    drawPath(path, color, style = stroke)
    if (!wraps) return
    val minX = pts.minOf { it.x }
    val maxX = pts.maxOf { it.x }
    if (maxX > size.width) translate(left = -size.width) { drawPath(path, color, style = stroke) }
    if (minX < 0) translate(left = size.width) { drawPath(path, color, style = stroke) }
}
