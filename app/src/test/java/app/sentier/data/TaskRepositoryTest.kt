package app.sentier.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.domain.Recurrence
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class TaskRepositoryTest {

    private val today = LocalDate.of(2026, 11, 25)
    private lateinit var db: AppDatabase
    private lateinit var repo: TaskRepository

    @Before
    fun setUp() {
        db = AppDatabase.create(ApplicationProvider.getApplicationContext(), inMemory = true)
        repo = TaskRepository(db.taskDao()) { today }
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun seedListsExist() = runBlocking {
        assertEquals(listOf("Perso", "Maison", "Travail"), repo.observeLists().first().map { it.name })
    }

    @Test
    fun todayShowsDueAndOverdueButNotFuture() = runBlocking {
        repo.addTask("Aujourd'hui", dueDay = today)
        repo.addTask("En retard", dueDay = today.minusDays(2))
        repo.addTask("Demain", dueDay = today.plusDays(1))
        repo.addTask("Sans date")
        val titles = repo.observeToday().first().map { it.title }.toSet()
        assertEquals(setOf("Aujourd'hui", "En retard"), titles)
    }

    @Test
    fun completeSimpleTask() = runBlocking {
        val id = repo.addTask("Courses", dueDay = today)
        val result = repo.complete(id)
        assertNotNull(result)
        assertNull(result!!.nextDue)
        val task = db.taskDao().getTask(id)!!
        assertTrue(task.isCompleted)
        assertEquals(1, task.completionCount)
        // Toujours visible aujourd'hui, dans « Fait ».
        assertEquals(listOf(id), repo.observeToday().first().map { it.id })

        repo.uncomplete(id)
        assertNull(db.taskDao().getTask(id)!!.completedAt)
    }

    @Test
    fun completeRecurringTaskMovesDueDateAndResetsSubtasks() = runBlocking {
        val id = repo.addTask("Arroser les plantes", dueDay = today)
        val task = db.taskDao().getTask(id)!!
        repo.update(task.copy(recurrence = Recurrence(Recurrence.Frequency.DAILY, 2).toRRule()))
        val subId = repo.addTask("Balcon", parentId = id)
        repo.toggleSubtask(db.taskDao().getTask(subId)!!)

        val result = repo.complete(id)!!
        assertEquals(today.plusDays(2), result.nextDue)
        val after = db.taskDao().getTask(id)!!
        assertNull(after.completedAt)
        assertEquals(today.plusDays(2).toEpochDay(), after.dueDay)
        assertEquals(today.toEpochDay(), after.lastCompletedDay)
        assertNull(db.taskDao().getTask(subId)!!.completedAt)

        // Visible dans « Fait aujourd'hui » malgré la nouvelle échéance.
        assertEquals(listOf(id), repo.observeToday().first().map { it.id })

        repo.undo(result)
        assertEquals(today.toEpochDay(), db.taskDao().getTask(id)!!.dueDay)
    }

    @Test
    fun subtaskCountsAndCascadeDelete() = runBlocking {
        val id = repo.addTask("Préparer le voyage", dueDay = today)
        val a = repo.addTask("Valise", parentId = id)
        repo.addTask("Billets", parentId = id)
        repo.toggleSubtask(db.taskDao().getTask(a)!!)
        val row = repo.observeToday().first().single()
        assertEquals(2, row.subtaskCount)
        assertEquals(1, row.subtaskDone)

        repo.delete(db.taskDao().getTask(id)!!)
        assertNull(db.taskDao().getTask(a))
    }

    @Test
    fun deletingListKeepsTasks() = runBlocking {
        val list = repo.observeLists().first().first()
        val id = repo.addTask("Lire", listId = list.id)
        repo.deleteList(list)
        val task = db.taskDao().getTask(id)!!
        assertNull(task.listId)
    }

    @Test
    fun reorderKeepsTheChosenOrder() = runBlocking {
        val a = repo.addTask("A")
        val b = repo.addTask("B")
        val c = repo.addTask("C")
        repo.reorder(listOf(c, a, b))
        val byPosition = repo.observeTasks(null).first().sortedBy { it.position }.map { it.title }
        assertEquals(listOf("C", "A", "B"), byPosition)
    }
}
