package app.sentier.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.domain.HabitTemplates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

/** Base telle qu'elle était à l'étape 1 (version 1 : tâches et listes seulement). */
@Database(entities = [Task::class, TaskList::class], version = 1, exportSchema = false)
abstract class Version1Database : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class MigrationTest {

    @Test
    fun step1DataSurvivesUpgrade() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-test.db"
        context.deleteDatabase(name)

        val v1 = Room.databaseBuilder(context, Version1Database::class.java, name).build()
        val listId = v1.taskDao().insertList(TaskList(name = "Perso", color = 0xFF2E7D5B))
        v1.taskDao().insert(Task(title = "Tâche de l'étape 1", listId = listId, dueDay = 20_000))
        v1.close()

        // Ouverture avec la version actuelle : Room vérifie que le schéma migré est exact.
        val db = AppDatabase.create(context, name = name)
        val tasks = db.taskDao().observeTasks(null).first()
        assertEquals(listOf("Tâche de l'étape 1"), tasks.map { it.title })

        val habits = HabitRepository(db.habitDao()) { LocalDate.of(2026, 11, 25) }
        val id = habits.addFromTemplate(HabitTemplates.first())
        habits.setValue(habits.getHabit(id)!!, LocalDate.of(2026, 11, 25), 2.0)
        // Étape 4 : rappel par habitude, vide par défaut.
        assertEquals(null, habits.getHabit(id)!!.reminderMinute)
        habits.update(habits.getHabit(id)!!.copy(reminderMinute = 7 * 60))
        assertEquals(listOf(7 * 60), db.habitDao().habitsWithReminder().map { it.reminderMinute })
        db.habitDao().putSteps(DailySteps(20_000, 1234))
        assertEquals(1234L, db.habitDao().observeTotalSteps().first())

        // Étape 3 : récompenses d'exemple créées par la migration, jeu utilisable.
        val game = GameRepository(db.gameDao()) { LocalDate.of(2026, 11, 25) }
        assertEquals(3, game.observe().first().rewards.size)
        game.onStepsGoal(20_000)
        assertEquals(20, game.observe().first().totalXp)
        db.close()
    }
}
