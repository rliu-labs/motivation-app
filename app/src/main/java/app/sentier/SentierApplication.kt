/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier

import android.app.Application
import app.sentier.data.AppDatabase
import app.sentier.data.BackupRepository
import app.sentier.data.ChallengeRepository
import app.sentier.data.GameRepository
import app.sentier.data.HabitRepository
import app.sentier.data.JourneyRepository
import app.sentier.data.Settings
import app.sentier.data.TaskRepository
import app.sentier.data.WeeklyReviewRepository
import app.sentier.steps.HealthConnectSource
import app.sentier.steps.SensorStepSource
import app.sentier.steps.StepsRepository
import app.sentier.widget.Widgets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

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
    val backup: BackupRepository by lazy { BackupRepository(database, settings) }
    val weekly: WeeklyReviewRepository by lazy { WeeklyReviewRepository(database.gameDao(), database.habitDao(), habits) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var widgetSync: Job? = null

    override fun onCreate() {
        super.onCreate()
        if (Widgets.anyPlaced(this)) startWidgetSync()
    }

    /**
     * Tant que l'appli tourne, met à jour les widgets dès que les tâches, les habitudes,
     * le jeu ou les pas changent. Ne démarre que si un widget est posé.
     */
    @OptIn(FlowPreview::class)
    @Synchronized
    fun startWidgetSync() {
        if (widgetSync?.isActive == true) return
        widgetSync = scope.launch {
            combine(
                tasks.observeToday(),
                habits.observeHabitsWithStats(),
                game.observe(),
                database.habitDao().observeTotalSteps(),
                settings.steps,
            ) { _, _, _, _, _ -> System.nanoTime() }
                .debounce(800)
                .collect { runCatching { Widgets.updateAll(this@SentierApplication) } }
        }
    }
}
