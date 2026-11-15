package ir.bourse.flashcards.ui.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.bourse.flashcards.data.FlashcardRepository
import ir.bourse.flashcards.data.ImportOutcome
import ir.bourse.flashcards.di.Graph
import ir.bourse.flashcards.domain.Flashcard
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ManageUiState(
    val loading: Boolean = true,
    val query: String = "",
    val cards: List<Flashcard> = emptyList(),
    val totalCount: Int = 0,
    val pendingDelete: Flashcard? = null,
)

/** One-shot results the screen turns into a snackbar / dialog text. */
sealed interface ManageEvent {
    data object CardDeleted : ManageEvent
    data class ImportSucceeded(val count: Int, val skipped: Int) : ManageEvent
    data class ImportFailed(val reason: String) : ManageEvent
    data object ExportSucceeded : ManageEvent
    data object ExportFailed : ManageEvent
}

class ManageViewModel(
    private val repository: FlashcardRepository = Graph.repository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val pendingDelete = MutableStateFlow<Flashcard?>(null)

    private val eventsChannel = Channel<ManageEvent>(Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()

    val state: StateFlow<ManageUiState> =
        combine(repository.observeCards(), query, pendingDelete) { cards, searchText, toDelete ->
            val needle = searchText.trim()
            val visible = if (needle.isEmpty()) {
                cards
            } else {
                cards.filter {
                    it.symbolName.contains(needle, ignoreCase = true) ||
                        it.symbolCode.contains(needle, ignoreCase = true)
                }
            }
            ManageUiState(
                loading = false,
                query = searchText,
                cards = visible,
                totalCount = cards.size,
                pendingDelete = toDelete,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ManageUiState(),
        )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun requestDelete(card: Flashcard) {
        pendingDelete.value = card
    }

    fun cancelDelete() {
        pendingDelete.value = null
    }

    fun confirmDelete() {
        val card = pendingDelete.value
        pendingDelete.value = null
        if (card == null) return
        viewModelScope.launch {
            repository.delete(card.id)
            eventsChannel.send(ManageEvent.CardDeleted)
        }
    }

    /** Serialises the whole deck; the screen writes it to the picked file. */
    suspend fun buildBackup(): String? = try {
        repository.exportJson()
    } catch (error: Exception) {
        eventsChannel.send(ManageEvent.ExportFailed)
        null
    }

    /** Reads a backup file's contents and merges it into the database. */
    fun importBackup(raw: String) {
        viewModelScope.launch {
            when (val outcome = repository.importJson(raw)) {
                is ImportOutcome.Imported ->
                    eventsChannel.send(ManageEvent.ImportSucceeded(outcome.count, outcome.skipped))

                is ImportOutcome.Failed ->
                    eventsChannel.send(ManageEvent.ImportFailed(outcome.reason))
            }
        }
    }

    fun notifyExportSucceeded() {
        viewModelScope.launch { eventsChannel.send(ManageEvent.ExportSucceeded) }
    }

    fun notifyExportFailed() {
        viewModelScope.launch { eventsChannel.send(ManageEvent.ExportFailed) }
    }
}
