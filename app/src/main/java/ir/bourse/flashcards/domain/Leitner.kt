package ir.bourse.flashcards.domain

/**
 * Minimal Leitner-style spaced repetition.
 *
 * A card moves one box up when both of its questions were answered correctly
 * and drops back to box 1 on any mistake. The lower the box, the sooner the
 * card returns for review, so weak symbols surface first.
 */
object Leitner {

    const val MIN_BOX = 1
    const val MAX_BOX = 5

    private const val DAY_MILLIS = 24L * 60L * 60L * 1000L

    /** Review interval per box, in days (box 1 is "again, right away"). */
    private val INTERVAL_DAYS = longArrayOf(0L, 0L, 1L, 3L, 7L, 16L)

    fun nextBox(currentBox: Int, allCorrect: Boolean): Int =
        if (allCorrect) (currentBox + 1).coerceAtMost(MAX_BOX) else MIN_BOX

    fun nextReviewAt(box: Int, now: Long): Long {
        val days = INTERVAL_DAYS[box.coerceIn(MIN_BOX, MAX_BOX)]
        return now + days * DAY_MILLIS
    }
}
