package ir.bourse.flashcards.ui.study

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.bourse.flashcards.R
import ir.bourse.flashcards.domain.CardState
import ir.bourse.flashcards.domain.Flashcard
import ir.bourse.flashcards.domain.PriceField
import ir.bourse.flashcards.domain.QuizQuestion
import ir.bourse.flashcards.domain.SessionScore
import ir.bourse.flashcards.ui.components.DetailRow
import ir.bourse.flashcards.ui.components.EmptyHint
import ir.bourse.flashcards.ui.components.FlipCard
import ir.bourse.flashcards.ui.theme.LocalExtraColors
import ir.bourse.flashcards.util.JalaliDate
import ir.bourse.flashcards.util.asPercent
import ir.bourse.flashcards.util.asToman
import ir.bourse.flashcards.util.toPersianDigits

/**
 * The study tab: one symbol at a time, two multiple-choice questions
 * (buy price, exit price), then the full card details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(viewModel: StudyViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.study_title)) },
            actions = {
                if (state.deckSize > 0) {
                    Text(
                        text = stringResource(
                            R.string.cards_due_today,
                            state.dueCount.toPersianDigits(),
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DailyGoalBar(answered = state.todayAnswered, goal = state.dailyGoal)

            when {
                state.loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                state.deckSize == 0 -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyHint(
                        title = stringResource(R.string.empty_state_title),
                        body = stringResource(R.string.empty_state_body),
                    )
                }

                state.finished -> SessionSummary(
                    state = state,
                    onRestart = { viewModel.startSession(practicedAll = state.practicedAll) },
                    onPracticeAll = { viewModel.startSession(practicedAll = true) },
                )

                else -> {
                    ScoreRow(state.score)
                    CardStage(
                        state = state,
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DailyGoalBar(answered: Int, goal: Int) {
    val progress = if (goal <= 0) 0f else (answered.toFloat() / goal).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(
                R.string.daily_progress,
                answered.toPersianDigits(),
                goal.toPersianDigits(),
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.small),
        )
    }
}

@Composable
private fun ScoreRow(score: SessionScore) {
    val extraColors = LocalExtraColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ScoreTile(
            text = stringResource(R.string.score_correct, score.correct.toPersianDigits()),
            color = extraColors.success,
            modifier = Modifier.weight(1f),
        )
        ScoreTile(
            text = stringResource(R.string.score_wrong, score.wrong.toPersianDigits()),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f),
        )
        ScoreTile(
            text = stringResource(R.string.score_streak, score.bestStreak.toPersianDigits()),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ScoreTile(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.12f),
        contentColor = color,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )
    }
}

@Composable
private fun CardStage(
    state: StudyUiState,
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier,
) {
    val cardState = state.cardState ?: return
    val question = state.currentQuestion ?: return
    val progress = if (state.sessionSize <= 0) 0f else state.position.toFloat() / state.sessionSize

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(
                R.string.session_position,
                (state.position + 1).toPersianDigits(),
                state.sessionSize.toPersianDigits(),
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(MaterialTheme.shapes.small),
        )
        FlipCard(
            flipped = state.revealed,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            front = { CardFront(cardState, question, state, viewModel) },
            back = { CardBack(cardState.card, state, viewModel) },
        )
    }
}

@Composable
private fun CardFront(
    cardState: CardState,
    question: QuizQuestion,
    state: StudyUiState,
    viewModel: StudyViewModel,
) {
    val selected = cardState.selection(state.step)
    val answered = selected != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = cardState.card.symbolName,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        if (cardState.card.symbolCode.isNotBlank() &&
            cardState.card.symbolCode != cardState.card.symbolName
        ) {
            Text(
                text = cardState.card.symbolCode,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = stringResource(
                if (question.field == PriceField.PURCHASE) R.string.question_buy else R.string.question_exit,
            ),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )

        question.options.forEachIndexed { index, option ->
            OptionButton(
                priceText = stringResource(R.string.price_format, option.price.asToman()),
                visualState = when {
                    !answered -> OptionVisualState.IDLE
                    option.isCorrect -> OptionVisualState.CORRECT
                    index == selected -> OptionVisualState.WRONG
                    else -> OptionVisualState.MUTED
                },
                enabled = !answered,
                onClick = { viewModel.selectOption(index) },
            )
        }

        if (answered) {
            AnswerFeedback(
                correct = state.lastAnswerCorrect == true,
                correctPrice = question.correctPrice,
            )
            if (state.step < cardState.lastQuestionIndex) {
                Button(
                    onClick = { viewModel.nextQuestion() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_next_question))
                }
            }
        }
    }
}

private enum class OptionVisualState { IDLE, CORRECT, WRONG, MUTED }

@Composable
private fun OptionButton(
    priceText: String,
    visualState: OptionVisualState,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val extraColors = LocalExtraColors.current

    val containerColor = when (visualState) {
        OptionVisualState.IDLE -> colors.surface
        OptionVisualState.CORRECT -> extraColors.successContainer
        OptionVisualState.WRONG -> extraColors.dangerContainer
        OptionVisualState.MUTED -> colors.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = when (visualState) {
        OptionVisualState.CORRECT -> extraColors.success
        OptionVisualState.WRONG -> extraColors.danger
        else -> colors.onSurface
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (visualState == OptionVisualState.IDLE) {
                colors.outline.copy(alpha = 0.4f)
            } else {
                contentColor.copy(alpha = 0.5f)
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = priceText,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            when (visualState) {
                OptionVisualState.CORRECT -> Icon(Icons.Filled.Check, contentDescription = null)
                OptionVisualState.WRONG -> Icon(Icons.Filled.Close, contentDescription = null)
                else -> Unit
            }
        }
    }
}

@Composable
private fun AnswerFeedback(correct: Boolean, correctPrice: Long?) {
    val extraColors = LocalExtraColors.current
    val accent = if (correct) extraColors.success else extraColors.danger
    val container = if (correct) extraColors.successContainer else extraColors.dangerContainer

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = container,
        contentColor = accent,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(if (correct) R.string.answer_correct else R.string.answer_wrong),
                style = MaterialTheme.typography.titleMedium,
            )
            if (correctPrice != null) {
                Text(
                    text = stringResource(R.string.answer_correct_price, correctPrice.asToman()),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun CardBack(card: Flashcard, state: StudyUiState, viewModel: StudyViewModel) {
    val extraColors = LocalExtraColors.current
    val profit = card.potentialProfitPercent

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AnswerFeedback(
            correct = state.lastAnswerCorrect == true,
            correctPrice = state.currentQuestion?.correctPrice,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = card.symbolName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.percent_format, profit.asPercent()),
                style = MaterialTheme.typography.titleSmall,
                color = if (profit >= 0) extraColors.success else extraColors.danger,
            )
        }

        HorizontalDivider(Modifier.padding(vertical = 4.dp))

        DetailRow(
            label = stringResource(R.string.field_purchase),
            value = stringResource(R.string.price_format, card.purchasePrice.asToman()),
        )
        DetailRow(
            label = stringResource(R.string.field_exit),
            value = stringResource(R.string.price_format, card.targetExitPrice.asToman()),
        )
        DetailRow(
            label = stringResource(R.string.field_stop_loss),
            value = card.stopLossPrice?.let {
                stringResource(R.string.price_format, it.asToman())
            } ?: stringResource(R.string.value_not_set),
            muted = card.stopLossPrice == null,
        )
        if (card.entryPoints.isNotBlank()) {
            DetailRow(stringResource(R.string.field_entry_points), card.entryPoints)
        }
        if (card.exitPoints.isNotBlank()) {
            DetailRow(stringResource(R.string.field_exit_points), card.exitPoints)
        }
        if (card.notes.isNotBlank()) {
            DetailRow(stringResource(R.string.field_notes), card.notes)
        }
        DetailRow(
            label = stringResource(R.string.field_last_updated),
            value = JalaliDate.formatLong(card.lastUpdated),
            muted = true,
        )
        DetailRow(
            label = stringResource(R.string.field_box),
            value = stringResource(
                R.string.box_value,
                card.box.toPersianDigits(),
                ir.bourse.flashcards.domain.Leitner.MAX_BOX.toPersianDigits(),
            ),
            muted = true,
        )

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.nextCard() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.action_next_card))
        }
    }
}

@Composable
private fun SessionSummary(
    state: StudyUiState,
    onRestart: () -> Unit,
    onPracticeAll: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Done,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = stringResource(R.string.session_finished_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(
                R.string.session_finished_body,
                state.score.correct.toPersianDigits(),
                state.score.wrong.toPersianDigits(),
                stringResource(R.string.accuracy_value, state.score.accuracyPercent.toPersianDigits()),
            ),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_restart))
        }
        if (!state.practicedAll) {
            OutlinedButton(onClick = onPracticeAll, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.toggle_all))
            }
        }
    }
}
