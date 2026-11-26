/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier

import android.app.Application
import app.sentier.data.AppDatabase
import app.sentier.data.ChallengeRepository
import app.sentier.data.GameRepository
import app.sentier.data.HabitRepository
import app.sentier.data.JourneyRepository
import app.sentier.data.Settings
import app.sentier.data.TaskRepository
import app.sentier.steps.HealthConnectSource
import app.sentier.steps.SensorStepSource
import app.sentier.steps.StepsRepository

class SentierApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }
    val settings: Settings by lazy { Settings(this) }
    val game: GameRepository by lazy { GameRepository(database.gameDao()) }
    val tasks: TaskRepository by lazy { TaskRepository(database.taskDao(), game = game) }
    val habits: HabitRepository by lazy { HabitRepository(database.habitDao(), game = game) }
    val challenges: ChallengeRepository by lazy { ChallengeRepository(database.gameDao(), database.habitDao(), game) }
    val journey: JourneyRepository by lazy { JourneyRepository(database.habitDao(), settings, game) }
    val steps: StepsRepository by lazy {
        StepsRepository(database.habitDao(), settings, HealthConnectSource(this), SensorStepSource(this), game = game, journey = journey)
    }
}
