package ir.bourse.flashcards.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ir.bourse.flashcards.domain.Flashcard

/**
 * Persisted card. `symbolName` is unique so importing the same symbol twice
 * updates the existing card instead of creating a duplicate.
 */
@Entity(
    tableName = "flashcards",
    indices = [Index(value = ["symbolName"], unique = true)],
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val symbolName: String,
    val symbolCode: String,
    val purchasePrice: Long,
    val targetExitPrice: Long,
    val stopLossPrice: Long?,
    val entryPoints: String,
    val exitPoints: String,
    val notes: String,
    val lastUpdated: Long,
    val box: Int,
    val nextReviewAt: Long,
)

fun FlashcardEntity.toDomain(): Flashcard = Flashcard(
    id = id,
    symbolName = symbolName,
    symbolCode = symbolCode,
    purchasePrice = purchasePrice,
    targetExitPrice = targetExitPrice,
    stopLossPrice = stopLossPrice,
    entryPoints = entryPoints,
    exitPoints = exitPoints,
    notes = notes,
    lastUpdated = lastUpdated,
    box = box,
    nextReviewAt = nextReviewAt,
)

fun Flashcard.toEntity(): FlashcardEntity = FlashcardEntity(
    id = id,
    symbolName = symbolName,
    symbolCode = symbolCode,
    purchasePrice = purchasePrice,
    targetExitPrice = targetExitPrice,
    stopLossPrice = stopLossPrice,
    entryPoints = entryPoints,
    exitPoints = exitPoints,
    notes = notes,
    lastUpdated = lastUpdated,
    box = box,
    nextReviewAt = nextReviewAt,
)
