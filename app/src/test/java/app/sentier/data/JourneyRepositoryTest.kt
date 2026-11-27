package app.sentier.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.domain.EuropeTours
import app.sentier.domain.TourDeFrance
import app.sentier.domain.WorldTour
import kotlinx.coroutines.flow.first
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
        settings.startJourney(WorldTour.journey.id, 0L, emptySet())
        game = GameRepository(db.gameDao()) { today }
        journey = JourneyRepository(db.habitDao(), settings, game)
    }

    @After
    fun tearDown() = runBlocking {
        // Les réglages survivent d'un test à l'autre : on remet le tour du monde pour les suivants.
        settings.startJourney(WorldTour.journey.id, 0L, emptySet())
        db.close()
    }

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

    @Test
    fun finishedWorldTourUnlocksTourDeFranceWithCarryOver() = runBlocking {
        // 950 000 pas à 75 cm = 712,5 km : le tour du monde (700 km) est bouclé, 12,5 km de rab.
        db.habitDao().putSteps(DailySteps(today.toEpochDay(), 950_000))
        journey.sync()
        var state = journey.observe().first()
        assertTrue(state.progress.done)
        assertEquals(listOf("france"), state.next.map { it.id })
        assertEquals(25 * 15, game.observe().first().totalXp)

        // Un pays d'Europe n'est pas encore accessible.
        assertFalse(journey.start("italie"))
        assertTrue(journey.start("france"))
        state = journey.observe().first()
        assertEquals("france", state.journey.id)
        assertEquals(setOf("monde"), state.done)
        assertTrue(state.isDone(WorldTour.journey))
        // Les pas faits après l'arrivée comptent pour le tour de France : Reims (6 km) est atteinte.
        assertEquals(12.5, state.progress.km, 0.01)
        assertEquals(listOf("Reims"), state.unseen.map { it.stop.city })
        assertEquals(26 * 15, game.observe().first().totalXp)
        assertTrue(state.next.isEmpty())

        // On ne repart pas une deuxième fois tant que le tour de France n'est pas fini.
        assertFalse(journey.start("france"))

        // Fin du tour de France : les pays d'Europe se débloquent.
        db.habitDao().putSteps(DailySteps(today.plusDays(1).toEpochDay(), 290_000))
        journey.sync()
        state = journey.observe().first()
        assertTrue(state.progress.done)
        assertEquals(EuropeTours.all.map { it.id }, state.next.map { it.id })
        assertEquals((25 + TourDeFrance.stops.size - 1) * 15, game.observe().first().totalXp)
        assertTrue(journey.start("portugal"))
        state = journey.observe().first()
        assertEquals("portugal", state.journey.id)
        assertEquals(setOf("monde", "france"), state.done)
    }

    @Test
    fun journeyProgressIsKeptInBackups() = runBlocking {
        settings.startJourney("france", 933_334L, setOf("monde"))
        val saved = settings.exportJson()
        settings.startJourney(WorldTour.journey.id, 0L, emptySet())
        settings.importJson(saved)
        val j = settings.currentJourney()
        assertEquals("france", j.current)
        assertEquals(933_334L, j.startSteps)
        assertEquals(setOf("monde"), j.done)

        // Un voyage inconnu dans une sauvegarde est ignoré.
        settings.importJson(org.json.JSONObject().put("journey_current", "atlantide").put("journeys_done", org.json.JSONArray(listOf("atlantide"))))
        assertEquals("france", settings.currentJourney().current)
        assertTrue(settings.currentJourney().done.isEmpty())
    }
}
