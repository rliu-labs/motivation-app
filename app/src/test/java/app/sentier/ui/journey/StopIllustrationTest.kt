package app.sentier.ui.journey

import app.sentier.domain.WorldTour
import org.junit.Assert.assertEquals
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
}
