package app.sentier.ui.tasks

import app.sentier.data.TaskWithCounts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class TasksLogicTest {

    private fun task(id: Long, title: String, notes: String = "", due: Long? = null, done: Long? = null, position: Int = 0, priority: Int = 0) =
        TaskWithCounts(id, title, notes, null, priority, due, null, done, null, 0, 0, position)

    @Test
    fun searchIgnoresCaseAndAccentsAndLooksInNotes() {
        val tasks = listOf(
            task(1, "Réviser l'anglais"),
            task(2, "Faire les courses", notes = "pain, café"),
            task(3, "Appeler le dentiste", done = 1L),
        )
        assertEquals(listOf(1L), TasksViewModel.search(tasks, "REVISER").map { it.id })
        assertEquals(listOf(2L), TasksViewModel.search(tasks, "cafe").map { it.id })
        // Tous les mots doivent apparaître ; les tâches terminées sont listées en dernier.
        assertEquals(listOf(1L, 2L, 3L), TasksViewModel.search(tasks, "e").map { it.id })
        assertEquals(emptyList<Long>(), TasksViewModel.search(tasks, "pain dentiste").map { it.id })
        assertEquals(emptyList<Long>(), TasksViewModel.search(tasks, "   ").map { it.id })
    }

    @Test
    fun manualOrderFollowsPositions() {
        val tasks = listOf(task(1, "A", position = 3), task(2, "B", position = 1), task(3, "C", position = 2))
        assertEquals(listOf(2L, 3L, 1L), TasksViewModel.manualOrder(tasks).map { it.id })
    }

    @Test
    fun dueByDayGroupsAndSortsByPriority() {
        val tasks = listOf(task(1, "A", due = 10, priority = 0), task(2, "B", due = 10, priority = 3), task(3, "C", due = 11), task(4, "D"))
        val map = TasksViewModel.dueByDay(tasks)
        assertEquals(listOf(2L, 1L), map.getValue(10).map { it.id })
        assertEquals(listOf(3L), map.getValue(11).map { it.id })
        assertEquals(setOf(10L, 11L), map.keys)
    }

    @Test
    fun calendarStartsOnMondayWithFullWeeks() {
        // Novembre 2026 commence un dimanche : six cases vides avant le 1er.
        val days = calendarDays(YearMonth.of(2026, 11))
        assertEquals(0, days.size % 7)
        assertEquals(6, days.indexOf(LocalDate.of(2026, 11, 1)))
        assertNull(days.first())
        assertEquals(LocalDate.of(2026, 11, 30), days.filterNotNull().last())
    }
}
