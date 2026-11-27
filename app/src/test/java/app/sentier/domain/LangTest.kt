package app.sentier.domain

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.util.Locale

class LangTest {

    @After
    fun backToFrench() {
        // Les autres tests attendent le français.
        Lang.english = false
    }

    @Test
    fun choiceAndSystemLanguage() {
        assertFalse(Lang.resolve(AppLanguage.FR, Locale.US))
        assertTrue(Lang.resolve(AppLanguage.EN, Locale.FRANCE))
        assertFalse(Lang.resolve(AppLanguage.SYSTEM, Locale.CANADA_FRENCH))
        assertTrue(Lang.resolve(AppLanguage.SYSTEM, Locale.GERMANY))
    }

    @Test
    fun textsFollowTheLanguage() {
        assertEquals("Bonjour", tr("Bonjour", "Hello"))
        assertEquals(Locale.FRANCE, Lang.locale)
        Lang.english = true
        assertEquals("Hello", tr("Bonjour", "Hello"))
        assertEquals(Locale.UK, Lang.locale)
        // Ce qui est calculé à chaque appel suit la langue.
        assertEquals("The Silk Road", Challenges.of(WeekActivity(0, 0)).first().title)
        assertEquals("First step", Badges.of(GameStats()).first().title)
        assertEquals("Every 2 days", Recurrence(Recurrence.Frequency.DAILY, interval = 2).describe())
        assertEquals("3 times a week", describeFrequency(3, 7))
        assertEquals("Mon", DayOfWeek.MONDAY.shortFr())
        assertEquals("Be active 5 days", WeeklyGoal(WeeklyGoal.Kind.ACTIVE_DAYS, 5).text)
        assertEquals("0 days", plural(0, "day"))
    }
}
