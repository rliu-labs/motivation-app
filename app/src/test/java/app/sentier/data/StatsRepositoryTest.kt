package app.sentier.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.domain.HabitTemplates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class StatsRepositoryTest {

    /** Un vendredi. */
    private val today = LocalDate.of(2026, 11, 27)
    private lateinit var db: AppDatabase
    private lateinit var game: GameRepository
    private lateinit var tasks: TaskRepository
    private lateinit var habits: HabitRepository
    private lateinit var stats: StatsRepository

    @Before
    fun setUp() {
        db = AppDatabase.create(ApplicationProvider.getApplicationContext(), inMemory = true)
        game = GameRepository(db.gameDao()) { today }
        tasks = TaskRepository(db.taskDao(), game) { today }
        habits = HabitRepository(db.habitDao(), game) { today }
        stats = StatsRepository(db.gameDao(), db.habitDao(), habits) { today }
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun weekMonthAndYear() = runBlocking {
        db.habitDao().putSteps(DailySteps(today.toEpochDay(), 6_000))
        db.habitDao().putSteps(DailySteps(today.minusDays(1).toEpochDay(), 10_000))
        db.habitDao().putSteps(DailySteps(today.minusDays(20).toEpochDay(), 4_000))
        db.habitDao().putSteps(DailySteps(today.minusDays(100).toEpochDay(), 8_000))
        tasks.complete(tasks.addTask("Une tâche", dueDay = today))
        val meditation = habits.getHabit(habits.addFromTemplate(HabitTemplates.first { it.name == "Méditer" }))!!
        habits.toggle(meditation, today)

        val week = stats.observe(StatsPeriod.WEEK).first()
        assertEquals(7, week.buckets.size)
        assertEquals("V", week.buckets.last().label)
        assertEquals(6_000L, week.buckets.last().steps)
        assertEquals(16_000L, week.totalSteps)
        assertEquals(8_000L, week.averageSteps)
        assertEquals(10_000L, week.bestDaySteps)
        assertEquals(1, week.tasksDone)
        assertEquals(1, week.habitsDone)
        assertEquals(20, week.xp)
        assertEquals(1, week.activeDays)
        // L'habitude a été créée aujourd'hui et faite aujourd'hui : 100 %.
        assertEquals(1f, week.habitRates.single().rate, 0.001f)

        val month = stats.observe(StatsPeriod.MONTH).first()
        assertEquals(30, month.buckets.size)
        assertEquals(20_000L, month.totalSteps)

        val year = stats.observe(StatsPeriod.YEAR).first()
        assertEquals(12, year.buckets.size)
        assertEquals("N", year.buckets.last().label)
        assertEquals(28_000L, year.totalSteps)
        assertEquals(20_000L, year.buckets.last().steps)
    }
}
