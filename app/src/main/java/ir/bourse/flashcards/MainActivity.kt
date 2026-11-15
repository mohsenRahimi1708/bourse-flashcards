package ir.bourse.flashcards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import ir.bourse.flashcards.ui.AppRoot
import ir.bourse.flashcards.ui.theme.BourseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // The app is Persian-only, so the whole tree is forced right-to-left
            // regardless of the device language.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                BourseTheme {
                    AppRoot()
                }
            }
        }
    }
}
