/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Une habitude, à la manière de Loop Habit Tracker : oui/non ou chiffrée,
 * avec une fréquence souple « [freqNum] fois tous les [freqDen] jours ».
 */
@Entity(tableName = "habit")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Long,
    /** Nom de l'icône (voir HabitIcons). */
    val icon: String = "check",
    /** 0 = oui/non, 1 = chiffrée. */
    val type: Int = TYPE_BOOLEAN,
    /** Valeur à atteindre chaque jour pour une habitude chiffrée (ex. 8 verres). */
    val target: Double = 1.0,
    val unit: String = "",
    @ColumnInfo(name = "freq_num") val freqNum: Int = 1,
    @ColumnInfo(name = "freq_den") val freqDen: Int = 1,
    /** 1 = facile, 2 = moyen, 3 = difficile (pour le jeu, étape 3). */
    val difficulty: Int = 1,
    val position: Int = 0,
    val archived: Boolean = false,
    @ColumnInfo(name = "created_day") val createdDay: Long,
    /** Heure du rappel de l'habitude (minutes depuis minuit), ou null s'il n'y en a pas. */
    @ColumnInfo(name = "reminder_minute") val reminderMinute: Int? = null,
) {
    val isNumeric: Boolean get() = type == TYPE_NUMERIC

    companion object {
        const val TYPE_BOOLEAN = 0
        const val TYPE_NUMERIC = 1
    }
}

/** Une valeur saisie pour une habitude un jour donné (1.0 = fait pour oui/non). */
@Entity(
    tableName = "habit_log",
    primaryKeys = ["habit_id", "day"],
    foreignKeys = [
        ForeignKey(entity = Habit::class, parentColumns = ["id"], childColumns = ["habit_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("day")],
)
data class HabitLog(
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val day: Long,
    val value: Double,
)

/** Note du jour attachée à une habitude, comme un petit journal. */
@Entity(
    tableName = "habit_note",
    primaryKeys = ["habit_id", "day"],
    foreignKeys = [
        ForeignKey(entity = Habit::class, parentColumns = ["id"], childColumns = ["habit_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("day")],
)
data class HabitNote(
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val day: Long,
    val text: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        /** Une note reste courte : quelques phrases. */
        const val MAX_LENGTH = 1000
    }
}

/** Nombre de pas d'une journée, mémorisé pour l'historique. */
@Entity(tableName = "daily_steps")
data class DailySteps(
    @PrimaryKey val day: Long,
    val steps: Long,
)
