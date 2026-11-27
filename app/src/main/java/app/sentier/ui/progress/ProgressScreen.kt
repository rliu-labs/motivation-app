/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.progress

import app.sentier.domain.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Activity
import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.platform.LocalContext
import app.sentier.data.GameState
import app.sentier.domain.AppLanguage
import app.sentier.ui.common.AppLocale
import app.sentier.data.Reward
import app.sentier.data.ThemeMode
import app.sentier.domain.Badge
import app.sentier.domain.Challenge
import app.sentier.domain.Streaks
import app.sentier.ui.common.SectionHeader
import java.text.NumberFormat
import java.util.Locale

/** Nom d'une langue, chacune écrite dans sa propre langue. */
private fun languageLabel(l: AppLanguage): String = when (l) {
    AppLanguage.SYSTEM -> tr("Comme le téléphone", "Same as the phone")
    AppLanguage.FR -> "Français"
    AppLanguage.EN -> "English"
}

/** Couleur de l'or, lisible sur fond clair et sombre. */
val GoldColor = Color(0xFFE0A100)

@Composable
fun ProgressScreen(
    onOpenCredits: () -> Unit,
    onOpenBackup: () -> Unit = {},
    onOpenStats: () -> Unit = {},
    onOpenReview: () -> Unit = {},
    vm: ProgressViewModel = viewModel(factory = ProgressViewModel.Factory),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val challenges by vm.challenges.collectAsStateWithLifecycle()
    val seasons by vm.seasons.collectAsStateWithLifecycle()
    val theme by vm.theme.collectAsStateWithLifecycle()
    val language by vm.language.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity
    var choosingTheme by remember { mutableStateOf(false) }
    var choosingLanguage by remember { mutableStateOf(false) }
    var buying by remember { mutableStateOf<Reward?>(null) }
    var editing by remember { mutableStateOf<Reward?>(null) }
    var creating by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text(
            tr("Progrès", "Progress"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
        )
        LevelCard(state)
        StreakCard(state, onBuyFreeze = vm::buyFreeze)
        ListItem(
            headlineContent = { Text(tr("Statistiques", "Statistics")) },
            supportingContent = { Text(tr("Pas, habitudes et XP sur la semaine, le mois et l'année", "Steps, habits and XP over the week, month and year")) },
            leadingContent = { Icon(Icons.Filled.BarChart, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onOpenStats).padding(horizontal = 4.dp),
        )
        ListItem(
            headlineContent = { Text(tr("Bilan de la semaine", "Weekly review")) },
            supportingContent = { Text(tr("Tes moments forts et ton objectif pour la semaine", "Your highlights and your goal for the week")) },
            leadingContent = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onOpenReview).padding(horizontal = 4.dp),
        )

        challenges?.let { week ->
            SectionHeader(tr("Défis de la semaine (${week.doneCount} sur ${week.list.size})", "Weekly challenges (${week.doneCount} of ${week.list.size})"))
            Text(
                when (week.daysLeft) {
                    0 -> tr("Dernier jour pour les relever ! De nouveaux défis arrivent lundi.", "Last day to take them on! New challenges arrive on Monday.")
                    1 -> tr("Encore 2 jours, aujourd'hui compris. De nouveaux défis arrivent lundi.", "2 days left, including today. New challenges arrive on Monday.")
                    else -> tr("Encore ${week.daysLeft + 1} jours, aujourd'hui compris. De nouveaux défis arrivent lundi.", "${week.daysLeft + 1} days left, including today. New challenges arrive on Monday.")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            for (c in week.list) ChallengeCard(c)
        }

        seasons?.let { SeasonSection(it) }

        SectionHeader(tr("Boutique de récompenses", "Reward shop"))
        Text(
            tr("Échange ton or contre de vrais petits plaisirs, choisis par toi.", "Trade your gold for real little treats, chosen by you."),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        for (reward in state.rewards) {
            RewardRow(
                reward = reward,
                affordable = state.gold >= reward.cost,
                onBuy = { buying = reward },
                onEdit = { editing = reward },
            )
        }
        OutlinedButton(onClick = { creating = true }, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(tr("Ajouter une récompense", "Add a reward"))
        }

        val unlocked = state.badges.count { it.unlocked }
        SectionHeader(tr("Badges ($unlocked sur ${state.badges.size})", "Badges ($unlocked of ${state.badges.size})"))
        for (badge in state.badges) BadgeRow(badge)

        RareBadgesSection(state.rareBadges)

        ListItem(
            headlineContent = { Text(tr("Thème", "Theme")) },
            supportingContent = { Text(theme.label) },
            leadingContent = { Icon(Icons.Filled.DarkMode, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable { choosingTheme = true }.padding(horizontal = 4.dp),
        )
        ListItem(
            headlineContent = { Text(tr("Langue", "Language")) },
            supportingContent = { Text(languageLabel(language)) },
            leadingContent = { Icon(Icons.Filled.Language, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable { choosingLanguage = true }.padding(horizontal = 4.dp),
        )
        ListItem(
            headlineContent = { Text(tr("Sauvegarde", "Backup")) },
            supportingContent = { Text(tr("Exporter ou importer toutes tes données", "Export or import all your data")) },
            leadingContent = { Icon(Icons.Filled.Backup, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onOpenBackup).padding(horizontal = 4.dp),
        )
        ListItem(
            headlineContent = { Text(tr("Crédits et licence", "Credits and license")) },
            supportingContent = { Text(tr("Logiciel libre, GPL-3.0", "Free software, GPL-3.0")) },
            leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onOpenCredits).padding(horizontal = 4.dp),
        )
        Spacer(Modifier.height(16.dp))
    }

    if (choosingTheme) {
        AlertDialog(
            onDismissRequest = { choosingTheme = false },
            title = { Text(tr("Thème", "Theme")) },
            text = {
                Column(Modifier.selectableGroup()) {
                    for (mode in ThemeMode.entries) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(selected = mode == theme, role = Role.RadioButton, onClick = {
                                    vm.setTheme(mode)
                                    choosingTheme = false
                                })
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = mode == theme, onClick = null)
                            Text(mode.label, modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { choosingTheme = false }) { Text(tr("Fermer", "Close")) } },
        )
    }

    if (choosingLanguage) {
        AlertDialog(
            onDismissRequest = { choosingLanguage = false },
            title = { Text(tr("Langue", "Language")) },
            text = {
                Column(Modifier.selectableGroup()) {
                    for (l in AppLanguage.entries) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(selected = l == language, role = Role.RadioButton, onClick = {
                                    choosingLanguage = false
                                    vm.setLanguage(l) { activity?.let { AppLocale.restart(it) } }
                                })
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = l == language, onClick = null)
                            Text(languageLabel(l), modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                    Text(
                        tr("L'appli redémarre pour changer de langue. Tes données ne changent pas.",
                            "The app restarts to change language. Your data stays the same."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            },
            confirmButton = { TextButton(onClick = { choosingLanguage = false }) { Text(tr("Fermer", "Close")) } },
        )
    }

    buying?.let { reward ->
        AlertDialog(
            onDismissRequest = { buying = null },
            title = { Text(tr("Prendre « ${reward.name} » ?", "Take “${reward.name}”?")) },
            text = { Text(tr("Cela coûte ${reward.cost} or. Il t'en restera ${state.gold - reward.cost}.", "It costs ${reward.cost} gold. You will have ${state.gold - reward.cost} left.")) },
            confirmButton = { TextButton(onClick = { vm.buy(reward); buying = null }) { Text(tr("Je me fais plaisir", "Treat myself")) } },
            dismissButton = { TextButton(onClick = { buying = null }) { Text(tr("Plus tard", "Later")) } },
        )
    }
    if (creating || editing != null) {
        RewardDialog(
            initial = editing,
            onDismiss = { creating = false; editing = null },
            onSave = { name, cost -> vm.save(editing, name, cost); creating = false; editing = null },
            onDelete = editing?.let { r -> { vm.delete(r); editing = null } },
        )
    }
}

@Composable
private fun LevelCard(state: GameState) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Niveau ${state.level.level}", "Level ${state.level.level}"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Paid, contentDescription = null, tint = GoldColor)
                Spacer(Modifier.width(6.dp))
                Text(tr("${state.gold} or", "${state.gold} gold"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            LinearProgressIndicator(
                progress = { state.level.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            )
            Text(
                tr("${state.level.xpInLevel} / ${state.level.xpForNext} XP avant le niveau ${state.level.level + 1}", "${state.level.xpInLevel} / ${state.level.xpForNext} XP to level ${state.level.level + 1}"),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                tr("Chaque tâche et chaque habitude rapporte de l'XP et de l'or, selon sa difficulté. Rien n'est jamais perdu.", "Every task and habit earns XP and gold, according to its difficulty. Nothing is ever lost."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun StreakCard(state: GameState, onBuyFreeze: () -> Unit) {
    val s = state.streak
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (s.current > 0) Color(0xFFF4511E) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        when (s.current) {
                            0 -> tr("Pas encore de série", "No streak yet")
                            1 -> tr("1 jour de suite", "1 day in a row")
                            else -> tr("${s.current} jours de suite", "${s.current} days in a row")
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        if (s.activeToday || s.current == 0) tr("Record : ${s.best} jours", "Best: ${s.best} days") else tr("Fais une action aujourd'hui pour la continuer", "Do something today to keep it going"),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        when (s.restDays) {
                            0 -> tr("Aucun jour de repos en réserve : 1 de plus tous les ${Streaks.DAYS_PER_REST_DAY} jours actifs.", "No rest days left: you earn 1 every ${Streaks.DAYS_PER_REST_DAY} active days.")
                            1 -> tr("1 jour de repos en réserve : un jour raté ne cassera pas la série.", "1 rest day saved: a missed day won't break the streak.")
                            else -> tr("${s.restDays} jours de repos en réserve : un jour raté ne cassera pas la série.", "${s.restDays} rest days saved: a missed day won't break the streak.")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider(Modifier.padding(horizontal = 20.dp))
            Row(Modifier.padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AcUnit, contentDescription = null, tint = Color(0xFF039BE5), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        tr("Gels de série : ${s.freezes} sur ${Streaks.MAX_FREEZES}", "Streak freezes: ${s.freezes} of ${Streaks.MAX_FREEZES}"),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        tr("Quand il ne reste plus de jour de repos, un gel protège la série pour un jour manqué. ", "When no rest days are left, a freeze protects the streak for one missed day. ") +
                            tr("Un gel est offert chaque trimestre.", "You get a free freeze every quarter."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val canBuy = s.freezes < Streaks.MAX_FREEZES && state.gold >= Streaks.FREEZE_COST
                FilledTonalButton(onClick = onBuyFreeze, enabled = canBuy, modifier = Modifier.padding(start = 8.dp)) {
                    Text(tr("${Streaks.FREEZE_COST} or", "${Streaks.FREEZE_COST} gold"))
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(c: Challenge) {
    val colors = MaterialTheme.colorScheme
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = (if (c.done) tr("Défi relevé : ", "Challenge completed: ") else tr("Défi : ", "Challenge: ")) + "${c.title}, ${c.goalText}"
            },
        colors = CardDefaults.cardColors(containerColor = if (c.done) colors.secondaryContainer else colors.surfaceContainerLow),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(if (c.done) colors.primary else colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (c.done) Icons.Filled.Check else GameIcons.of(c.icon),
                    contentDescription = null,
                    tint = if (c.done) colors.onPrimary else colors.primary,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(c.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text("+${c.xp} XP", style = MaterialTheme.typography.labelLarge, color = colors.primary)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Filled.Paid, contentDescription = null, tint = GoldColor, modifier = Modifier.size(16.dp))
                    Text("${c.gold}", style = MaterialTheme.typography.labelLarge)
                }
                Text(c.goalText, style = MaterialTheme.typography.bodyMedium)
                Text(
                    c.flavor,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = colors.onSurfaceVariant,
                )
                LinearProgressIndicator(
                    progress = { c.progress },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(6.dp).clip(CircleShape),
                )
                Text(
                    when {
                        c.done -> tr("Relevé ! Bonus versé.", "Done! Bonus paid.")
                        c.hint != null -> c.hint
                        else -> "${formatCount(c.current)} / ${formatCount(c.goal)}"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (c.done) colors.primary else colors.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatCount(n: Long): String = NumberFormat.getIntegerInstance(app.sentier.domain.Lang.locale).format(n)

@Composable
private fun RewardRow(reward: Reward, affordable: Boolean, onBuy: () -> Unit, onEdit: () -> Unit) {
    ListItem(
        headlineContent = { Text(reward.name) },
        supportingContent = { Text(tr("${reward.cost} or", "${reward.cost} gold")) },
        leadingContent = {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(GoldColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) { Icon(GameIcons.of(reward.icon), contentDescription = null, tint = GoldColor) }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = tr("Modifier ${reward.name}", "Edit ${reward.name}")) }
                FilledTonalButton(onClick = onBuy, enabled = affordable) { Text(tr("Prendre", "Take")) }
            }
        },
    )
}

@Composable
private fun BadgeRow(badge: Badge) {
    val color = if (badge.unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    ListItem(
        modifier = Modifier.semantics {
            contentDescription = (if (badge.unlocked) tr("Badge obtenu : ", "Badge earned: ") else tr("Badge à obtenir : ", "Badge to earn: ")) + badge.title
        },
        headlineContent = {
            Text(badge.title, color = if (badge.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
        },
        supportingContent = {
            Text(
                if (badge.unlocked) badge.description else "${badge.description} (${badge.current.coerceAtMost(badge.goal)} / ${badge.goal})",
            )
        },
        leadingContent = {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(if (badge.unlocked) GameIcons.of(badge.icon) else Icons.Filled.Lock, contentDescription = null, tint = color)
            }
        },
    )
}

@Composable
private fun RewardDialog(
    initial: Reward?,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var cost by remember { mutableStateOf(initial?.cost?.toString() ?: "20") }
    val costValue = cost.toIntOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) tr("Nouvelle récompense", "New reward") else tr("Modifier la récompense", "Edit reward")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(tr("Récompense", "Reward")) },
                    placeholder = { Text(tr("Un café en terrasse", "A coffee on a terrace")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                OutlinedTextField(
                    value = cost,
                    onValueChange = { v -> cost = v.filter(Char::isDigit).take(5) },
                    label = { Text(tr("Prix en or", "Price in gold")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text(tr("Supprimer cette récompense", "Delete this reward"), color = MaterialTheme.colorScheme.error) }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, costValue ?: 1) },
                enabled = name.isNotBlank() && costValue != null && costValue > 0,
            ) { Text(tr("Enregistrer", "Save")) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr("Annuler", "Cancel")) } },
    )
}

/** Résumé du jeu en haut de l'écran Aujourd'hui : niveau, or et série. */
@Composable
fun GameSummary(state: GameState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(tr("Niveau ${state.level.level}", "Level ${state.level.level}"), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        LinearProgressIndicator(
            progress = { state.level.progress },
            modifier = Modifier.width(48.dp).height(6.dp).clip(CircleShape),
        )
        Spacer(Modifier.width(4.dp))
        Icon(Icons.Filled.Paid, contentDescription = null, tint = GoldColor, modifier = Modifier.size(18.dp))
        Text("${state.gold}", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.width(4.dp))
        Icon(
            Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = if (state.streak.current > 0) Color(0xFFF4511E) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text("${state.streak.current}", style = MaterialTheme.typography.labelLarge)
    }
}
