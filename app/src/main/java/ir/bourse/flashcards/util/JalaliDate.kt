package ir.bourse.flashcards.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Gregorian ↔ Jalali (Solar Hijri) conversion, so dates can be shown the way
 * Iranian users expect them without any extra dependency and with no network.
 *
 * The conversion follows the well-known jalaali algorithm.
 */
object JalaliDate {

    private val MONTH_NAMES = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
    )

    private val WEEK_DAY_NAMES = arrayOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه",
    )

    private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /** Stable `yyyy-MM-dd` key used for the daily statistics table. */
    fun todayKey(): String = dayKeyFormat.format(Date())

    fun keyFor(millis: Long): String = dayKeyFormat.format(Date(millis))

    /** Parses a `yyyy-MM-dd` key back into epoch millis (start of that day). */
    fun millisFromKey(key: String): Long? = runCatching { dayKeyFormat.parse(key)?.time }.getOrNull()

    /** Converts a Gregorian date to Jalali: (year, month 1..12, day). */
    fun toJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val daysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
            ((gy2 + 399) / 400) + gd + daysInMonth[gm - 1]

        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }

        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + days / 31
            jd = 1 + days % 31
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + (days - 186) % 30
        }
        return Triple(jy, jm, jd)
    }

    /** e.g. `۲۵ مرداد ۱۴۰۳` */
    fun formatLong(millis: Long): String {
        val (jy, jm, jd) = jalaliFor(millis)
        return "${jd.toString().toPersianDigits()} ${MONTH_NAMES[jm - 1]} ${jy.toString().toPersianDigits()}"
    }

    /** e.g. `۱۴۰۳/۰۵/۲۵` */
    fun formatShort(millis: Long): String {
        val (jy, jm, jd) = jalaliFor(millis)
        return "${jy.toString().toPersianDigits()}/${jm.toString().padStart(2, '0').toPersianDigits()}/" +
            jd.toString().padStart(2, '0').toPersianDigits()
    }

    /** Short weekday + day, used for the seven-day chart, e.g. `شنبه ۲۵`. */
    fun formatDayLabel(millis: Long): String {
        val calendar = Calendar.getInstance(Locale.US).apply { timeInMillis = millis }
        // Calendar.SUNDAY == 1; Persian week starts on Saturday.
        val persianIndex = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> 0
            Calendar.SUNDAY -> 1
            Calendar.MONDAY -> 2
            Calendar.TUESDAY -> 3
            Calendar.WEDNESDAY -> 4
            Calendar.THURSDAY -> 5
            else -> 6
        }
        val (_, _, jd) = jalaliFor(millis)
        return "${WEEK_DAY_NAMES[persianIndex]} ${jd.toString().toPersianDigits()}"
    }

    /** e.g. `مرداد ۱۴۰۳` */
    fun formatMonthYear(millis: Long): String {
        val (jy, jm, _) = jalaliFor(millis)
        return "${MONTH_NAMES[jm - 1]} ${jy.toString().toPersianDigits()}"
    }

    private fun jalaliFor(millis: Long): Triple<Int, Int, Int> {
        val calendar = Calendar.getInstance(Locale.US).apply { timeInMillis = millis }
        return toJalali(
            gy = calendar.get(Calendar.YEAR),
            gm = calendar.get(Calendar.MONTH) + 1,
            gd = calendar.get(Calendar.DAY_OF_MONTH),
        )
    }
}
