/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.celebrate

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Ce que l'on fête. */
data class Celebration(
    val key: String,
    val title: String,
    val text: String,
    val icon: ImageVector,
)

/** Vrai si les animations sont coupées dans les réglages d'accessibilité du téléphone. */
@Composable
fun animationsOff(): Boolean {
    val context = LocalContext.current
    return remember {
        runCatching { Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
            .getOrDefault(false)
    }
}

private val ConfettiColors = listOf(
    Color(0xFF2E7D5B), Color(0xFFE0A100), Color(0xFFEF5350), Color(0xFF42A5F5),
    Color(0xFFAB47BC), Color(0xFF26A69A), Color(0xFFFF9800),
)

private class Piece(
    val x: Float, val vx: Float, val vy: Float, val spin: Float,
    val color: Color, val w: Float, val h: Float, val sway: Float,
)

/** Pluie de confettis qui tombe une fois, sans bloquer les gestes. Rien si les animations sont coupées. */
@Composable
fun Confetti(trigger: Any, modifier: Modifier = Modifier) {
    if (animationsOff()) return
    val pieces = remember(trigger) {
        val r = Random(trigger.hashCode())
        List(140) {
            val angle = Math.toRadians(r.nextDouble(-120.0, -60.0))
            val speed = r.nextDouble(0.6, 1.4).toFloat()
            Piece(
                x = r.nextFloat(),
                vx = (cos(angle) * speed * 0.35).toFloat(),
                vy = (sin(angle) * speed).toFloat(),
                spin = r.nextFloat() * 720f - 360f,
                color = ConfettiColors[r.nextInt(ConfettiColors.size)],
                w = r.nextFloat() * 6f + 6f,
                h = r.nextFloat() * 4f + 4f,
                sway = r.nextFloat() * 6.28f,
            )
        }
    }
    val time = remember(trigger) { Animatable(0f) }
    LaunchedEffect(trigger) { time.animateTo(1f, tween(2800, easing = LinearEasing)) }
    Canvas(modifier.fillMaxSize()) {
        val t = time.value
        if (t >= 1f) return@Canvas
        val alpha = if (t > 0.75f) (1f - t) / 0.25f else 1f
        for (p in pieces) {
            // Lancé vers le haut depuis le bas de l'écran, puis retombe avec la gravité.
            val x = (p.x + p.vx * t) * size.width + sin(p.sway + t * 12f) * 12.dp.toPx()
            val y = size.height * (1.02f + p.vy * 1.7f * t + 1.6f * t * t)
            if (y > size.height + 20f) continue
            rotate(p.spin * t, Offset(x, y)) {
                drawRect(
                    p.color.copy(alpha = alpha),
                    topLeft = Offset(x - p.w.dp.toPx() / 2, y - p.h.dp.toPx() / 2),
                    size = Size(p.w.dp.toPx(), p.h.dp.toPx()),
                )
            }
        }
    }
}

/** Carte de fête par-dessus l'appli : icône qui rebondit, texte et confettis. */
@Composable
fun CelebrationOverlay(celebration: Celebration, onDismiss: () -> Unit) {
    val pop = remember(celebration.key) { Animatable(0.4f) }
    LaunchedEffect(celebration.key) { pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            Modifier
                .padding(32.dp)
                .widthIn(max = 360.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier.size(84.dp).scale(pop.value).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(celebration.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(48.dp))
                }
                Text(celebration.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text(celebration.text, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                Button(onClick = onDismiss) { Text("Génial !") }
            }
        }
        Confetti(celebration.key)
    }
}
