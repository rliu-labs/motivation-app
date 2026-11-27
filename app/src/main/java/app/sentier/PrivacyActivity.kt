/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import app.sentier.ui.common.French
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
    // Toujours en français, même sur un téléphone réglé dans une autre langue.
    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(French.wrap(newBase))

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
                        Text("Sentier et tes données", style = MaterialTheme.typography.headlineSmall)
                        Text("Sentier lit ton nombre de pas, pour afficher ta progression vers ton objectif du jour et ton historique. Si tu l'actives, il lit aussi tes nuits de sommeil et tes séances de sport, pour cocher tes habitudes correspondantes.")
                        Text("Ces données restent sur ton téléphone. Elles ne sont envoyées nulle part et ne sont partagées avec personne.")
                        Text("Tu peux retirer l'autorisation à tout moment dans les réglages de Health Connect.")
                        Button(onClick = { finish() }) { Text("Compris") }
                    }
                }
            }
        }
    }
}
