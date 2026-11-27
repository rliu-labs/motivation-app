package app.sentier.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.domain.HabitTemplates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class HabitRepositoryTest {
    private var today = LocalDate.of(2026, 11, 25)
    private lateinit var db: AppDatabase
    private lateinit var repo: HabitRepository

    @Before
    fun setUp() {
        db = AppDatabase.create(ApplicationProvider.getApplicationContext(), inMemory = true)
        repo = HabitRepository(db.habitDao()) { today }
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun toggleBooleanHabit() = runBlocking {
        val id = repo.addFromTemplate(HabitTemplates.first { it.name == "Méditer" })
        val habit = repo.getHabit(id)!!
        assertTrue(repo.toggle(habit))
        var stats = repo.observeHabitsWithStats().first().single()
        assertTrue(stats.isDoneOn(today.toEpochDay()))
        assertTrue(stats.strength > 0)

        assertFalse(repo.toggle(habit))
        stats = repo.observeHabitsWithStats().first().single()
        assertFalse(stats.isDoneOn(today.toEpochDay()))
        assertEquals(0.0, stats.strength, 1e-9)
    }

    @Test
    fun numericHabitPartialAndComplete() = runBlocking {
        val id = repo.addFromTemplate(HabitTemplates.first { it.name == "Boire de l'eau" })
        val habit = repo.getHabit(id)!!
        repo.setValue(habit, today, 4.0)
        var stats = repo.observeHabitsWithStats().first().single()
        assertEquals(0.5, stats.completionOn(today.toEpochDay()), 1e-9)
        assertFalse(stats.isDoneOn(today.toEpochDay()))

        repo.setValue(habit, today, 8.0)
        stats = repo.observeHabitsWithStats().first().single()
        assertTrue(stats.isDoneOn(today.toEpochDay()))

        repo.setValue(habit, today, 0.0)
        stats = repo.observeHabitsWithStats().first().single()
        assertTrue(stats.values.isEmpty())
    }

    @Test
    fun strengthGrowsOverDays() = runBlocking {
        val created = today
        val id = repo.addFromTemplate(HabitTemplates.first { it.name == "Méditer" })
        val habit = repo.getHabit(id)!!
        for (i in 0 until 14) repo.toggle(habit, created.plusDays(i.toLong()))
        today = created.plusDays(13)
        repo = HabitRepository(db.habitDao()) { today }
        val stats = repo.observeHabitsWithStats().first().single()
        assertTrue("force ${stats.strength}", stats.strength > 0.5)
    }

    @Test
    fun deletingHabitDeletesLogs() = runBlocking {
        val id = repo.addFromTemplate(HabitTemplates.first())
        val habit = repo.getHabit(id)!!
        repo.setValue(habit, today, 3.0)
        repo.delete(habit)
        assertTrue(db.habitDao().observeLogsSince(0).first().isEmpty())
    }

    @Test
    fun dailyNoteIsSavedTrimmedAndErased() = runBlocking {
        val id = repo.addFromTemplate(HabitTemplates.first { it.name == "Méditer" })
        repo.setNote(id, today, "  10 minutes, calme retrouvé.  ")
        repo.setNote(id, today.minusDays(1), "Difficile de se concentrer")
        assertEquals(mapOf(id to "10 minutes, calme retrouvé."), repo.observeNotesOn(today).first())
        assertEquals(
            listOf("10 minutes, calme retrouvé.", "Difficile de se concentrer"),
            repo.observeNotes(id).first().map { it.text },
        )

        // Réécrire remplace, un texte vide efface, un texte trop long est coupé.
        repo.setNote(id, today, "Encore mieux")
        assertEquals("Encore mieux", repo.observeNotesOn(today).first()[id])
        repo.setNote(id, today, "   ")
        assertTrue(repo.observeNotesOn(today).first().isEmpty())
        repo.setNote(id, today, "x".repeat(HabitNote.MAX_LENGTH + 50))
        assertEquals(HabitNote.MAX_LENGTH, repo.observeNotesOn(today).first()[id]!!.length)

        // Supprimer l'habitude supprime son journal.
        repo.delete(repo.getHabit(id)!!)
        assertTrue(repo.observeNotes(id).first().isEmpty())
    }
}
