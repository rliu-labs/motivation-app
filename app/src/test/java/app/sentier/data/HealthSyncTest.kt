package app.sentier.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.domain.HabitTemplates
import app.sentier.domain.HealthSession
import app.sentier.steps.HealthReader
import app.sentier.steps.HealthSync
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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class HealthSyncTest {

    private val today = LocalDate.of(2026, 11, 27)
    private val zone = ZoneId.of("Europe/Paris")
    private lateinit var db: AppDatabase
    private lateinit var settings: Settings
    private lateinit var game: GameRepository
    private lateinit var habits: HabitRepository
    private val reader = FakeReader()
    private lateinit var health: HealthSync

    class FakeReader : HealthReader {
        var granted = true
        var sleep = listOf<HealthSession>()
        var sport = listOf<HealthSession>()
        override suspend fun hasHealthPermission() = granted
        override suspend fun readSleep(from: Instant, to: Instant) = sleep.filter { it.end in from..to }
        override suspend fun readExercise(from: Instant, to: Instant) = sport.filter { it.start in from..to }
    }

    private fun at(d: LocalDate, h: Int, m: Int = 0) = LocalDateTime.of(d.year, d.month, d.dayOfMonth, h, m).atZone(zone).toInstant()

    @Before
    fun setUp() = runBlocking {
        db = AppDatabase.create(ApplicationProvider.getApplicationContext(), inMemory = true)
        settings = Settings(ApplicationProvider.getApplicationContext())
        game = GameRepository(db.gameDao()) { today }
        habits = HabitRepository(db.habitDao(), game) { today }
        health = HealthSync(db.habitDao(), habits, settings, reader, { today }, { zone })
    }

    @After
    fun tearDown() = runBlocking {
        // Les réglages survivent d'un test à l'autre.
        settings.setHealthAuto(false)
        db.close()
    }

    @Test
    fun nothingHappensWhileOff() = runBlocking {
        val sleep = habits.addFromTemplate(HabitTemplates.first { it.name == "Dormir 7 heures" })
        reader.sleep = listOf(HealthSession(at(today.minusDays(1), 23), at(today, 7)))
        health.sync()
        assertNull(db.habitDao().getLog(sleep, today.toEpochDay()))
        assertFalse(health.observe().first().on)
    }

    @Test
    fun sleepAndSportFillTheHabitsWithoutLoweringManualValues() = runBlocking {
        val sleepId = habits.addFromTemplate(HabitTemplates.first { it.name == "Dormir 7 heures" })
        val sportId = habits.addFromTemplate(HabitTemplates.first { it.name == "Faire du sport" })
        val sleepHabit = habits.getHabit(sleepId)!!
        // Valeur saisie à la main hier : 8 heures, plus que ce que dit la montre.
        habits.setValue(sleepHabit, today.minusDays(1), 8.0)
        reader.sleep = listOf(
            HealthSession(at(today.minusDays(1), 23, 0), at(today, 6, 30)),
            HealthSession(at(today.minusDays(2), 23, 30), at(today.minusDays(1), 6, 30)),
        )
        reader.sport = listOf(HealthSession(at(today, 7), at(today, 7, 40)), HealthSession(at(today.minusDays(3), 18), at(today.minusDays(3), 19)))

        health.setOn(true)

        assertEquals(7.5, db.habitDao().getLog(sleepId, today.toEpochDay())!!.value, 1e-9)
        assertEquals(8.0, db.habitDao().getLog(sleepId, today.minusDays(1).toEpochDay())!!.value, 1e-9)
        assertEquals(1.0, db.habitDao().getLog(sportId, today.toEpochDay())!!.value, 1e-9)
        assertEquals(1.0, db.habitDao().getLog(sportId, today.minusDays(3).toEpochDay())!!.value, 1e-9)
        assertNull(db.habitDao().getLog(sportId, today.minusDays(1).toEpochDay()))

        val state = health.observe().first()
        assertTrue(state.on && state.granted)
        assertEquals(450L, state.lastNightMinutes)
        assertEquals(40L, state.sportTodayMinutes)
        assertEquals("Dormir 7 heures", state.sleepHabit?.name)
        assertEquals("Faire du sport", state.sportHabit?.name)

        // Relire ne donne pas de récompense en double.
        val xp = game.observe().first().totalXp
        health.sync()
        assertEquals(xp, game.observe().first().totalXp)
    }

    @Test
    fun withdrawnPermissionIsReported() = runBlocking {
        reader.granted = false
        health.setOn(true)
        val state = health.observe().first()
        assertTrue(state.on)
        assertFalse(state.granted)
        assertTrue(state.read)
    }
}
