package app.sentier.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.domain.HabitTemplates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class ChallengeRepositoryTest {

    /** Un jeudi : la semaine a commencé le lundi 23. */
    private val today = LocalDate.of(2026, 11, 26)
    private lateinit var db: AppDatabase
    private lateinit var game: GameRepository
    private lateinit var tasks: TaskRepository
    private lateinit var habits: HabitRepository
    private lateinit var challenges: ChallengeRepository

    @Before
    fun setUp() {
        db = AppDatabase.create(ApplicationProvider.getApplicationContext(), inMemory = true)
        game = GameRepository(db.gameDao()) { today }
        tasks = TaskRepository(db.taskDao(), game) { today }
        habits = HabitRepository(db.habitDao(), game) { today }
        challenges = ChallengeRepository(db.gameDao(), db.habitDao(), game) { today }
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun weekStartsEmpty() = runBlocking {
        val week = challenges.observe().first()
        assertEquals(today.minusDays(3).toEpochDay(), week.weekStart)
        assertEquals(3, week.daysLeft)
        assertEquals(0, week.doneCount)
        assertTrue(challenges.sync().isEmpty())
    }

    @Test
    fun stepsAndSportRunEarnTheirBonusOnce() = runBlocking {
        // 80 000 pas cette semaine ; les pas de la semaine dernière ne comptent pas.
        db.habitDao().putSteps(DailySteps(today.minusDays(4).toEpochDay(), 50_000))
        for (back in 0L..3L) db.habitDao().putSteps(DailySteps(today.minusDays(back).toEpochDay(), 20_000))
        // Sport 7 jours d'affilée, commencé la semaine dernière.
        val sport = habits.getHabit(habits.addFromTemplate(HabitTemplates.first { it.name == "Faire du sport" }))!!
        for (back in 0L..6L) habits.toggle(sport, today.minusDays(back))

        val week = challenges.observe().first()
        assertEquals(80_000L, week.list.first { it.title == "La route de la soie" }.current)
        assertEquals(7L, week.list.first { it.title == "Esprit olympique" }.current)
        // 4 jours actifs depuis lundi : pas encore Kaizen.
        assertEquals(4L, week.list.first { it.title == "Kaizen" }.current)

        val done = challenges.sync().map { it.title }
        assertEquals(listOf("La route de la soie", "Esprit olympique"), done)
        assertTrue(challenges.sync().isEmpty())
        assertEquals(setOf(0, 1), challenges.observe().first().rewarded)

        val state = game.observe().first()
        // 7 séances (10 XP chacune) + 60 + 80 de bonus.
        assertEquals(70 + 60 + 80, state.totalXp)
        assertTrue(state.badges.first { it.id == "challenge_1" }.unlocked)
        assertTrue(state.treasures.first { it.id == "panama" }.owned)
        // Le bonus ne compte pas comme une journée d'action de plus.
        assertEquals(7, state.streak.current)
    }

    @Test
    fun hardTasksCount() = runBlocking {
        repeat(3) { i ->
            val id = tasks.addTask("Difficile $i")
            tasks.update(db.taskDao().getTask(id)!!.copy(difficulty = 3))
            tasks.complete(id)
        }
        tasks.complete(tasks.addTask("Facile"))
        val week = challenges.observe().first()
        assertEquals(4L, week.list.first { it.title == "Pierre après pierre" }.current)
        assertEquals(3L, week.list.first { it.title == "Les travaux d'Hercule" }.current)
        assertEquals(listOf("Les travaux d'Hercule"), challenges.sync().map { it.title })
    }
}
