/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier

import app.sentier.domain.tr
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import app.sentier.ui.common.AppLocale
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.sentier.ui.theme.SentierTheme

/** Page affichée par Health Connect pour expliquer l'usage des pas. */
class PrivacyActivity : ComponentActivity() {
    // Dans la langue choisie, même si le téléphone est réglé dans une autre langue.
    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(AppLocale.wrap(newBase))

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SentierTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(tr("Sentier et tes données", "Sentier and your data"), style = MaterialTheme.typography.headlineSmall)
                        Text(tr("Sentier lit ton nombre de pas, pour afficher ta progression vers ton objectif du jour et ton historique. Si tu l'actives, il lit aussi tes nuits de sommeil et tes séances de sport, pour cocher tes habitudes correspondantes.", "Sentier reads your step count to show your progress towards today's goal and your history. If you turn it on, it also reads your sleep and workouts to check the matching habits."))
                        Text(tr("Ces données restent sur ton téléphone. Elles ne sont envoyées nulle part et ne sont partagées avec personne.", "This data stays on your phone. It is not sent anywhere and not shared with anyone."))
                        Text(tr("Tu peux retirer l'autorisation à tout moment dans les réglages de Health Connect.", "You can remove the permission at any time in Health Connect's settings."))
                        Button(onClick = { finish() }) { Text(tr("Compris", "Got it")) }
                    }
                }
            }
        }
    }
}
