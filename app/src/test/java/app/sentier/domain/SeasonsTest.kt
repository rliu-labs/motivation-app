package app.sentier.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SeasonsTest {

    @Test
    fun codesAndIdsAreUniqueAndPeriodsDoNotOverlap() {
        assertEquals(Seasons.all.size, Seasons.all.map { it.code }.toSet().size)
        assertEquals(Seasons.all.size, Seasons.all.map { it.id }.toSet().size)
        var day = LocalDate.of(2027, 1, 1)
        while (day.year == 2027) {
            assertTrue("Deux défis le $day", Seasons.all.count { it.contains(day) } <= 1)
            day = day.plusDays(1)
        }
        for (s in Seasons.all) assertFalse(s.endIn(2027).isBefore(s.startIn(2027)))
    }

    @Test
    fun activeAndNext() {
        assertEquals("novembre", Seasons.active(LocalDate.of(2026, 11, 27))?.id)
        assertEquals("avent", Seasons.active(LocalDate.of(2026, 12, 24))?.id)
        assertEquals("fetes", Seasons.active(LocalDate.of(2026, 12, 25))?.id)
        assertEquals("nouvel_an", Seasons.active(LocalDate.of(2027, 1, 1))?.id)
        assertNull(Seasons.active(LocalDate.of(2027, 5, 10)))

        val (next, start) = Seasons.next(LocalDate.of(2027, 5, 10))
        assertEquals("ete", next.id)
        assertEquals(LocalDate.of(2027, 6, 21), start)
        // Le dernier jour de l'année, le prochain est celui du Nouvel An suivant.
        assertEquals(LocalDate.of(2027, 1, 1), Seasons.next(LocalDate.of(2026, 12, 31)).second)
    }

    @Test
    fun challengeProgressAndHints() {
        val avent = Seasons.all.first { it.id == "avent" }
        val c = Seasons.challenge(avent, LocalDate.of(2026, 12, 10), SeasonActivity(activeDays = 9), rewarded = false)
        assertEquals(9L, c.current)
        assertEquals(14, c.daysLeft)
        assertFalse(c.done)
        assertEquals(Seasons.keyOf(2026, 8), c.key)

        val ete = Seasons.all.first { it.id == "ete" }
        val empty = Seasons.challenge(ete, LocalDate.of(2027, 7, 1), SeasonActivity(), rewarded = false)
        assertTrue(empty.hint!!.contains("pas"))
        val done = Seasons.challenge(ete, LocalDate.of(2027, 7, 1), SeasonActivity(steps = 250_000), rewarded = false)
        assertTrue(done.done)
        assertEquals(200_000L, done.current)
    }

    @Test
    fun rareBadgesCountWinsOverTheYears() {
        val wins = Seasons.winsOf(listOf(Seasons.keyOf(2026, 8), Seasons.keyOf(2027, 8), Seasons.keyOf(2027, 0)))
        assertEquals(mapOf("avent" to 2, "nouvel_an" to 1), wins)
        val badges = RareBadges.of(wins)
        assertEquals(Seasons.all.size, badges.size)
        assertTrue(badges.all { it.rare })
        assertEquals(2, badges.first { it.id == "saison_avent" }.current)
        assertTrue(badges.first { it.id == "saison_avent" }.unlocked)
        assertFalse(badges.first { it.id == "saison_ete" }.unlocked)
    }
}
