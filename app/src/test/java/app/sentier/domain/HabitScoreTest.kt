package app.sentier.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitScoreTest {
    private val start = 1000L

    private fun daily(days: Iterable<Long>) = days.associateWith { 1.0 }

    @Test
    fun nothingDoneIsZero() {
        assertEquals(0.0, HabitScore.compute(emptyMap(), 1, 1, start, start + 30), 1e-9)
    }

    @Test
    fun thirtyDaysInARowIsStrong() {
        val today = start + 29
        val s = HabitScore.compute(daily(start..today), 1, 1, start, today)
        assertTrue("score $s", s in 0.75..0.85)
    }

    @Test
    fun oneMissedDayOnlyLowersALittle() {
        val today = start + 30
        val all = HabitScore.compute(daily(start..today), 1, 1, start, today)
        val missed = HabitScore.compute(daily((start..today) - (today - 1)), 1, 1, start, today)
        assertTrue(missed < all)
        assertTrue("un jour raté ne remet pas à zéro : $missed", missed > all * 0.9)
    }

    @Test
    fun todayNotYetDoneDoesNotPenalize() {
        val today = start + 20
        val upToYesterday = HabitScore.compute(daily(start until today), 1, 1, start, today)
        val yesterdayView = HabitScore.compute(daily(start until today), 1, 1, start, today - 1)
        assertEquals(yesterdayView, upToYesterday, 1e-9)
    }

    @Test
    fun threeTimesAWeekDoneThreeTimesAWeek() {
        val today = start + 55
        val done = (start..today).filter { (it - start) % 7 in setOf(0L, 2L, 4L) }
        val s = HabitScore.compute(daily(done), 3, 7, start, today)
        assertTrue("score $s", s > 0.75)
        // Faire 1 fois par semaine pour une habitude « 3 fois » donne nettement moins.
        val few = (start..today).filter { (it - start) % 7 == 0L }
        assertTrue(HabitScore.compute(daily(few), 3, 7, start, today) < s * 0.6)
    }

    @Test
    fun numericCompletion() {
        assertEquals(0.5, HabitScore.completionOf(4.0, numeric = true, target = 8.0), 1e-9)
        assertEquals(1.0, HabitScore.completionOf(10.0, numeric = true, target = 8.0), 1e-9)
        assertEquals(1.0, HabitScore.completionOf(1.0, numeric = false, target = 1.0), 1e-9)
        assertEquals(0.0, HabitScore.completionOf(0.0, numeric = false, target = 1.0), 1e-9)
    }

    @Test
    fun frequencyDescriptions() {
        assertEquals("Chaque jour", describeFrequency(1, 1))
        assertEquals("3 fois par semaine", describeFrequency(3, 7))
        assertEquals("10 fois par mois", describeFrequency(10, 30))
    }
}
