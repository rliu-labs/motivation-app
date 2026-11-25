/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

/** Saisie rapide : on tape, on valide, la tâche est créée. */
@Composable
fun QuickAddBar(
    placeholder: String,
    onAdd: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val submit = {
        if (text.isNotBlank()) {
            onAdd(text.trim())
            text = ""
        }
    }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("quickAdd"),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
        trailingIcon = {
            if (text.isNotBlank()) {
                IconButton(onClick = submit) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Ajouter")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { submit() }),
    )
}
