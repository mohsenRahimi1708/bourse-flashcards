package ir.bourse.flashcards.data

import ir.bourse.flashcards.data.json.ExportBundle
import ir.bourse.flashcards.data.json.FlashcardDto
import ir.bourse.flashcards.data.json.toDomainOrNull
import ir.bourse.flashcards.data.json.toDto
import ir.bourse.flashcards.data.local.AppDatabase
import ir.bourse.flashcards.data.local.DailyStatEntity
import ir.bourse.flashcards.data.local.SymbolStatEntity
import ir.bourse.flashcards.data.local.toDomain
import ir.bourse.flashcards.data.local.toEntity
import ir.bourse.flashcards.domain.DailyStat
import ir.bourse.flashcards.domain.Flashcard
import ir.bourse.flashcards.domain.Leitner
import ir.bourse.flashcards.domain.SymbolStat
import ir.bourse.flashcards.util.JalaliDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Result of validating and saving a card. */
sealed interface SaveOutcome {
    data class Saved(val id: Long) : SaveOutcome
    data object DuplicateName : SaveOutcome
}

/** Result of a JSON import. */
sealed interface ImportOutcome {
    data class Imported(val count: Int, val skipped: Int) : ImportOutcome
    data class Failed(val reason: String) : ImportOutcome
}

/**
 * Single source of truth for cards and statistics. The UI only ever talks to
 * this class, which keeps Room and the JSON format out of the ViewModels.
 */
class FlashcardRepository(private val database: AppDatabase) {

    private val cardDao = database.flashcardDao()
    private val statsDao = database.statsDao()

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // ---------------------------------------------------------------- cards

    fun observeCards(): Flow<List<Flashcard>> =
        cardDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    fun observeCard(id: Long): Flow<Flashcard?> =
        cardDao.observeById(id).map { it?.toDomain() }

    suspend fun getCard(id: Long): Flashcard? = cardDao.findById(id)?.toDomain()

    suspend fun save(card: Flashcard): SaveOutcome {
        val name = card.symbolName.trim()
        if (name.isEmpty()) return SaveOutcome.DuplicateName

        val existingWithName = cardDao.findByName(name)
        if (existingWithName != null && existingWithName.id != card.id) {
            return SaveOutcome.DuplicateName
        }

        val prepared = card.copy(
            symbolName = name,
            symbolCode = card.symbolCode.trim(),
            lastUpdated = System.currentTimeMillis(),
        ).toEntity()

        val id = if (card.id == 0L) {
            cardDao.insert(prepared)
        } else {
            cardDao.update(prepared)
            // Keep the statistics label in sync when a symbol is renamed.
            statsDao.renameSymbol(card.id, name)
            card.id
        }
        return SaveOutcome.Saved(id)
    }

    suspend fun delete(cardId: Long) {
        cardDao.deleteById(cardId)
        statsDao.deleteSymbolStat(cardId)
    }

    // ----------------------------------------------------------- statistics

    fun observeSymbolStats(): Flow<List<SymbolStat>> =
        statsDao.observeSymbolStats().map { rows -> rows.map { it.toDomain() } }

    fun observeRecentDays(limit: Int = 7): Flow<List<DailyStat>> =
        statsDao.observeRecentDays(limit).map { rows -> rows.map { it.toDomain() } }

    /**
     * Today's counters. The date key is resolved when collection starts, which
     * is enough for a session that lives within one day.
     */
    fun observeToday(): Flow<DailyStat> = flow {
        val key = JalaliDate.todayKey()
        emitAll(
            statsDao.observeDay(key).map { row ->
                row?.toDomain() ?: DailyStat(date = key, goal = DailyStat.DEFAULT_DAILY_GOAL)
            },
        )
    }

    /** Records one answered question for the symbol and for today. */
    suspend fun recordAnswer(card: Flashcard, correct: Boolean) {
        val now = System.currentTimeMillis()

        val existing = statsDao.findSymbolStat(card.id)
        val updatedSymbol = if (existing == null) {
            SymbolStatEntity(
                flashcardId = card.id,
                symbolName = card.symbolName,
                correct = if (correct) 1 else 0,
                wrong = if (correct) 0 else 1,
                lastAnsweredAt = now,
            )
        } else if (correct) {
            existing.copy(correct = existing.correct + 1, lastAnsweredAt = now)
        } else {
            existing.copy(wrong = existing.wrong + 1, lastAnsweredAt = now)
        }
        statsDao.upsertSymbolStat(updatedSymbol)

        val todayKey = JalaliDate.todayKey()
        val today = statsDao.findDay(todayKey)
            ?: DailyStatEntity(todayKey, correct = 0, wrong = 0, goal = DailyStat.DEFAULT_DAILY_GOAL)
        statsDao.upsertDay(
            if (correct) today.copy(correct = today.correct + 1) else today.copy(wrong = today.wrong + 1),
        )
    }

    /** Moves the card through the Leitner boxes once both questions were answered. */
    suspend fun applySchedule(cardId: Long, allCorrect: Boolean) {
        val card = cardDao.findById(cardId) ?: return
        val now = System.currentTimeMillis()
        val box = Leitner.nextBox(card.box, allCorrect)
        cardDao.updateSchedule(cardId, box, Leitner.nextReviewAt(box, now))
    }

    // -------------------------------------------------------------- backups

    suspend fun exportJson(): String {
        val cards = cardDao.getAll().map { it.toDomain() }
        val bundle = ExportBundle(
            exportedAt = System.currentTimeMillis(),
            cards = cards.map { it.toDto() },
        )
        return json.encodeToString(bundle)
    }

    /**
     * Imports cards from a backup. Existing symbols (same name) are updated but
     * keep their review schedule; malformed entries are skipped and counted.
     */
    suspend fun importJson(raw: String): ImportOutcome {
        if (raw.isBlank()) return ImportOutcome.Failed("محتوای فایل خالی است")

        val dtos: List<FlashcardDto> = try {
            runCatching { json.decodeFromString<ExportBundle>(raw).cards }
                .getOrElse { json.decodeFromString<List<FlashcardDto>>(raw) }
        } catch (error: Exception) {
            return ImportOutcome.Failed(error.message ?: "ساختار JSON نامعتبر است")
        }

        var imported = 0
        var skipped = 0
        dtos.forEach { dto ->
            val card = dto.toDomainOrNull()
            if (card == null) {
                skipped++
                return@forEach
            }
            val existing = cardDao.findByName(card.symbolName)
            if (existing == null) {
                cardDao.insert(card.toEntity())
            } else {
                cardDao.update(
                    card.copy(
                        id = existing.id,
                        box = existing.box,
                        nextReviewAt = existing.nextReviewAt,
                        lastUpdated = System.currentTimeMillis(),
                    ).toEntity(),
                )
            }
            imported++
        }
        return ImportOutcome.Imported(imported, skipped)
    }

    // ----------------------------------------------------------------- seed

    /** Inserts the sample cards the first time the app runs. */
    suspend fun seedIfEmpty() {
        if (cardDao.count() > 0) return
        cardDao.insertAll(SeedData.cards.map { it.toEntity() })
    }
}
