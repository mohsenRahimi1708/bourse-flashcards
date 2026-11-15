package ir.bourse.flashcards.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Colors that Material 3 has no slot for (correct/incorrect feedback). */
data class ExtraColors(
    val success: Color,
    val successContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
)

val LocalExtraColors = staticCompositionLocalOf {
    ExtraColors(
        success = SuccessLight,
        successContainer = SuccessContainerLight,
        danger = DangerLight,
        dangerContainer = DangerContainerLight,
    )
}

private val LightScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
)

private val DarkScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
)

@Composable
fun BourseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val extraColors = if (darkTheme) {
        ExtraColors(
            success = SuccessDark,
            successContainer = SuccessContainerDark,
            danger = DangerDark,
            dangerContainer = DangerContainerDark,
        )
    } else {
        ExtraColors(
            success = SuccessLight,
            successContainer = SuccessContainerLight,
            danger = DangerLight,
            dangerContainer = DangerContainerLight,
        )
    }

    CompositionLocalProvider(LocalExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = VazirTypography,
            content = content,
        )
    }
}
