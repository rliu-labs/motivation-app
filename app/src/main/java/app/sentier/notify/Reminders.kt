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
import app.sentier.data.GameRepository
import app.sentier.data.HabitRepository
import app.sentier.data.ReminderSettings
import app.sentier.data.TaskRepository
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

enum class ReminderKind(val workName: String) {
    MORNING("rappel-matin"),
    EVENING("rappel-soir"),
}

/** Programme les rappels avec WorkManager (ils survivent au redémarrage du téléphone). */
object Reminders {
    /** Un rappel qui arrive plus de 2 heures en retard (téléphone en veille profonde) n'est pas affiché. */
    private val MAX_DELAY: Duration = Duration.ofHours(2)

    /** Applique de nouveaux réglages : reprogramme ce qui est actif, annule le reste. */
    fun apply(context: Context, settings: ReminderSettings) {
        val wm = WorkManager.getInstance(context)
        for (kind in ReminderKind.entries) {
            val (on, minute) = settings.of(kind)
            if (on) schedule(context, kind, minute, ExistingWorkPolicy.REPLACE) else wm.cancelUniqueWork(kind.workName)
        }
        WeeklyReviewReminder.apply(context, settings)
    }

    /** À l'ouverture de l'appli : s'assure que les rappels actifs sont bien programmés. */
    suspend fun ensureScheduled(app: SentierApplication) {
        val settings = app.settings.currentReminders()
        if (!settings.anyOn) return
        for (kind in ReminderKind.entries) {
            val (on, minute) = settings.of(kind)
            if (on) schedule(app, kind, minute, ExistingWorkPolicy.KEEP)
        }
        WeeklyReviewReminder.ensureScheduled(app)
    }

    internal fun schedule(context: Context, kind: ReminderKind, minute: Int, policy: ExistingWorkPolicy) {
        val now = LocalDateTime.now()
        val next = ReminderContent.nextOccurrence(now, minute)
        val at = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(Duration.between(now, next))
            .setInputData(workDataOf(ReminderWorker.KEY_KIND to kind.name, ReminderWorker.KEY_AT to at))
            .addTag("rappel")
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(kind.workName, policy, request)
    }

    /** Calcule le message du rappel à partir des tâches, habitudes et de la série du jour. */
    suspend fun build(
        taskRepo: TaskRepository,
        habitRepo: HabitRepository,
        game: GameRepository,
        kind: ReminderKind,
    ): ReminderMessage? {
        val today = taskRepo.today().toEpochDay()
        val tasks = ReminderContent.pendingTasks(taskRepo.observeToday().first(), today)
        val habits = ReminderContent.pendingHabits(habitRepo.observeHabitsWithStats().first(), today)
        val streak = game.observe().first().streak
        return when (kind) {
            ReminderKind.MORNING -> ReminderContent.morning(tasks, habits, streak)
            ReminderKind.EVENING -> ReminderContent.evening(tasks, habits, streak)
        }
    }

    /** Construit et affiche le rappel tout de suite. Renvoie false s'il n'y avait rien à dire. */
    suspend fun show(app: SentierApplication, kind: ReminderKind): Boolean {
        val message = build(app.tasks, app.habits, app.game, kind) ?: return false
        when (kind) {
            ReminderKind.MORNING -> Notifier.showMorning(app, message)
            ReminderKind.EVENING -> Notifier.showEvening(app, message)
        }
        return true
    }

    internal fun isTooLate(targetMillis: Long, nowMillis: Long): Boolean =
        nowMillis - targetMillis > MAX_DELAY.toMillis()

}

/** Rappel actif et heure (minutes depuis minuit) pour [kind]. */
internal fun ReminderSettings.of(kind: ReminderKind): Pair<Boolean, Int> = when (kind) {
    ReminderKind.MORNING -> morningOn to morningMinute
    ReminderKind.EVENING -> eveningOn to eveningMinute
}

/** Affiche un rappel, puis programme le suivant pour le lendemain. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as SentierApplication
        val kind = inputData.getString(KEY_KIND)?.let { runCatching { ReminderKind.valueOf(it) }.getOrNull() }
            ?: return Result.success()
        val (on, minute) = app.settings.currentReminders().of(kind)
        if (!on) return Result.success()

        val target = inputData.getLong(KEY_AT, System.currentTimeMillis())
        if (!Reminders.isTooLate(target, System.currentTimeMillis())) {
            runCatching { Reminders.show(app, kind) }
        }
        // Le suivant attend la fin de celui-ci, puis part pour le lendemain à la même heure.
        Reminders.schedule(app, kind, minute, ExistingWorkPolicy.APPEND_OR_REPLACE)
        return Result.success()
    }

    companion object {
        const val KEY_KIND = "kind"
        const val KEY_AT = "at"
    }
}
