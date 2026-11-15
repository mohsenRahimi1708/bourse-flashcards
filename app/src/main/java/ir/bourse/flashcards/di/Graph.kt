package ir.bourse.flashcards.di

import android.content.Context
import ir.bourse.flashcards.data.FlashcardRepository
import ir.bourse.flashcards.data.local.AppDatabase

/**
 * Minimal manual dependency container. One application-wide repository is all
 * this app needs, so a DI framework would be more ceremony than value.
 */
object Graph {

    @Volatile
    private var repositoryInstance: FlashcardRepository? = null

    val repository: FlashcardRepository
        get() = repositoryInstance
            ?: error("Graph.init() must be called from the Application before use")

    fun init(context: Context) {
        if (repositoryInstance != null) return
        synchronized(this) {
            if (repositoryInstance == null) {
                repositoryInstance = FlashcardRepository(AppDatabase.get(context.applicationContext))
            }
        }
    }
}
