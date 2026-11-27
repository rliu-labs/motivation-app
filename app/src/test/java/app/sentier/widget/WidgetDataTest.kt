package app.sentier.widget

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.data.AppDatabase
import app.sentier.data.DailySteps
import app.sentier.data.GameRepository
import app.sentier.data.HabitRepository
import app.sentier.data.Settings
import app.sentier.data.StepSource
import app.sentier.data.TaskRepository
import app.sentier.domain.HabitTemplates
import app.sentier.notify.ReminderActions
import app.sentier.notify.ReminderItem
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class WidgetDataTest {

    private val today = LocalDate.of(2026, 11, 27)
    private lateinit var db: AppDatabase
    private lateinit var settings: Settings
    private lateinit var game: GameRepository
    private lateinit var tasks: TaskRepository
    private lateinit var habits: HabitRepository

    @Before
    fun setUp() = runBlocking {
        db = AppDatabase.create(ApplicationProvider.getApplicationContext(), inMemory = true)
        settings = Settings(ApplicationProvider.getApplicationContext())
        settings.setSource(StepSource.NONE)
        settings.setGoal(8_000)
        game = GameRepository(db.gameDao()) { today }
        tasks = TaskRepository(db.taskDao(), game) { today }
        habits = HabitRepository(db.habitDao(), game) { today }
    }

    @After
    fun tearDown() = db.close()

    private suspend fun load() = WidgetData.load(tasks, habits, db.habitDao(), game, settings)

    @Test
    fun emptyDay() = runBlocking {
        val data = load()
        assertEquals(0, data.remaining)
        assertEquals("Tout est fait pour aujourd'hui. Bravo !", data.summary)
        assertFalse(data.stepsOn)
        assertEquals("Pas de série", data.streakText)
    }

    @Test
    fun listsWhatIsLeftAndCheckingUpdatesIt() = runBlocking {
        val call = tasks.addTask("Appeler le dentiste", dueDay = today)
        tasks.addTask("Demain", dueDay = today.plusDays(1))
        habits.addFromTemplate(HabitTemplates.first { it.name == "Méditer" })
        habits.addFromTemplate(HabitTemplates.first { it.name == "Boire de l'eau" })
        settings.setSource(StepSource.SENSOR)
        db.habitDao().putSteps(DailySteps(today.toEpochDay(), 6_480))

        var data = load()
        assertEquals(listOf("Appeler le dentiste", "Méditer", "Boire de l'eau"), data.items.map { it.title })
        assertEquals("Encore 3 choses à faire", data.summary)
        // L'eau est chiffrée : pas de case à cocher dans le widget.
        assertEquals(listOf(true, true, false), data.items.map { it.canComplete })
        assertTrue(data.stepsOn)
        assertEquals("6 480", data.stepsText.replace(' ', ' '))
        assertEquals(0.81f, data.stepProgress, 0.001f)

        // Cocher depuis le widget : la tâche disparaît, l'XP et la série suivent.
        ReminderActions.complete(tasks, habits, ReminderItem.Type.TASK, call)
        data = load()
        assertEquals(listOf("Méditer", "Boire de l'eau"), data.items.map { it.title })
        assertEquals(1, data.streak)
        assertTrue(data.activeToday)
    }

    @Test
    fun manyItemsAreCapped() = runBlocking {
        repeat(12) { tasks.addTask("Tâche $it", dueDay = today) }
        val data = load()
        assertEquals(WidgetData.MAX_ITEMS, data.items.size)
        assertEquals(12, data.remaining)
    }
}
