package app.sentier.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveGoalTest {

    @Test
    fun notEnoughDaysKeepsTheGoal() {
        assertEquals(8_000, AdaptiveGoal.compute(listOf(5_000L, 6_000L, 0L, 0L), 8_000))
    }

    @Test
    fun goalIsTheMedianPlusTenPercentRounded() {
        // Médiane 6 000 (les jours sans pas ne comptent pas) → 6 600 → arrondi à 6 500.
        val days = listOf(4_000L, 5_500L, 6_000L, 0L, 6_200L, 9_000L, 0L)
        assertEquals(6_500, AdaptiveGoal.compute(days, 7_000))
    }

    @Test
    fun goalMovesGently() {
        val big = List(14) { 15_000L }
        // 16 500 visés, mais au plus 1 000 de plus par jour.
        assertEquals(9_000, AdaptiveGoal.compute(big, 8_000))
        val small = List(14) { 1_000L }
        assertEquals(7_000, AdaptiveGoal.compute(small, 8_000))
        // Jamais sous 3 000 pas.
        assertEquals(3_000, AdaptiveGoal.compute(small, 3_500))
    }
}
