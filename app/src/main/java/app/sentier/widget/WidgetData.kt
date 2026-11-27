/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.widget

import app.sentier.data.GameRepository
import app.sentier.data.HabitDao
import app.sentier.data.HabitRepository
import app.sentier.data.Settings
import app.sentier.data.StepSource
import app.sentier.data.TaskRepository
import app.sentier.notify.ReminderContent
import app.sentier.notify.ReminderItem
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.Locale

/** Ce qu'affichent les widgets de l'écran d'accueil. */
data class WidgetData(
    val streak: Int = 0,
    /** Vrai si aujourd'hui compte déjà dans la série. */
    val activeToday: Boolean = false,
    val level: Int = 1,
    val steps: Long = 0,
    val stepGoal: Int = 8000,
    /** Faux si le comptage des pas n'est pas activé. */
    val stepsOn: Boolean = false,
    /** Les premières choses à faire aujourd'hui (tâches puis habitudes). */
    val items: List<ReminderItem> = emptyList(),
    /** Nombre total de choses à faire aujourd'hui. */
    val remaining: Int = 0,
) {
    val stepProgress: Float get() = if (stepGoal <= 0) 0f else (steps.toFloat() / stepGoal).coerceIn(0f, 1f)

    val stepsText: String get() = formatInt(steps)
    val goalText: String get() = formatInt(stepGoal.toLong())

    val streakText: String
        get() = when (streak) {
            0 -> "Pas de série"
            1 -> "1 jour"
            else -> "$streak jours"
        }

    /** Résumé sous le titre du widget « Aujourd'hui ». */
    val summary: String
        get() = when {
            remaining == 0 -> "Tout est fait pour aujourd'hui. Bravo !"
            remaining == 1 -> "Encore 1 chose à faire"
            else -> "Encore $remaining choses à faire"
        }

    companion object {
        /** Nombre de lignes affichées au plus dans le widget « Aujourd'hui ». */
        const val MAX_ITEMS = 8

        private fun formatInt(n: Long): String = NumberFormat.getIntegerInstance(Locale.FRANCE).format(n)

        suspend fun load(
            tasks: TaskRepository,
            habits: HabitRepository,
            habitDao: HabitDao,
            game: GameRepository,
            settings: Settings,
        ): WidgetData {
            val today = tasks.today().toEpochDay()
            val pending = ReminderContent.pendingTasks(tasks.observeToday().first(), today) +
                ReminderContent.pendingHabits(habits.observeHabitsWithStats().first(), today)
            val state = game.observe().first()
            val stepSettings = settings.currentSteps()
            val steps = habitDao.observeStepsSince(today).first().firstOrNull { it.day == today }?.steps ?: 0L
            return WidgetData(
                streak = state.streak.current,
                activeToday = state.streak.activeToday,
                level = state.level.level,
                steps = steps,
                stepGoal = stepSettings.goal,
                stepsOn = stepSettings.source != StepSource.NONE,
                items = pending.take(MAX_ITEMS),
                remaining = pending.size,
            )
        }
    }
}
