package app.sentier.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PassportTest {

    @Test
    fun emptyPassportShowsTheCurrentJourney() {
        val p = Passport.of(emptyMap(), WorldTour.journey.id)
        assertEquals(listOf(WorldTour.journey.id), p.pages.map { it.journey.id })
        assertEquals(0, p.stamps)
        assertEquals(0, p.countries)
        assertNull(p.firstDay)
        // Pas de tampon pour le point de départ.
        assertEquals(WorldTour.journey.stops.size - 1, p.pages.first().stamps.size)
    }

    @Test
    fun stampsComeFromRewardedStopsOfEveryJourney() {
        val world = WorldTour.journey
        val france = TourDeFrance.journey
        val reached = buildMap {
            for (i in 1 until world.stops.size) put(world.keyOf(i), 20_000L + i)
            put(france.keyOf(1), 20_100L)
            put(france.keyOf(2), 20_101L)
        }
        val p = Passport.of(reached, france.id)
        assertEquals(listOf(world.id, france.id), p.pages.map { it.journey.id })
        assertTrue(p.pages[0].complete)
        assertEquals(world.stops.size - 1 + 2, p.stamps)
        assertEquals(1, p.journeysDone)
        assertEquals(20_001L, p.firstDay)
        val first = p.pages[1].stamps.first()
        assertEquals(france.stops[1].city, first.stop.city)
        assertEquals(20_100L, first.day)
        assertNull(p.pages[1].stamps[2].day)
        assertTrue(p.countries > 10)
    }
}
