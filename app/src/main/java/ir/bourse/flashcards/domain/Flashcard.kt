package ir.bourse.flashcards.domain

/**
 * A single study card for one Tehran Stock Exchange symbol.
 *
 * All prices are stored in **Toman** as whole numbers, because the numbers
 * shown on trading screens for these symbols are integers in practice.
 *
 * [potentialProfitPercent] is intentionally *derived* from [purchasePrice] and
 * [targetExitPrice] instead of being stored, so the two numbers on the card can
 * never contradict the profit percentage shown to the user.
 */
data class Flashcard(
    val id: Long = 0L,
    val symbolName: String,
    val symbolCode: String = "",
    val purchasePrice: Long,
    val targetExitPrice: Long,
    val stopLossPrice: Long? = null,
    val entryPoints: String = "",
    val exitPoints: String = "",
    val notes: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    /** Leitner box, 1..5. Higher boxes come back for review later. */
    val box: Int = 1,
    /** Epoch millis at which this card becomes due for review. */
    val nextReviewAt: Long = 0L,
) {
    val potentialProfitPercent: Double
        get() = if (purchasePrice > 0L) {
            (targetExitPrice - purchasePrice) * 100.0 / purchasePrice
        } else {
            0.0
        }

    /** True when the card should be shown in today's review queue. */
    fun isDue(now: Long = System.currentTimeMillis()): Boolean = nextReviewAt <= now
}
