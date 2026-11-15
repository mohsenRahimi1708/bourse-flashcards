package ir.bourse.flashcards.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.bourse.flashcards.data.FlashcardRepository
import ir.bourse.flashcards.di.Graph
import ir.bourse.flashcards.domain.CardState
import ir.bourse.flashcards.domain.DailyStat
import ir.bourse.flashcards.domain.Flashcard
import ir.bourse.flashcards.domain.QuizFactory
import ir.bourse.flashcards.domain.QuizQuestion
import ir.bourse.flashcards.domain.SessionScore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Everything the study screen renders. */
data class StudyUiState(
    val loading: Boolean = true,
    val deckSize: Int = 0,
    val dueCount: Int = 0,
    val cardState: CardState? = null,
    /** Index of the question currently on the front of the card. */
    val step: Int = 0,
    /** True once both questions are answered: the card flips to its details. */
    val revealed: Boolean = false,
    val lastAnswerCorrect: Boolean? = null,
    val score: SessionScore = SessionScore(),
    val position: Int = 0,
    val sessionSize: Int = 0,
    val finished: Boolean = false,
    val practicedAll: Boolean = false,
    val todayAnswered: Int = 0,
    val dailyGoal: Int = DailyStat.DEFAULT_DAILY_GOAL,
) {
    val currentQuestion: QuizQuestion?
        get() = cardState?.questions?.getOrNull(step)

    val isCurrentAnswered: Boolean
        get() = cardState?.selection(step) != null
}

/**
 * Drives the quiz: builds a session from the cards that are due, generates the
 * two multiple-choice questions per card, records answers and advances the
 * Leitner boxes.
 */
class StudyViewModel(
    private val repository: FlashcardRepository = Graph.repository,
) : ViewModel() {

    private val random = Random(System.currentTimeMillis())

    private val _state = MutableStateFlow(StudyUiState())
    val state: StateFlow<StudyUiState> = _state.asStateFlow()

    private var allCards: List<Flashcard> = emptyList()
    private var queue: List<Flashcard> = emptyList()
    private var sessionStarted = false

    init {
        viewModelScope.launch {
            repository.observeCards().collect { cards ->
                allCards = cards
                val due = cards.count { it.isDue() }
                _state.update { it.copy(loading = false, deckSize = cards.size, dueCount = due) }
                if (!sessionStarted && cards.isNotEmpty()) {
                    sessionStarted = true
                    startSession(practicedAll = false)
                }
            }
        }
        viewModelScope.launch {
            repository.observeToday().collect { day ->
                _state.update { it.copy(todayAnswered = day.answered, dailyGoal = day.goal) }
            }
        }
    }

    /**
     * Starts a fresh session. When [practicedAll] is false only the cards that
     * are due come up; if nothing is due the whole deck is used so the user is
     * never stuck on an empty session.
     */
    fun startSession(practicedAll: Boolean = _state.value.practicedAll) {
        val now = System.currentTimeMillis()
        val due = allCards.filter { it.nextReviewAt <= now }.sortedBy { it.box }
        val pool = if (practicedAll || due.isEmpty()) allCards else due

        if (pool.isEmpty()) {
            queue = emptyList()
            _state.update {
                it.copy(
                    cardState = null,
                    finished = true,
                    sessionSize = 0,
                    position = 0,
                    practicedAll = practicedAll,
                    score = SessionScore(),
                )
            }
            return
        }

        queue = pool.shuffled(random)
        _state.update {
            it.copy(
                cardState = null,
                finished = false,
                practicedAll = practicedAll,
                sessionSize = queue.size,
                position = 0,
                step = 0,
                revealed = false,
                lastAnswerCorrect = null,
                score = SessionScore(),
            )
        }
        presentCard(0)
    }

    /** Records the tapped option for the current question. */
    fun selectOption(optionIndex: Int) {
        val snapshot = _state.value
        val cardState = snapshot.cardState ?: return
        val step = snapshot.step
        val question = cardState.questions.getOrNull(step) ?: return
        val option = question.options.getOrNull(optionIndex) ?: return
        if (cardState.selection(step) != null) return // already answered

        val answers = cardState.answers.toMutableList().apply { this[step] = optionIndex }
        val updated = cardState.copy(answers = answers)
        val isLastQuestion = step == cardState.lastQuestionIndex

        _state.update {
            it.copy(
                cardState = updated,
                lastAnswerCorrect = option.isCorrect,
                // Both questions are done: flip the card to its details.
                revealed = updated.allAnswered,
                score = it.score.record(option.isCorrect),
            )
        }

        viewModelScope.launch {
            repository.recordAnswer(cardState.card, option.isCorrect)
            if (isLastQuestion && updated.allAnswered) {
                val allCorrect = updated.questions.indices.all { index ->
                    updated.isCorrectAt(index) == true
                }
                repository.applySchedule(cardState.card.id, allCorrect)
            }
        }
    }

    /** Moves from the first question (buy price) to the second (exit price). */
    fun nextQuestion() {
        val snapshot = _state.value
        val cardState = snapshot.cardState ?: return
        if (cardState.selection(snapshot.step) == null) return
        if (snapshot.step >= cardState.lastQuestionIndex) return
        _state.update {
            it.copy(step = it.step + 1, revealed = false, lastAnswerCorrect = null)
        }
    }

    /** Advances to the next card in the session. */
    fun nextCard() {
        val next = _state.value.position + 1
        if (next >= queue.size) {
            _state.update {
                it.copy(finished = true, cardState = null, revealed = false, lastAnswerCorrect = null)
            }
            return
        }
        presentCard(next)
    }

    private fun presentCard(position: Int) {
        val card = queue.getOrNull(position)
        if (card == null) {
            _state.update { it.copy(finished = true, cardState = null, revealed = false) }
            return
        }
        val questions = QuizFactory.buildQuestions(allCards, card, random)
        _state.update {
            it.copy(
                cardState = CardState(
                    card = card,
                    questions = questions,
                    answers = List(questions.size) { null },
                ),
                step = 0,
                revealed = false,
                lastAnswerCorrect = null,
                position = position,
                finished = false,
            )
        }
    }
}
