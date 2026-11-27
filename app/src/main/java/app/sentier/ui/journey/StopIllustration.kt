/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.journey

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.sentier.domain.Journey

/** Silhouettes de monuments dessinées par l'appli (aucune image téléchargée). */
enum class Landmark {
    TOWER, MINARET, DOME, TEMPLE, MOSQUE, ROCK, PYRAMIDS, ROCK_CHURCH, IWAN, TAJ, STUPA, HALL,
    PAGODA, LANTERNS, KHMER, SPLIT_GATE, OPERA, MEETING_HOUSE, PEAKS, STATUE, COLONIAL, STEP_PYRAMID,
    SKYLINE, CHURCH,
    // Voyages par pays.
    CATHEDRAL, SPIRES, CASTLE, CHATEAU, FAIRY_CASTLE, ABBEY_ISLAND, STONES, STONEHENGE, BRIDGE, BROKEN_BRIDGE,
    LEANING_TOWER, CANAL, COLOSSEUM, CLIFF_VILLAGE, SASSI, LIGHTHOUSE, CLOCK_TOWER, MOORISH, MODERN, CURVES,
    VINEYARD, VOLCANO, SNOW_PEAKS, HALF_TIMBERED, GATE, AQUEDUCT, BASALT, PALACE, ARCH, HILL_CHURCH, TRULLI,
    HARBOUR, TRAM, LOCH, FOREST, WAREHOUSE,
}

/** Ambiance d'une illustration : ciel, soleil (ou lune), sol, monument. */
data class Scene(
    val landmark: Landmark,
    val skyTop: Long,
    val skyBottom: Long,
    val ground: Long,
    val ink: Long,
    val night: Boolean = false,
    val label: String,
)

/** Une scène par escale, dans l'ordre du tour du monde (le retour à Paris reprend la première). */
val StopScenes: List<Scene> = listOf(
    Scene(Landmark.TOWER, 0xFF8EC5FC, 0xFFE0C3FC, 0xFF7FA87A, 0xFF3A3F5C, label = "la tour Eiffel"),
    Scene(Landmark.MINARET, 0xFFFFB56B, 0xFFFFE3B3, 0xFFD9A066, 0xFF8A4B2A, label = "le minaret de la Koutoubia"),
    Scene(Landmark.DOME, 0xFFFFD3A5, 0xFFFD6585, 0xFFB98B5E, 0xFF5B3A29, label = "le Panthéon"),
    Scene(Landmark.TEMPLE, 0xFF6DD5FA, 0xFFFFFFFF, 0xFFC9B48A, 0xFF6B5B45, label = "le Parthénon"),
    Scene(Landmark.MOSQUE, 0xFFF6D365, 0xFFFDA085, 0xFF6E8BA3, 0xFF3D3550, label = "une mosquée et ses minarets"),
    Scene(Landmark.ROCK, 0xFFFFC371, 0xFFFF5F6D, 0xFFC1754C, 0xFF7A3B26, label = "la façade du Trésor taillée dans la roche"),
    Scene(Landmark.PYRAMIDS, 0xFF89F7FE, 0xFFFFE29F, 0xFFE2B36B, 0xFF9C6B30, label = "les pyramides de Gizeh"),
    Scene(Landmark.ROCK_CHURCH, 0xFFA1C4FD, 0xFFC2E9FB, 0xFF8E9B5A, 0xFF6B4A33, label = "une église creusée dans la roche"),
    Scene(Landmark.IWAN, 0xFF4FACFE, 0xFF00F2FE, 0xFFCDB38B, 0xFF1F4E79, label = "une mosquée à coupole turquoise"),
    Scene(Landmark.TAJ, 0xFFFBC2EB, 0xFFA6C1EE, 0xFF8DB580, 0xFFF4F1EA, label = "le Taj Mahal"),
    Scene(Landmark.STUPA, 0xFF84FAB0, 0xFF8FD3F4, 0xFF9AA76B, 0xFFF2EEE6, label = "un stupa aux yeux peints"),
    Scene(Landmark.HALL, 0xFFFFECD2, 0xFFFCB69F, 0xFF9E7C5A, 0xFF8B1E1E, label = "un pavillon impérial"),
    Scene(Landmark.HALL, 0xFFE0EAFC, 0xFFCFDEF3, 0xFF7E9C76, 0xFF2F4858, label = "un palais au toit recourbé"),
    Scene(Landmark.PAGODA, 0xFFFDCBF1, 0xFFE6DEE9, 0xFF7BA05B, 0xFF4A2C3A, label = "une pagode à cinq étages"),
    Scene(Landmark.LANTERNS, 0xFF30355E, 0xFF5B4B8A, 0xFF3E3A5E, 0xFF1E1B33, night = true, label = "les lanternes de la vieille ville"),
    Scene(Landmark.KHMER, 0xFFFF9A8B, 0xFFFFE3A3, 0xFF6F8F4E, 0xFF4B3B2A, label = "les tours d'Angkor Vat"),
    Scene(Landmark.SPLIT_GATE, 0xFFFFDDE1, 0xFFEE9CA7, 0xFF5E8C61, 0xFF5A3A2E, label = "une porte de temple fendue"),
    Scene(Landmark.OPERA, 0xFF48C6EF, 0xFF6F86D6, 0xFF2F6F8F, 0xFFFFFFFF, label = "l'opéra et ses voiles"),
    Scene(Landmark.MEETING_HOUSE, 0xFFC3CFE2, 0xFFF5F7FA, 0xFF5F8D5A, 0xFF6B2D1F, label = "une maison de réunion maorie"),
    Scene(Landmark.PEAKS, 0xFF96E6A1, 0xFFD4FC79, 0xFF6E8B3D, 0xFF4F5B3A, label = "les terrasses incas dans la montagne"),
    Scene(Landmark.STATUE, 0xFF00C6FB, 0xFF005BEA, 0xFF2E7D4F, 0xFF1C3D2E, label = "le Christ du Corcovado"),
    Scene(Landmark.COLONIAL, 0xFFF9D423, 0xFFFF4E50, 0xFF3D8361, 0xFF6A3E5A, label = "des façades coloniales et un palmier"),
    Scene(Landmark.STEP_PYRAMID, 0xFFF6D365, 0xFFFDA085, 0xFF8C9F4E, 0xFF6E4A2F, label = "une pyramide à degrés"),
    Scene(Landmark.SKYLINE, 0xFF667EEA, 0xFF764BA2, 0xFF3C3B5C, 0xFF1F1E36, night = true, label = "les gratte-ciel de Manhattan"),
    Scene(Landmark.CHURCH, 0xFF0F2027, 0xFF2C5364, 0xFF4E6E70, 0xFFE8EEF0, night = true, label = "une église sous une aurore boréale"),
)

