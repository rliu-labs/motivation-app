package app.sentier.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TreasuresTest {

    @Test
    fun treasuresAreUniqueAndComplete() {
        val all = Treasures.of(GameStats(), 0)
        assertEquals(Treasures.count, all.size)
        assertEquals(all.size, all.map { it.id }.toSet().size)
        assertTrue(all.none { it.owned })
        assertTrue(all.all { it.description.isNotBlank() && it.anecdote.isNotBlank() && it.origin.isNotBlank() })
        // Chaque façon de gagner a ses trésors, et chaque rareté existe.
        assertEquals(TreasureSource.entries.toSet(), all.map { it.source }.toSet())
        assertEquals(Rarity.entries.toSet(), all.map { it.rarity }.toSet())
    }

    @Test
    fun levelsStreaksBadgesAndChallengesUnlockTreasures() {
        val stats = GameStats(level = 5, bestStreak = 7, challenges = 1)
        val owned = Treasures.of(stats, badgesUnlocked = 3).filter { it.owned }.map { it.id }.toSet()
        assertEquals(setOf("compass", "astrolabe", "raku", "matryoshka", "mercator", "panama"), owned)
    }

    @Test
    fun theLampNeedsEveryBadge() {
        val total = Badges.of(GameStats()).size
        val lamp = { n: Int -> Treasures.of(GameStats(), n).first { it.id == "lamp" } }
        assertEquals(false, lamp(total - 1).owned)
        assertEquals(true, lamp(total).owned)
        assertEquals("Obtenir tous les badges", lamp(0).howTo)
    }
}
