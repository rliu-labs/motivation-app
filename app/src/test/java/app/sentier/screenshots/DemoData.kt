package app.sentier.screenshots

import app.sentier.SentierApplication
import app.sentier.data.DailySteps
import app.sentier.data.ReminderSettings
import app.sentier.data.StepSource
import app.sentier.domain.HabitTemplates
import app.sentier.domain.Recurrence
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/** Données d'exemple pour les captures d'écran du README. */
object DemoData {
    suspend fun seed(app: SentierApplication) {
        val today = LocalDate.now()
        val tasks = app.tasks
        val lists = tasks.observeLists().first().associateBy { it.name }
        val perso = lists.getValue("Perso").id
        val maison = lists.getValue("Maison").id
        val travail = lists.getValue("Travail").id
        val dao = app.database.taskDao()

        suspend fun task(title: String, list: Long?, due: LocalDate?, priority: Int = 0, rrule: Recurrence? = null): Long {
            val id = tasks.addTask(title, listId = list, dueDay = due)
            dao.getTask(id)?.let { tasks.update(it.copy(priority = priority, recurrence = rrule?.toRRule())) }
            return id
        }

        task("Envoyer le dossier à la mutuelle", travail, today.minusDays(1), priority = 3)
        task("Appeler le dentiste", perso, today, priority = 2)
        val courses = task("Faire les courses", maison, today, priority = 1)
        listOf("Fruits et légumes", "Pain", "Café").forEachIndexed { i, s ->
            val sub = tasks.addTask(s, parentId = courses)
            if (i == 0) dao.getTask(sub)?.let { tasks.toggleSubtask(it) }
        }
        val plantes = task("Arroser les plantes", maison, today, rrule = Recurrence(Recurrence.Frequency.DAILY, 2))
        tasks.complete(plantes)
        val marche = task("Marcher 20 minutes", perso, today)
        tasks.complete(marche)
        task("Préparer le week-end", perso, today.plusDays(1))
        task("Réviser l'anglais", perso, today.plusDays(3), rrule = Recurrence(Recurrence.Frequency.WEEKLY, days = setOf(today.plusDays(3).dayOfWeek)))
        task("Lire un chapitre", perso, null)
        task("Point d'équipe", travail, today.plusDays(2), priority = 1)

        val habits = app.habits
        fun tpl(name: String) = HabitTemplates.first { it.name == name }
        val eau = habits.getHabit(habits.addFromTemplate(tpl("Boire de l'eau")))!!
        val meditation = habits.getHabit(habits.addFromTemplate(tpl("Méditer")))!!
        val sport = habits.getHabit(habits.addFromTemplate(tpl("Faire du sport")))!!
        val lecture = habits.getHabit(habits.addFromTemplate(tpl("Lire")))!!
        for (back in 1L..30L) {
            val d = today.minusDays(back)
            habits.setValue(eau, d, if (back % 4 == 0L) 6.0 else 8.0)
            if (back % 6 != 0L) habits.toggle(meditation, d)
            if (back % 7 in setOf(1L, 3L, 5L)) habits.toggle(sport, d)
            if (back % 3 != 0L) habits.setValue(lecture, d, 12.0)
        }
        habits.setValue(eau, today, 5.0)
        habits.toggle(meditation, today)

        val steps = listOf(6_480L, 9_215L, 8_102L, 4_350L, 10_420L, 7_630L, 5_240L)
        steps.forEachIndexed { i, n ->
            app.database.habitDao().putSteps(DailySteps(today.minusDays((6 - i).toLong()).toEpochDay(), n))
        }
        for (back in 7L..40L) app.database.habitDao().putSteps(DailySteps(today.minusDays(back).toEpochDay(), 7_000L + back * 37))
        app.settings.setSource(StepSource.SENSOR)

        // Jeu : objectifs de pas atteints et une récompense déjà prise.
        steps.forEachIndexed { i, n -> if (n >= 8_000) app.game.onStepsGoal(today.minusDays((6 - i).toLong()).toEpochDay()) }
        app.game.observe().first().rewards.firstOrNull { it.name == "Un dessert" }?.let { app.game.buy(it) }

        // Voyage : étapes déjà récompensées et annoncées.
        app.journey.sync()
        app.journey.markSeen()

        // Défis de la semaine déjà récompensés, trésors déjà annoncés.
        app.challenges.sync()
        val state = app.game.observe().first()
        app.settings.addTreasuresSeen(state.treasures.filter { it.owned }.map { it.id })
        app.settings.setCelebrated(state.level.level, state.badges.filter { it.unlocked }.map { it.id }.toSet())

        app.settings.setOnboarded()

        // Rappels : le matin à 8 h et le soir à 20 h 30.
        app.settings.setReminders(ReminderSettings(morningOn = true, eveningOn = true))
    }
}
