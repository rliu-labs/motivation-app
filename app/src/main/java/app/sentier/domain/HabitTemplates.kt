/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

import app.sentier.data.Habit

/** Modèles d'habitudes prêts à l'emploi. */
data class HabitTemplate(
    val name: String,
    val icon: String,
    val color: Long,
    val type: Int = Habit.TYPE_BOOLEAN,
    val target: Double = 1.0,
    val unit: String = "",
    val freqNum: Int = 1,
    val freqDen: Int = 1,
)

val HabitTemplates = listOf(
    HabitTemplate(tr("Boire de l'eau", "Drink water"), "water", 0xFF42A5F5, Habit.TYPE_NUMERIC, 8.0, tr("verres", "glasses")),
    HabitTemplate(tr("Dormir 7 heures", "Sleep 7 hours"), "sleep", 0xFF5C6BC0, Habit.TYPE_NUMERIC, 7.0, tr("heures", "hours")),
    HabitTemplate(tr("Méditer", "Meditate"), "meditation", 0xFF26A69A),
    HabitTemplate(tr("Faire du sport", "Work out"), "sport", 0xFFEF5350, freqNum = 3, freqDen = 7),
    HabitTemplate(tr("Lire", "Read"), "book", 0xFF8D6E63, Habit.TYPE_NUMERIC, 10.0, "pages"),
    HabitTemplate(tr("Pas d'écran au coucher", "No screens at bedtime"), "phone_off", 0xFFAB47BC),
)
