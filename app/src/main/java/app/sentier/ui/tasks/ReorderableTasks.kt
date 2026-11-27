/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.tasks

import app.sentier.domain.tr
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.sentier.data.TaskWithCounts

/**
 * Ordre des tâches pendant un glissement : la liste bouge à l'écran tout de suite,
 * et le nouvel ordre n'est enregistré qu'au moment de lâcher la tâche.
 */
@Stable
class ReorderState(private val listState: LazyListState, private val onMove: (List<Long>) -> Unit) {
    val items = mutableStateListOf<TaskWithCounts>()
    var draggingId by mutableStateOf<Long?>(null)
        private set
    var offset by mutableFloatStateOf(0f)
        private set

    /** Reprend les tâches de la base, sauf pendant un glissement. */
    fun sync(tasks: List<TaskWithCounts>) {
        if (draggingId != null) return
        if (items.map { it.id to it } != tasks.map { it.id to it }) {
            items.clear()
            items.addAll(tasks)
        }
    }

    fun start(id: Long) {
        draggingId = id
        offset = 0f
    }

    fun drag(dy: Float) {
        val id = draggingId ?: return
        offset += dy
        val visible = listState.layoutInfo.visibleItemsInfo
        val current = visible.firstOrNull { it.key == id } ?: return
        val center = current.offset + offset + current.size / 2f
        val target = visible.firstOrNull { info ->
            info.key != id && info.key is Long && items.any { it.id == info.key } &&
                center > info.offset && center < info.offset + info.size
        } ?: return
        val from = items.indexOfFirst { it.id == id }
        val to = items.indexOfFirst { it.id == target.key }
        if (from < 0 || to < 0) return
        items.add(to, items.removeAt(from))
        offset += current.offset - target.offset
    }

    fun end() {
        if (draggingId != null) onMove(items.map { it.id })
        draggingId = null
        offset = 0f
    }

    /** Pour les lecteurs d'écran : déplace d'une place vers le haut (-1) ou le bas (+1). */
    fun moveBy(id: Long, delta: Int): Boolean {
        val from = items.indexOfFirst { it.id == id }
        val to = from + delta
        if (from < 0 || to !in items.indices) return false
        items.add(to, items.removeAt(from))
        onMove(items.map { it.id })
        return true
    }
}

@Composable
fun rememberReorderState(listState: LazyListState, onMove: (List<Long>) -> Unit): ReorderState =
    remember(listState) { ReorderState(listState, onMove) }

/** Les tâches de « Mon ordre », chacune avec une poignée pour la faire glisser. */
fun LazyListScope.reorderableTasks(
    state: ReorderState,
    row: @Composable (task: TaskWithCounts, modifier: Modifier, handle: @Composable () -> Unit) -> Unit,
) {
    itemsIndexed(state.items, key = { _, t -> t.id }) { index, task ->
        val dragging = state.draggingId == task.id
        val modifier = if (dragging) {
            Modifier.zIndex(1f).graphicsLayer { translationY = state.offset }
        } else {
            Modifier.animateItem()
        }
        Surface(
            modifier = modifier,
            tonalElevation = if (dragging) 3.dp else 0.dp,
            shadowElevation = if (dragging) 6.dp else 0.dp,
        ) {
            row(task, Modifier) {
                DragHandle(task, index, state)
            }
        }
    }
}

@Composable
private fun DragHandle(task: TaskWithCounts, index: Int, state: ReorderState) {
    val actions = buildList {
        if (index > 0) add(CustomAccessibilityAction(tr("Monter", "Move up")) { state.moveBy(task.id, -1) })
        if (index < state.items.lastIndex) add(CustomAccessibilityAction(tr("Descendre", "Move down")) { state.moveBy(task.id, 1) })
    }
    Box(
        Modifier
            .size(48.dp)
            .pointerInput(task.id, state) {
                detectDragGestures(
                    onDragStart = { state.start(task.id) },
                    onDrag = { change, amount ->
                        change.consume()
                        state.drag(amount.y)
                    },
                    onDragEnd = { state.end() },
                    onDragCancel = { state.end() },
                )
            }
            .semantics {
                contentDescription = tr("Déplacer ${task.title}", "Move ${task.title}")
                customActions = actions
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.DragHandle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
