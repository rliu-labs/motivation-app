package app.sentier.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Parcours de base : ajouter une tâche, la valider, ouvrir les autres onglets. */
@RunWith(AndroidJUnit4::class)
// Écran de la taille d'un téléphone récent (l'écran Robolectric par défaut est très petit).
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class AppSmokeTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

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
        rule.onNodeWithContentDescription("Retour").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTextExactly("Aujourd'hui")).fetchSemanticsNodes().isNotEmpty() }
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

    private fun hasTextExactly(text: String) = androidx.compose.ui.test.hasText(text, substring = false)
}
