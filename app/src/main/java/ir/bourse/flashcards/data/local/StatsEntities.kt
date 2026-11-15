package ir.bourse.flashcards.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.bourse.flashcards.domain.DailyStat
import ir.bourse.flashcards.domain.SymbolStat

/** Answer counters for one symbol, used for per-symbol accuracy. */
@Entity(tableName = "symbol_stats")
data class SymbolStatEntity(
    @PrimaryKey val flashcardId: Long,
    val symbolName: String,
    val correct: Int,
    val wrong: Int,
    val lastAnsweredAt: Long,
)

/** Answer counters for one calendar day, used for the daily goal and chart. */
@Entity(tableName = "daily_stats")
data class DailyStatEntity(
    @PrimaryKey val date: String,
    val correct: Int,
    val wrong: Int,
    val goal: Int,
)

fun SymbolStatEntity.toDomain(): SymbolStat = SymbolStat(
    flashcardId = flashcardId,
    symbolName = symbolName,
    correct = correct,
    wrong = wrong,
    lastAnsweredAt = lastAnsweredAt,
)

fun DailyStatEntity.toDomain(): DailyStat = DailyStat(
    date = date,
    correct = correct,
    wrong = wrong,
    goal = goal,
)
