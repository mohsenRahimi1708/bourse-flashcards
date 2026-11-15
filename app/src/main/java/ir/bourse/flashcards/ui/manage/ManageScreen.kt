package ir.bourse.flashcards.ui.manage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.bourse.flashcards.R
import ir.bourse.flashcards.domain.Flashcard
import ir.bourse.flashcards.ui.theme.LocalExtraColors
import ir.bourse.flashcards.util.asPercent
import ir.bourse.flashcards.util.asToman
import ir.bourse.flashcards.util.toPersianDigits
import kotlinx.coroutines.launch

/** Card list with search, editing, deletion and JSON backup / restore. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageScreen(
    onAddCard: () -> Unit,
    onEditCard: (Long) -> Unit,
    viewModel: ManageViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var pendingExport by remember { mutableStateOf<String?>(null) }

    // Export -----------------------------------------------------------------
    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val json = pendingExport
        pendingExport = null
        if (uri == null || json == null) return@rememberLauncherForActivityResult

        val written = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(json.toByteArray(Charsets.UTF_8))
            } ?: error("empty stream")
        }.isSuccess

        if (written) viewModel.notifyExportSucceeded() else viewModel.notifyExportFailed()
    }

    // Import -----------------------------------------------------------------
    val openDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
        viewModel.importBackup(text.orEmpty())
    }

    // One-shot results become snackbars --------------------------------------
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            val message = when (event) {
                ManageEvent.CardDeleted -> context.getString(R.string.card_deleted)
                is ManageEvent.ImportSucceeded -> if (event.skipped > 0) {
                    context.getString(
                        R.string.import_success_with_skipped,
                        event.count.toPersianDigits(),
                        event.skipped.toPersianDigits(),
                    )
                } else {
                    context.getString(R.string.import_success, event.count.toPersianDigits())
                }

                is ManageEvent.ImportFailed -> context.getString(R.string.import_failed, event.reason)
                ManageEvent.ExportSucceeded -> context.getString(R.string.export_success)
                ManageEvent.ExportFailed -> context.getString(R.string.export_failed)
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(stringResource(R.string.manage_title)) },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val json = viewModel.buildBackup() ?: return@launch
                                pendingExport = json
                                createDocument.launch(context.getString(R.string.json_file_name))
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = stringResource(R.string.action_export),
                        )
                    }
                    IconButton(onClick = { openDocument.launch(arrayOf("application/json", "text/*")) }) {
                        Icon(
                            imageVector = Icons.Filled.FileOpen,
                            contentDescription = stringResource(R.string.action_import),
                        )
                    }
                },
            )

            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            )

            Text(
                text = stringResource(R.string.card_count, state.totalCount.toPersianDigits()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (state.cards.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.manage_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp),
                        )
                    }
                }
                items(items = state.cards, key = { it.id }) { card ->
                    FlashcardRow(
                        card = card,
                        onEdit = { onEditCard(card.id) },
                        onDelete = { viewModel.requestDelete(card) },
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddCard,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.action_new_card)) },
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }

    state.pendingDelete?.let { card ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = { Text(stringResource(R.string.delete_dialog_body, card.symbolName)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlashcardRow(
    card: Flashcard,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val extraColors = LocalExtraColors.current
    val profit = card.potentialProfitPercent

    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = card.symbolName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.percent_format, profit.asPercent()),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (profit >= 0) extraColors.success else extraColors.danger,
                    )
                }
                Text(
                    text = stringResource(R.string.buy_exit_compact,
                        card.purchasePrice.asToman(),
                        card.targetExitPrice.asToman(),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Text(
                        text = stringResource(
                            R.string.box_value,
                            card.box.toPersianDigits(),
                            ir.bourse.flashcards.domain.Leitner.MAX_BOX.toPersianDigits(),
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.action_edit))
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
