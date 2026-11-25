/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Green = Color(0xFF2E7D5B)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB4F1D2),
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFF4D6357),
    tertiary = Color(0xFF3D6373),
    background = Color(0xFFF6FBF4),
    surface = Color(0xFFF6FBF4),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF98D5B6),
    onPrimary = Color(0xFF003824),
    primaryContainer = Color(0xFF0F5137),
    onPrimaryContainer = Color(0xFFB4F1D2),
    secondary = Color(0xFFB4CCBD),
    tertiary = Color(0xFFA5CCDF),
    background = Color(0xFF101510),
    surface = Color(0xFF101510),
)

/** Couleurs de priorité, comme Tasks.org : rouge, orange, bleu, gris. */
object PriorityColors {
    val High = Color(0xFFE53935)
    val Medium = Color(0xFFFB8C00)
    val Low = Color(0xFF1E88E5)
    val None = Color(0xFF9E9E9E)

    fun of(priority: Int): Color = when (priority) {
        3 -> High
        2 -> Medium
        1 -> Low
        else -> None
    }
}

/** Couleurs proposées pour les listes. */
val ListPalette: List<Long> = listOf(
    0xFF2E7D5B, 0xFFFF9800, 0xFF42A5F5, 0xFFAB47BC,
    0xFFEF5350, 0xFF26A69A, 0xFF8D6E63, 0xFF5C6BC0,
)

@Composable
fun SentierTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
