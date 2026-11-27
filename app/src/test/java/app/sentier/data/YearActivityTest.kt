package app.sentier.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class YearActivityTest {

    /** Un vendredi. */
    private val today = LocalDate.of(2026, 11, 27)

    private fun event(kind: Int, day: LocalDate) = GameEvent(kind = kind, refId = 1, day = day.toEpochDay(), xp = 10, gold = 1)

    @Test
    fun yearStartsOnAMondayAndCoversFiftyThreeWeeks() {
        val year = YearActivity.of(today, emptyList())
        assertEquals(DayOfWeek.MONDAY, year.start.dayOfWeek)
        assertEquals(53, year.weeks)
        assertEquals(0, year.activeDays)
        assertEquals(0, year.bestRun)
    }

    @Test
    fun countsActionsAndLevelsRelativeToTheBestDay() {
        val events = buildList {
            repeat(8) { add(event(GameEvent.HABIT, today)) }
            add(event(GameEvent.TASK, today.minusDays(1)))
            add(event(GameEvent.STEPS, today.minusDays(2)))
            add(event(GameEvent.HABIT, today.minusDays(2)))
            add(event(GameEvent.HABIT, today.minusDays(2)))
            add(event(GameEvent.HABIT, today.minusDays(3)))
            // Achats et défis ne sont pas des actions ; trop ancien : hors du calendrier.
            add(event(GameEvent.PURCHASE, today.minusDays(10)))
            add(event(GameEvent.CHALLENGE, today.minusDays(11)))
            add(event(GameEvent.HABIT, today.minusDays(400)))
        }
        val year = YearActivity.of(today, events)
        assertEquals(4, year.activeDays)
        assertEquals(13, year.total)
        assertEquals(4, year.bestRun)
        assertEquals(4, year.levelOn(today.toEpochDay()))
        assertEquals(2, year.levelOn(today.minusDays(2).toEpochDay()))
        assertEquals(1, year.levelOn(today.minusDays(1).toEpochDay()))
        assertEquals(0, year.levelOn(today.minusDays(10).toEpochDay()))
    }
}
