/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import app.sentier.domain.Passport
import app.sentier.domain.PassportState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Le passeport, d'après les escales récompensées du journal du jeu et le voyage en cours. */
class PassportRepository(private val gameDao: GameDao, private val settings: Settings) {
    fun observe(): Flow<PassportState> =
        combine(gameDao.observeRefDays(GameEvent.STOP), settings.journey) { stops, j ->
            Passport.of(stops.associate { it.refId to it.day }, j.current)
        }
}
