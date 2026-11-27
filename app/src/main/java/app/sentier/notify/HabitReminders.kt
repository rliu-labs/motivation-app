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
import app.sentier.data.Habit
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

/** Rappel de chaque habitude, à l'heure choisie dans sa fiche. */
object HabitReminders {
    private const val TAG = "rappel-habitude"

    private fun workName(habitId: Long) = "$TAG-$habitId"

    /** Après l'enregistrement d'une habitude : programme son rappel, ou l'annule. */
    fun update(context: Context, habit: Habit) {
        val minute = habit.reminderMinute
        if (minute == null || habit.archived) {
            cancel(context, habit.id)
        } else {
            schedule(context, habit.id, minute, ExistingWorkPolicy.REPLACE)
        }
    }

    fun cancel(context: Context, habitId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(habitId))
    }

    /** À l'ouverture de l'appli : s'assure que les rappels des habitudes sont bien programmés. */
    suspend fun ensureScheduled(app: SentierApplication) {
        for (h in app.database.habitDao().habitsWithReminder()) {
            schedule(app, h.id, h.reminderMinute ?: continue, ExistingWorkPolicy.KEEP)
        }
    }

    /** Après une restauration : repart de zéro avec les habitudes de la sauvegarde. */
    suspend fun rescheduleAll(app: SentierApplication) {
        WorkManager.getInstance(app).cancelAllWorkByTag(TAG)
        for (h in app.database.habitDao().habitsWithReminder()) {
            schedule(app, h.id, h.reminderMinute ?: continue, ExistingWorkPolicy.REPLACE)
        }
    }

    internal fun schedule(context: Context, habitId: Long, minute: Int, policy: ExistingWorkPolicy) {
        val now = LocalDateTime.now()
        val next = ReminderContent.nextOccurrence(now, minute)
        val at = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val request = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInitialDelay(Duration.between(now, next))
            .setInputData(workDataOf(HabitReminderWorker.KEY_ID to habitId, HabitReminderWorker.KEY_AT to at))
            .addTag(TAG)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(workName(habitId), policy, request)
    }

    /** Affiche le rappel de l'habitude tout de suite. Renvoie false s'il n'y avait rien à rappeler. */
    suspend fun show(app: SentierApplication, habitId: Long): Boolean {
        val stats = app.habits.observeHabitWithStats(habitId).first() ?: return false
        val message = ReminderContent.habitReminder(stats, app.habits.today().toEpochDay()) ?: return false
        Notifier.showHabit(app, message)
        return true
    }
}

/** Affiche le rappel d'une habitude, puis programme celui du lendemain. */
class HabitReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as SentierApplication
        val id = inputData.getLong(KEY_ID, -1L).takeIf { it > 0 } ?: return Result.success()
        // Habitude supprimée, archivée ou sans rappel : on s'arrête là.
        val habit = app.database.habitDao().getHabit(id) ?: return Result.success()
        val minute = habit.reminderMinute ?: return Result.success()
        if (habit.archived) return Result.success()

        val target = inputData.getLong(KEY_AT, System.currentTimeMillis())
        if (!Reminders.isTooLate(target, System.currentTimeMillis())) {
            runCatching { HabitReminders.show(app, id) }
        }
        HabitReminders.schedule(app, id, minute, ExistingWorkPolicy.APPEND_OR_REPLACE)
        return Result.success()
    }

    companion object {
        const val KEY_ID = "id"
        const val KEY_AT = "at"
    }
}
