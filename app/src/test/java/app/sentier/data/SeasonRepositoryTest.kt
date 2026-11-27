package app.sentier.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.domain.HabitTemplates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class SeasonRepositoryTest {

    private lateinit var db: AppDatabase

    private fun repos(today: LocalDate): Triple<GameRepository, HabitRepository, SeasonRepository> {
        val game = GameRepository(db.gameDao()) { today }
        return Triple(game, HabitRepository(db.habitDao(), game) { today }, SeasonRepository(db.gameDao(), db.habitDao(), game) { today })
    }

    @org.junit.Before
    fun setUp() {
        db = AppDatabase.create(ApplicationProvider.getApplicationContext(), inMemory = true)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun novemberSportDaysEarnARareBadgeOnce() = runBlocking {
        val today = LocalDate.of(2026, 11, 27)
        val (game, habits, seasons) = repos(today)
        val sport = habits.getHabit(habits.addFromTemplate(HabitTemplates.first { it.name == "Faire du sport" }))!!
        // Une séance fin octobre ne compte pas ; 8 séances en novembre.
        habits.toggle(sport, LocalDate.of(2026, 10, 30))
        for (d in listOf(2, 5, 9, 12, 16, 20, 24, 27)) habits.toggle(sport, LocalDate.of(2026, 11, d))

        val state = seasons.observe().first()
        val c = state.active!!
        assertEquals("novembre", c.season.id)
        assertEquals(8L, c.current)
        assertTrue(c.done)
        assertEquals(3, c.daysLeft)

        assertNotNull(seasons.sync())
        assertNull(seasons.sync())
        assertTrue(seasons.observe().first().active!!.rewarded)

        val g = game.observe().first()
        // 9 séances à 10 XP + 100 XP de bonus.
        assertEquals(90 + 100, g.totalXp)
        val badge = g.rareBadges.first { it.id == "saison_novembre" }
        assertTrue(badge.unlocked)
        assertEquals(1, badge.current)
        // Les badges ordinaires ne changent pas, et le bonus ne compte pas comme une journée d'action.
        assertEquals(14, g.badges.size)
        assertEquals(9, db.gameDao().observeActiveDays().first().size)
    }

    @Test
    fun withoutActiveSeasonTheNextOneIsAnnounced() = runBlocking {
        val (_, _, seasons) = repos(LocalDate.of(2027, 5, 10))
        val state = seasons.observe().first()
        assertNull(state.active)
        assertEquals("ete", state.next?.id)
        assertEquals(LocalDate.of(2027, 6, 21), state.nextStart)
        assertNull(seasons.sync())
    }

    @Test
    fun adventCountsActiveDaysAndSteps() = runBlocking {
        val today = LocalDate.of(2026, 12, 3)
        val (_, habits, seasons) = repos(today)
        val water = habits.getHabit(habits.addFromTemplate(HabitTemplates.first { it.name == "Méditer" }))!!
        habits.toggle(water, LocalDate.of(2026, 11, 30))
        habits.toggle(water, LocalDate.of(2026, 12, 1))
        habits.toggle(water, LocalDate.of(2026, 12, 3))
        val c = seasons.observe().first().active!!
        assertEquals("avent", c.season.id)
        assertEquals(2L, c.current)
        assertFalse(c.done)
    }
}
