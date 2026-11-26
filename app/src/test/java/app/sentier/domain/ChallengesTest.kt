package app.sentier.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ChallengesTest {

    @Test
    fun weekStartsOnMonday() {
        // Le 26 novembre 2026 est un jeudi.
        assertEquals(LocalDate.of(2026, 11, 23), Challenges.weekStart(LocalDate.of(2026, 11, 26)))
        assertEquals(LocalDate.of(2026, 11, 23), Challenges.weekStart(LocalDate.of(2026, 11, 23)))
        assertEquals(LocalDate.of(2026, 11, 23), Challenges.weekStart(LocalDate.of(2026, 11, 29)))
    }

    @Test
    fun sportRunCountsUpToTodayOrYesterday() {
        assertEquals(3, Challenges.runEndingAt(setOf(8L, 9L, 10L), 10))
        // La séance du jour n'est pas encore faite : la série d'hier compte toujours.
        assertEquals(3, Challenges.runEndingAt(setOf(8L, 9L, 10L), 11))
        assertEquals(0, Challenges.runEndingAt(setOf(8L, 9L, 10L), 12))
        assertEquals(1, Challenges.runEndingAt(setOf(5L, 7L), 7))
    }

    @Test
    fun challengesFollowTheWeek() {
        val empty = Challenges.of(WeekActivity(weekStart = 0, today = 3))
        assertEquals(6, empty.size)
        assertTrue(empty.none { it.done })
        assertNotNull(empty.first { it.title == "Esprit olympique" }.hint)

        val week = Challenges.of(
            WeekActivity(weekStart = 0, today = 3, steps = 70_000, tasks = 4, hardTasks = 3, habits = 15, activeDays = 4, sportRun = 9, hasSport = true)
        )
        val done = week.filter { it.done }.map { it.title }.toSet()
        assertEquals(setOf("La route de la soie", "Esprit olympique", "Les travaux d'Hercule", "Le rituel du maté"), done)
        val sport = week.first { it.title == "Esprit olympique" }
        assertEquals(7L, sport.current)
        assertNull(sport.hint)
        assertFalse(week.first { it.title == "Kaizen" }.done)
        assertEquals(0.4f, week.first { it.title == "Pierre après pierre" }.progress, 0.001f)
    }

    @Test
    fun keysAreUniquePerWeekAndChallenge() {
        val keys = (0L..3L).flatMap { w -> (0..5).map { Challenges.keyOf(20_000 + w * 7, it) } }
        assertEquals(keys.size, keys.toSet().size)
    }
}
