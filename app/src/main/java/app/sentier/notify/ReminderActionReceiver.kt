/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.notify

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.sentier.SentierApplication
import app.sentier.data.HabitRepository
import app.sentier.data.TaskRepository
import app.sentier.domain.GameRules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Bouton « Fait » d'une notification : valide la tâche ou l'habitude sans ouvrir l'appli. */
class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DONE) return
        val type = intent.getStringExtra(EXTRA_TYPE)?.let { runCatching { ReminderItem.Type.valueOf(it) }.getOrNull() } ?: return
        val id = intent.getLongExtra(EXTRA_ID, -1L).takeIf { it > 0 } ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION, Notifier.itemId(type, id))
        val app = context.applicationContext as SentierApplication
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val text = runCatching { ReminderActions.complete(app.tasks, app.habits, type, id) }.getOrNull()
                if (text != null) Notifier.showDone(app, notificationId, text) else Notifier.cancel(app, notificationId)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_DONE = "app.sentier.action.FAIT"
        private const val EXTRA_TYPE = "type"
        private const val EXTRA_ID = "id"
        private const val EXTRA_NOTIFICATION = "notification"

        fun intent(context: Context, type: ReminderItem.Type, id: Long, notificationId: Int): PendingIntent =
            PendingIntent.getBroadcast(
                context, notificationId,
                Intent(context, ReminderActionReceiver::class.java)
                    .setAction(ACTION_DONE)
                    .putExtra(EXTRA_TYPE, type.name)
                    .putExtra(EXTRA_ID, id)
                    .putExtra(EXTRA_NOTIFICATION, notificationId),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
    }
}

object ReminderActions {
    /**
     * Valide la tâche ou l'habitude et renvoie le message de bravo,
     * ou null si elle n'existe plus ou était déjà faite.
     */
    suspend fun complete(tasks: TaskRepository, habits: HabitRepository, type: ReminderItem.Type, id: Long): String? = when (type) {
        ReminderItem.Type.TASK -> {
            tasks.complete(id)?.let { result ->
                val gain = result.gain?.let { " +${it.xp} XP, +${it.gold} or" }.orEmpty()
                "« ${result.before.title} » : c'est fait !$gain"
            }
        }
        ReminderItem.Type.HABIT -> {
            val today = habits.today()
            val stats = habits.observeHabitWithStats(id).first()
            if (stats == null || stats.habit.isNumeric || stats.isDoneOn(today.toEpochDay())) {
                null
            } else {
                habits.toggle(stats.habit, today)
                val xp = GameRules.xpFor(stats.habit.difficulty)
                "« ${stats.habit.name} » : c'est fait ! +$xp XP, +${GameRules.goldFor(xp)} or"
            }
        }
    }
}
