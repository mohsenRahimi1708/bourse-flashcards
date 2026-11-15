package ir.bourse.flashcards.ui.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.bourse.flashcards.R
import ir.bourse.flashcards.util.asPercent
import ir.bourse.flashcards.util.asToman
import ir.bourse.flashcards.util.parseTomanOrNull

/**
 * Full-screen form used both for creating and editing a card. Prices accept
 * Persian or Latin digits.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    cardId: Long,
    onFinished: () -> Unit,
    viewModel: EditCardViewModel = viewModel(),
) {
    LaunchedEffect(cardId) { viewModel.load(cardId) }

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onFinished()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    stringResource(
                        if (state.isNew) R.string.edit_title_new else R.string.edit_title_edit,
                    ),
                )
            },
            navigationIcon = {
                IconButton(onClick = onFinished) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            },
        )

        if (state.loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (state.error == EditError.NOT_FOUND) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.card_not_found))
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.symbolName,
                onValueChange = viewModel::onSymbolNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_symbol_name)) },
                placeholder = { Text(stringResource(R.string.field_symbol_hint)) },
                singleLine = true,
                isError = state.error == EditError.NAME_REQUIRED || state.error == EditError.DUPLICATE_NAME,
                shape = MaterialTheme.shapes.medium,
            )

            OutlinedTextField(
                value = state.symbolCode,
                onValueChange = viewModel::onSymbolCodeChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_symbol_code)) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
            )

            PriceInput(
                value = state.purchasePrice,
                onValueChange = viewModel::onPurchasePriceChange,
                label = stringResource(R.string.field_purchase),
                isError = state.error == EditError.PRICE_REQUIRED,
            )

            PriceInput(
                value = state.targetExitPrice,
                onValueChange = viewModel::onTargetExitPriceChange,
                label = stringResource(R.string.field_exit),
                isError = state.error == EditError.PRICE_REQUIRED,
            )

            state.profitPercent?.let { percent ->
                Text(
                    text = stringResource(
                        R.string.profit_preview,
                        stringResource(R.string.percent_format, percent.asPercent()),
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            PriceInput(
                value = state.stopLossPrice,
                onValueChange = viewModel::onStopLossPriceChange,
                label = stringResource(R.string.field_stop_loss),
                isError = false,
            )

            OutlinedTextField(
                value = state.entryPoints,
                onValueChange = viewModel::onEntryPointsChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_entry_points)) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
            )

            OutlinedTextField(
                value = state.exitPoints,
                onValueChange = viewModel::onExitPointsChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_exit_points)) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
            )

            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_notes)) },
                minLines = 3,
                shape = MaterialTheme.shapes.medium,
            )

            state.error?.let { error ->
                Text(
                    text = stringResource(error.messageRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Surface(
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Button(
                onClick = viewModel::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}

@Composable
private fun PriceInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
) {
    val preview = remember(value) { value.parseTomanOrNull() }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        supportingText = {
            Text(
                text = preview?.let {
                    stringResource(R.string.price_format, it.asToman())
                } ?: stringResource(R.string.field_price_helper),
            )
        },
        shape = MaterialTheme.shapes.medium,
    )
}

private fun EditError.messageRes(): Int = when (this) {
    EditError.NAME_REQUIRED -> R.string.error_name_required
    EditError.PRICE_REQUIRED -> R.string.error_price_required
    EditError.DUPLICATE_NAME -> R.string.error_name_duplicate
    EditError.NOT_FOUND -> R.string.card_not_found
    EditError.SAVE_FAILED -> R.string.error_save_failed
}
