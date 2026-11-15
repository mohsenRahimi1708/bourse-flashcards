package ir.bourse.flashcards

import android.app.Application
import ir.bourse.flashcards.di.Graph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BourseApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Graph.init(this)
        // Seed the sample symbols once, on a background thread.
        appScope.launch { Graph.repository.seedIfEmpty() }
    }
}