/** Scène d'une escale du tour du monde. */
fun sceneFor(index: Int): Scene = StopScenes.getOrElse(index) { StopScenes.first() }

/** Scène d'une escale d'un voyage (le retour au départ d'une boucle reprend la première scène). */
fun sceneFor(journey: Journey, index: Int): Scene {
    val scenes = JourneyScenes.of(journey.id)
    return scenes.getOrElse(index) { scenes.first() }
}

/** Illustration d'une escale, dessinée à la taille voulue. */
@Composable
fun StopIllustration(journey: Journey, index: Int, modifier: Modifier = Modifier) {
    val scene = sceneFor(journey, index)
    Canvas(
        modifier
            .fillMaxWidth()
            .height(140.dp)
            .semantics { contentDescription = "Illustration : ${scene.label}" },
    ) { drawScene(scene) }
}

/** Dessin dans un repère de 100 × 60 unités centré, le ciel et le sol couvrant toute la largeur. */
private fun DrawScope.drawScene(scene: Scene) {
    drawRect(Brush.verticalGradient(listOf(Color(scene.skyTop), Color(scene.skyBottom))))
    val s = size.height / 60f
    val ox = (size.width - 100f * s) / 2f
    val u = Units(this, s, ox)

    // Soleil ou lune, étoiles et aurore pour les scènes de nuit.
    if (scene.night) {
        for ((x, y) in listOf(8f to 6f, 22f to 12f, 40f to 5f, 63f to 9f, 85f to 4f, 93f to 14f, 30f to 18f)) {
            drawCircle(Color.White.copy(alpha = 0.8f), radius = 0.5f * s, center = u.p(x, y))
        }
        if (scene.landmark == Landmark.CHURCH) {
            drawPath(
                u.path(0f to 20f, 25f to 8f, 50f to 16f, 75f to 6f, 100f to 14f, 100f to 22f, 75f to 14f, 50f to 24f, 25f to 16f, 0f to 28f),
                Brush.verticalGradient(listOf(Color(0x9966FFCC), Color(0x0066FFCC)), startY = 6f * s, endY = 28f * s),
            )
        }
        drawCircle(Color(0xFFFFF6D5), radius = 4f * s, center = u.p(82f, 12f))
    } else {
        drawCircle(Color(0xFFFFF3B0).copy(alpha = 0.9f), radius = 6f * s, center = u.p(80f, 13f))
    }

    // Collines puis sol.
    drawPath(u.path(-50f to 50f, 10f to 44f, 35f to 47f, 70f to 42f, 150f to 48f, 150f to 60f, -50f to 60f), Color(scene.ground).copy(alpha = 0.6f))
    drawRect(Color(scene.ground), topLeft = Offset(0f, 50f * s), size = Size(size.width, size.height - 50f * s))

    u.landmark(scene.landmark, Color(scene.ink), Color(scene.skyBottom))
}

