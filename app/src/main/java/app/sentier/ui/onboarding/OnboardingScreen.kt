/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.onboarding

import app.sentier.domain.tr
import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sentier.domain.HabitTemplates
import app.sentier.notify.Notifier
import app.sentier.notify.ReminderContent
import app.sentier.data.ReminderSettings
import app.sentier.ui.habits.HabitIcons
import androidx.compose.ui.platform.LocalContext
import java.text.NumberFormat
import java.util.Locale

private const val PAGES = 4
private val GOALS = listOf(5_000, 8_000, 10_000, 12_000)

/** Accueil du premier lancement : bienvenue, premières habitudes, objectif de pas et rappels. */
@Composable
fun OnboardingScreen(onDone: () -> Unit, vm: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory)) {
    OnboardingContent(
        onFinish = { choices -> vm.finish(choices, onDone) },
        onSkip = { vm.skip(onDone) },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingContent(onFinish: (OnboardingChoices) -> Unit, onSkip: () -> Unit) {
    val context = LocalContext.current
    var page by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableStateOf(listOf<Int>()) }
    var goal by rememberSaveable { mutableIntStateOf(8_000) }
    var adaptive by rememberSaveable { mutableStateOf(false) }
    var morning by rememberSaveable { mutableStateOf(true) }
    var evening by rememberSaveable { mutableStateOf(true) }

    val choices = OnboardingChoices(
        habits = selected.sorted().map { HabitTemplates[it] },
        stepGoal = goal,
        adaptive = adaptive,
        morning = morning,
        evening = evening,
    )
    // La réponse ne change rien aux choix : les rappels sont programmés, Android les affichera une fois autorisés.
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    fun finish() {
        if ((morning || evening) && Build.VERSION.SDK_INT >= 33 && !Notifier.canPost(context)) {
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        onFinish(choices)
    }

    BackHandler(enabled = page > 0) { page-- }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxSize().safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
                if (page < PAGES - 1) TextButton(onClick = onSkip) { Text(tr("Passer", "Skip")) } else Spacer(Modifier.height(48.dp))
            }
            AnimatedContent(
                targetState = page,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                label = "accueil",
            ) { p ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(Modifier.widthIn(max = 560.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        when (p) {
                            0 -> WelcomePage()
                            1 -> HabitsPage(selected) { i -> selected = if (i in selected) selected - i else selected + i }
                            2 -> StepsPage(goal, adaptive, onGoal = { goal = it }, onAdaptive = { adaptive = it })
                            else -> RemindersPage(morning, evening, onMorning = { morning = it }, onEvening = { evening = it })
                        }
                    }
                }
            }
            Dots(page)
            Row(
                Modifier.fillMaxWidth().widthIn(max = 560.dp).padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (page > 0) TextButton(onClick = { page-- }) { Text(tr("Retour", "Back")) }
                Spacer(Modifier.weight(1f))
                Button(onClick = { if (page < PAGES - 1) page++ else finish() }) {
                    Text(
                        when (page) {
                            0 -> tr("Commencer", "Start")
                            PAGES - 1 -> tr("C'est parti", "Let's go")
                            else -> tr("Suivant", "Next")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Dots(page: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.semantics { contentDescription = tr("Étape ${page + 1} sur $PAGES", "Step ${page + 1} of $PAGES") },
    ) {
        repeat(PAGES) { i ->
            Box(
                Modifier
                    .size(if (i == page) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (i == page) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
            )
        }
    }
}

@Composable
private fun PageHeader(icon: ImageVector, title: String, text: String) {
    Spacer(Modifier.height(16.dp))
    Box(
        Modifier.size(88.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(44.dp))
    }
    Spacer(Modifier.height(20.dp))
    Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
    Spacer(Modifier.height(8.dp))
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun WelcomePage() {
    PageHeader(
        Icons.Filled.WbSunny,
        tr("Bienvenue sur Sentier", "Welcome to Sentier"),
        tr("Un peu de progrès chaque jour, sans pression. Tout reste sur ton téléphone.", "A little progress every day, without pressure. Everything stays on your phone."),
    )
    Feature(Icons.Filled.Checklist, tr("Tes tâches et tes habitudes du jour, réunies sur un seul écran.", "Your tasks and habits for the day, together on one screen."))
    Feature(Icons.Filled.Map, tr("Tes pas te font voyager autour du monde, puis en France et en Europe, escale par escale.", "Your steps take you around the world, then through France and Europe, stop by stop."))
    Feature(Icons.Filled.EmojiEvents, tr("De l'XP, de l'or et des trésors à gagner. Rien n'est jamais perdu.", "XP, gold and treasures to earn. Nothing is ever lost."))
}

@Composable
private fun Feature(icon: ImageVector, text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
    }
}

@Composable
private fun HabitsPage(selected: List<Int>, onToggle: (Int) -> Unit) {
    PageHeader(
        Icons.Filled.SelfImprovement,
        tr("Tes premières habitudes", "Your first habits"),
        tr("Choisis-en une ou deux pour commencer. Tu pourras les modifier ou en créer d'autres à tout moment.", "Pick one or two to start. You can change them or create others at any time."),
    )
    HabitTemplates.forEachIndexed { i, t ->
        val checked = i in selected
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (checked) Color(t.color).copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainer,
            ),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(CardDefaults.shape).clickable { onToggle(i) },
        ) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(HabitIcons.of(t.icon), contentDescription = null, tint = Color(t.color))
                Column(Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(t.name, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        templateDetail(t.target, t.unit, t.freqNum, t.freqDen),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Checkbox(checked = checked, onCheckedChange = { onToggle(i) })
            }
        }
    }
}

private fun templateDetail(target: Double, unit: String, freqNum: Int, freqDen: Int): String {
    val freq = if (freqNum >= freqDen) tr("chaque jour", "every day") else if (freqDen == 7) tr("$freqNum fois par semaine", "$freqNum times a week") else tr("$freqNum fois tous les $freqDen jours", "$freqNum times every $freqDen days")
    return if (unit.isNotBlank()) "${target.toInt()} $unit, $freq" else freq.replaceFirstChar { it.uppercase() }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepsPage(goal: Int, adaptive: Boolean, onGoal: (Int) -> Unit, onAdaptive: (Boolean) -> Unit) {
    PageHeader(
        Icons.AutoMirrored.Filled.DirectionsWalk,
        tr("Ton objectif de pas", "Your step goal"),
        tr("Combien de pas veux-tu faire par jour ? Pour compter tes pas, active-les ensuite dans l'onglet Habitudes.", "How many steps do you want to walk a day? To count your steps, turn them on later in the Habits tab."),
    )
    val format = NumberFormat.getIntegerInstance(app.sentier.domain.Lang.locale)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), modifier = Modifier.fillMaxWidth()) {
        for (g in GOALS) {
            FilterChip(selected = g == goal, onClick = { onGoal(g) }, label = { Text(tr("${format.format(g)} pas", "${format.format(g)} steps")) })
        }
    }
    Spacer(Modifier.height(16.dp))
    SwitchRow(
        title = tr("Objectif adaptatif", "Adaptive goal"),
        text = tr("Il suit ton rythme réel des deux dernières semaines, pour rester à ta portée.", "It follows your real pace of the last two weeks, so it stays within reach."),
        checked = adaptive,
        onChange = onAdaptive,
    )
}

@Composable
private fun RemindersPage(morning: Boolean, evening: Boolean, onMorning: (Boolean) -> Unit, onEvening: (Boolean) -> Unit) {
    PageHeader(
        Icons.Filled.NotificationsActive,
        tr("Un petit rappel ?", "A little reminder?"),
        tr("Sentier peut te donner le programme le matin et te dire le soir ce qui reste. Tu pourras changer les heures dans les réglages.", "Sentier can give you the plan in the morning and tell you in the evening what is left. You can change the times in the settings."),
    )
    SwitchRow(
        title = tr("Le matin à ${ReminderContent.formatMinute(ReminderSettings.DEFAULT_MORNING)}", "In the morning at ${ReminderContent.formatMinute(ReminderSettings.DEFAULT_MORNING)}"),
        text = tr("Le programme de la journée.", "The plan for the day."),
        checked = morning,
        onChange = onMorning,
    )
    SwitchRow(
        title = tr("Le soir à ${ReminderContent.formatMinute(ReminderSettings.DEFAULT_EVENING)}", "In the evening at ${ReminderContent.formatMinute(ReminderSettings.DEFAULT_EVENING)}"),
        text = tr("Ce qui reste, seulement s'il reste quelque chose.", "What is left, only if something is left."),
        checked = evening,
        onChange = onEvening,
    )
}

@Composable
private fun SwitchRow(title: String, text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange, modifier = Modifier.semantics { contentDescription = title })
    }
}
