package app.sentier.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
class JourneyRepositoryTest {

    private val today = LocalDate.of(2026, 11, 26)
    private lateinit var db: AppDatabase
    private lateinit var settings: Settings
    private lateinit var game: GameRepository
    private lateinit var journey: JourneyRepository

    @Before
    fun setUp() = runBlocking {
        db = AppDatabase.create(ApplicationProvider.getApplicationContext(), inMemory = true)
        settings = Settings(ApplicationProvider.getApplicationContext())
        settings.setStepLength(75)
        settings.setJourneySeen(0)
        game = GameRepository(db.gameDao()) { today }
        journey = JourneyRepository(db.habitDao(), settings, game)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun stepsUnlockStopsOnceWithXp() = runBlocking {
        // 18 000 pas à 75 cm = 13,5 km : Marrakech est atteinte.
        db.habitDao().putSteps(DailySteps(today.toEpochDay(), 18_000))
        journey.sync()
        journey.sync()
        var state = journey.observe().first()
        assertEquals(13.5, state.progress.km, 1e-9)
        assertEquals(listOf("Marrakech"), state.unseen.map { it.stop.city })
        val game1 = game.observe().first()
        assertEquals(15, game1.totalXp)
        assertEquals(3, game1.gold)

        journey.markSeen()
        state = journey.observe().first()
        assertTrue(state.unseen.isEmpty())

        // Le lendemain, 12 000 pas de plus (9 km, total 22,5 km) : Rome.
        db.habitDao().putSteps(DailySteps(today.plusDays(1).toEpochDay(), 12_000))
        journey.sync()
        state = journey.observe().first()
        assertEquals(listOf("Rome"), state.unseen.map { it.stop.city })
        assertEquals("Athènes", state.progress.nextStop?.city)
        assertEquals(30, game.observe().first().totalXp)
    }
}
