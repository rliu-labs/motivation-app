package app.sentier.domain

import app.sentier.data.GameEvent
import app.sentier.data.Habit
import app.sentier.data.HabitWithStats
import app.sentier.notify.WeeklyReviewReminder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class WeeklyReviewTest {

    /** Dimanche 22 novembre 2026 : le bilan porte sur la semaine du 16 au 22. */
    private val sunday = LocalDate.of(2026, 11, 22)

    private fun event(kind: Int, day: LocalDate, ref: Long = 1) = GameEvent(kind = kind, refId = ref, day = day.toEpochDay(), xp = 10, gold = 1)

    @Test
    fun reviewedWeekEndsOnSunday() {
        assertEquals(LocalDate.of(2026, 11, 16), WeeklyReviews.reviewedStart(sunday))
        // En semaine, c'est la semaine passée qui est en bilan.
        assertEquals(LocalDate.of(2026, 11, 16), WeeklyReviews.reviewedStart(LocalDate.of(2026, 11, 25)))
        assertEquals(LocalDate.of(2026, 11, 16), WeeklyReviews.reviewedStart(LocalDate.of(2026, 11, 23)))
    }

    @Test
    fun goalIsALittleMoreThanLastWeek() {
        assertEquals(WeeklyGoal(WeeklyGoal.Kind.ACTIVE_DAYS, 3), WeeklyReviews.nextGoal(WeekNumbers()))
        assertEquals(WeeklyGoal(WeeklyGoal.Kind.ACTIVE_DAYS, 5), WeeklyReviews.nextGoal(WeekNumbers(activeDays = 4)))
        assertEquals(WeeklyGoal(WeeklyGoal.Kind.HABITS, 23), WeeklyReviews.nextGoal(WeekNumbers(activeDays = 7, habits = 20)))
        assertEquals(WeeklyGoal(WeeklyGoal.Kind.HABITS, 3), WeeklyReviews.nextGoal(WeekNumbers(activeDays = 7, habits = 2)))
        assertEquals("Être actif 5 jours", WeeklyGoal(WeeklyGoal.Kind.ACTIVE_DAYS, 5).text)
        assertEquals("Être actif les 7 jours", WeeklyGoal(WeeklyGoal.Kind.ACTIVE_DAYS, 7).text)
    }

    @Test
    fun reviewCountsTheWeekAndComparesWithThePreviousOne() {
        val start = WeeklyReviews.reviewedStart(sunday)
        val events = buildList {
            // Semaine en bilan : actif lundi, mardi, jeudi et dimanche.
            for (d in listOf(0L, 1L, 3L, 6L)) add(event(GameEvent.HABIT, start.plusDays(d)))
            add(event(GameEvent.HABIT, start.plusDays(3), ref = 2))
            add(event(GameEvent.TASK, start.plusDays(3)))
            add(event(GameEvent.PURCHASE, start.plusDays(2)))
            // Semaine d'avant : 2 jours actifs.
            add(event(GameEvent.HABIT, start.minusDays(3)))
            add(event(GameEvent.HABIT, start.minusDays(5)))
        }
        val habit = Habit(id = 1, name = "Méditer", color = 0, createdDay = start.minusDays(30).toEpochDay())
        val values = listOf(0L, 1L, 3L, 6L).associate { start.plusDays(it).toEpochDay() to 1.0 }
        val steps = mapOf(start.plusDays(2).toEpochDay() to 9_500L, start.plusDays(4).toEpochDay() to 4_000L)

        val r = WeeklyReviews.build(sunday, events, steps, listOf(HabitWithStats(habit, values, 0.5)))
        assertEquals(4, r.week.activeDays)
        assertEquals(5, r.week.habits)
        assertEquals(1, r.week.tasks)
        assertEquals(13_500L, r.week.steps)
        assertEquals(2, r.previous.activeDays)
        assertEquals("Belle semaine", r.headline)
        assertTrue(r.message, r.message.contains("2 jours de plus"))
        assertEquals(WeeklyGoal(WeeklyGoal.Kind.ACTIVE_DAYS, 5), r.goal)
        assertNull(r.ongoing)
        val titles = r.highlights.map { it.title }
        assertTrue(titles.toString(), "Record sur 3 mois" in titles)
        assertTrue(titles.toString(), "Meilleure journée" in titles)
        assertTrue(r.highlights.first { it.title == "Meilleure journée" }.detail.startsWith("Jeudi"))
        assertEquals("Méditer, réussie 4 jours sur 7.", r.highlights.first { it.title == "Habitude phare" }.detail)
        assertTrue(r.highlights.size <= 4)
        assertTrue(r.summary, r.summary.startsWith("4 jours actifs, 5 habitudes, 1 tâche."))

        // Le mercredi suivant : même bilan, et l'objectif se suit dans la semaine en cours.
        val wednesday = sunday.plusDays(3)
        val later = WeeklyReviews.build(wednesday, events + event(GameEvent.HABIT, sunday.plusDays(1)), steps, emptyList())
        assertEquals(r.week, later.week)
        assertEquals(1, later.ongoing?.activeDays)
    }

    @Test
    fun emptyWeekStillEncourages() {
        val r = WeeklyReviews.build(sunday, emptyList(), emptyMap(), emptyList())
        assertEquals("Une semaine pour repartir", r.headline)
        assertTrue(r.highlights.isEmpty())
        assertEquals(3, r.goal.target)
    }

    @Test
    fun perfectWeek() {
        val start = WeeklyReviews.reviewedStart(sunday)
        val events = (0L..6L).map { event(GameEvent.HABIT, start.plusDays(it)) }
        val r = WeeklyReviews.build(sunday, events, emptyMap(), emptyList())
        assertEquals("Semaine parfaite !", r.headline)
        assertEquals("Semaine parfaite", r.highlights.first().title)
        assertEquals(WeeklyGoal.Kind.HABITS, r.goal.kind)
    }

    @Test
    fun nextSundayAtTheChosenTime() {
        val friday = LocalDateTime.of(2026, 11, 27, 10, 0)
        assertEquals(LocalDateTime.of(2026, 11, 29, 18, 0), WeeklyReviewReminder.nextOccurrence(friday, 18 * 60))
        // Le dimanche avant l'heure : le jour même ; après : le dimanche suivant.
        assertEquals(LocalDateTime.of(2026, 11, 29, 18, 0), WeeklyReviewReminder.nextOccurrence(LocalDateTime.of(2026, 11, 29, 9, 0), 18 * 60))
        assertEquals(LocalDateTime.of(2026, 12, 6, 18, 0), WeeklyReviewReminder.nextOccurrence(LocalDateTime.of(2026, 11, 29, 18, 0), 18 * 60))
    }
}
