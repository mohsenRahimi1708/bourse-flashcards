package ir.bourse.flashcards.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ir.bourse.flashcards.R

/** Vazirmatn is bundled in `res/font`, so the app looks the same on every device. */
val VazirFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

private fun TextStyle.withPersianFont(): TextStyle = copy(fontFamily = VazirFontFamily)

val VazirTypography: Typography = Typography().run {
    Typography(
        displayLarge = displayLarge.withPersianFont(),
        displayMedium = displayMedium.withPersianFont(),
        displaySmall = displaySmall.withPersianFont(),
        headlineLarge = headlineLarge.withPersianFont(),
        headlineMedium = headlineMedium.withPersianFont(),
        headlineSmall = headlineSmall.withPersianFont(),
        titleLarge = titleLarge.withPersianFont(),
        titleMedium = titleMedium.withPersianFont(),
        titleSmall = titleSmall.withPersianFont(),
        bodyLarge = bodyLarge.withPersianFont(),
        bodyMedium = bodyMedium.withPersianFont(),
        bodySmall = bodySmall.withPersianFont(),
        labelLarge = labelLarge.withPersianFont(),
        labelMedium = labelMedium.withPersianFont(),
        labelSmall = labelSmall.withPersianFont(),
    )
}
