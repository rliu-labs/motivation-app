package app.sentier.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JourneyTest {

    @Test
    fun journeysAreWellFormed() {
        for (j in Journeys.all) {
            assertEquals(0.0, j.stops.first().km, 0.0)
            assertTrue("${j.title} : distances croissantes", j.stops.zipWithNext().all { (a, b) -> b.km > a.km })
            assertTrue(j.stops.all { it.fact.isNotBlank() })
        }
        val keys = Journeys.all.flatMap { j -> j.stops.indices.map { Journeys.keyOf(j, it) } }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun startsAtTheFirstJourney() {
        val p = Journeys.progress(0.0)
        assertEquals(0, p.journeyIndex)
        assertEquals(1, p.reachedCount)
        assertEquals("Sévrier", p.nextStop?.name)
        assertEquals(6.0, p.kmToNext, 1e-9)
        assertFalse(p.allDone)
        assertTrue(Journeys.reachedStops(0.0).isEmpty())
    }

    @Test
    fun extraDistanceCarriesOverToTheNextJourney() {
        val first = Journeys.all[0].lengthKm
        val p = Journeys.progress(first + 25.0)
        assertEquals(1, p.journeyIndex)
        assertEquals(25.0, p.km, 1e-9)
        assertEquals("Pradelles", p.nextStop?.name)
        assertEquals(listOf(Journeys.all[0]), p.finishedJourneys)
        // Les 7 étapes d'Annecy (hors départ) et la première de Stevenson.
        assertEquals(8, Journeys.reachedStops(first + 25.0).size)
    }

    @Test
    fun exactlyOnAStopCountsAsReached() {
        val p = Journeys.progress(13.0)
        assertEquals("Bout du Lac", p.nextStop?.name)
        assertEquals("Duingt", Journeys.reachedStops(13.0).last().stop.name)
    }

    @Test
    fun everythingDone() {
        val p = Journeys.progress(Journeys.totalKm + 50)
        assertTrue(p.allDone)
        assertNull(p.nextStop)
        assertEquals(1f, p.fraction)
        assertEquals(Journeys.all, p.finishedJourneys)
        assertEquals(Journeys.all.sumOf { it.stops.size - 1 }, Journeys.reachedStops(Journeys.totalKm).size)
    }

    @Test
    fun stepsToKilometres() {
        assertEquals(7.5, Journeys.kmFor(10_000, 75), 1e-9)
    }
}
