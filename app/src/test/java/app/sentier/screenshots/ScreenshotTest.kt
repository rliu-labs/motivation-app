package app.sentier.screenshots

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sentier.MainActivity
import app.sentier.SentierApplication
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Génère les captures d'écran du README dans app/build/screenshots.
 * GitHub Actions les copie dans docs/screenshots à chaque fusion sur main.
 * (Android 11 simulé : couleurs de la marque, sans couleurs dynamiques.)
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [30], qualifiers = "w411dp-h891dp-xhdpi")
class ScreenshotTest {

    @get:Rule
    val rule = createEmptyComposeRule()

    private val outDir = File("build/screenshots").apply { mkdirs() }

    @Before
    fun seed() = runBlocking {
        DemoData.seed(ApplicationProvider.getApplicationContext<SentierApplication>())
    }

    @Test
    fun lightTheme() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitFor("Habitudes du jour")
            save(scenario, "aujourdhui")

            rule.onNode(hasContentDescription("Rappels")).performClick()
            waitFor("Le soir")
            save(scenario, "rappels")
            rule.onAllNodes(hasContentDescription("Retour")).onFirst().performClick()
            waitFor("Habitudes du jour")

            rule.onNodeWithText("Tâches").performClick()
            waitFor("Toutes")
            save(scenario, "taches")

            rule.onNodeWithText("Faire les courses").performClick()
            waitFor("Sous-tâches")
            save(scenario, "detail-tache")
            rule.onAllNodes(hasContentDescription("Retour")).onFirst().performClick()
            rule.waitForIdle()

            rule.onNodeWithText("Habitudes").performClick()
            waitFor("Mes habitudes")
            save(scenario, "habitudes")

            rule.onNodeWithText("Voyage").performClick()
            waitFor("Dernière escale")
            save(scenario, "voyage")
            rule.onNode(hasScrollAction()).performScrollToNode(hasText("Trésors rares", substring = true))
            waitFor("Trésors rares")
            save(scenario, "carnet")

            rule.onNodeWithText("Progrès").performClick()
            waitFor("Boutique de récompenses")
            save(scenario, "progres")
            rule.onNodeWithText("Défis de la semaine", substring = true).performScrollTo()
            waitFor("Défis de la semaine")
            save(scenario, "defis")

            rule.onNodeWithText("Statistiques").performScrollTo().performClick()
            waitFor("Habitudes réussies")
            save(scenario, "statistiques")
            rule.onAllNodes(hasContentDescription("Retour")).onFirst().performClick()
            waitFor("Boutique de récompenses")

            rule.onNodeWithText("Bilan de la semaine").performScrollTo().performClick()
            waitFor("Moments forts")
            save(scenario, "bilan")
        }
    }

    @Test
    @Config(qualifiers = "+night")
    fun darkTheme() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitFor("Habitudes du jour")
            save(scenario, "aujourdhui-nuit")
        }
    }

    @Test
    @Config(qualifiers = "w960dp-h600dp-land-hdpi")
    fun tabletLandscape() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitFor("Habitudes du jour")
            save(scenario, "tablette")
            rule.onNodeWithText("Voyage").performClick()
            waitFor("Dernière escale")
            save(scenario, "tablette-voyage")
        }
    }

    private fun waitFor(text: String) {
        rule.waitUntil(10_000) { rule.onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty() }
        rule.waitForIdle()
        // Laisse les animations (anneaux, listes) se terminer.
        rule.mainClock.advanceTimeBy(2_000)
        rule.waitForIdle()
    }

    /**
     * Dessine la fenêtre de l'activité dans une image. (captureToImage attend une image
     * de la surface matérielle, que Robolectric ne fournit pas toujours à temps.)
     */
    private fun save(scenario: ActivityScenario<MainActivity>, name: String) {
        lateinit var bitmap: Bitmap
        scenario.onActivity { activity ->
            val view = activity.window.decorView
            bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
        }
        File(outDir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
