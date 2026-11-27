/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.common

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Petite vibration de confirmation quand on coche quelque chose. Elle suit le réglage
 * « Vibration au toucher » du téléphone : rien ne vibre s'il est coupé.
 */
@Composable
fun rememberTick(): () -> Unit {
    val view = LocalView.current
    return remember(view) {
        {
            val effect = if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY
            view.performHapticFeedback(effect)
        }
    }
}
