package app.sentier.ui.journey

import app.sentier.domain.Journeys
import app.sentier.domain.TourDeFrance
import app.sentier.domain.WorldTour
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StopIllustrationTest {

    @Test
    fun everyStopHasAScene() {
        // Une scène par escale ; le retour à Paris reprend celle du départ.
        assertEquals(WorldTour.stops.size - 1, StopScenes.size)
        assertEquals(Landmark.TOWER, sceneFor(WorldTour.stops.lastIndex).landmark)
        assertEquals(Landmark.TAJ, sceneFor(WorldTour.stops.indexOfFirst { it.city == "Agra" }).landmark)
        assertEquals(Landmark.CHURCH, sceneFor(WorldTour.stops.indexOfFirst { it.city == "Reykjavik" }).landmark)
    }

    @Test
    fun everyJourneyHasItsScenesAndSouvenirIcons() {
        for (j in Journeys.all) {
            val scenes = JourneyScenes.of(j.id)
            val expected = if (j == WorldTour.journey) j.stops.size - 1 else j.stops.size
            assertEquals("scènes de ${j.id}", expected, scenes.size)
            for (s in j.stops) assertTrue("icône de ${s.souvenir.name}", s.souvenir.icon in SouvenirIcons.names)
        }
        val france = TourDeFrance.journey
        assertEquals(Landmark.ABBEY_ISLAND, sceneFor(france, france.stops.indexOfFirst { it.city == "Mont-Saint-Michel" }).landmark)
        assertEquals(Landmark.ARCH, sceneFor(france, france.stops.lastIndex).landmark)
    }
}