internal class Units(val d: DrawScope, val s: Float, val ox: Float) {
    fun p(x: Float, y: Float) = Offset(ox + x * s, y * s)

    fun path(vararg pts: Pair<Float, Float>): Path = Path().apply {
        pts.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(ox + x * s, y * s) else lineTo(ox + x * s, y * s) }
        close()
    }

    fun rect(x: Float, y: Float, w: Float, h: Float, c: Color) = d.drawRect(c, topLeft = p(x, y), size = Size(w * s, h * s))

    fun tri(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, c: Color) = d.drawPath(path(x1 to y1, x2 to y2, x3 to y3), c)

    fun poly(c: Color, vararg pts: Pair<Float, Float>) = d.drawPath(path(*pts), c)

    /** Demi-disque posé sur [baseY]. */
    fun dome(cx: Float, baseY: Float, r: Float, c: Color) =
        d.drawArc(c, 180f, 180f, true, topLeft = p(cx - r, baseY - r), size = Size(2 * r * s, 2 * r * s))

    fun circle(cx: Float, cy: Float, r: Float, c: Color) = d.drawCircle(c, radius = r * s, center = p(cx, cy))

    /** Coupole en bulbe (Taj Mahal, stupas). */
    fun onion(cx: Float, baseY: Float, w: Float, h: Float, c: Color) {
        val path = Path().apply {
            moveTo(ox + (cx - w / 2) * s, baseY * s)
            cubicTo(ox + (cx - w * 0.75f) * s, (baseY - h * 0.6f) * s, ox + (cx - w * 0.1f) * s, (baseY - h * 0.8f) * s, ox + cx * s, (baseY - h) * s)
            cubicTo(ox + (cx + w * 0.1f) * s, (baseY - h * 0.8f) * s, ox + (cx + w * 0.75f) * s, (baseY - h * 0.6f) * s, ox + (cx + w / 2) * s, baseY * s)
            close()
        }
        d.drawPath(path, c)
    }

    /** Toit asiatique aux bords relevés. */
    fun curvedRoof(cx: Float, y: Float, w: Float, h: Float, c: Color) {
        val path = Path().apply {
            moveTo(ox + (cx - w / 2) * s, (y - h * 0.3f) * s)
            quadraticTo(ox + (cx - w * 0.3f) * s, (y + h * 0.1f) * s, ox + (cx - w * 0.25f) * s, (y - h * 0.2f) * s)
            lineTo(ox + (cx - w * 0.15f) * s, (y - h) * s)
            lineTo(ox + (cx + w * 0.15f) * s, (y - h) * s)
            lineTo(ox + (cx + w * 0.25f) * s, (y - h * 0.2f) * s)
            quadraticTo(ox + (cx + w * 0.3f) * s, (y + h * 0.1f) * s, ox + (cx + w / 2) * s, (y - h * 0.3f) * s)
            lineTo(ox + (cx + w * 0.3f) * s, y * s)
            lineTo(ox + (cx - w * 0.3f) * s, y * s)
            close()
        }
        d.drawPath(path, c)
    }

    fun landmark(kind: Landmark, ink: Color, light: Color) {
        val g = 50f // niveau du sol
        when (kind) {
            Landmark.TOWER -> {
                poly(ink, 38f to g, 46f to 30f, 48f to 12f, 50f to 4f, 52f to 12f, 54f to 30f, 62f to g, 57f to g, 52f to 36f, 48f to 36f, 43f to g)
                rect(44f, 30f, 12f, 2f, ink)
                rect(46.5f, 18f, 7f, 1.5f, ink)
            }
            Landmark.MINARET -> {
                rect(44f, 14f, 12f, 36f, ink)
                rect(42.5f, 12f, 15f, 3f, ink)
                rect(47f, 6f, 6f, 6f, ink)
                dome(50f, 6f, 3f, ink)
                for (y in listOf(20f, 28f, 36f)) rect(48f, y, 4f, 5f, light.copy(alpha = 0.5f))
                rect(10f, 42f, 20f, 8f, ink.copy(alpha = 0.7f))
                rect(70f, 40f, 22f, 10f, ink.copy(alpha = 0.7f))
            }
            Landmark.DOME -> {
                dome(50f, 34f, 16f, ink)
                rect(30f, 34f, 40f, 16f, ink)
                tri(26f, 34f, 50f, 24f, 74f, 34f, ink)
                for (x in listOf(30f, 37f, 44f, 51f, 58f, 65f)) rect(x + 1f, 36f, 2f, 14f, light.copy(alpha = 0.45f))
            }
            Landmark.TEMPLE -> {
                rect(22f, 46f, 56f, 4f, ink)
                for (i in 0..7) rect(24f + i * 7f, 30f, 3.2f, 16f, ink)
                rect(22f, 27f, 56f, 3f, ink)
                tri(20f, 27f, 50f, 18f, 80f, 27f, ink)
            }
            Landmark.MOSQUE -> {
                rect(30f, 34f, 40f, 16f, ink)
                dome(50f, 34f, 13f, ink)
                dome(36f, 34f, 5f, ink)
                dome(64f, 34f, 5f, ink)
                for (x in listOf(22f, 76f)) {
                    rect(x, 12f, 2.5f, 38f, ink)
                    tri(x - 0.5f, 12f, x + 1.25f, 5f, x + 3f, 12f, ink)
                }
            }
            Landmark.ROCK -> {
                poly(ink.copy(alpha = 0.55f), 0f to g, 0f to 8f, 30f to 2f, 70f to 4f, 100f to 10f, 100f to g)
                rect(34f, 22f, 32f, 28f, ink)
                tri(34f, 22f, 50f, 14f, 66f, 22f, ink)
                for (x in listOf(36f, 43f, 54f, 61f)) rect(x, 28f, 2.5f, 22f, light.copy(alpha = 0.35f))
                rect(47f, 34f, 6f, 16f, Color.Black.copy(alpha = 0.35f))
            }
            Landmark.PYRAMIDS -> {
                tri(14f, g, 38f, 18f, 62f, g, ink)
                tri(46f, g, 64f, 26f, 82f, g, ink.copy(alpha = 0.85f))
                tri(76f, g, 86f, 36f, 96f, g, ink.copy(alpha = 0.7f))
                tri(38f, 18f, 62f, g, 46f, g, Color.Black.copy(alpha = 0.15f))
            }
            Landmark.ROCK_CHURCH -> {
                poly(Color.Black.copy(alpha = 0.25f), 26f to g, 30f to 40f, 70f to 40f, 74f to g)
                rect(44f, 30f, 12f, 20f, ink)
                rect(34f, 36f, 32f, 9f, ink)
                rect(47.5f, 20f, 5f, 10f, ink)
                rect(45f, 23f, 10f, 3f, ink)
            }
            Landmark.IWAN -> {
                rect(28f, 30f, 44f, 20f, ink)
                onion(50f, 30f, 22f, 18f, Color(0xFF26A69A))
                rect(43f, 36f, 14f, 14f, light.copy(alpha = 0.55f))
                dome(50f, 36f, 7f, light.copy(alpha = 0.55f))
                for (x in listOf(24f, 73.5f)) rect(x, 18f, 2.5f, 32f, ink)
            }
            Landmark.TAJ -> {
                rect(30f, 32f, 40f, 18f, ink)
                onion(50f, 32f, 20f, 20f, ink)
                rect(49.5f, 9f, 1f, 4f, ink)
                onion(37f, 32f, 6f, 6f, ink)
                onion(63f, 32f, 6f, 6f, ink)
                rect(45f, 38f, 10f, 12f, Color.Black.copy(alpha = 0.18f))
                for (x in listOf(20f, 78f)) {
                    rect(x, 22f, 2.5f, 28f, ink)
                    dome(x + 1.25f, 22f, 2f, ink)
                }
            }
            Landmark.STUPA -> {
                dome(50f, g, 18f, ink)
                rect(44f, 22f, 12f, 10f, Color(0xFFD4A017))
                poly(Color(0xFFD4A017), 45f to 22f, 50f to 8f, 55f to 22f)
                for (x in listOf(46f, 52f)) circle(x + 1f, 27f, 1f, Color(0xFF1A237E))
                rect(10f, 6f, 1f, 30f, ink.copy(alpha = 0.5f))
                for (i in 0..5) rect(11f + i * 6f, 6f + i * 2f, 4f, 3f, listOf(Color(0xFFE53935), Color(0xFFFDD835), Color(0xFF43A047), Color(0xFF1E88E5), Color.White)[i % 5])
            }
            Landmark.HALL -> {
                rect(24f, 44f, 52f, 6f, ink.copy(alpha = 0.8f))
                rect(30f, 30f, 40f, 14f, ink)
                curvedRoof(50f, 30f, 56f, 10f, ink)
                curvedRoof(50f, 21f, 36f, 7f, ink)
                for (x in listOf(33f, 41f, 49f, 57f, 65f)) rect(x, 32f, 2f, 12f, light.copy(alpha = 0.35f))
            }
            Landmark.PAGODA -> {
                rect(47f, 14f, 6f, 36f, ink)
                for (i in 0..4) curvedRoof(50f, 46f - i * 7.5f, 30f - i * 4f, 4f, ink)
                rect(49.5f, 4f, 1f, 8f, ink)
            }
            Landmark.LANTERNS -> {
                for ((x, w, h) in listOf(Triple(8f, 18f, 18f), Triple(28f, 20f, 22f), Triple(50f, 18f, 16f), Triple(70f, 22f, 20f))) {
                    rect(x, g - h, w, h, ink)
                    tri(x - 2f, g - h, x + w / 2, g - h - 6f, x + w + 2f, g - h, ink)
                    rect(x + w / 2 - 2f, g - h / 2, 4f, 5f, Color(0xFFFFC107).copy(alpha = 0.6f))
                }
                d.drawLine(Color.White.copy(alpha = 0.3f), p(0f, 14f), p(100f, 20f), strokeWidth = 0.3f * s)
                for (i in 0..9) {
                    val x = 5f + i * 10f
                    circle(x, 15f + x * 0.06f + 2.5f, 2f, listOf(Color(0xFFFF7043), Color(0xFFFFCA28), Color(0xFFEC407A))[i % 3])
                }
            }
            Landmark.KHMER -> {
                rect(20f, 40f, 60f, 10f, ink)
                for ((x, h) in listOf(28f to 16f, 40f to 22f, 50f to 30f, 60f to 22f, 72f to 16f)) {
                    poly(ink, x - 4f to 40f, x - 2.5f to 40f - h * 0.6f, x - 1f to 40f - h, x + 1f to 40f - h, x + 2.5f to 40f - h * 0.6f, x + 4f to 40f)
                }
                d.drawRect(light.copy(alpha = 0.25f), topLeft = p(-50f, 52f), size = Size(200f * s, 8f * s))
            }
            Landmark.SPLIT_GATE -> {
                poly(ink, 24f to g, 26f to 30f, 32f to 22f, 36f to 12f, 44f to 12f, 44f to g)
                poly(ink, 56f to g, 56f to 12f, 64f to 12f, 68f to 22f, 74f to 30f, 76f to g)
                for (y in listOf(20f, 28f, 36f)) {
                    rect(36f, y, 8f, 1.2f, light.copy(alpha = 0.4f))
                    rect(56f, y, 8f, 1.2f, light.copy(alpha = 0.4f))
                }
                rect(40f, 46f, 20f, 4f, ink.copy(alpha = 0.7f))
            }
            Landmark.OPERA -> {
                rect(18f, 44f, 64f, 6f, ink.copy(alpha = 0.85f))
                for ((x, w, h) in listOf(Triple(24f, 16f, 18f), Triple(36f, 18f, 24f), Triple(52f, 16f, 20f), Triple(64f, 14f, 15f))) {
                    poly(ink, x to 44f, x + w * 0.2f to 44f - h, x + w to 44f)
                }
                d.drawLine(Color.Black.copy(alpha = 0.2f), p(18f, 44f), p(82f, 44f), strokeWidth = 0.6f * s)
            }
            Landmark.MEETING_HOUSE -> {
                rect(34f, 32f, 32f, 18f, ink)
                poly(ink, 28f to 34f, 50f to 18f, 72f to 34f, 68f to 35f, 50f to 22f, 32f to 35f)
                rect(46f, 38f, 8f, 12f, Color.Black.copy(alpha = 0.3f))
                circle(50f, 17f, 1.5f, ink)
                // Geyser.
                poly(Color.White.copy(alpha = 0.75f), 82f to g, 84f to 30f, 86f to 18f, 88f to 30f, 90f to g)
                circle(86f, 18f, 3f, Color.White.copy(alpha = 0.6f))
            }
            Landmark.PEAKS -> {
                poly(ink, 10f to g, 40f to 10f, 58f to 30f, 70f to 14f, 96f to g)
                for (i in 0..4) d.drawLine(light.copy(alpha = 0.5f), p(30f + i * 2f, 30f + i * 4f), p(55f - i * 1f, 32f + i * 4f), strokeWidth = 0.6f * s)
                tri(36f, 16f, 40f, 10f, 44f, 16f, Color.White.copy(alpha = 0.7f))
            }
            Landmark.STATUE -> {
                poly(ink.copy(alpha = 0.85f), 20f to g, 46f to 22f, 54f to 22f, 80f to g)
                rect(48.5f, 14f, 3f, 9f, ink)
                rect(40f, 15f, 20f, 2f, ink)
                circle(50f, 12.5f, 1.8f, ink)
                d.drawLine(Color.White.copy(alpha = 0.5f), p(0f, 54f), p(100f, 54f), strokeWidth = 0.4f * s)
            }
            Landmark.COLONIAL -> {
                val colors = listOf(Color(0xFF4FC3F7), Color(0xFFFFB74D), Color(0xFFF06292), Color(0xFFAED581))
                colors.forEachIndexed { i, c ->
                    val x = 14f + i * 15f
                    rect(x, 28f, 15f, 22f, c)
                    rect(x, 26f, 15f, 2f, ink)
                    for (wx in listOf(x + 2.5f, x + 9f)) rect(wx, 32f, 3.5f, 6f, ink.copy(alpha = 0.6f))
                    rect(x + 5.5f, 41f, 4f, 9f, ink.copy(alpha = 0.6f))
                }
                rect(82.5f, 22f, 1.5f, 28f, ink)
                for (a in listOf(-30f to -6f, -20f to 2f, 30f to -6f, 20f to 2f)) {
                    poly(Color(0xFF2E7D32), 83f to 22f, 83f + a.first * 0.35f to 22f + a.second, 83f + a.first * 0.25f to 24f + a.second)
                }
            }
            Landmark.STEP_PYRAMID -> {
                for (i in 0..4) rect(24f + i * 5f, 44f - i * 6f, 52f - i * 10f, 6f, ink.copy(alpha = 1f - i * 0.05f))
                rect(44f, 14f, 12f, 6f, ink)
                rect(47.5f, 20f, 5f, 30f, light.copy(alpha = 0.25f))
            }
            Landmark.SKYLINE -> {
                for ((x, w, h) in listOf(
                    Triple(6f, 10f, 20f), Triple(16f, 8f, 28f), Triple(24f, 12f, 18f), Triple(36f, 9f, 34f),
                    Triple(56f, 10f, 26f), Triple(66f, 8f, 38f), Triple(74f, 12f, 22f), Triple(86f, 10f, 30f),
                )) {
                    rect(x, g - h, w, h, ink)
                    for (wy in 0 until (h / 4).toInt()) rect(x + 2f, g - h + 2f + wy * 4f, w - 4f, 1f, Color(0xFFFFE082).copy(alpha = 0.45f))
                }
                // Empire State Building.
                rect(45f, 14f, 10f, 36f, ink)
                rect(47f, 8f, 6f, 6f, ink)
                rect(49.5f, 2f, 1f, 6f, ink)
            }
            Landmark.CHURCH -> {
                poly(ink, 30f to g, 30f to 40f, 38f to 36f, 38f to 30f, 44f to 26f, 44f to 18f, 48f to 14f, 50f to 4f, 52f to 14f, 56f to 18f, 56f to 26f, 62f to 30f, 62f to 36f, 70f to 40f, 70f to g)
                rect(48f, 38f, 4f, 12f, Color.Black.copy(alpha = 0.3f))
            }
            else -> europeLandmark(kind, ink, light)
        }
    }
}
