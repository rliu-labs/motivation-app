package app.sentier.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JourneysTest {

    @Test
    fun journeysAreWellFormed() {
        val all = Journeys.all
        assertEquals("identifiants uniques", all.size, all.map { it.id }.toSet().size)
        assertEquals(WorldTour.journey, all.first())
        // Les clés des escales ne se chevauchent pas d'un voyage à l'autre, et celles du tour du monde n'ont pas changé.
        val keys = all.flatMap { j -> j.stops.indices.map { j.keyOf(it) } }
        assertEquals(keys.size, keys.toSet().size)
        assertEquals(1_001L, WorldTour.keyOf(1))
        for (j in all) {
            assertEquals(j.id, 0.0, j.stops.first().km, 0.0)
            assertTrue("${j.id} : distances croissantes", j.stops.zipWithNext().all { (a, b) -> b.km > a.km })
            assertTrue("${j.id} : au moins 8 escales", j.stops.size >= 9)
            assertTrue("${j.id} : première escale proche", j.stops[1].km <= 10)
            assertTrue(j.id, j.title.isNotBlank() && j.tagline.isNotBlank())
            if (j.requires != null) assertNotNull(j.id, Journeys.byId(j.requires))
            for (s in j.stops) {
                assertTrue(s.city, s.architecture.isNotBlank() && s.culture.isNotBlank() && s.anecdote.isNotBlank())
                assertTrue(s.city, s.souvenir.name.isNotBlank() && s.souvenir.description.isNotBlank())
                j.area?.let { assertTrue("${s.city} hors de la carte ${j.id}", it.contains(s.lat, s.lon)) }
            }
            j.area?.let { assertTrue("${j.id} : carte couverte par la grille d'Europe", EuropeMask.covers(it)) }
        }
    }

    @Test
    fun journeysUnlockInOrder() {
        assertTrue(Journeys.available(emptySet()).isEmpty())
        assertEquals(listOf("france"), Journeys.available(setOf("monde")).map { it.id })
        val europe = Journeys.available(setOf("monde", "france")).map { it.id }
        assertEquals(
            listOf("italie", "espagne", "portugal", "royaume-uni", "allemagne", "pays-bas", "belgique", "suisse", "autriche", "grece",
                "irlande", "norvege", "suede", "danemark", "pologne", "tchequie", "hongrie", "croatie"),
            europe,
        )
        assertFalse("italie" in Journeys.available(setOf("monde", "france", "italie")).map { it.id })
        assertEquals("Tour de France", Journeys.prerequisite(EuropeTours.italie)?.title)
    }

    @Test
    fun progressOnACountryTour() {
        val france = TourDeFrance.journey
        val p = france.progress(25.0)
        assertEquals("Strasbourg", p.current.city)
        assertEquals("Chamonix", p.nextStop?.city)
        assertEquals(15.0, p.kmToNext, 1e-9)
        assertEquals(listOf("Reims", "Strasbourg"), france.reachedStops(25.0).map { it.stop.city })
        assertEquals(2_002L, france.reachedStops(25.0).last().key)
        assertTrue(france.progress(france.lengthKm).done)
    }

    @Test
    fun europeMaskKnowsLandFromSea() {
        assertTrue("Paris", EuropeMask.isLandAt(48.85, 2.35))
        assertTrue("Madrid", EuropeMask.isLandAt(40.42, -3.71))
        assertTrue("Berlin", EuropeMask.isLandAt(52.52, 13.38))
        assertTrue("Rome", EuropeMask.isLandAt(41.9, 12.5))
        assertFalse("golfe de Gascogne", EuropeMask.isLandAt(45.5, -4.0))
        assertFalse("mer Tyrrhénienne", EuropeMask.isLandAt(40.0, 12.0))
        assertFalse("mer du Nord", EuropeMask.isLandAt(55.0, 3.0))
        assertFalse("hors grille", EuropeMask.isLandAt(20.0, 0.0))
    }
}
