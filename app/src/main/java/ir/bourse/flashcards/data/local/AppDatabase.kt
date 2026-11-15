package ir.bourse.flashcards.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FlashcardEntity::class,
        SymbolStatEntity::class,
        DailyStatEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun flashcardDao(): FlashcardDao

    abstract fun statsDao(): StatsDao

    companion object {
        private const val DATABASE_NAME = "bourse-flashcards.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME,
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
