/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.steps

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Capteur de pas du téléphone, en secours si Health Connect n'a pas de données.
 * Le capteur donne le total depuis le dernier redémarrage ; on calcule les pas
 * du jour par différence entre deux lectures (voir StepsRepository).
 */
class SensorStepSource(context: Context) {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val sensor: Sensor? = manager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    val isAvailable: Boolean get() = sensor != null

    /** Lit le compteur cumulé, ou null si indisponible. */
    suspend fun readCounter(): Float? {
        val m = manager ?: return null
        val s = sensor ?: return null
        return withTimeoutOrNull(5_000) {
            suspendCancellableCoroutine { cont ->
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        m.unregisterListener(this)
                        if (cont.isActive) cont.resume(event.values[0])
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
                }
                m.registerListener(listener, s, SensorManager.SENSOR_DELAY_NORMAL)
                cont.invokeOnCancellation { m.unregisterListener(listener) }
            }
        }
    }

    companion object {
        /**
         * Nouvelle valeur des pas du jour à partir d'une lecture du compteur.
         * Si le compteur a baissé, le téléphone a redémarré : la lecture entière compte.
         */
        fun accumulate(lastCounter: Float?, lastDay: Long?, todaySoFar: Long, counter: Float, today: Long): Long {
            val base = if (lastDay == today) todaySoFar else 0L
            if (lastCounter == null) return base
            val delta = if (counter >= lastCounter) counter - lastCounter else counter
            return base + delta.toLong()
        }
    }
}
