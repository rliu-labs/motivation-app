/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.notify

import app.sentier.domain.tr
import app.sentier.data.HabitWithStats
import app.sentier.data.TaskWithCounts
import app.sentier.data.isDoneOn
import app.sentier.domain.StreakInfo
import app.sentier.ui.habits.formatValue
import java.time.LocalDateTime
import java.time.LocalTime

/** Une chose à faire, telle qu'affichée dans un rappel. */
data class ReminderItem(
    val type: Type,
    val id: Long,
    val title: String,
    /** Précision affichée sous le titre (ex. « 5 / 8 verres », « En retard »). */
    val detail: String? = null,
    /** Vrai si on peut la valider d'un geste depuis la notification. */
    val canComplete: Boolean = true,
) {
    enum class Type { TASK, HABIT }
}

/** Contenu d'un rappel : le résumé et les choses listées. */
data class ReminderMessage(
    val title: String,
    val text: String,
    val items: List<ReminderItem>,
)

/**
 * Textes des rappels du matin et du soir. Tout est calculé ici, sans Android,
 * pour être testé facilement.
 */
object ReminderContent {
    /** Nombre maximum de choses détaillées dans un rappel. */
    const val MAX_ITEMS = 5

    /** Tâches encore à faire aujourd'hui (en retard comprises). */
    fun pendingTasks(tasks: List<TaskWithCounts>, today: Long): List<ReminderItem> =
        tasks.filter { !it.isDoneOn(today) }.map { t ->
            val late = t.dueDay != null && t.dueDay < today
            ReminderItem(ReminderItem.Type.TASK, t.id, t.title, detail = if (late) tr("En retard", "Overdue") else null)
        }

    /** Habitudes encore à faire aujourd'hui. */
    fun pendingHabits(habits: List<HabitWithStats>, today: Long): List<ReminderItem> =
        habits.filter { isDueToday(it, today) }.map { h ->
            val detail = if (h.habit.isNumeric) {
                "${formatValue(h.valueOn(today))} / ${formatValue(h.habit.target)} ${h.habit.unit}".trim()
            } else null
            ReminderItem(ReminderItem.Type.HABIT, h.habit.id, h.habit.name, detail, canComplete = !h.habit.isNumeric)
        }

    /**
     * Une habitude est à faire aujourd'hui si elle ne l'est pas déjà et si son quota
     * n'est pas atteint : « 3 fois par semaine » déjà fait 3 fois ces 7 derniers jours
     * ne rappelle rien.
     */
    fun isDueToday(h: HabitWithStats, today: Long): Boolean {
        if (h.habit.archived || h.isDoneOn(today)) return false
        if (h.habit.freqNum >= h.habit.freqDen) return true
        val window = (today - h.habit.freqDen + 1)..today
        return window.count { h.isDoneOn(it) } < h.habit.freqNum
    }

    /** Le matin : le programme de la journée. Rien à envoyer si la journée est vide. */
    fun morning(tasks: List<ReminderItem>, habits: List<ReminderItem>, streak: StreakInfo): ReminderMessage? {
        if (tasks.isEmpty() && habits.isEmpty()) return null
        val late = tasks.count { it.detail == tr("En retard", "Overdue") }
        val lateText = if (late > 0) tr(" (dont $late en retard)", " ($late overdue)") else ""
        val tasksText = if (tasks.isNotEmpty()) count(tasks.size, tr("tâche", "task"), tr("tâches", "tasks")) + lateText else null
        val habitsText = if (habits.isNotEmpty()) count(habits.size, tr("habitude", "habit"), tr("habitudes", "habits")) else null
        val streakText = if (streak.current > 1) tr(" Série en cours : ${streak.current} jours.", " Current streak: ${streak.current} days.") else ""
        return ReminderMessage(
            title = tr("Au programme aujourd'hui", "On the plan today"),
            text = listOfNotNull(tasksText, habitsText).joinToString(tr(" et ", " and ")) + "." + streakText,
            items = (tasks + habits).take(MAX_ITEMS),
        )
    }

    /** Le soir : ce qui reste, à valider d'un geste. Rien à envoyer si tout est fait. */
    fun evening(tasks: List<ReminderItem>, habits: List<ReminderItem>, streak: StreakInfo): ReminderMessage? {
        if (tasks.isEmpty() && habits.isEmpty()) return null
        val rest = tr("Il reste ", "Still to do: ") + remaining(tasks.size, habits.size)
        return if (streak.current > 0 && !streak.activeToday) {
            ReminderMessage(
                title = tr("Ta série de ${count(streak.current, "jour", "jours")} t'attend", "Your ${count(streak.current, "day", "days")} streak is waiting"),
                text = tr("$rest. Une seule suffit pour la garder.", "$rest. Just one is enough to keep it."),
                items = (tasks + habits).take(MAX_ITEMS),
            )
        } else {
            ReminderMessage(
                title = tr("Encore un petit effort ?", "One more little effort?"),
                text = tr("$rest pour aujourd'hui.", "$rest for today."),
                items = (tasks + habits).take(MAX_ITEMS),
            )
        }
    }

    /**
     * Rappel d'une habitude à son heure : rien si elle est déjà faite aujourd'hui
     * ou si son quota de la période est atteint.
     */
    fun habitReminder(h: HabitWithStats, today: Long): ReminderMessage? {
        if (!isDueToday(h, today)) return null
        val item = pendingHabits(listOf(h), today).first()
        val text = if (h.habit.isNumeric) {
            tr("Aujourd'hui : ${item.detail}. Chaque petit pas compte.", "Today: ${item.detail}. Every little step counts.")
        } else {
            tr("C'est l'heure. Un geste suffit pour la cocher.", "It's time. One tap is enough to check it.")
        }
        return ReminderMessage(title = h.habit.name, text = text, items = listOf(item))
    }

    /** Prochain passage à [minuteOfDay] (minutes depuis minuit), strictement après [now]. */
    fun nextOccurrence(now: LocalDateTime, minuteOfDay: Int): LocalDateTime {
        val time = LocalTime.of(minuteOfDay / 60, minuteOfDay % 60)
        val today = now.toLocalDate().atTime(time)
        return if (today.isAfter(now)) today else today.plusDays(1)
    }

    /** Heure lisible, ex. « 08:00 ». */
    fun formatMinute(minuteOfDay: Int): String = "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60)

    private fun remaining(tasks: Int, habits: Int): String = listOfNotNull(
        if (tasks > 0) count(tasks, tr("tâche", "task"), tr("tâches", "tasks")) else null,
        if (habits > 0) count(habits, tr("habitude", "habit"), tr("habitudes", "habits")) else null,
    ).joinToString(tr(" et ", " and "))

    private fun count(n: Int, one: String, many: String) = "$n ${if (n > 1) many else one}"
}
