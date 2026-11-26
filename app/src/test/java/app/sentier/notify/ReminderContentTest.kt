package app.sentier.notify

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.data.Habit
import app.sentier.data.HabitWithStats
import app.sentier.data.TaskWithCounts
import app.sentier.domain.StreakInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class ReminderContentTest {

    private val today = 20_000L

    private fun task(id: Long, title: String, due: Long? = today, completedAt: Long? = null, lastDone: Long? = null) =
        TaskWithCounts(id, title, "", null, 0, due, null, completedAt, lastDone, 0, 0)

    private fun habit(id: Long, name: String, doneDays: Set<Long> = emptySet(), num: Int = 1, den: Int = 1, numeric: Boolean = false) =
        HabitWithStats(
            Habit(
                id = id, name = name, color = 0, type = if (numeric) Habit.TYPE_NUMERIC else Habit.TYPE_BOOLEAN,
                target = if (numeric) 8.0 else 1.0, unit = if (numeric) "verres" else "",
                freqNum = num, freqDen = den, createdDay = today - 30,
            ),
            values = doneDays.associateWith { if (numeric) 8.0 else 1.0 },
            strength = 0.5,
        )

    @Test
    fun pendingTasksSkipDoneOnesAndFlagLateOnes() {
        val items = ReminderContent.pendingTasks(
            listOf(
                task(1, "En retard", due = today - 2),
                task(2, "Aujourd'hui"),
                task(3, "Finie", completedAt = 1L),
                // Tâche répétée validée aujourd'hui : elle est passée à demain.
                task(4, "Répétée", due = today + 1, lastDone = today),
            ),
            today,
        )
        assertEquals(listOf(1L, 2L), items.map { it.id })
        assertEquals("En retard", items[0].detail)
        assertNull(items[1].detail)
    }

    @Test
    fun habitsFollowTheirFrequency() {
        assertTrue(ReminderContent.isDueToday(habit(1, "Méditer"), today))
        assertFalse(ReminderContent.isDueToday(habit(1, "Méditer", setOf(today)), today))
        // 3 fois par semaine, déjà 3 fois ces 7 derniers jours : rien à rappeler.
        assertFalse(ReminderContent.isDueToday(habit(2, "Sport", setOf(today - 1, today - 3, today - 5), 3, 7), today))
        // Seulement 2 fois : on rappelle.
        assertTrue(ReminderContent.isDueToday(habit(2, "Sport", setOf(today - 1, today - 3), 3, 7), today))
        // Une séance d'il y a 8 jours ne compte plus.
        assertTrue(ReminderContent.isDueToday(habit(2, "Sport", setOf(today - 1, today - 3, today - 8), 3, 7), today))
    }

    @Test
    fun numericHabitsShowProgressAndCannotBeCompletedFromTheNotification() {
        val items = ReminderContent.pendingHabits(listOf(habit(1, "Boire de l'eau", numeric = true), habit(2, "Méditer")), today)
        assertEquals("0 / 8 verres", items[0].detail)
        assertFalse(items[0].canComplete)
        assertTrue(items[1].canComplete)
    }

    @Test
    fun morningSummary() {
        val tasks = listOf(
            ReminderItem(ReminderItem.Type.TASK, 1, "A", "En retard"),
            ReminderItem(ReminderItem.Type.TASK, 2, "B"),
        )
        val habits = listOf(ReminderItem(ReminderItem.Type.HABIT, 1, "Méditer"))
        val m = ReminderContent.morning(tasks, habits, StreakInfo(current = 12))!!
        assertEquals("Au programme aujourd'hui", m.title)
        assertEquals("2 tâches (dont 1 en retard) et 1 habitude. Série en cours : 12 jours.", m.text)
        assertEquals(3, m.items.size)

        assertNull(ReminderContent.morning(emptyList(), emptyList(), StreakInfo()))
    }

    @Test
    fun eveningMentionsTheStreakOnlyWhenItIsAtRisk() {
        val one = listOf(ReminderItem(ReminderItem.Type.TASK, 1, "A"))
        val atRisk = ReminderContent.evening(one, emptyList(), StreakInfo(current = 5, activeToday = false))!!
        assertEquals("Ta série de 5 jours t'attend", atRisk.title)
        assertEquals("Il reste 1 tâche. Une seule suffit pour la garder.", atRisk.text)

        val safe = ReminderContent.evening(one, one, StreakInfo(current = 5, activeToday = true))!!
        assertEquals("Encore un petit effort ?", safe.title)
        assertEquals("Il reste 1 tâche et 1 habitude pour aujourd'hui.", safe.text)

        assertNull(ReminderContent.evening(emptyList(), emptyList(), StreakInfo(current = 5)))
    }

    @Test
    fun itemsAreCapped() {
        val many = (1L..9L).map { ReminderItem(ReminderItem.Type.TASK, it, "T$it") }
        assertEquals(ReminderContent.MAX_ITEMS, ReminderContent.evening(many, emptyList(), StreakInfo())!!.items.size)
    }

    @Test
    fun nextOccurrence() {
        val morning = LocalDateTime.of(2026, 11, 26, 7, 59)
        assertEquals(LocalDateTime.of(2026, 11, 26, 8, 0), ReminderContent.nextOccurrence(morning, 8 * 60))
        // Pile à l'heure ou après : demain.
        assertEquals(LocalDateTime.of(2026, 11, 27, 8, 0), ReminderContent.nextOccurrence(morning.plusMinutes(1), 8 * 60))
        assertEquals(LocalDateTime.of(2026, 11, 27, 8, 0), ReminderContent.nextOccurrence(morning.plusHours(12), 8 * 60))
        assertEquals("08:05", ReminderContent.formatMinute(8 * 60 + 5))
    }

    @Test
    fun lateRemindersAreDropped() {
        val target = 1_000_000_000L
        assertFalse(Reminders.isTooLate(target, target + 60_000))
        assertTrue(Reminders.isTooLate(target, target + 3 * 3_600_000))
    }
}
