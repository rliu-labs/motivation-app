package app.sentier.domain

import app.sentier.data.Habit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class HealthLinkTest {
    private val zone = ZoneId.of("Europe/Paris")
    private fun at(d: Int, h: Int, m: Int = 0) = LocalDateTime.of(2026, 11, d, h, m).atZone(zone).toInstant()

    @Test
    fun aNightCountsForTheDayOfWaking() {
        val nights = listOf(
            HealthSession(at(25, 23, 10), at(26, 6, 40), awakeMinutes = 15),
            HealthSession(at(26, 14, 0), at(26, 14, 30)),
        )
        assertEquals(mapOf(LocalDate.of(2026, 11, 26) to 435L + 30L), HealthLink.sleepByDay(nights, zone))
    }

    @Test
    fun sportIsGroupedByDay() {
        val s = listOf(HealthSession(at(26, 7), at(26, 7, 45)), HealthSession(at(26, 18), at(26, 18, 30)), HealthSession(at(27, 9), at(27, 10)))
        val byDay = HealthLink.sportByDay(s, zone)
        assertEquals(75L to 2, byDay[LocalDate.of(2026, 11, 26)])
        assertEquals(60L to 1, byDay[LocalDate.of(2026, 11, 27)])
    }

    @Test
    fun valuesFollowTheHabitType() {
        val sleepHours = Habit(name = "Dormir 7 heures", color = 0, icon = "sleep", type = Habit.TYPE_NUMERIC, target = 7.0, unit = "heures", createdDay = 0)
        val sleepYesNo = sleepHours.copy(type = Habit.TYPE_BOOLEAN)
        val sportYesNo = Habit(name = "Faire du sport", color = 0, icon = "sport", createdDay = 0)
        val sportMinutes = sportYesNo.copy(type = Habit.TYPE_NUMERIC, unit = "minutes", target = 30.0)
        val sportSessions = sportYesNo.copy(type = Habit.TYPE_NUMERIC, unit = "séances", target = 1.0)
        val reading = Habit(name = "Lire", color = 0, icon = "book", createdDay = 0)
        val day = HealthDay(sleepMinutes = 445, sportMinutes = 50, sportSessions = 2)

        assertEquals(7.4, HealthLink.valueFor(sleepHours, day)!!, 1e-9)
        assertEquals(1.0, HealthLink.valueFor(sleepYesNo, day)!!, 1e-9)
        assertNull(HealthLink.valueFor(sleepYesNo, day.copy(sleepMinutes = 360)))
        assertEquals(1.0, HealthLink.valueFor(sportYesNo, day)!!, 1e-9)
        assertEquals(50.0, HealthLink.valueFor(sportMinutes, day)!!, 1e-9)
        assertEquals(2.0, HealthLink.valueFor(sportSessions, day)!!, 1e-9)
        assertNull(HealthLink.valueFor(sportYesNo, HealthDay()))
        assertNull(HealthLink.valueFor(reading, day))
    }
}
