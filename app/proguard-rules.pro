# Keep kotlinx.serialization generated serializers for the JSON import/export DTOs.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class ir.bourse.flashcards.data.json.** {
    *** Companion;
}
-keepclasseswithmembers class ir.bourse.flashcards.data.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
