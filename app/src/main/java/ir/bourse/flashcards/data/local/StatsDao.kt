package ir.bourse.flashcards.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {

    @Query("SELECT * FROM symbol_stats ORDER BY symbolName COLLATE NOCASE ASC")
    fun observeSymbolStats(): Flow<List<SymbolStatEntity>>

    @Query("SELECT * FROM symbol_stats WHERE flashcardId = :flashcardId")
    suspend fun findSymbolStat(flashcardId: Long): SymbolStatEntity?

    @Upsert
    suspend fun upsertSymbolStat(stat: SymbolStatEntity)

    @Query("UPDATE symbol_stats SET symbolName = :symbolName WHERE flashcardId = :flashcardId")
    suspend fun renameSymbol(flashcardId: Long, symbolName: String)

    @Query("DELETE FROM symbol_stats WHERE flashcardId = :flashcardId")
    suspend fun deleteSymbolStat(flashcardId: Long)

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT :limit")
    fun observeRecentDays(limit: Int): Flow<List<DailyStatEntity>>

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    fun observeDay(date: String): Flow<DailyStatEntity?>

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun findDay(date: String): DailyStatEntity?

    @Upsert
    suspend fun upsertDay(day: DailyStatEntity)
}
