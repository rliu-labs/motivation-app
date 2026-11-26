package app.sentier.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldTourTest {

    @Test
    fun tourIsWellFormed() {
        val stops = WorldTour.stops
        assertEquals(0.0, stops.first().km, 0.0)
        assertTrue("distances croissantes", stops.zipWithNext().all { (a, b) -> b.km > a.km })
        assertEquals("Le tour finit où il commence", stops.first().city, stops.last().city)
        for (s in stops) {
            assertTrue(s.city, s.architecture.isNotBlank() && s.culture.isNotBlank() && s.anecdote.isNotBlank())
            assertTrue(s.city, s.souvenir.name.isNotBlank() && s.souvenir.description.isNotBlank())
            assertTrue(s.city, s.lat in WorldMask.LAT_BOTTOM..WorldMask.LAT_TOP && s.lon in -180.0..180.0)
        }
        // Les premières escales sont proches, pour avancer vite au début.
        assertTrue(stops[1].km <= 10)
        val keys = stops.indices.map { WorldTour.keyOf(it) }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun startsInParis() {
        val p = WorldTour.progress(0.0)
        assertEquals(0, p.currentIndex)
        assertEquals("Paris", p.current.city)
        assertEquals("Marrakech", p.nextStop?.city)
        assertEquals(8.0, p.kmToNext, 1e-9)
        assertFalse(p.done)
        assertTrue(WorldTour.reachedStops(0.0).isEmpty())
    }

    @Test
    fun exactlyOnAStopCountsAsReached() {
        val p = WorldTour.progress(20.0)
        assertEquals("Rome", p.current.city)
        assertEquals("Athènes", p.nextStop?.city)
        assertEquals(listOf("Marrakech", "Rome"), WorldTour.reachedStops(20.0).map { it.stop.city })
    }

    @Test
    fun tourDone() {
        val p = WorldTour.progress(WorldTour.lengthKm + 30)
        assertTrue(p.done)
        assertNull(p.nextStop)
        assertEquals(1f, p.fraction)
        assertEquals(WorldTour.stops.size, p.reached.size)
        assertEquals(WorldTour.stops.size - 1, WorldTour.reachedStops(WorldTour.lengthKm).size)
    }

    @Test
    fun stepsToKilometres() {
        assertEquals(7.5, WorldTour.kmFor(10_000, 75), 1e-9)
    }

    @Test
    fun worldMaskKnowsLandFromSea() {
        fun land(lat: Double, lon: Double) = WorldMask.isLand(
            ((lon + 180) / WorldMask.STEP).toInt(),
            ((WorldMask.LAT_TOP - lat) / WorldMask.STEP).toInt(),
        )
        assertTrue("France", land(47.0, 2.0))
        assertTrue("Brésil", land(-10.0, -55.0))
        assertTrue("Australie", land(-25.0, 134.0))
        assertFalse("Atlantique", land(30.0, -40.0))
        assertFalse("Pacifique", land(0.0, -140.0))
    }
}
