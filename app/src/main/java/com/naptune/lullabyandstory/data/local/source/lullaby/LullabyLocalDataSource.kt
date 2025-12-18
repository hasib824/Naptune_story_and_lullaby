package com.naptune.lullabyandstory.data.local.source.lullaby

import com.naptune.lullabyandstory.data.local.dao.LullabyWithLocalizedName
import com.naptune.lullabyandstory.data.local.dao.LullabyWithTranslation
import com.naptune.lullabyandstory.data.local.dao.StoryWithFullLocalization
import com.naptune.lullabyandstory.data.local.dao.StoryWithLocalizedName
import com.naptune.lullabyandstory.data.local.dao.StoryWithNameTranslation
import com.naptune.lullabyandstory.data.local.entity.LullabyLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryAudioLanguageLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryDescriptionTranslationLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryNameTranslationLocalEntity
import com.naptune.lullabyandstory.data.local.entity.TranslationLocalEntity
import kotlinx.coroutines.flow.Flow

interface LullabyLocalDataSource {


    fun getAllLullabies(): Flow<List<LullabyLocalEntity>>

    suspend fun getLullabyById(documentId: String): LullabyLocalEntity?

    fun searchLullabies(query: String): Flow<List<LullabyLocalEntity>>

    suspend fun insertLullaby(lullaby: LullabyLocalEntity)

    suspend fun insertAllLullabies(lullabies: List<LullabyLocalEntity>): Int

    suspend fun updateLullaby(lullaby: LullabyLocalEntity)

    suspend fun updateLocalPath(musicLocalPath: String, documentId: String)

    suspend fun markAsDownloaded(documentId: String): Int

    suspend fun deleteLullaby(lullaby: LullabyLocalEntity)

    suspend fun deleteAllLullabies(): Int

    suspend fun getLullabyCount(): Int

    suspend fun getLullabiesPaginated(limit: Int, offset: Int): List<LullabyLocalEntity>


    // =====================================================
    // FAVOURITE OPERATIONS
    // =====================================================

    suspend fun toggleLullabyFavourite(lullabyId: String): Int

    fun isLullabyFavourite(lullabyId: String): Flow<Boolean>

    fun getFavouriteLullabies(): Flow<List<LullabyLocalEntity>>
    // ✅ NEW: Language-aware favourite lullabies

    fun getFavouriteLullabiesWithLocalizedNames(languageCode: String): Flow<List<LullabyWithLocalizedName>>



    // ✅ NEW: Language-aware favourite stories


    // =====================================================
    // TRANSLATION OPERATIONS
    // =====================================================

    fun getAllTranslations(): Flow<List<TranslationLocalEntity>>

    suspend fun getTranslationByLullabyDocumentId(lullabyDocumentId: String): TranslationLocalEntity?

    suspend fun getTranslationByLullabyId(lullabyId: String): TranslationLocalEntity?

    suspend fun insertTranslation(translation: TranslationLocalEntity): Int

    suspend fun insertAllTranslations(translations: List<TranslationLocalEntity>): Int

    suspend fun updateTranslation(translation: TranslationLocalEntity): Int

    suspend fun deleteTranslation(translation: TranslationLocalEntity): Int

    suspend fun deleteAllTranslations(): Int

    suspend fun getTranslationCount(): Int

    // =====================================================
    // JOIN OPERATIONS (Lullaby + Translation)
    // =====================================================

    suspend fun getLullabyWithTranslation(documentId: String): LullabyWithTranslation?

    // ✅ OPTIMIZED: Single method for language-specific lullabies
    fun getAllLullabiesWithLocalizedNames(languageCode: String): Flow<List<LullabyWithLocalizedName>>



    // =====================================================
    // FAVOURITE METADATA OPERATIONS (LIFO ordering support)
    // =====================================================

    /**
     * Insert favourite metadata when user favourites an item
     * This enables LIFO ordering for favourites
     */
    suspend fun insertFavouriteMetadata(itemId: String, itemType: String): Int

    /**
     * Delete favourite metadata when user unfavourites an item
     */
    suspend fun deleteFavouriteMetadata(itemId: String, itemType: String): Int

    /**
     * Check if favourite metadata exists for an item
     */
    suspend fun hasFavouriteMetadata(itemId: String, itemType: String): Boolean

    /**
     * Get favourite count
     */
    suspend fun getFavouriteCount(): Int
}