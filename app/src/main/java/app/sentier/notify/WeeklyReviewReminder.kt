/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import app.sentier.SentierApplication
import app.sentier.data.ReminderSettings
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** Le bilan de la semaine, chaque dimanche à l'heure choisie. */
object WeeklyReviewReminder {
    private const val WORK = "bilan-semaine"

    /** Prochain dimanche à [minute] (minutes depuis minuit), strictement après [now]. */
    fun nextOccurrence(now: LocalDateTime, minute: Int): LocalDateTime {
        val sunday = now.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val at = sunday.atStartOfDay().plusMinutes(minute.toLong())
        return if (at.isAfter(now)) at else at.plusWeeks(1)
    }

    /** Applique les réglages : programme le bilan, ou l'annule. */
    fun apply(context: Context, settings: ReminderSettings) {
        if (settings.weeklyOn) {
            schedule(context, settings.weeklyMinute, ExistingWorkPolicy.REPLACE)
        } else {
            WorkManager.getInstance(context).cancelUniqueWork(WORK)
        }
    }

    /** À l'ouverture de l'appli : s'assure que le bilan est bien programmé. */
    suspend fun ensureScheduled(app: SentierApplication) {
        val settings = app.settings.currentReminders()
        if (settings.weeklyOn) schedule(app, settings.weeklyMinute, ExistingWorkPolicy.KEEP)
    }

    internal fun schedule(context: Context, minute: Int, policy: ExistingWorkPolicy) {
        val now = LocalDateTime.now()
        val next = nextOccurrence(now, minute)
        val at = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val request = OneTimeWorkRequestBuilder<WeeklyReviewWorker>()
            .setInitialDelay(Duration.between(now, next))
            .setInputData(workDataOf(WeeklyReviewWorker.KEY_AT to at))
            .addTag(WORK)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK, policy, request)
    }

    /** Affiche le bilan tout de suite. */
    suspend fun show(app: SentierApplication) {
        val review = app.weekly.current()
        Notifier.showWeeklyReview(app, review.headline, review.summary)
    }
}

/** Affiche le bilan du dimanche, puis programme celui de la semaine suivante. */
class WeeklyReviewWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as SentierApplication
        val settings = app.settings.currentReminders()
        if (!settings.weeklyOn) return Result.success()
        val target = inputData.getLong(KEY_AT, System.currentTimeMillis())
        if (!Reminders.isTooLate(target, System.currentTimeMillis())) {
            runCatching { WeeklyReviewReminder.show(app) }
        }
        WeeklyReviewReminder.schedule(app, settings.weeklyMinute, ExistingWorkPolicy.APPEND_OR_REPLACE)
        return Result.success()
    }

    companion object {
        const val KEY_AT = "at"
    }
}
