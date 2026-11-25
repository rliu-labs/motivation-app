package app.sentier.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate

class RecurrenceTest {

    // Mercredi 25 novembre 2026
    private val today = LocalDate.of(2026, 11, 25)

    @Test
    fun rruleRoundTrip() {
        val rules = listOf(
            Recurrence(Recurrence.Frequency.DAILY),
            Recurrence(Recurrence.Frequency.DAILY, 3),
            Recurrence(Recurrence.Frequency.WEEKLY, days = setOf(MONDAY, WEDNESDAY, FRIDAY)),
            Recurrence(Recurrence.Frequency.WEEKLY, 2),
            Recurrence(Recurrence.Frequency.MONTHLY),
        )
        for (r in rules) assertEquals(r, Recurrence.parse(r.toRRule()))
        assertEquals("FREQ=WEEKLY;BYDAY=MO,WE,FR", rules[2].toRRule())
    }

    @Test
    fun parseInvalid() {
        assertNull(Recurrence.parse(null))
        assertNull(Recurrence.parse(""))
        assertNull(Recurrence.parse("FREQ=YEARLY"))
        assertEquals(Recurrence(Recurrence.Frequency.DAILY, 2), Recurrence.parse("RRULE:FREQ=DAILY;INTERVAL=2"))
    }

    @Test
    fun dailyFromDueDate() {
        val r = Recurrence(Recurrence.Frequency.DAILY)
        assertEquals(today.plusDays(1), r.nextDueDate(today, today, fromCompletion = false))
    }

    @Test
    fun overdueTaskJumpsToFuture() {
        val r = Recurrence(Recurrence.Frequency.DAILY, 2)
        // Échéance il y a 5 jours (20/11) : 22, 24, 26 → le 26.
        assertEquals(LocalDate.of(2026, 11, 26), r.nextDueDate(today.minusDays(5), today, fromCompletion = false))
    }

    @Test
    fun fromCompletionUsesToday() {
        val r = Recurrence(Recurrence.Frequency.DAILY, 3)
        assertEquals(today.plusDays(3), r.nextDueDate(today.minusDays(10), today, fromCompletion = true))
    }

    @Test
    fun earlyCompletionKeepsSchedule() {
        val r = Recurrence(Recurrence.Frequency.DAILY, 7)
        // Due demain, faite aujourd'hui → dans 8 jours.
        assertEquals(today.plusDays(8), r.nextDueDate(today.plusDays(1), today, fromCompletion = false))
    }

    @Test
    fun weekdaysSkipWeekend() {
        val r = Recurrence(Recurrence.Frequency.WEEKLY, days = Recurrence.WEEKDAYS)
        val friday = LocalDate.of(2026, 11, 27)
        assertEquals(LocalDate.of(2026, 11, 30), r.nextAfter(friday))
    }

    @Test
    fun specificDays() {
        val r = Recurrence(Recurrence.Frequency.WEEKLY, days = setOf(MONDAY, FRIDAY))
        assertEquals(LocalDate.of(2026, 11, 27), r.nextAfter(today)) // mer. → ven.
        assertEquals(LocalDate.of(2026, 11, 30), r.nextAfter(LocalDate.of(2026, 11, 27))) // ven. → lun.
    }

    @Test
    fun everyTwoWeeksOnMonday() {
        val r = Recurrence(Recurrence.Frequency.WEEKLY, 2, setOf(MONDAY))
        val monday = LocalDate.of(2026, 11, 23)
        assertEquals(LocalDate.of(2026, 12, 7), r.nextAfter(monday))
    }

    @Test
    fun monthly() {
        val r = Recurrence(Recurrence.Frequency.MONTHLY)
        assertEquals(LocalDate.of(2026, 12, 25), r.nextDueDate(today, today, false))
        // 31 janvier → 28 février
        assertEquals(LocalDate.of(2027, 2, 28), r.nextAfter(LocalDate.of(2027, 1, 31)))
    }

    @Test
    fun descriptions() {
        assertEquals("Chaque jour", Recurrence(Recurrence.Frequency.DAILY).describe())
        assertEquals("Tous les 3 jours", Recurrence(Recurrence.Frequency.DAILY, 3).describe())
        assertEquals("En semaine (lun.–ven.)", Recurrence(Recurrence.Frequency.WEEKLY, days = Recurrence.WEEKDAYS).describe())
        assertEquals("Chaque semaine : lun., ven.", Recurrence(Recurrence.Frequency.WEEKLY, days = setOf(FRIDAY, MONDAY)).describe())
        assertEquals("Chaque mois", Recurrence(Recurrence.Frequency.MONTHLY).describe())
    }
}
