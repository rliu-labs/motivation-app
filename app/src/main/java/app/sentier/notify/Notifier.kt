/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.notify

import app.sentier.domain.tr
import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.sentier.MainActivity
import app.sentier.R

/** Affiche les notifications de Sentier. */
object Notifier {
    const val CHANNEL = "rappels"
    const val CHANNEL_HABITS = "rappels-habitudes"
    const val CHANNEL_WEEKLY = "bilan-semaine"
    private const val GROUP_EVENING = "app.sentier.SOIR"
    const val ID_MORNING = 1
    const val ID_EVENING = 2
    const val ID_WEEKLY = 3

    /** Écran à ouvrir quand on touche une notification. */
    const val EXTRA_OPEN = "app.sentier.OUVRIR"
    const val OPEN_REVIEW = "bilan"

    /** Identifiant de la notification d'une chose à faire (stable d'un soir à l'autre). */
    fun itemId(type: ReminderItem.Type, id: Long): Int = when (type) {
        ReminderItem.Type.TASK -> 100_000 + (id % 100_000).toInt()
        ReminderItem.Type.HABIT -> 200_000 + (id % 100_000).toInt()
    }

    /** Vrai si Android laisse Sentier afficher des notifications. */
    fun canPost(context: Context): Boolean {
        val granted = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        return granted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun ensureChannel(context: Context) {
        NotificationManagerCompat.from(context).createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(tr("Rappels", "Reminders"))
                .setDescription(tr("Le programme du matin et ce qui reste à faire le soir", "The morning plan and what is left to do in the evening"))
                .build()
        )
    }

    fun ensureHabitChannel(context: Context) {
        NotificationManagerCompat.from(context).createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_HABITS, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(tr("Rappels des habitudes", "Habit reminders"))
                .setDescription(tr("Le rappel de chaque habitude, à l'heure choisie", "Each habit's reminder, at the chosen time"))
                .build()
        )
    }

    /** Le bilan du dimanche : un résumé, et l'écran du bilan quand on le touche. */
    fun showWeeklyReview(context: Context, title: String, text: String) {
        if (!canPost(context)) return
        NotificationManagerCompat.from(context).createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_WEEKLY, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(tr("Bilan de la semaine", "Weekly review"))
                .setDescription(tr("Le résumé de ta semaine, chaque dimanche", "A summary of your week, every Sunday"))
                .build()
        )
        val notification = base(context, title, text, CHANNEL_WEEKLY)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openApp(context, OPEN_REVIEW))
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .build()
        post(context, ID_WEEKLY, notification)
    }

    /** Le rappel d'une habitude à son heure, avec un bouton « Fait » si elle se coche d'un geste. */
    fun showHabit(context: Context, message: ReminderMessage) {
        if (!canPost(context)) return
        val item = message.items.firstOrNull() ?: return
        ensureHabitChannel(context)
        val id = itemId(item.type, item.id)
        val builder = base(context, message.title, message.text, CHANNEL_HABITS)
        if (item.canComplete) {
            builder.addAction(0, tr("Fait", "Done"), ReminderActionReceiver.intent(context, item.type, item.id, id))
        }
        post(context, id, builder.build())
    }

    /** Le programme de la journée, un seul message. */
    fun showMorning(context: Context, message: ReminderMessage) {
        if (!canPost(context)) return
        ensureChannel(context)
        val manager = NotificationManagerCompat.from(context)
        manager.cancelAll()
        val notification = base(context, message.title, message.text)
            .setStyle(inbox(message))
            .build()
        post(context, ID_MORNING, notification)
    }

    /**
     * Ce qui reste le soir : un résumé, puis une notification par chose à faire
     * avec un bouton « Fait ». Seul le résumé fait du bruit.
     */
    fun showEvening(context: Context, message: ReminderMessage) {
        if (!canPost(context)) return
        ensureChannel(context)
        NotificationManagerCompat.from(context).cancelAll()
        val summary = base(context, message.title, message.text)
            .setStyle(inbox(message))
            .setGroup(GROUP_EVENING)
            .setGroupSummary(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            .build()
        for (item in message.items) {
            val id = itemId(item.type, item.id)
            val builder = base(context, item.title, item.detail ?: tr("À faire aujourd'hui", "To do today"))
                .setGroup(GROUP_EVENING)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            if (item.canComplete) {
                builder.addAction(0, tr("Fait", "Done"), ReminderActionReceiver.intent(context, item.type, item.id, id))
            }
            post(context, id, builder.build())
        }
        post(context, ID_EVENING, summary)
    }

    /** Remplace la notification d'une chose validée par un bravo qui s'efface tout seul. */
    fun showDone(context: Context, notificationId: Int, text: String) {
        if (!canPost(context)) return
        val notification = base(context, tr("Bravo !", "Well done!"), text)
            .setGroup(GROUP_EVENING)
            .setSilent(true)
            .setTimeoutAfter(4_000)
            .build()
        post(context, notificationId, notification)
    }

    fun cancel(context: Context, notificationId: Int) = NotificationManagerCompat.from(context).cancel(notificationId)

    private fun base(context: Context, title: String, text: String, channel: String = CHANNEL) =
        NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF2E7D5B.toInt())
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openApp(context))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

    private fun inbox(message: ReminderMessage): NotificationCompat.InboxStyle {
        val style = NotificationCompat.InboxStyle().setSummaryText(message.text)
        for (item in message.items) style.addLine(item.detail?.let { "${item.title} · $it" } ?: item.title)
        return style
    }

    private fun openApp(context: Context, open: String? = null): PendingIntent = PendingIntent.getActivity(
        context, if (open == null) 0 else 1,
        Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .apply { if (open != null) putExtra(EXTRA_OPEN, open) },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    @SuppressLint("MissingPermission") // vérifié par canPost()
    private fun post(context: Context, id: Int, notification: android.app.Notification) {
        runCatching { NotificationManagerCompat.from(context).notify(id, notification) }
    }
}
