package app.sentier.ui.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.SentierApplication
import app.sentier.domain.HabitTemplates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class OnboardingTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun walkThroughAndChoose() {
        var result: OnboardingChoices? = null
        var skipped = false
        rule.setContent { OnboardingContent(onFinish = { result = it }, onSkip = { skipped = true }) }

        rule.onNodeWithText("Bienvenue sur Sentier").assertIsDisplayed()
        rule.onNodeWithText("Commencer").performClick()

        rule.onNodeWithText("Tes premières habitudes").assertIsDisplayed()
        rule.onNodeWithText("Méditer").performScrollTo().performClick()
        rule.onNodeWithText("Lire").performScrollTo().performClick()
        rule.onNodeWithText("Lire").performClick() // finalement non
        rule.onNodeWithText("Suivant").performClick()

        rule.onNodeWithText("Ton objectif de pas").assertIsDisplayed()
        rule.onNodeWithText("10", substring = true).performClick()
        rule.onNodeWithText("Suivant").performClick()

        rule.onNodeWithText("Un petit rappel ?").assertIsDisplayed()
        rule.onNodeWithContentDescription("Le soir à 20:30").performClick()
        rule.onNodeWithText("C'est parti").performClick()

        val choices = result!!
        assertEquals(listOf("Méditer"), choices.habits.map { it.name })
        assertEquals(10_000, choices.stepGoal)
        assertTrue(choices.morning)
        assertFalse(choices.evening)
        assertFalse(skipped)
    }

    @Test
    fun skipLeavesRightAway() {
        var skipped = false
        rule.setContent { OnboardingContent(onFinish = {}, onSkip = { skipped = true }) }
        rule.onNodeWithText("Passer").performClick()
        assertTrue(skipped)
    }

    @Test
    fun choicesCreateHabitsAndSettings() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<SentierApplication>()
        val choices = OnboardingChoices(
            habits = listOf("Méditer", "Boire de l'eau").map { n -> HabitTemplates.first { it.name == n } },
            stepGoal = 6_000,
        )
        OnboardingViewModel.apply(app, choices)
        assertEquals(listOf("Méditer", "Boire de l'eau"), app.habits.observeHabitsWithStats().first().map { it.habit.name })
        assertEquals(6_000, app.settings.currentSteps().goal)
    }
}
