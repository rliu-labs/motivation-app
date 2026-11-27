package app.sentier.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.domain.HabitTemplates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class BackupRepositoryTest {

    private val today = LocalDate.of(2026, 11, 27)
    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()
    private lateinit var source: AppDatabase
    private lateinit var target: AppDatabase
    private lateinit var settings: Settings

    @Before
    fun setUp() {
        source = AppDatabase.create(context, inMemory = true)
        target = AppDatabase.create(context, inMemory = true)
        settings = Settings(context)
    }

    @After
    fun tearDown() {
        source.close()
        target.close()
    }

    private suspend fun fill() {
        val game = GameRepository(source.gameDao()) { today }
        val tasks = TaskRepository(source.taskDao(), game) { today }
        val habits = HabitRepository(source.habitDao(), game) { today }
        val parent = tasks.addTask("Faire les courses", dueDay = today)
        tasks.addTask("Pain", parentId = parent)
        tasks.complete(tasks.addTask("Appeler le dentiste", dueDay = today))
        val water = habits.getHabit(habits.addFromTemplate(HabitTemplates.first { it.name == "Boire de l'eau" }))!!
        habits.setValue(water, today, 8.0)
        habits.setValue(water, today.minusDays(1), 5.5)
        habits.setNote(water.id, today, "Une bouteille au bureau, ça aide.")
        source.habitDao().putSteps(DailySteps(today.toEpochDay(), 9_120))
        game.addReward("Un café en terrasse", 25)
    }

    @Test
    fun exportThenRestoreGivesTheSameData() = runBlocking {
        fill()
        settings.setGoal(9_000)
        settings.addTreasuresSeen(listOf("compass"))
        val text = BackupRepository(source, settings).export()

        // Réglages modifiés entre-temps : la restauration les remet.
        settings.setGoal(6_000)
        val repo = BackupRepository(target, settings)
        val file = repo.read(text)
        assertEquals(3, file.tasks)
        assertEquals(1, file.habits)
        repo.restore(file)

        val tasks = target.taskDao()
        assertEquals(
            source.taskDao().observeTasks(null).first().map { it.title }.sorted(),
            tasks.observeTasks(null).first().map { it.title }.sorted(),
        )
        assertEquals(listOf(5.5, 8.0), target.habitDao().observeLogsSince(0).first().map { it.value }.sorted())
        assertEquals(9_120L, target.habitDao().observeTotalSteps().first())
        val waterId = target.habitDao().observeHabits().first().single().id
        assertEquals(listOf("Une bouteille au bureau, ça aide."), target.habitDao().observeNotes(waterId).first().map { it.text })
        val game = GameRepository(target.gameDao()) { today }.observe().first()
        assertEquals(GameRepository(source.gameDao()) { today }.observe().first().totalXp, game.totalXp)
        assertTrue(game.rewards.any { it.name == "Un café en terrasse" })
        assertEquals(9_000, settings.currentSteps().goal)
        assertEquals(setOf("compass"), settings.treasuresSeen.first())

        // Les nouvelles données continuent normalement après la restauration.
        val newId = TaskRepository(tasks, GameRepository(target.gameDao()) { today }) { today }.addTask("Après")
        assertTrue(newId > 0)
    }

    @Test
    fun restoreReplacesEverything() = runBlocking {
        fill()
        val text = BackupRepository(source, settings).export()
        val tasks = TaskRepository(target.taskDao(), GameRepository(target.gameDao()) { today }) { today }
        tasks.addTask("Tâche qui doit disparaître")
        val repo = BackupRepository(target, settings)
        repo.restore(repo.read(text))
        val titles = target.taskDao().observeTasks(null).first().map { it.title }
        assertTrue("Tâche qui doit disparaître" !in titles)
    }

    @Test
    fun unknownColumnsAreIgnored() = runBlocking {
        fill()
        val json = JSONObject(BackupRepository(source, settings).export())
        val row = json.getJSONObject("tables").getJSONArray("reward").getJSONObject(0)
        row.put("cost) VALUES (1); DROP TABLE task; --", 1)
        val repo = BackupRepository(target, settings)
        repo.restore(repo.read(json.toString()))
        assertEquals(source.taskDao().observeTasks(null).first().size, target.taskDao().observeTasks(null).first().size)
        assertEquals(source.gameDao().observeRewards().first(), target.gameDao().observeRewards().first())
    }

    @Test
    fun badFilesAreRejectedWithoutChangingAnything() = runBlocking {
        val repo = BackupRepository(target, settings)
        TaskRepository(target.taskDao(), GameRepository(target.gameDao()) { today }) { today }.addTask("À garder")
        fun rejects(text: String, message: String) {
            try {
                repo.read(text)
                fail("Le fichier aurait dû être refusé")
            } catch (e: BackupException) {
                assertTrue(e.message!!, e.message!!.contains(message))
            }
        }
        rejects("pas du json", "n'est pas une sauvegarde")
        rejects("""{"format":"autre"}""", "n'est pas une sauvegarde")
        rejects("""{"format":"sentier-sauvegarde","schema":99,"tables":{}}""", "plus récente")
        rejects("""{"format":"sentier-sauvegarde","schema":3,"tables":{}}""", "incomplète")
        rejects("""{"format":"sentier-sauvegarde","schema":2,"tables":{}}""", "trop ancienne")

        // Une ligne impossible (tâche sans titre) : tout est annulé.
        fill()
        val json = JSONObject(BackupRepository(source, settings).export())
        json.getJSONObject("tables").getJSONArray("task").getJSONObject(0).put("title", JSONObject.NULL)
        try {
            repo.restore(repo.read(json.toString()))
            fail("La restauration aurait dû échouer")
        } catch (e: BackupException) {
            assertTrue(e.message!!.contains("rien n'a été modifié"))
        }
        assertEquals(listOf("À garder"), target.taskDao().observeTasks(null).first().map { it.title })
    }

    @Test
    fun habitReminderIsKeptAndOlderBackupsStillRestore() = runBlocking {
        fill()
        val water = source.habitDao().observeHabits().first().single()
        source.habitDao().update(water.copy(reminderMinute = 9 * 60 + 30))
        val text = BackupRepository(source, settings).export()

        val repo = BackupRepository(target, settings)
        repo.restore(repo.read(text))
        assertEquals(9 * 60 + 30, target.habitDao().observeHabits().first().single().reminderMinute)

        // Sauvegarde faite avant les rappels d'habitude (schéma 3, sans la colonne) : toujours lisible.
        val old = JSONObject(text).put("schema", 3)
        val habitsRows = old.getJSONObject("tables").getJSONArray("habit")
        for (i in 0 until habitsRows.length()) habitsRows.getJSONObject(i).remove("reminder_minute")
        // … ni les notes du jour (schéma 5).
        old.getJSONObject("tables").remove("habit_note")
        repo.restore(repo.read(old.toString()))
        val restored = target.habitDao().observeHabits().first().single()
        assertEquals("Boire de l'eau", restored.name)
        assertEquals(null, restored.reminderMinute)
        assertTrue(target.habitDao().observeNotes(restored.id).first().isEmpty())
    }
}
