/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier

import android.app.Application
import app.sentier.data.AppDatabase
import app.sentier.data.HabitRepository
import app.sentier.data.Settings
import app.sentier.data.TaskRepository
import app.sentier.steps.HealthConnectSource
import app.sentier.steps.SensorStepSource
import app.sentier.steps.StepsRepository

class SentierApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }
    val settings: Settings by lazy { Settings(this) }
    val tasks: TaskRepository by lazy { TaskRepository(database.taskDao()) }
    val habits: HabitRepository by lazy { HabitRepository(database.habitDao()) }
    val steps: StepsRepository by lazy {
        StepsRepository(database.habitDao(), settings, HealthConnectSource(this), SensorStepSource(this))
    }
}
