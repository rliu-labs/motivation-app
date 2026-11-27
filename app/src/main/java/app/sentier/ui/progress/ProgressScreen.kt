/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.progress

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
import app.sentier.data.GameState
import app.sentier.data.Reward
import app.sentier.data.ThemeMode
import app.sentier.domain.Badge
import app.sentier.domain.Challenge
import app.sentier.domain.Streaks
import app.sentier.ui.common.SectionHeader
import java.text.NumberFormat
import java.util.Locale

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
    var choosingTheme by remember { mutableStateOf(false) }
    var buying by remember { mutableStateOf<Reward?>(null) }
    var editing by remember { mutableStateOf<Reward?>(null) }
    var creating by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text(
            "Progrès",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
        )
        LevelCard(state)
        StreakCard(state, onBuyFreeze = vm::buyFreeze)
        ListItem(
            headlineContent = { Text("Statistiques") },
            supportingContent = { Text("Pas, habitudes et XP sur la semaine, le mois et l'année") },
            leadingContent = { Icon(Icons.Filled.BarChart, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onOpenStats).padding(horizontal = 4.dp),
        )
        ListItem(
            headlineContent = { Text("Bilan de la semaine") },
            supportingContent = { Text("Tes moments forts et ton objectif pour la semaine") },
            leadingContent = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onOpenReview).padding(horizontal = 4.dp),
        )

        challenges?.let { week ->
            SectionHeader("Défis de la semaine (${week.doneCount} sur ${week.list.size})")
            Text(
                when (week.daysLeft) {
                    0 -> "Dernier jour pour les relever ! De nouveaux défis arrivent lundi."
                    1 -> "Encore 2 jours, aujourd'hui compris. De nouveaux défis arrivent lundi."
                    else -> "Encore ${week.daysLeft + 1} jours, aujourd'hui compris. De nouveaux défis arrivent lundi."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            for (c in week.list) ChallengeCard(c)
        }

        seasons?.let { SeasonSection(it) }

        SectionHeader("Boutique de récompenses")
        Text(
            "Échange ton or contre de vrais petits plaisirs, choisis par toi.",
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
            Text("Ajouter une récompense")
        }

        val unlocked = state.badges.count { it.unlocked }
        SectionHeader("Badges ($unlocked sur ${state.badges.size})")
        for (badge in state.badges) BadgeRow(badge)

        RareBadgesSection(state.rareBadges)

        ListItem(
            headlineContent = { Text("Thème") },
            supportingContent = { Text(theme.label) },
            leadingContent = { Icon(Icons.Filled.DarkMode, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable { choosingTheme = true }.padding(horizontal = 4.dp),
        )
        ListItem(
            headlineContent = { Text("Sauvegarde") },
            supportingContent = { Text("Exporter ou importer toutes tes données") },
            leadingContent = { Icon(Icons.Filled.Backup, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onOpenBackup).padding(horizontal = 4.dp),
        )
        ListItem(
            headlineContent = { Text("Crédits et licence") },
            supportingContent = { Text("Logiciel libre, GPL-3.0") },
            leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onOpenCredits).padding(horizontal = 4.dp),
        )
        Spacer(Modifier.height(16.dp))
    }

    if (choosingTheme) {
        AlertDialog(
            onDismissRequest = { choosingTheme = false },
            title = { Text("Thème") },
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
            confirmButton = { TextButton(onClick = { choosingTheme = false }) { Text("Fermer") } },
        )
    }

    buying?.let { reward ->
        AlertDialog(
            onDismissRequest = { buying = null },
            title = { Text("Prendre « ${reward.name} » ?") },
            text = { Text("Cela coûte ${reward.cost} or. Il t'en restera ${state.gold - reward.cost}.") },
            confirmButton = { TextButton(onClick = { vm.buy(reward); buying = null }) { Text("Je me fais plaisir") } },
            dismissButton = { TextButton(onClick = { buying = null }) { Text("Plus tard") } },
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
                Text("Niveau ${state.level.level}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Paid, contentDescription = null, tint = GoldColor)
                Spacer(Modifier.width(6.dp))
                Text("${state.gold} or", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            LinearProgressIndicator(
                progress = { state.level.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            )
            Text(
                "${state.level.xpInLevel} / ${state.level.xpForNext} XP avant le niveau ${state.level.level + 1}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Chaque tâche et chaque habitude rapporte de l'XP et de l'or, selon sa difficulté. Rien n'est jamais perdu.",
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
                            0 -> "Pas encore de série"
                            1 -> "1 jour de suite"
                            else -> "${s.current} jours de suite"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        if (s.activeToday || s.current == 0) "Record : ${s.best} jours" else "Fais une action aujourd'hui pour la continuer",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        when (s.restDays) {
                            0 -> "Aucun jour de repos en réserve : 1 de plus tous les ${Streaks.DAYS_PER_REST_DAY} jours actifs."
                            1 -> "1 jour de repos en réserve : un jour raté ne cassera pas la série."
                            else -> "${s.restDays} jours de repos en réserve : un jour raté ne cassera pas la série."
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
                        "Gels de série : ${s.freezes} sur ${Streaks.MAX_FREEZES}",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        "Quand il ne reste plus de jour de repos, un gel protège la série pour un jour manqué. " +
                            "Un gel est offert chaque trimestre.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val canBuy = s.freezes < Streaks.MAX_FREEZES && state.gold >= Streaks.FREEZE_COST
                FilledTonalButton(onClick = onBuyFreeze, enabled = canBuy, modifier = Modifier.padding(start = 8.dp)) {
                    Text("${Streaks.FREEZE_COST} or")
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
                contentDescription = (if (c.done) "Défi relevé : " else "Défi : ") + "${c.title}, ${c.goalText}"
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
                        c.done -> "Relevé ! Bonus versé."
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

private fun formatCount(n: Long): String = NumberFormat.getIntegerInstance(Locale.FRANCE).format(n)

@Composable
private fun RewardRow(reward: Reward, affordable: Boolean, onBuy: () -> Unit, onEdit: () -> Unit) {
    ListItem(
        headlineContent = { Text(reward.name) },
        supportingContent = { Text("${reward.cost} or") },
        leadingContent = {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(GoldColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) { Icon(GameIcons.of(reward.icon), contentDescription = null, tint = GoldColor) }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Modifier ${reward.name}") }
                FilledTonalButton(onClick = onBuy, enabled = affordable) { Text("Prendre") }
            }
        },
    )
}

@Composable
private fun BadgeRow(badge: Badge) {
    val color = if (badge.unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    ListItem(
        modifier = Modifier.semantics {
            contentDescription = (if (badge.unlocked) "Badge obtenu : " else "Badge à obtenir : ") + badge.title
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
        title = { Text(if (initial == null) "Nouvelle récompense" else "Modifier la récompense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Récompense") },
                    placeholder = { Text("Un café en terrasse") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                OutlinedTextField(
                    value = cost,
                    onValueChange = { v -> cost = v.filter(Char::isDigit).take(5) },
                    label = { Text("Prix en or") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Supprimer cette récompense", color = MaterialTheme.colorScheme.error) }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, costValue ?: 1) },
                enabled = name.isNotBlank() && costValue != null && costValue > 0,
            ) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
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
        Text("Niveau ${state.level.level}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
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
