package ir.bourse.flashcards.util

import java.util.Locale

private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
private const val PERSIAN_THOUSANDS_SEPARATOR = '٬'

/** Converts a number to Persian digits. */
fun Int.toPersianDigits(): String = toString().toPersianDigits()

/** Converts Latin digits inside a string to Persian digits. */
fun String.toPersianDigits(): String = buildString(length) {
    for (character in this@toPersianDigits) {
        append(if (character in '0'..'9') PERSIAN_DIGITS[character - '0'] else character)
    }
}

/** Converts Persian/Arabic-Indic digits back to Latin digits (for text input). */
fun String.toLatinDigits(): String = buildString(length) {
    for (character in this@toLatinDigits) {
        append(
            when (character) {
                in '۰'..'۹' -> '0' + (character - '۰')
                in '٠'..'٩' -> '0' + (character - '٠')
                // Tolerate separators users may paste in.
                '٬', ',', ' ', '\u200f', '\u200e' -> continue
                else -> character
            }
        )
    }
}

/** Formats a price with Persian thousands separators, e.g. `۱۲٬۵۰۰`. */
fun Long.grouped(): String =
    String.format(Locale.US, "%,d", this)
        .replace(',', PERSIAN_THOUSANDS_SEPARATOR)
        .toPersianDigits()

/** Formats a whole number of Toman for display. */
fun Long.asToman(): String = grouped()

/** Parses a user-typed price (Persian or Latin digits) into a number of Toman. */
fun String.parseTomanOrNull(): Long? {
    val normalized = toLatinDigits().trim()
    if (normalized.isEmpty()) return null
    return normalized.toLongOrNull()?.takeIf { it > 0L }
}

/**
 * Formats a percentage keeping one decimal only when it is not a whole number.
 */
fun Double.asPercent(): String {
    val rounded = Math.round(this * 10.0) / 10.0
    val text = if (rounded % 1.0 == 0.0) {
        rounded.toLong().toString()
    } else {
        String.format(Locale.US, "%.1f", rounded)
    }
    return text.toPersianDigits()
}

/** Formats a percentage as a signed value, e.g. `+۳۰٫۸`. */
fun Double.asSignedPercent(): String {
    val prefix = if (this > 0) "+" else ""
    return prefix + asPercent()
}
