/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.steps

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.sentier.SentierApplication
import java.util.concurrent.TimeUnit

/** Relève le capteur de pas toutes les 15 minutes quand il sert de source. */
class StepSampleWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as SentierApplication
        runCatching { app.steps.sampleSensor() }
        return Result.success()
    }

    companion object {
        private const val NAME = "releve-capteur-pas"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<StepSampleWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(NAME)
        }
    }
}
