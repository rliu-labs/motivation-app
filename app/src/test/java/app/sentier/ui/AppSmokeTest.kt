package app.sentier.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.MainActivity
import androidx.test.core.app.ApplicationProvider
import app.sentier.SentierApplication
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Parcours de base : ajouter une tâche, la valider, ouvrir les autres onglets. */
@RunWith(AndroidJUnit4::class)
// Écran de la taille d'un téléphone récent (l'écran Robolectric par défaut est très petit).
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class AppSmokeTest {

    val rule = createAndroidComposeRule<MainActivity>()

    /** L'accueil du premier lancement est déjà passé (il a son propre test). */
    @get:Rule
    val chain: RuleChain = RuleChain
        .outerRule(object : ExternalResource() {
            override fun before() = runBlocking {
                ApplicationProvider.getApplicationContext<SentierApplication>().settings.setOnboarded()
                Unit
            }
        })
        .around(rule)

    @Test
    fun addAndCompleteTaskToday() {
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Aujourd'hui")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Rien de prévu", substring = true).assertIsDisplayed()

        rule.onNodeWithTag("quickAdd").performTextInput("Boire un verre d'eau")
        rule.onNodeWithTag("quickAdd").performImeAction()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Boire un verre d'eau")).fetchSemanticsNodes().isNotEmpty() }

        rule.onNodeWithContentDescription("Valider Boire un verre d'eau").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Fait aujourd'hui")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Tout est fait", substring = true).assertExists()
    }

    @Test
    fun openTaskDetailsAndOtherTabs() {
        rule.onNodeWithTag("quickAdd").performTextInput("Appeler maman")
        rule.onNodeWithTag("quickAdd").performImeAction()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Appeler maman")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Appeler maman").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Répétition")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Ne se répète pas").performClick()
        rule.onNodeWithText("Chaque jour").performClick()
        rule.onNodeWithText("OK").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Chaque jour")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithContentDescription("Retour").performClick()

        rule.onNodeWithText("Tâches").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Toutes")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Habitudes").performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Nouvelle habitude")).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Progrès").performClick()
        rule.onNodeWithText("Crédits et licence").performScrollTo().performClick()
        rule.onNodeWithText("Tasks.org").assertExists()
    }

    @Test
    fun openRemindersSettings() {
        rule.onNodeWithContentDescription("Rappels").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Le soir")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Le matin").assertIsDisplayed()
        rule.onNodeWithText("À 08:00").assertIsDisplayed()
        rule.onNodeWithText("À 20:30").assertIsDisplayed()
        rule.onNodeWithText("Le dimanche à 18:00").performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription("Retour").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Aujourd'hui")).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun openJourney() {
        rule.onNodeWithText("Voyage").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Tour du monde")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Escale 0 sur 25").assertExists()
        rule.onNodeWithText("Active le comptage des pas", substring = true).assertExists()
        rule.onNodeWithText("Point de départ").assertExists()
    }

    @Test
    fun openStatsAndTheme() {
        rule.onNodeWithText("Progrès").performClick()
        rule.onNodeWithText("Statistiques").performScrollTo().performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Habitudes réussies")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("30 jours").performClick()
        rule.onNodeWithText("12 mois").performClick()
        rule.onNodeWithText("Aucun pas enregistré sur la période.").assertExists()
        rule.onNodeWithTag("yearHeatmap").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("0 jours actifs sur l'année", substring = true).assertExists()
        rule.onNodeWithContentDescription("Retour").performClick()

        rule.onNodeWithText("Bilan de la semaine").performScrollTo().performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Moments forts")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Une semaine pour repartir").assertIsDisplayed()
        rule.onNodeWithText("Être actif 3 jours").performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription("Retour").performClick()

        rule.onNodeWithText("Thème").performScrollTo().performClick()
        rule.onNodeWithText("Sombre").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Sombre")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Thème").performScrollTo().performClick()
        rule.onNodeWithText("Comme le téléphone").performClick()
    }

    @Test
    fun openBackup() {
        rule.onNodeWithText("Progrès").performClick()
        rule.onNodeWithText("Sauvegarde").performScrollTo().performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Exporter mes données")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Importer une sauvegarde").assertIsDisplayed()
        rule.onNodeWithText("Sauvegarde automatique").performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription("Retour").performClick()
    }

    @Test
    fun openChallengesAndTreasures() {
        rule.onNodeWithText("Progrès").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Défis de la semaine (0 sur 6)")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Défis de la semaine (0 sur 6)").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("La route de la soie").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Ajoute l'habitude « Faire du sport » pour relever ce défi.").assertExists()

        rule.onNodeWithText("Voyage").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Tour du monde")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNode(androidx.compose.ui.test.hasScrollAction())
            .performScrollToNode(androidx.compose.ui.test.hasText("Trésors rares", substring = true))
        rule.onNodeWithText("Trésors rares (0 sur 17)").assertIsDisplayed()
        rule.onNodeWithContentDescription("Trésor à gagner : Atteindre le niveau 3").performClick()
        rule.onNodeWithText("Boussole de marin").assertIsDisplayed()
        rule.onNodeWithText("Fermer").performClick()
    }

    @Test
    fun addHabitFromTemplateAndCheckItToday() {
        rule.onNodeWithText("Habitudes").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Ajoute ta première habitude")).fetchSemanticsNodes().isNotEmpty() }
        // Sans Health Connect (Robolectric), la carte propose de connecter le podomètre.
        rule.onNodeWithText("Compte tes pas").assertExists()
        rule.onNodeWithText("Méditer").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Mes habitudes")).fetchSemanticsNodes().isNotEmpty() }

        rule.onAllNodes(hasTextExactly("Aujourd'hui"))[0].performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Habitudes du jour")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithContentDescription("Cocher Méditer").performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Méditer : fait")).fetchSemanticsNodes().isNotEmpty()
        }

        // Note du jour : écrite depuis Aujourd'hui, visible sous l'habitude et dans son journal.
        rule.onNodeWithContentDescription("Note du jour : Méditer").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(androidx.compose.ui.test.hasTestTag("habitNote")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("habitNote").performTextInput("Dix minutes au calme")
        rule.onNodeWithText("Enregistrer").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Dix minutes au calme")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithContentDescription("Modifier la note : Méditer").assertExists()

        rule.onNodeWithText("Habitudes").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Mes habitudes")).fetchSemanticsNodes().isNotEmpty() }
        rule.onAllNodes(hasTextExactly("Méditer"))[0].performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Journal")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Dix minutes au calme").assertExists()
    }

    @Test
    fun createNumericHabit() {
        rule.onNodeWithText("Habitudes").performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Nouvelle habitude")).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithContentDescription("Nouvelle habitude").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Modèles")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Boire de l'eau").performClick()
        rule.onNodeWithText("Enregistrer").performScrollTo().performClick()
        // Retour à la liste : l'habitude y apparaît avec son objectif.
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("8 verres · Chaque jour")).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun habitReminderAtChosenTime() {
        rule.onNodeWithText("Habitudes").performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Nouvelle habitude")).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithContentDescription("Nouvelle habitude").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Modèles")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Méditer").performClick()
        rule.onNodeWithContentDescription("Rappel de l'habitude").performScrollTo().performClick()
        rule.onNodeWithText("À 09:00").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Enregistrer").performScrollTo().performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Mes habitudes")).fetchSemanticsNodes().isNotEmpty() }

        // Le rappel est enregistré avec l'habitude.
        val dao = ApplicationProvider.getApplicationContext<SentierApplication>().database.habitDao()
        rule.waitUntil(5_000) { runBlocking { dao.habitsWithReminder().map { it.name to it.reminderMinute } } == listOf("Méditer" to 9 * 60) }
    }

    @Test
    fun searchTasksAndUseCalendarAndManualOrder() {
        rule.onNodeWithTag("quickAdd").performTextInput("Réviser l'anglais")
        rule.onNodeWithTag("quickAdd").performImeAction()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Réviser l'anglais")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Tâches").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Toutes")).fetchSemanticsNodes().isNotEmpty() }

        // Recherche sans accents.
        rule.onNodeWithContentDescription("Chercher une tâche").performClick()
        rule.onNodeWithTag("taskSearch").performTextInput("reviser")
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Réviser l'anglais")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("taskSearch").performTextClearance()
        rule.onNodeWithTag("taskSearch").performTextInput("piano")
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Aucune tâche ne correspond à « piano ».")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithContentDescription("Fermer la recherche").performClick()

        // Mon ordre : chaque tâche a sa poignée.
        rule.onNodeWithText("Mon ordre").performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Déplacer Réviser l'anglais")).fetchSemanticsNodes().isNotEmpty()
        }

        // Calendrier : une tâche ajoutée arrive le jour choisi (aujourd'hui par défaut).
        rule.onNodeWithText("Calendrier").performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Mois suivant")).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("quickAdd").performTextInput("Arroser les plantes")
        rule.onNodeWithTag("quickAdd").performImeAction()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Arroser les plantes")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Par date").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("En retard")).fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithText("Arroser les plantes").assertExists()
    }

    private fun hasTextExactly(text: String) = androidx.compose.ui.test.hasText(text, substring = false)
}
