# فلش‌کارت بورس — Bourse Flashcards

A fully offline Android flashcard app (Kotlin + Jetpack Compose, Material 3) for memorising
Tehran Stock Exchange symbols together with their buy price, exit price, stop loss and
potential profit — so that seeing a symbol name is enough to recall the numbers.

The entire UI is Persian and right-to-left, with the **Vazirmatn** font bundled in `res/font`.

---

## Features

| Area | What it does |
| --- | --- |
| **Study** | Shows only the symbol name, then asks two multiple-choice questions per card: *buy price* and *exit price* (4 options each, plausible distractors taken from other cards' prices). Tap an answer → correct/incorrect feedback → the card flips to show **all** fields. |
| **Scoring** | Live session counters: correct, wrong, current/best streak, per-symbol accuracy and a daily-goal progress bar. |
| **Manage** | Add, edit, delete, search (by name or code), plus **JSON export / import** through the system file picker (backup & sharing). |
| **Statistics** | Overall accuracy, correct/wrong totals, day goal, a seven-day bar chart and accuracy per symbol. |
| **Spaced repetition** | Leitner boxes 1–5: both questions right → the card moves up a box (review after 0/1/3/7/16 days); any mistake → back to box 1. Due cards come first. |
| **Offline** | Room data only, no `INTERNET` permission, no paid APIs, no analytics. |
| **Theme** | Material 3 light + dark schemes, dynamic-free (fixed brand palette), RTL everywhere. |

---

## File tree

```
.
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml                 # version catalog (AGP 8.5.2, Kotlin 1.9.24, Compose BOM)
│   └── wrapper/…                          # Gradle 8.7 wrapper
├── gradlew, gradlew.bat
├── .gitignore
└── app/
    ├── build.gradle.kts                   # compileSdk 34, minSdk 24, targetSdk 34, Compose + Room/KSP
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/ir/bourse/flashcards/
        │   ├── BourseApp.kt               # Application: graph init + first-run seed
        │   ├── MainActivity.kt            # single activity, forces RTL
        │   ├── di/Graph.kt                # manual dependency container
        │   ├── data/
        │   │   ├── FlashcardRepository.kt # single source of truth (cards, stats, backup, scheduling)
        │   │   ├── SeedData.kt            # 8 sample Iranian symbols
        │   │   ├── json/FlashcardDtos.kt  # kotlinx.serialization wire format
        │   │   └── local/
        │   │       ├── AppDatabase.kt
        │   │       ├── FlashcardEntity.kt
        │   │       ├── FlashcardDao.kt
        │   │       ├── StatsEntities.kt
        │   │       └── StatsDao.kt
        │   ├── domain/
        │   │   ├── Flashcard.kt           # model (+ derived profit %)
        │   │   ├── QuizModels.kt          # question, option, card state, session score
        │   │   ├── QuizFactory.kt         # distractor generation + shuffling
        │   │   ├── Leitner.kt             # spaced repetition
        │   │   └── StatsModels.kt
        │   ├── ui/
        │   │   ├── AppRoot.kt             # NavHost + 3-tab bottom bar
        │   │   ├── components/Common.kt   # flip card, detail rows, chips, empty hint
        │   │   ├── study/{StudyScreen,StudyViewModel}.kt
        │   │   ├── manage/{ManageScreen,ManageViewModel,EditCardScreen,EditCardViewModel}.kt
        │   │   ├── stats/{StatsScreen,StatsViewModel}.kt
        │   │   └── theme/{Color,Type,Theme}.kt
        │   └── util/
        │       ├── PersianFormat.kt       # Persian digits, price parsing/formatting
        │       └── JalaliDate.kt          # Gregorian → Jalali conversion + Persian dates
        └── res/
            ├── drawable/ic_launcher_foreground.xml
            ├── font/vazirmatn_{regular,medium,bold}.ttf
            ├── mipmap-*/ic_launcher*.{png,xml}
            ├── values/{colors,strings,themes}.xml
            ├── values-night/{colors,themes}.xml
            └── xml/{backup_rules,data_extraction_rules,locales_config}.xml
```

Architecture: **MVVM + Repository**. Compose screens observe `StateFlow` from ViewModels
(`collectAsStateWithLifecycle`); ViewModels only talk to `FlashcardRepository`; Room + KSP
persist cards, per-symbol counters and per-day counters.

---

## Build & run

### Android Studio (recommended)

1. Open Android Studio (Hedgehog 2023.1.1 or newer) → **Open** → select this folder.
2. Wait for the Gradle sync (JDK 17 is required: *Settings → Build → Gradle → Gradle JDK*).
3. Run the `app` configuration on an emulator or a device with **API 24+**.

### Command line

```bash
# point Gradle at your SDK once
echo "sdk.dir=$HOME/Android/Sdk" > local.properties

./gradlew assembleDebug      # → app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug       # install on a connected device
```

Verified with Gradle 8.7 + JDK 17: `./gradlew assembleDebug` completes and produces
`app/build/outputs/apk/debug/app-debug.apk`.

---

## Using the app

* **Study** — the first launch seeds eight example symbols. Each card asks the buy price,
  then the exit price; after both answers the card flips and every field is shown. A wrong
  answer sends the card back to box 1, a fully correct card moves one box up.
* **کارت‌ها (Cards)** — search by name/code, tap a card to edit, use the bin icon to delete.
  The two top-bar icons export/import the whole deck as JSON.
* **آمار (Statistics)** — accuracy overall and per symbol, plus the last seven days against
  the daily goal (20 answers per day).

### JSON backup format

Export produces (import also accepts a bare array of card objects):

```json
{
  "format": "bourse-flashcards",
  "version": 1,
  "exportedAt": 1730000000000,
  "cards": [
    {
      "symbolName": "فولاد",
      "symbolCode": "فولاد",
      "purchasePrice": 5200,
      "targetExitPrice": 6800,
      "stopLossPrice": 4800,
      "entryPoints": "۵٬۱۵۰ تا ۵٬۲۵۰",
      "exitPoints": "۶٬۷۵۰ تا ۶٬۹۰۰",
      "notes": "…",
      "lastUpdated": 1730000000000
    }
  ]
}
```

Import merges by symbol name: an existing symbol is updated (its review schedule is kept),
new ones are inserted, malformed entries are skipped and reported.

---

## Design notes

* **Prices are whole Toman**, stored as `Long`, displayed with Persian digits and the Persian
  thousand separator (`۱۲٬۵۰۰ تومان`). Price inputs accept Persian or Latin digits.
* **`potentialProfitPercent` is derived** from `purchasePrice` and `targetExitPrice`
  (`(exit − buy) / buy × 100`) instead of being stored, so the percentage can never
  contradict the two prices on the card.
* **Dates** are converted to the Jalali calendar locally (`util/JalaliDate.kt`, no dependency)
  and rendered like `۲۵ مرداد ۱۴۰۳`.
* **Distractors** are taken from other cards' buy/exit prices; if the deck is too small,
  scaled variants (±5…25 %) of the correct price fill the remaining slots.
* **Seed data is illustrative only** — the prices are example values for practising the app,
  not live quotes and not investment advice. Replace them with your own targets.
