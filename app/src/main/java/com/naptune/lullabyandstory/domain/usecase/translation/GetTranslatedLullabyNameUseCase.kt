package com.naptune.lullabyandstory.domain.usecase.translation

import android.util.Log
import com.naptune.lullabyandstory.data.repository.LullabyRepositoryImpl
import com.naptune.lullabyandstory.utils.LanguageManager
import javax.inject.Inject

class GetTranslatedLullabyNameUseCase @Inject constructor(
    private val lullabyRepository: LullabyRepositoryImpl,
    private val languageManager: LanguageManager
) {

    suspend operator fun invoke(
        lullabyDocumentId: String,
        fallbackName: String,
        languageCode: String? = null
    ): String {
        return try {
            // Use provided language code or get current language
            val currentLanguage = languageCode ?: languageManager.getLanguage()

            Log.d("GetTranslatedLullabyNameUseCase", "🌍 Getting translated name for lullaby: $lullabyDocumentId in language: $currentLanguage")

            // Get translation for this lullaby
            val translation = lullabyRepository.getTranslationByLullabyDocumentId(lullabyDocumentId)

            if (translation != null) {
                val translatedName = translation.getMusicName(currentLanguage)
                Log.d("GetTranslatedLullabyNameUseCase", "✅ Found translation: '$translatedName' for language: $currentLanguage")
                translatedName
            } else {
                Log.d("GetTranslatedLullabyNameUseCase", "⚠️ No translation found, using fallback: '$fallbackName'")
                fallbackName
            }

        } catch (e: Exception) {
            Log.e("GetTranslatedLullabyNameUseCase", "❌ Error getting translated name: ${e.message}")
            fallbackName
        }
    }

    /**
     * Get translated name for multiple lullabies in batch
     */
    suspend fun getTranslatedNames(
        lullabyDocumentIds: List<String>,
        fallbackNames: Map<String, String>,
        languageCode: String? = null
    ): Map<String, String> {
        return try {
            val currentLanguage = languageCode ?: languageManager.getLanguage()
            Log.d("GetTranslatedLullabyNameUseCase", "🌍 Getting translated names for ${lullabyDocumentIds.size} lullabies in language: $currentLanguage")

            val result = mutableMapOf<String, String>()

            lullabyDocumentIds.forEach { documentId ->
                val fallbackName = fallbackNames[documentId] ?: "Unknown"
                val translatedName = invoke(documentId, fallbackName, currentLanguage)
                result[documentId] = translatedName
            }

            Log.d("GetTranslatedLullabyNameUseCase", "✅ Translated ${result.size} lullaby names")
            result

        } catch (e: Exception) {
            Log.e("GetTranslatedLullabyNameUseCase", "❌ Error getting translated names: ${e.message}")
            fallbackNames
        }
    }
}