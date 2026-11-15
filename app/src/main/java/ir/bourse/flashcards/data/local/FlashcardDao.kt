package ir.bourse.flashcards.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {

    @Query("SELECT * FROM flashcards ORDER BY symbolName COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE id = :id")
    fun observeById(id: Long): Flow<FlashcardEntity?>

    @Query("SELECT * FROM flashcards WHERE id = :id")
    suspend fun findById(id: Long): FlashcardEntity?

    @Query("SELECT * FROM flashcards WHERE symbolName = :symbolName LIMIT 1")
    suspend fun findByName(symbolName: String): FlashcardEntity?

    @Query("SELECT * FROM flashcards ORDER BY symbolName COLLATE NOCASE ASC")
    suspend fun getAll(): List<FlashcardEntity>

    @Query("SELECT COUNT(*) FROM flashcards")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<FlashcardEntity>): List<Long>

    @Update
    suspend fun update(card: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Leitner scheduling is written separately so editing a card never resets it. */
    @Query("UPDATE flashcards SET box = :box, nextReviewAt = :nextReviewAt WHERE id = :id")
    suspend fun updateSchedule(id: Long, box: Int, nextReviewAt: Long)
}
