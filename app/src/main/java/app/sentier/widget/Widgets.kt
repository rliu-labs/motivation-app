/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import app.sentier.MainActivity
import app.sentier.R
import app.sentier.SentierApplication
import app.sentier.notify.ReminderActions
import app.sentier.notify.ReminderItem

/** Met à jour les widgets posés sur l'écran d'accueil. */
object Widgets {
    /** Vrai si au moins un widget de Sentier est posé. */
    fun anyPlaced(context: Context): Boolean {
        val manager = AppWidgetManager.getInstance(context) ?: return false
        return listOf(TodayWidgetReceiver::class.java, StepsWidgetReceiver::class.java).any { cls ->
            runCatching { manager.getAppWidgetIds(ComponentName(context, cls)).isNotEmpty() }.getOrDefault(false)
        }
    }

    suspend fun updateAll(context: Context) {
        TodayWidget().updateAll(context)
        StepsWidget().updateAll(context)
    }

    internal suspend fun load(context: Context): WidgetData {
        val app = context.applicationContext as SentierApplication
        return runCatching {
            WidgetData.load(app.tasks, app.habits, app.database.habitDao(), app.game, app.settings)
        }.getOrDefault(WidgetData())
    }
}

/** Widget « Aujourd'hui » : la série, les pas et les choses à faire, à cocher d'un geste. */
class TodayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = Widgets.load(context)
        provideContent { GlanceTheme { TodayContent(data) } }
    }
}

/** Petit widget « Pas et série ». */
class StepsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = Widgets.load(context)
        provideContent { GlanceTheme { StepsContent(data) } }
    }
}

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        (context.applicationContext as? SentierApplication)?.startWidgetSync()
    }
}

class StepsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StepsWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        (context.applicationContext as? SentierApplication)?.startWidgetSync()
    }
}

/** Coche une tâche ou une habitude depuis le widget, XP et or compris, sans ouvrir l'appli. */
class CompleteFromWidget : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val app = context.applicationContext as SentierApplication
        val type = parameters[TYPE]?.let { runCatching { ReminderItem.Type.valueOf(it) }.getOrNull() } ?: return
        val id = parameters[ID] ?: return
        runCatching { ReminderActions.complete(app.tasks, app.habits, type, id) }
        Widgets.updateAll(context)
    }

    companion object {
        val TYPE = ActionParameters.Key<String>("type")
        val ID = ActionParameters.Key<Long>("id")
    }
}

@Composable
private fun TodayContent(data: WidgetData) {
    val colors = GlanceTheme.colors
    Column(
        GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(20.dp)
            .background(colors.widgetBackground)
            .padding(14.dp),
    ) {
        Row(
            GlanceModifier.fillMaxWidth().clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(GlanceModifier.defaultWeight()) {
                Text(
                    "Aujourd'hui",
                    style = TextStyle(color = colors.onSurface, fontSize = 17.sp, fontWeight = FontWeight.Bold),
                )
                Text(data.summary, style = TextStyle(color = colors.onSurfaceVariant, fontSize = 13.sp), maxLines = 1)
            }
            Text(
                "🔥 ${data.streak}",
                style = TextStyle(color = colors.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold),
            )
            if (data.stepsOn) {
                Spacer(GlanceModifier.width(10.dp))
                Text(
                    "👣 ${data.stepsText}",
                    style = TextStyle(color = colors.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold),
                )
            }
        }
        Spacer(GlanceModifier.height(8.dp))
        if (data.items.isEmpty()) {
            Text(
                "Rien d'autre au programme. Profite !",
                style = TextStyle(color = colors.onSurfaceVariant, fontSize = 14.sp),
                modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>()),
            )
        } else {
            LazyColumn(GlanceModifier.fillMaxWidth()) {
                items(data.items, itemId = { it.id * 2 + if (it.type == ReminderItem.Type.HABIT) 1 else 0 }) { item ->
                    ItemRow(item)
                }
            }
        }
    }
}

@Composable
private fun ItemRow(item: ReminderItem) {
    val colors = GlanceTheme.colors
    Row(
        GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (item.canComplete) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_check),
                contentDescription = "Valider ${item.title}",
                colorFilter = ColorFilter.tint(colors.primary),
                modifier = GlanceModifier
                    .size(28.dp)
                    .padding(2.dp)
                    .clickable(
                        actionRunCallback<CompleteFromWidget>(
                            actionParametersOf(CompleteFromWidget.TYPE to item.type.name, CompleteFromWidget.ID to item.id)
                        )
                    ),
            )
        } else {
            Spacer(GlanceModifier.size(28.dp))
        }
        Spacer(GlanceModifier.width(8.dp))
        Column(GlanceModifier.defaultWeight().clickable(actionStartActivity<MainActivity>())) {
            Text(item.title, style = TextStyle(color = colors.onSurface, fontSize = 14.sp), maxLines = 1)
            item.detail?.let {
                Text(it, style = TextStyle(color = if (it == "En retard") colors.error else colors.onSurfaceVariant, fontSize = 12.sp), maxLines = 1)
            }
        }
    }
}

@Composable
private fun StepsContent(data: WidgetData) {
    val colors = GlanceTheme.colors
    Column(
        GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(20.dp)
            .background(colors.widgetBackground)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (data.stepsOn) {
            Text(
                "👣 ${data.stepsText}",
                style = TextStyle(color = colors.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
            Text("sur ${data.goalText} pas", style = TextStyle(color = colors.onSurfaceVariant, fontSize = 12.sp), maxLines = 1)
            Spacer(GlanceModifier.height(6.dp))
            LinearProgressIndicator(
                progress = data.stepProgress,
                modifier = GlanceModifier.fillMaxWidth().height(6.dp),
                color = colors.primary,
                backgroundColor = colors.surfaceVariant,
            )
        } else {
            Text("Pas non comptés", style = TextStyle(color = colors.onSurfaceVariant, fontSize = 13.sp), maxLines = 2)
        }
        Spacer(GlanceModifier.height(8.dp))
        Text(
            "🔥 ${data.streakText}",
            style = TextStyle(color = colors.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
        Text("Niveau ${data.level}", style = TextStyle(color = colors.onSurfaceVariant, fontSize = 12.sp), maxLines = 1)
    }
}
