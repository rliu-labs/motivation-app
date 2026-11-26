package app.sentier.notify

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.data.AppDatabase
import app.sentier.data.GameRepository
import app.sentier.data.HabitRepository
import app.sentier.data.TaskRepository
import app.sentier.domain.HabitTemplates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class ReminderNotificationsTest {

    private val today = LocalDate.of(2026, 11, 26)
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var db: AppDatabase
    private lateinit var game: GameRepository
    private lateinit var tasks: TaskRepository
    private lateinit var habits: HabitRepository

    @Before
    fun setUp() {
        db = AppDatabase.create(context, inMemory = true)
        game = GameRepository(db.gameDao()) { today }
        tasks = TaskRepository(db.taskDao(), game) { today }
        habits = HabitRepository(db.habitDao(), game) { today }
        shadowOf(context as Application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    }

    @After
    fun tearDown() = db.close()

    private val manager get() = context.getSystemService(NotificationManager::class.java)

    @Test
    fun eveningShowsWhatIsLeftWithADoneButton() = runBlocking {
        tasks.addTask("Appeler le dentiste", dueDay = today)
        val done = tasks.addTask("Déjà faite", dueDay = today)
        tasks.complete(done)
        habits.addFromTemplate(HabitTemplates.first { it.name == "Méditer" })
        habits.addFromTemplate(HabitTemplates.first { it.name == "Boire de l'eau" })

        val message = Reminders.build(tasks, habits, game, ReminderKind.EVENING)!!
        assertEquals(listOf("Appeler le dentiste", "Méditer", "Boire de l'eau"), message.items.map { it.title })
        // La tâche faite aujourd'hui compte pour la série : pas d'alerte de série.
        assertEquals("Il reste 1 tâche et 2 habitudes pour aujourd'hui.", message.text)

        Notifier.showEvening(context, message)
        val shown = shadowOf(manager).allNotifications
        assertEquals(4, shown.size) // le résumé et les 3 choses à faire
        val withButton = shown.filter { n -> n.actions?.any { it.title == "Fait" } == true }
        assertEquals(2, withButton.size) // l'eau est chiffrée : pas de bouton
    }

    @Test
    fun nothingLeftMeansNoEveningReminder() = runBlocking {
        val id = tasks.addTask("Seule tâche", dueDay = today)
        tasks.complete(id)
        assertNull(Reminders.build(tasks, habits, game, ReminderKind.EVENING))
    }

    @Test
    fun doneButtonCompletesTaskAndHabitWithXp() = runBlocking {
        val taskId = tasks.addTask("Sortir les poubelles", dueDay = today)
        val text = ReminderActions.complete(tasks, habits, ReminderItem.Type.TASK, taskId)
        assertEquals("« Sortir les poubelles » : c'est fait ! +10 XP, +2 or", text)
        assertNotNull(db.taskDao().getTask(taskId)!!.completedAt)
        // Un second appui ne compte pas deux fois.
        assertNull(ReminderActions.complete(tasks, habits, ReminderItem.Type.TASK, taskId))

        val habitId = habits.addFromTemplate(HabitTemplates.first { it.name == "Méditer" })
        assertEquals(
            "« Méditer » : c'est fait ! +10 XP, +2 or",
            ReminderActions.complete(tasks, habits, ReminderItem.Type.HABIT, habitId),
        )
        assertTrue(habits.observeHabitWithStats(habitId).first()!!.isDoneOn(today.toEpochDay()))
        assertNull(ReminderActions.complete(tasks, habits, ReminderItem.Type.HABIT, habitId))

        assertEquals(20, game.observe().first().totalXp)
    }

    @Test
    fun morningListsTheDay() = runBlocking {
        tasks.addTask("En retard", dueDay = today.minusDays(1))
        tasks.addTask("Demain", dueDay = today.plusDays(1))
        val message = Reminders.build(tasks, habits, game, ReminderKind.MORNING)!!
        assertEquals("1 tâche (dont 1 en retard).", message.text)

        Notifier.showMorning(context, message)
        assertEquals(1, shadowOf(manager).allNotifications.size)
    }
}
