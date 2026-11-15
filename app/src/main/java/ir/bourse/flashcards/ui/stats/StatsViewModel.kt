package ir.bourse.flashcards.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.bourse.flashcards.data.FlashcardRepository
import ir.bourse.flashcards.di.Graph
import ir.bourse.flashcards.domain.DailyStat
import ir.bourse.flashcards.domain.SymbolStat
import ir.bourse.flashcards.util.JalaliDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val loading: Boolean = true,
    val totalCards: Int = 0,
    val correct: Int = 0,
    val wrong: Int = 0,
    val today: DailyStat = DailyStat(date = ""),
    val perSymbol: List<SymbolStat> = emptyList(),
    /** Oldest → newest, always seven entries. */
    val lastSevenDays: List<DailyStat> = emptyList(),
) {
    val answered: Int get() = correct + wrong
    val accuracyPercent: Int get() = if (answered == 0) 0 else (correct * 100) / answered
}

class StatsViewModel(
    private val repository: FlashcardRepository = Graph.repository,
) : ViewModel() {

    val state: StateFlow<StatsUiState> = combine(
        repository.observeCards(),
        repository.observeSymbolStats(),
        repository.observeRecentDays(PAGE_SIZE),
        repository.observeToday(),
    ) { cards, perSymbol, days, today ->
        val filled = fillSevenDays(days)
        StatsUiState(
            loading = false,
            totalCards = cards.size,
            correct = perSymbol.sumOf { it.correct },
            wrong = perSymbol.sumOf { it.wrong },
            today = today,
            perSymbol = perSymbol.filter { it.answered > 0 },
            lastSevenDays = filled,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )

    /** Expands the sparse rows into a fixed seven-day window ending today. */
    private fun fillSevenDays(rows: List<DailyStat>): List<DailyStat> {
        val byDate = rows.associateBy { it.date }
        val todayMillis = JalaliDate.millisFromKey(JalaliDate.todayKey()) ?: System.currentTimeMillis()
        return (DAYS_RANGE - 1 downTo 0).map { offset ->
            val key = JalaliDate.keyFor(todayMillis - offset * DAY_MILLIS)
            byDate[key] ?: DailyStat(date = key)
        }
    }

    private companion object {
        const val DAYS_RANGE = 7
        const val PAGE_SIZE = 30
        const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}
