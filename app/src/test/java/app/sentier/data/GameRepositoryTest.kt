package app.sentier.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class GameRepositoryTest {

    private val today = LocalDate.of(2026, 11, 26)
    private lateinit var db: AppDatabase
    private lateinit var game: GameRepository
    private lateinit var tasks: TaskRepository
    private lateinit var habits: HabitRepository

    @Before
    fun setUp() {
        db = AppDatabase.create(ApplicationProvider.getApplicationContext(), inMemory = true)
        game = GameRepository(db.gameDao()) { today }
        tasks = TaskRepository(db.taskDao(), game) { today }
        habits = HabitRepository(db.habitDao(), game) { today }
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun exampleRewardsExist() = runBlocking {
        assertEquals(3, game.observe().first().rewards.size)
    }

    @Test
    fun completingATaskGivesXpAndGoldAndUndoTakesThemBack() = runBlocking {
        val id = tasks.addTask("Difficile")
        tasks.update(db.taskDao().getTask(id)!!.copy(difficulty = 3))
        val result = tasks.complete(id)!!
        assertEquals(Gain(30, 6), result.gain)
        var state = game.observe().first()
        assertEquals(30, state.totalXp)
        assertEquals(6, state.gold)
        assertEquals(1, state.streak.current)

        tasks.undo(result)
        state = game.observe().first()
        assertEquals(0, state.totalXp)
        assertEquals(0, state.gold)
    }

    @Test
    fun uncompletingATaskTakesTheGainBack() = runBlocking {
        val id = tasks.addTask("Simple")
        tasks.complete(id)
        tasks.uncomplete(id)
        assertEquals(0, game.observe().first().totalXp)
    }

    @Test
    fun repeatedTaskGivesXpEachTime() = runBlocking {
        val id = tasks.addTask("Arroser", dueDay = today)
        tasks.update(db.taskDao().getTask(id)!!.copy(recurrence = "FREQ=DAILY"))
        tasks.complete(id)
        tasks.complete(id)
        assertEquals(20, game.observe().first().totalXp)
    }

    @Test
    fun habitRewardsOncePerDay() = runBlocking {
        val habit = habits.getHabit(habits.addHabit(Habit(name = "Méditer", color = 0, createdDay = 0)))!!
        assertTrue(habits.toggle(habit))
        assertEquals(10, game.observe().first().totalXp)
        assertFalse(habits.toggle(habit))
        assertEquals(0, game.observe().first().totalXp)

        val water = habits.getHabit(
            habits.addHabit(Habit(name = "Eau", color = 0, type = Habit.TYPE_NUMERIC, target = 8.0, createdDay = 0))
        )!!
        habits.setValue(water, today, 4.0)
        assertEquals(0, game.observe().first().totalXp)
        habits.setValue(water, today, 8.0)
        habits.setValue(water, today, 9.0)
        assertEquals(10, game.observe().first().totalXp)
        habits.setValue(water, today, 2.0)
        assertEquals(0, game.observe().first().totalXp)
    }

    @Test
    fun buyingNeedsEnoughGold() = runBlocking {
        val reward = game.observe().first().rewards.first { it.cost == 20 }
        assertFalse(game.buy(reward))
        repeat(5) { tasks.complete(tasks.addTask("T$it")) }
        assertEquals(10, game.observe().first().gold)
        game.onStepsGoal(today.toEpochDay())
        game.onStepsGoal(today.toEpochDay())
        val days = (1L..3L).map { today.minusDays(it).toEpochDay() }
        days.forEach { game.onStepsGoal(it) }
        // 5 tâches (10 or) + 4 jours d'objectif de pas (16 or) = 26 or.
        assertEquals(26, game.observe().first().gold)
        assertTrue(game.buy(reward))
        val state = game.observe().first()
        assertEquals(6, state.gold)
        assertTrue(state.badges.first { it.id == "reward_1" }.unlocked)
        // Un achat ne compte pas comme une journée active.
        assertEquals(4, state.streak.current)
    }

    @Test
    fun rewardsCanBeEdited() = runBlocking {
        val id = game.addReward("  Un café  ", 15)
        val reward = game.observe().first().rewards.first { it.id == id }
        assertEquals("Un café", reward.name)
        game.updateReward(reward.copy(cost = 0))
        assertEquals(1, game.observe().first().rewards.first { it.id == id }.cost)
        game.deleteReward(reward)
        assertNull(game.observe().first().rewards.firstOrNull { it.id == id })
    }
}
