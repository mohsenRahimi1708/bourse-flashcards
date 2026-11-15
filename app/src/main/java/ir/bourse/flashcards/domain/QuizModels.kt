package ir.bourse.flashcards.domain

/** Which price a quiz question asks about. */
enum class PriceField { PURCHASE, EXIT }

/** One multiple-choice option. Exactly one option per question is correct. */
data class QuizOption(
    val price: Long,
    val isCorrect: Boolean,
)

/** A multiple-choice question about one price of one symbol. */
data class QuizQuestion(
    val field: PriceField,
    val symbolName: String,
    val correctPrice: Long,
    val options: List<QuizOption>,
) {
    val correctIndex: Int get() = options.indexOfFirst { it.isCorrect }
}

/**
 * Everything the study screen needs for the card currently on the table:
 * the two questions plus which option the user has picked for each of them
 * (`null` means "not answered yet").
 */
data class CardState(
    val card: Flashcard,
    val questions: List<QuizQuestion> = emptyList(),
    val answers: List<Int?> = emptyList(),
) {
    val lastQuestionIndex: Int get() = questions.lastIndex

    val allAnswered: Boolean
        get() = questions.isNotEmpty() && answers.all { it != null }

    /** Picked option for a question, or null when it is still unanswered. */
    fun selection(questionIndex: Int): Int? = answers.getOrNull(questionIndex)

    /** True / false for a answered question, null when unanswered. */
    fun isCorrectAt(questionIndex: Int): Boolean? {
        val picked = selection(questionIndex) ?: return null
        return questions.getOrNull(questionIndex)?.options?.getOrNull(picked)?.isCorrect
    }
}

/** Running score of the current study session. */
data class SessionScore(
    val correct: Int = 0,
    val wrong: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
) {
    val total: Int get() = correct + wrong
    val accuracyPercent: Int get() = if (total == 0) 0 else (correct * 100) / total

    fun record(isCorrect: Boolean): SessionScore = if (isCorrect) {
        val newStreak = streak + 1
        copy(correct = correct + 1, streak = newStreak, bestStreak = maxOf(bestStreak, newStreak))
    } else {
        copy(wrong = wrong + 1, streak = 0)
    }
}
