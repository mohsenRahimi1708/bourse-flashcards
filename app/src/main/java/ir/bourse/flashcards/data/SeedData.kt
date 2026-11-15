package ir.bourse.flashcards.data

import ir.bourse.flashcards.domain.Flashcard

/**
 * Seed data used on the very first launch so the app is immediately usable.
 *
 * NOTE: these prices are **example values for studying the app**, not live
 * quotes and not investment advice. The user is expected to replace them with
 * their own buy / exit targets.
 */
object SeedData {

    private fun daysAgo(days: Long): Long =
        System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L

    val cards: List<Flashcard> = listOf(
        Flashcard(
            symbolName = "فولاد",
            symbolCode = "فولاد",
            purchasePrice = 5_200,
            targetExitPrice = 6_800,
            stopLossPrice = 4_800,
            entryPoints = "۵٬۱۵۰ تا ۵٬۲۵۰",
            exitPoints = "۶٬۷۵۰ تا ۶٬۹۰۰",
            notes = "محدوده حمایتی ۵٬۱۰۰ مهم است؛ شکست آن حد ضرر را فعال می‌کند.",
            lastUpdated = daysAgo(2),
        ),
        Flashcard(
            symbolName = "خودرو",
            symbolCode = "خودرو",
            purchasePrice = 2_450,
            targetExitPrice = 3_100,
            stopLossPrice = 2_250,
            entryPoints = "۲٬۴۰۰ تا ۲٬۵۰۰",
            exitPoints = "۳٬۰۵۰ تا ۳٬۱۵۰",
            notes = "نوسان بالا؛ حجم معاملات را قبل از ورود بررسی کن.",
            lastUpdated = daysAgo(3),
        ),
        Flashcard(
            symbolName = "شپنا",
            symbolCode = "شپنا",
            purchasePrice = 8_900,
            targetExitPrice = 11_500,
            stopLossPrice = 8_200,
            entryPoints = "۸٬۸۰۰ تا ۸٬۹۵۰",
            exitPoints = "۱۱٬۴۰۰ تا ۱۱٬۶۰۰",
            notes = "گروه پالایشی؛ به گزارش‌های ماهانه حساس است.",
            lastUpdated = daysAgo(1),
        ),
        Flashcard(
            symbolName = "وبملت",
            symbolCode = "وبملت",
            purchasePrice = 3_150,
            targetExitPrice = 4_000,
            stopLossPrice = 2_900,
            entryPoints = "۳٬۱۰۰ تا ۳٬۲۰۰",
            exitPoints = "۳٬۹۵۰ تا ۴٬۰۵۰",
            notes = "سهم بانکی کم‌ریسک‌تر؛ مناسب ورود پله‌ای.",
            lastUpdated = daysAgo(4),
        ),
        Flashcard(
            symbolName = "کگل",
            symbolCode = "کگل",
            purchasePrice = 6_400,
            targetExitPrice = 8_300,
            stopLossPrice = 5_900,
            entryPoints = "۶٬۳۵۰ تا ۶٬۴۵۰",
            exitPoints = "۸٬۲۵۰ تا ۸٬۳۵۰",
            notes = "گروه معدنی؛ همبستگی بالا با فولاد.",
            lastUpdated = daysAgo(5),
        ),
        Flashcard(
            symbolName = "فملی",
            symbolCode = "فملی",
            purchasePrice = 5_750,
            targetExitPrice = 7_400,
            stopLossPrice = 5_300,
            entryPoints = "۵٬۷۰۰ تا ۵٬۸۰۰",
            exitPoints = "۷٬۳۵۰ تا ۷٬۴۵۰",
            notes = "نقدشوندگی خوب؛ مناسب حجم بالا.",
            lastUpdated = daysAgo(6),
        ),
        Flashcard(
            symbolName = "تاپیکو",
            symbolCode = "تاپیکو",
            purchasePrice = 1_800,
            targetExitPrice = 2_350,
            stopLossPrice = 1_650,
            entryPoints = "۱٬۷۸۰ تا ۱٬۸۲۰",
            exitPoints = "۲٬۳۰۰ تا ۲٬۴۰۰",
            notes = "سرمایه‌گذاری غیرمستقیم؛ حرکت کندتر از سهم‌های تکی.",
            lastUpdated = daysAgo(7),
        ),
        Flashcard(
            symbolName = "خساپا",
            symbolCode = "خساپا",
            purchasePrice = 1_550,
            targetExitPrice = 2_000,
            stopLossPrice = 1_400,
            entryPoints = "۱٬۵۳۰ تا ۱٬۵۶۰",
            exitPoints = "۱٬۹۸۰ تا ۲٬۰۲۰",
            notes = "پرنوسان؛ فقط با حد ضرر سفت.",
            lastUpdated = daysAgo(8),
        ),
    )
}
