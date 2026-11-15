package ir.bourse.flashcards.domain

import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.random.Random

/**
 * Builds the multiple-choice questions for a card.
 *
 * Distractors come from the "price universe" of the other cards in the deck
 * (their buy and exit prices), which keeps the options realistic. When the deck
 * is too small to fill four slots, scaled copies of the correct price are used
 * as a fallback.
 */
object QuizFactory {

    /** Four options per question: one correct + three distractors. */
    const val OPTION_COUNT = 4

    fun buildQuestions(all: List<Flashcard>, card: Flashcard, random: Random): List<QuizQuestion> {
        val priceUniverse = all.asSequence()
            .filter { it.id != card.id }
            .flatMap { sequenceOf(it.purchasePrice, it.targetExitPrice) }
            .filter { it > 0L }
            .distinct()
            .toList()

        return listOf(
            PriceField.PURCHASE to card.purchasePrice,
            PriceField.EXIT to card.targetExitPrice,
        ).map { (field, price) ->
            QuizQuestion(
                field = field,
                symbolName = card.symbolName,
                correctPrice = price,
                options = buildOptions(price, priceUniverse, random),
            )
        }
    }

    private fun buildOptions(correct: Long, priceUniverse: List<Long>, random: Random): List<QuizOption> {
        val distractors = LinkedHashSet<Long>()

        priceUniverse.shuffled(random).forEach { candidate ->
            if (distractors.size < OPTION_COUNT - 1 && candidate != correct && candidate > 0L) {
                distractors.add(candidate)
            }
        }

        // Fallback distractors: plausible percentages around the correct price.
        val factors = doubleArrayOf(0.95, 1.05, 0.9, 1.1, 0.85, 1.15, 0.8, 1.2, 0.75, 1.25)
        var attempt = 0
        while (distractors.size < OPTION_COUNT - 1 && attempt < factors.size * 8) {
            // After the first pass, drift a little further out each round.
            val drift = 1.0 + 0.04 * (attempt / factors.size)
            val value = (correct * factors[attempt % factors.size] * drift).roundToLong()
            if (value > 0L && value != correct && abs(value - correct) > 2L) {
                distractors.add(value)
            }
            attempt++
        }

        return (distractors + correct)
            .shuffled(random)
            .map { QuizOption(price = it, isCorrect = it == correct) }
    }
}
