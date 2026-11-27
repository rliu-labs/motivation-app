package app.sentier.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameTest {

    @Test
    fun levels() {
        assertEquals(LevelInfo(1, 0, 50), GameRules.levelOf(0))
        assertEquals(LevelInfo(1, 49, 50), GameRules.levelOf(49))
        assertEquals(LevelInfo(2, 0, 75), GameRules.levelOf(50))
        assertEquals(LevelInfo(3, 10, 100), GameRules.levelOf(135))
        assertEquals(1, GameRules.levelOf(-20).level)
    }

    @Test
    fun rewardsByDifficulty() {
        assertEquals(10, GameRules.xpFor(1))
        assertEquals(20, GameRules.xpFor(2))
        assertEquals(30, GameRules.xpFor(3))
        assertEquals(4, GameRules.goldFor(20))
    }

    @Test
    fun noActivityMeansNoStreak() {
        val s = Streaks.compute(emptySet(), 100)
        assertEquals(0, s.current)
        assertEquals(1, s.restDays)
    }

    @Test
    fun consecutiveDays() {
        val s = Streaks.compute((96L..100L).toSet(), 100)
        assertEquals(5, s.current)
        assertEquals(5, s.best)
        assertTrue(s.activeToday)
    }

    @Test
    fun todayNotDoneYetDoesNotBreak() {
        val s = Streaks.compute((95L..99L).toSet(), 100)
        assertEquals(5, s.current)
        assertFalse(s.activeToday)
        assertEquals(1, s.restDays)
    }

    @Test
    fun oneMissedDayUsesTheRestDay() {
        // Actif du 90 au 94, raté le 95, actif du 96 au 100.
        val s = Streaks.compute((90L..94L).toSet() + (96L..100L).toSet(), 100)
        assertEquals(10, s.current)
        // Le jour de repos est consommé, puis un nouveau est gagné au 7e jour actif.
        assertEquals(1, s.restDays)
    }

    @Test
    fun twoMissedDaysWithoutReserveBreak() {
        val s = Streaks.compute((90L..94L).toSet() + (97L..100L).toSet(), 100)
        assertEquals(4, s.current)
        assertEquals(5, s.best)
    }

    @Test
    fun aWeekEarnsARestDayUpToTwo() {
        assertEquals(2, Streaks.compute((80L..86L).toSet(), 86).restDays)
        assertEquals(2, Streaks.compute((60L..100L).toSet(), 100).restDays)
    }

    @Test
    fun aFreezeSavesTheStreakWhenRestDaysAreGone() {
        // Même trou de deux jours que ci-dessus, avec un gel acheté avant : la série tient.
        val s = Streaks.compute((90L..94L).toSet() + (97L..100L).toSet(), 100, boughtFreezes = listOf(92L))
        assertEquals(9, s.current)
        assertEquals(0, s.freezes)
        assertEquals(1, s.freezesUsed)
    }

    @Test
    fun aFreezeBoughtAfterTheBreakDoesNotRepairIt() {
        val s = Streaks.compute((90L..94L).toSet() + (97L..100L).toSet(), 100, boughtFreezes = listOf(98L))
        assertEquals(4, s.current)
        assertEquals(1, s.freezes)
    }

    @Test
    fun oneFreeFreezeAtStartAndEachQuarterUpToTwo() {
        val jan1 = java.time.LocalDate.of(2027, 1, 1).toEpochDay()
        // Aucune action : le gel de bienvenue attend déjà.
        assertEquals(1, Streaks.compute(emptySet(), jan1, gifts = true).freezes)
        // Actif depuis début décembre : gel de départ, puis celui du 1er janvier.
        val dec1 = java.time.LocalDate.of(2026, 12, 1).toEpochDay()
        assertEquals(2, Streaks.compute((dec1..jan1).toSet(), jan1, gifts = true).freezes)
        // La réserve plafonne à deux, même avec un achat en plus.
        assertEquals(2, Streaks.compute((dec1..jan1).toSet(), jan1, boughtFreezes = listOf(jan1), gifts = true).freezes)
    }

    @Test
    fun badges() {
        val none = Badges.of(GameStats())
        assertTrue(none.none { it.unlocked })
        val some = Badges.of(GameStats(tasksDone = 12, bestStreak = 7, level = 5))
        val unlocked = some.filter { it.unlocked }.map { it.id }.toSet()
        assertEquals(setOf("first_task", "tasks_10", "streak_7", "level_5"), unlocked)
    }
}
