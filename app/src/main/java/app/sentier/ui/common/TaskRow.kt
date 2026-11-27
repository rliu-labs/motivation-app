/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.sentier.data.TaskList
import app.sentier.data.TaskWithCounts
import app.sentier.ui.theme.PriorityColors
import java.time.LocalDate

/** Case ronde à cocher, colorée selon la priorité (à la Tasks.org). */
@Composable
fun PriorityCheckbox(
    checked: Boolean,
    priority: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
) {
    val color = PriorityColors.of(priority)
    val tick = rememberTick()
    val fill by animateColorAsState(if (checked) color else Color.Transparent, label = "fill")
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable {
                if (!checked) tick()
                onClick()
            }
            .semantics { contentDescription = if (checked) "Décocher $label" else "Valider $label" },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(fill)
                .border(2.5.dp, color, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun TaskRow(
    task: TaskWithCounts,
    checked: Boolean,
    list: TaskList?,
    today: LocalDate,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Élément affiché au bout de la ligne (poignée pour déplacer la tâche). */
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 4.dp, end = 16.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PriorityCheckbox(checked = checked, priority = task.priority, onClick = onToggle, label = task.title)
        Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
            Text(
                task.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (checked) TextDecoration.LineThrough else null,
                color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
            val due = task.dueDay?.let(LocalDate::ofEpochDay)
            val hasMeta = (due != null && !checked) || task.recurrence != null || task.subtaskCount > 0 || list != null
            if (hasMeta) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    if (due != null && !checked) {
                        val late = due.isBefore(today)
                        Meta(
                            icon = { Icon(Icons.Filled.Event, null, Modifier.size(14.dp), tint = it) },
                            text = friendlyDate(due, today),
                            color = if (late) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (task.recurrence != null) {
                        Icon(
                            Icons.Filled.Repeat, contentDescription = "Tâche répétée",
                            modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (task.subtaskCount > 0) {
                        Meta(
                            icon = { Icon(Icons.AutoMirrored.Filled.List, null, Modifier.size(14.dp), tint = it) },
                            text = "${task.subtaskDone}/${task.subtaskCount}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (list != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(Color(list.color)))
                            Spacer(Modifier.width(4.dp))
                            Text(list.name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        trailing?.invoke()
    }
}

@Composable
private fun Meta(icon: @Composable (Color) -> Unit, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon(color)
        Spacer(Modifier.width(3.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}
