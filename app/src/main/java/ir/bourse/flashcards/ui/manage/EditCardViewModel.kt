package ir.bourse.flashcards.ui.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.bourse.flashcards.data.FlashcardRepository
import ir.bourse.flashcards.data.SaveOutcome
import ir.bourse.flashcards.di.Graph
import ir.bourse.flashcards.domain.Flashcard
import ir.bourse.flashcards.util.parseTomanOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Validation problems, resolved to Persian text by the screen. */
enum class EditError { NAME_REQUIRED, PRICE_REQUIRED, DUPLICATE_NAME, NOT_FOUND, SAVE_FAILED }

data class EditUiState(
    val loading: Boolean = true,
    val cardId: Long = 0L,
    val symbolName: String = "",
    val symbolCode: String = "",
    val purchasePrice: String = "",
    val targetExitPrice: String = "",
    val stopLossPrice: String = "",
    val entryPoints: String = "",
    val exitPoints: String = "",
    val notes: String = "",
    val error: EditError? = null,
    val saved: Boolean = false,
) {
    val isNew: Boolean get() = cardId == 0L

    val profitPercent: Double?
        get() {
            val buy = purchasePrice.parseTomanOrNull() ?: return null
            val exit = targetExitPrice.parseTomanOrNull() ?: return null
            return (exit - buy) * 100.0 / buy
        }
}

/** Backs the add / edit form. */
class EditCardViewModel(
    private val repository: FlashcardRepository = Graph.repository,
) : ViewModel() {

    private val _state = MutableStateFlow(EditUiState())
    val state: StateFlow<EditUiState> = _state.asStateFlow()

    /** Snapshot of the stored card, kept so a review schedule survives editing. */
    private var loaded: Flashcard? = null

    /** Guards against reloading the same card after a configuration change. */
    private var requestedId: Long? = null

    fun load(cardId: Long) {
        if (requestedId == cardId) return
        requestedId = cardId

        if (cardId <= 0L) {
            _state.value = EditUiState(loading = false, cardId = 0L)
            return
        }
        viewModelScope.launch {
            val card = repository.getCard(cardId)
            if (card == null) {
                _state.update { it.copy(loading = false, error = EditError.NOT_FOUND) }
            } else {
                loaded = card
                _state.value = EditUiState(
                    loading = false,
                    cardId = card.id,
                    symbolName = card.symbolName,
                    symbolCode = card.symbolCode,
                    purchasePrice = card.purchasePrice.toString(),
                    targetExitPrice = card.targetExitPrice.toString(),
                    stopLossPrice = card.stopLossPrice?.toString() ?: "",
                    entryPoints = card.entryPoints,
                    exitPoints = card.exitPoints,
                    notes = card.notes,
                )
            }
        }
    }

    fun onSymbolNameChange(value: String) = _state.update { it.copy(symbolName = value, error = null) }
    fun onSymbolCodeChange(value: String) = _state.update { it.copy(symbolCode = value) }
    fun onPurchasePriceChange(value: String) = _state.update { it.copy(purchasePrice = value, error = null) }
    fun onTargetExitPriceChange(value: String) = _state.update { it.copy(targetExitPrice = value, error = null) }
    fun onStopLossPriceChange(value: String) = _state.update { it.copy(stopLossPrice = value) }
    fun onEntryPointsChange(value: String) = _state.update { it.copy(entryPoints = value) }
    fun onExitPointsChange(value: String) = _state.update { it.copy(exitPoints = value) }
    fun onNotesChange(value: String) = _state.update { it.copy(notes = value) }

    fun save() {
        val current = _state.value
        val name = current.symbolName.trim()
        if (name.isEmpty()) {
            _state.update { it.copy(error = EditError.NAME_REQUIRED) }
            return
        }
        val purchase = current.purchasePrice.parseTomanOrNull()
        val target = current.targetExitPrice.parseTomanOrNull()
        if (purchase == null || target == null) {
            _state.update { it.copy(error = EditError.PRICE_REQUIRED) }
            return
        }
        val stopLoss = current.stopLossPrice.parseTomanOrNull()

        val card = Flashcard(
            id = current.cardId,
            symbolName = name,
            symbolCode = current.symbolCode,
            purchasePrice = purchase,
            targetExitPrice = target,
            stopLossPrice = stopLoss,
            entryPoints = current.entryPoints.trim(),
            exitPoints = current.exitPoints.trim(),
            notes = current.notes.trim(),
            lastUpdated = System.currentTimeMillis(),
            box = loaded?.box ?: 1,
            nextReviewAt = loaded?.nextReviewAt ?: 0L,
        )

        viewModelScope.launch {
            when (repository.save(card)) {
                is SaveOutcome.Saved -> _state.update { it.copy(saved = true, error = null) }
                SaveOutcome.DuplicateName -> _state.update { it.copy(error = EditError.DUPLICATE_NAME) }
            }
        }
    }
}
