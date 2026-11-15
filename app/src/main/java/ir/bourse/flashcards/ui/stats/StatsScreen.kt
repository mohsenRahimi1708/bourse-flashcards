package ir.bourse.flashcards.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.bourse.flashcards.R
import ir.bourse.flashcards.domain.DailyStat
import ir.bourse.flashcards.domain.SymbolStat
import ir.bourse.flashcards.ui.theme.LocalExtraColors
import ir.bourse.flashcards.util.JalaliDate
import ir.bourse.flashcards.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.stats_title)) })

        if (state.loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { SummaryTiles(state) }
            item { DailyGoalCard(state.today) }
            item { WeeklyChart(state.lastSevenDays) }

            item {
                Text(
                    text = stringResource(R.string.stats_per_symbol),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            if (state.perSymbol.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.stats_no_symbols),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(items = state.perSymbol, key = { it.flashcardId }) { stat ->
                SymbolAccuracyRow(stat)
            }

            item {
                Text(
                    text = stringResource(R.string.sample_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SummaryTiles(state: StatsUiState) {
    val extraColors = LocalExtraColors.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(
                label = stringResource(R.string.stats_accuracy),
                value = stringResource(
                    R.string.accuracy_value,
                    state.accuracyPercent.toPersianDigits(),
                ),
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.stats_total_answers),
                value = state.answered.toPersianDigits(),
                accent = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(
                label = stringResource(R.string.stats_correct),
                value = state.correct.toPersianDigits(),
                accent = extraColors.success,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.stats_wrong),
                value = state.wrong.toPersianDigits(),
                accent = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.stats_cards),
                value = state.totalCards.toPersianDigits(),
                accent = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = accent.copy(alpha = 0.10f),
        contentColor = accent,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

@Composable
private fun DailyGoalCard(today: DailyStat) {
    val progress = if (today.goal <= 0) 0f else (today.answered.toFloat() / today.goal).coerceIn(0f, 1f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(
                    R.string.daily_progress,
                    today.answered.toPersianDigits(),
                    today.goal.toPersianDigits(),
                ),
                style = MaterialTheme.typography.titleSmall,
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(MaterialTheme.shapes.small),
            )
        }
    }
}

@Composable
private fun WeeklyChart(days: List<DailyStat>) {
    if (days.isEmpty()) return
    val maxAnswered = days.maxOfOrNull { it.answered }?.coerceAtLeast(1) ?: 1

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.stats_last_days),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            days.forEach { day ->
                val barHeight = (day.answered.toFloat() / maxAnswered * 96f).dp.coerceAtLeast(3.dp)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (day.answered > 0) day.answered.toPersianDigits() else "۰",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight)
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (day.goalReached) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                },
                            ),
                    )
                    Text(
                        text = JalaliDate.millisFromKey(day.date)
                            ?.let { JalaliDate.formatDayLabel(it) }
                            .orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun SymbolAccuracyRow(stat: SymbolStat) {
    val extraColors = LocalExtraColors.current
    val accuracy = stat.accuracyPercent
    val barColor = if (accuracy >= 60) extraColors.success else MaterialTheme.colorScheme.error

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stat.symbolName,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.answered_count, stat.answered.toPersianDigits()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.accuracy_value, accuracy.toPersianDigits()),
                style = MaterialTheme.typography.labelLarge,
                color = barColor,
            )
        }
        LinearProgressIndicator(
            progress = { accuracy / 100f },
            color = barColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(MaterialTheme.shapes.small),
        )
    }
}
