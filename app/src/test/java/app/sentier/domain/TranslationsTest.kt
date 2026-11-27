/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationsTest {
    private val french = Journeys.all

    @Test
    fun everyJourneyHasACompleteEnglishVersion() {
        assertTrue(french.size >= 12)
        for (j in french) {
            val en = JourneysEn.all[j.id]
            assertNotNull("Pas de traduction pour ${j.id}", en)
            assertEquals("Nombre d'escales de ${j.id}", j.stops.size, en!!.stops.size)
            assertTrue(en.title.isNotBlank() && en.tagline.isNotBlank())
            en.stops.forEachIndexed { i, s ->
                val texts = listOf(s.city, s.country, s.architecture, s.culture, s.anecdote, s.souvenirName, s.souvenirDescription)
                assertTrue("Texte vide : ${j.id} n° $i", texts.all { it.isNotBlank() })
            }
        }
        assertTrue("Traduction d'un voyage inconnu", JourneysEn.all.keys.all { id -> french.any { it.id == id } })
    }

    @Test
    fun englishTextsHaveTheLengthOfTheFrenchOnes() {
        // Une escale décalée d'un cran (texte d'une autre ville) se verrait à des longueurs très différentes.
        for (j in french) {
            val en = JourneysEn.all.getValue(j.id)
            j.stops.zip(en.stops).forEachIndexed { i, (fr, e) ->
                for ((a, b) in listOf(fr.architecture to e.architecture, fr.culture to e.culture, fr.anecdote to e.anecdote)) {
                    val ratio = b.length.toDouble() / a.length
                    assertTrue("${j.id} n° $i (${fr.city}) : « $b »", ratio in 0.5..1.6)
                }
            }
        }
    }

    @Test
    fun localizedKeepsKeysAndNumbers() {
        val fr = WorldTour.journey
        assertSame(fr, fr.localized(english = false))
        val en = fr.localized(english = true)
        assertEquals("World tour", en.title)
        assertEquals(fr.id, en.id)
        assertEquals(fr.keyBase, en.keyBase)
        assertEquals(fr.stops.map { it.km }, en.stops.map { it.km })
        assertEquals(fr.stops.map { it.lat to it.lon }, en.stops.map { it.lat to it.lon })
        assertEquals(fr.stops.map { it.souvenir.icon }, en.stops.map { it.souvenir.icon })
        assertEquals("Marrakesh", en.stops[1].city)
        assertEquals("Morocco", en.stops[1].country)
        assertEquals("Babouches", en.stops[1].souvenir.name)
    }

    @Test
    fun countryJourneysInEnglish() {
        val italy = EuropeTours.italie.localized(english = true)
        assertEquals("Italy", italy.title)
        assertEquals("Venice", italy.stops.first().city)
        val greece = EuropeTours2.grece.localized(english = true)
        assertEquals("Santorini", greece.stops.last().city)
        assertEquals("Tour de France", TourDeFrance.journey.localized(english = true).title)
    }
}
