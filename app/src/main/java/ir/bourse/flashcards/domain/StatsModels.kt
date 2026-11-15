package ir.bourse.flashcards.domain

/** Accuracy of one symbol, aggregated over every answered question. */
data class SymbolStat(
    val flashcardId: Long,
    val symbolName: String,
    val correct: Int,
    val wrong: Int,
    val lastAnsweredAt: Long,
) {
    val answered: Int get() = correct + wrong
    val accuracyPercent: Int get() = if (answered == 0) 0 else (correct * 100) / answered
}

/** Per-day counters used for the daily study goal and the seven-day chart. */
data class DailyStat(
    val date: String,
    val correct: Int = 0,
    val wrong: Int = 0,
    val goal: Int = DEFAULT_DAILY_GOAL,
) {
    val answered: Int get() = correct + wrong
    val goalReached: Boolean get() = answered >= goal

    companion object {
        /** Default daily goal: answers (not cards) per day. */
        const val DEFAULT_DAILY_GOAL = 20
    }
}
