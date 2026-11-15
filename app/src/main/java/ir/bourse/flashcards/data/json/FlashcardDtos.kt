package ir.bourse.flashcards.data.json

import ir.bourse.flashcards.domain.Flashcard
import kotlinx.serialization.Serializable

/**
 * Wire format of a card. The profit percentage is not stored because it is
 * always derived from the buy and exit prices (see [Flashcard]).
 */
@Serializable
data class FlashcardDto(
    val symbolName: String,
    val symbolCode: String = "",
    val purchasePrice: Long,
    val targetExitPrice: Long,
    val stopLossPrice: Long? = null,
    val entryPoints: String = "",
    val exitPoints: String = "",
    val notes: String = "",
    val lastUpdated: Long = 0L,
)

/** Envelope written by the exporter; import also accepts a bare array. */
@Serializable
data class ExportBundle(
    val format: String = "bourse-flashcards",
    val version: Int = 1,
    val exportedAt: Long = 0L,
    val cards: List<FlashcardDto> = emptyList(),
)

fun Flashcard.toDto(): FlashcardDto = FlashcardDto(
    symbolName = symbolName,
    symbolCode = symbolCode,
    purchasePrice = purchasePrice,
    targetExitPrice = targetExitPrice,
    stopLossPrice = stopLossPrice,
    entryPoints = entryPoints,
    exitPoints = exitPoints,
    notes = notes,
    lastUpdated = lastUpdated,
)

/** Returns null when the DTO is not usable as a card. */
fun FlashcardDto.toDomainOrNull(): Flashcard? {
    val name = symbolName.trim()
    if (name.isEmpty()) return null
    if (purchasePrice <= 0L || targetExitPrice <= 0L) return null
    return Flashcard(
        symbolName = name,
        symbolCode = symbolCode.trim(),
        purchasePrice = purchasePrice,
        targetExitPrice = targetExitPrice,
        stopLossPrice = stopLossPrice?.takeIf { it > 0L },
        entryPoints = entryPoints,
        exitPoints = exitPoints,
        notes = notes,
        lastUpdated = if (lastUpdated > 0L) lastUpdated else System.currentTimeMillis(),
    )
}
