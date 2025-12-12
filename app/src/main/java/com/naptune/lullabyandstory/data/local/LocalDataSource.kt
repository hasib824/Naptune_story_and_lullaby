package com.naptune.lullabyandstory.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.naptune.lullabyandstory.data.local.dao.FavouriteMetadataDao
import com.naptune.lullabyandstory.data.local.dao.LullabyDao
import com.naptune.lullabyandstory.data.local.dao.StoryDao
import com.naptune.lullabyandstory.data.local.dao.StoryAudioLanguageDao
import com.naptune.lullabyandstory.data.local.dao.StoryDescriptionTranslationDao
import com.naptune.lullabyandstory.data.local.dao.StoryNameTranslationDao
import com.naptune.lullabyandstory.data.local.dao.TranslationDao
import com.naptune.lullabyandstory.data.local.dao.LullabyWithTranslation
import com.naptune.lullabyandstory.data.local.entity.FavouriteMetadataEntity
import com.naptune.lullabyandstory.data.local.entity.LullabyLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryAudioLanguageLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryDescriptionTranslationLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryNameTranslationLocalEntity
import com.naptune.lullabyandstory.data.local.entity.TranslationLocalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class LocalDataSource @Inject constructor(
    private val lullabyDao: LullabyDao,
    private val storyDao: StoryDao,
    private val storyAudioLanguageDao: StoryAudioLanguageDao,
    private val storyDescriptionTranslationDao: StoryDescriptionTranslationDao,
    private val storyNameTranslationDao: StoryNameTranslationDao,
    private val translationDao: TranslationDao,
    private val favouriteMetadataDao: FavouriteMetadataDao
) {

    // =====================================================
    // LULLABY OPERATIONS
    // =====================================================

    fun getAllLullabies(): Flow<List<LullabyLocalEntity>> = lullabyDao.getAllLullabies()

    suspend fun getLullabyById(documentId: String): LullabyLocalEntity? = 
        withContext(Dispatchers.IO) {
            lullabyDao.getLullabyById(documentId)
        }

    fun searchLullabies(query: String): Flow<List<LullabyLocalEntity>> =
        lullabyDao.searchLullabies(query)

    suspend fun insertLullaby(lullaby: LullabyLocalEntity) = 
        withContext(Dispatchers.IO) {
            lullabyDao.insertLullaby(lullaby)
        }

    suspend fun insertAllLullabies(lullabies: List<LullabyLocalEntity>) = 
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "💾 Inserting ${lullabies.size} lullabies...")
                lullabyDao.insertAllLullabies(lullabies)
                Log.d("LocalDataSource", "✅ Lullabies inserted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error inserting lullabies: ${e.message}")
                throw e
            }
        }

    suspend fun updateLullaby(lullaby: LullabyLocalEntity) = 
        withContext(Dispatchers.IO) {
            lullabyDao.updateLullaby(lullaby)
        }

    suspend fun updateLocalPath(musicLocalPath: String, documentId: String) = 
        withContext(Dispatchers.IO) {
            lullabyDao.updateLocalPath(musicLocalPath, documentId)
        }

    suspend fun markAsDownloaded(documentId: String): Int = 
        withContext(Dispatchers.IO) {
            Log.d("LocalDataSource", "💾 Marking lullaby as downloaded: $documentId")
            lullabyDao.updateIsDownloaded(documentId, true)
        }

    suspend fun deleteLullaby(lullaby: LullabyLocalEntity) = 
        withContext(Dispatchers.IO) {
            lullabyDao.deleteLullaby(lullaby)
        }

    suspend fun deleteAllLullabies() = 
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🗑️ Deleting all lullabies...")
                lullabyDao.deleteAllLullabies()
                Log.d("LocalDataSource", "✅ All lullabies deleted")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error deleting lullabies: ${e.message}")
                throw e
            }
        }

    suspend fun getLullabyCount(): Int = 
        withContext(Dispatchers.IO) {
            try {
                val count = lullabyDao.getLullabyCount()
                Log.d("LocalDataSource", "📊 Lullaby count: $count")
                count
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error getting lullaby count: ${e.message}")
                0
            }
        }

    suspend fun getLullabiesPaginated(limit: Int, offset: Int): List<LullabyLocalEntity> = 
        withContext(Dispatchers.IO) {
            lullabyDao.getLullabiesPaginated(limit, offset)
        }

    // =====================================================
    // STORY OPERATIONS  
    // =====================================================

    suspend fun insertAllStories(stories: List<StoryLocalEntity>) = 
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "💾 Inserting ${stories.size} stories...")
                storyDao.insertAllStories(stories)
                Log.d("LocalDataSource", "✅ Stories inserted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error inserting stories: ${e.message}")
                throw e
            }
        }

    fun getAllStories(): Flow<List<StoryLocalEntity>> {
        Log.d("LocalDataSource", "📖 Getting all stories from database...")
        return storyDao.getAllStories()
    }

    suspend fun getStoriesCount(): Int = 
        withContext(Dispatchers.IO) {
            try {
                val count = storyDao.getStoriesCount()
                Log.d("LocalDataSource", "📊 Story count: $count")
                count
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error getting story count: ${e.message}")
                0
            }
        }

    suspend fun toggleStoryFavourite(documentId: String) = 
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "❤️ Toggling story favourite: $documentId")
                storyDao.toggleStoryFavourite(documentId)
                Log.d("LocalDataSource", "✅ Story favourite toggled successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error toggling story favourite: ${e.message}")
                throw e
            }
        }

    fun checkIfItemIsFavourite(documentId: String): Flow<Boolean> {
        Log.d("LocalDataSource", "🔍 Checking if story is favourite: $documentId")
        return storyDao.checkIfItemIsFavourite(documentId)
    }

    suspend fun deleteAllStories() = 
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🗑️ Deleting all stories...")
                storyDao.deleteAllStories()
                Log.d("LocalDataSource", "✅ All stories deleted")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error deleting stories: ${e.message}")
                throw e
            }
        }
    
    // =====================================================
    // FAVOURITE OPERATIONS
    // =====================================================
    
    suspend fun toggleLullabyFavourite(lullabyId: String) = 
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "❤️ Toggling lullaby favourite: $lullabyId")
                lullabyDao.toggleLullabyFavourite(lullabyId)
                Log.d("LocalDataSource", "✅ Lullaby favourite toggled successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error toggling lullaby favourite: ${e.message}")
                throw e
            }
        }
    
    fun isLullabyFavourite(lullabyId: String): Flow<Boolean> {
        Log.d("LocalDataSource", "🔍 Checking if lullaby is favourite: $lullabyId")
        return lullabyDao.isLullabyFavourite(lullabyId)
    }
    
    fun getFavouriteLullabies(): Flow<List<LullabyLocalEntity>> {
        Log.d("LocalDataSource", "❤️ Getting favourite lullabies")
        return lullabyDao.getFavouriteLullabies()
    }

    // ✅ NEW: Language-aware favourite lullabies
    fun getFavouriteLullabiesWithLocalizedNames(languageCode: String): Flow<List<com.naptune.lullabyandstory.data.local.dao.LullabyWithLocalizedName>> {
        Log.d("LocalDataSource", "❤️ Getting favourite lullabies with localized names for language: $languageCode")
        return translationDao.getFavouriteLullabiesWithTranslations(languageCode)
    }
    
    fun getFavouriteStories(): Flow<List<StoryLocalEntity>> {
        Log.d("LocalDataSource", "❤️ Getting favourite stories")
        return storyDao.getFavouriteStories()
    }

    // ✅ NEW: Language-aware favourite stories
    fun getFavouriteStoriesWithLocalizedNames(languageCode: String): Flow<List<com.naptune.lullabyandstory.data.local.dao.StoryWithLocalizedName>> {
        Log.d("LocalDataSource", "❤️ Getting favourite stories with localized names for language: $languageCode")
        return storyNameTranslationDao.getFavouriteStoriesWithNameTranslations(languageCode)
    }

    // =====================================================
    // TRANSLATION OPERATIONS
    // =====================================================

    fun getAllTranslations(): Flow<List<TranslationLocalEntity>> {
        Log.d("LocalDataSource", "🌍 Getting all translations")
        return translationDao.getAllTranslations()
    }

    suspend fun getTranslationByLullabyDocumentId(lullabyDocumentId: String): TranslationLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🔍 Getting translation for lullaby document ID: $lullabyDocumentId")
                translationDao.getTranslationByLullabyDocumentId(lullabyDocumentId)
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error getting translation: ${e.message}")
                null
            }
        }

    suspend fun getTranslationByLullabyId(lullabyId: String): TranslationLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🔍 Getting translation for lullaby ID: $lullabyId")
                translationDao.getTranslationByLullabyId(lullabyId)
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error getting translation by lullaby ID: ${e.message}")
                null
            }
        }

    suspend fun insertTranslation(translation: TranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "💾 Inserting translation: ${translation.lullabyId}")
                translationDao.insertTranslation(translation)
                Log.d("LocalDataSource", "✅ Translation inserted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error inserting translation: ${e.message}")
                throw e
            }
        }

    suspend fun insertAllTranslations(translations: List<TranslationLocalEntity>) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "💾 Inserting ${translations.size} translations...")
                translationDao.insertAllTranslations(translations)
                Log.d("LocalDataSource", "✅ Translations inserted successfully")
                Log.d("LocalDataSource", "🔗 Translation relationships: ${translations.map { "${it.lullabyId}->${it.lullabyDocumentId}" }}")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error inserting translations: ${e.message}")
                throw e
            }
        }

    suspend fun updateTranslation(translation: TranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🔄 Updating translation: ${translation.lullabyId}")
                translationDao.updateTranslation(translation)
                Log.d("LocalDataSource", "✅ Translation updated successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error updating translation: ${e.message}")
                throw e
            }
        }

    suspend fun deleteTranslation(translation: TranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🗑️ Deleting translation: ${translation.lullabyId}")
                translationDao.deleteTranslation(translation)
                Log.d("LocalDataSource", "✅ Translation deleted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error deleting translation: ${e.message}")
                throw e
            }
        }

    suspend fun deleteAllTranslations() =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🗑️ Deleting all translations...")
                translationDao.deleteAllTranslations()
                Log.d("LocalDataSource", "✅ All translations deleted")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error deleting all translations: ${e.message}")
                throw e
            }
        }

    suspend fun getTranslationCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                val count = translationDao.getTranslationCount()
                Log.d("LocalDataSource", "📊 Translation count: $count")
                count
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error getting translation count: ${e.message}")
                0
            }
        }

    // =====================================================
    // JOIN OPERATIONS (Lullaby + Translation)
    // =====================================================

    suspend fun getLullabyWithTranslation(documentId: String): LullabyWithTranslation? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🔗 Getting lullaby with translation for ID: $documentId")
                translationDao.getLullabyWithTranslation(documentId)
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error getting lullaby with translation: ${e.message}")
                null
            }
        }

    // ✅ OPTIMIZED: Single method for language-specific lullabies
    fun getAllLullabiesWithLocalizedNames(languageCode: String): Flow<List<com.naptune.lullabyandstory.data.local.dao.LullabyWithLocalizedName>> {
        Log.d("LocalDataSource", "🚀 Getting all lullabies with localized names for language: $languageCode")
        return translationDao.getAllLullabiesWithTranslations(languageCode)
    }

    // =====================================================
    // STORY NAME TRANSLATION OPERATIONS
    // =====================================================

    fun getAllStoryNameTranslations(): Flow<List<StoryNameTranslationLocalEntity>> {
        Log.d("LocalDataSource", "🌍 Getting all story name translations")
        return storyNameTranslationDao.getAllStoryNameTranslations()
    }

    suspend fun getStoryNameTranslationByDocumentId(storyDocumentId: String): StoryNameTranslationLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🔍 Getting story name translation for document ID: $storyDocumentId")
                storyNameTranslationDao.getStoryNameTranslationByDocumentId(storyDocumentId)
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error getting story name translation: ${e.message}")
                null
            }
        }

    suspend fun getStoryNameTranslationById(storyId: String): StoryNameTranslationLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🔍 Getting story name translation for story ID: $storyId")
                storyNameTranslationDao.getStoryNameTranslationById(storyId)
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error getting story name translation by story ID: ${e.message}")
                null
            }
        }

    suspend fun insertStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "💾 Inserting story name translation: ${storyNameTranslation.storyId}")
                storyNameTranslationDao.insertStoryNameTranslation(storyNameTranslation)
                Log.d("LocalDataSource", "✅ Story name translation inserted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error inserting story name translation: ${e.message}")
                throw e
            }
        }

    suspend fun insertAllStoryNameTranslations(storyNameTranslations: List<StoryNameTranslationLocalEntity>) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "💾 Inserting ${storyNameTranslations.size} story name translations...")
                storyNameTranslationDao.insertAllStoryNameTranslations(storyNameTranslations)
                Log.d("LocalDataSource", "✅ Story name translations inserted successfully")
                Log.d("LocalDataSource", "🔗 Story name translation relationships: ${storyNameTranslations.map { "${it.storyId}->${it.storyDocumentId}" }}")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error inserting story name translations: ${e.message}")
                throw e
            }
        }

    suspend fun updateStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🔄 Updating story name translation: ${storyNameTranslation.storyId}")
                storyNameTranslationDao.updateStoryNameTranslation(storyNameTranslation)
                Log.d("LocalDataSource", "✅ Story name translation updated successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error updating story name translation: ${e.message}")
                throw e
            }
        }

    suspend fun deleteStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🗑️ Deleting story name translation: ${storyNameTranslation.storyId}")
                storyNameTranslationDao.deleteStoryNameTranslation(storyNameTranslation)
                Log.d("LocalDataSource", "✅ Story name translation deleted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error deleting story name translation: ${e.message}")
                throw e
            }
        }

    suspend fun deleteAllStoryNameTranslations() =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🗑️ Deleting all story name translations...")
                storyNameTranslationDao.deleteAllStoryNameTranslations()
                Log.d("LocalDataSource", "✅ All story name translations deleted")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error deleting all story name translations: ${e.message}")
                throw e
            }
        }

    suspend fun getStoryNameTranslationCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                val count = storyNameTranslationDao.getStoryNameTranslationCount()
                Log.d("LocalDataSource", "📊 Story name translation count: $count")
                count
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error getting story name translation count: ${e.message}")
                0
            }
        }

    // ✅ OPTIMIZED: Single method for language-specific stories
    fun getAllStoriesWithLocalizedNames(languageCode: String): Flow<List<com.naptune.lullabyandstory.data.local.dao.StoryWithLocalizedName>> {
        Log.d("LocalDataSource", "🚀 Getting all stories with localized names for language: $languageCode")
        return storyNameTranslationDao.getAllStoriesWithNameTranslations(languageCode)
    }

    // 🚀 ULTRA OPTIMIZED: Single method for FULL story localization (Name + Description)
    fun getAllStoriesWithFullLocalization(languageCode: String): Flow<List<com.naptune.lullabyandstory.data.local.dao.StoryWithFullLocalization>> {
        Log.d("LocalDataSource", "🚀 ULTRA OPTIMIZED: Getting all stories with FULL localization for language: $languageCode")
        return storyNameTranslationDao.getAllStoriesWithFullLocalization(languageCode)
    }

    // 🚀 ULTRA OPTIMIZED: Single method for FULL favourite story localization (Name + Description)
    fun getFavouriteStoriesWithFullLocalization(languageCode: String): Flow<List<com.naptune.lullabyandstory.data.local.dao.StoryWithFullLocalization>> {
        Log.d("LocalDataSource", "❤️ ULTRA OPTIMIZED: Getting favourite stories with FULL localization for language: $languageCode")
        return storyNameTranslationDao.getFavouriteStoriesWithFullLocalization(languageCode)
    }

    // =====================================================
    // STORY DESCRIPTION TRANSLATION OPERATIONS
    // =====================================================

    suspend fun insertAllStoryDescriptionTranslations(storyDescriptionTranslations: List<StoryDescriptionTranslationLocalEntity>) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "💾 Inserting ${storyDescriptionTranslations.size} story description translations...")
                storyDescriptionTranslationDao.insertAllStoryDescriptionTranslations(storyDescriptionTranslations)
                Log.d("LocalDataSource", "✅ Story description translations inserted successfully")
                Log.d("LocalDataSource", "🔗 Story description translation relationships: ${storyDescriptionTranslations.map { "${it.storyId}->${it.storyDocumentId}" }}")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error inserting story description translations: ${e.message}")
                throw e
            }
        }

    suspend fun deleteAllStoryDescriptionTranslations() =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🗑️ Deleting all story description translations...")
                storyDescriptionTranslationDao.deleteAllStoryDescriptionTranslations()
                Log.d("LocalDataSource", "✅ All story description translations deleted")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error deleting all story description translations: ${e.message}")
                throw e
            }
        }

    // =====================================================
    // STORY AUDIO LANGUAGE OPERATIONS
    // =====================================================

    suspend fun insertAllStoryAudioLanguages(storyAudioLanguages: List<StoryAudioLanguageLocalEntity>) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🎵 Inserting ${storyAudioLanguages.size} story audio languages...")
                storyAudioLanguageDao.insertAllStoryAudioLanguages(storyAudioLanguages)
                Log.d("LocalDataSource", "✅ Story audio languages inserted successfully")
                Log.d("LocalDataSource", "🔗 Story audio language relationships: ${storyAudioLanguages.map { "${it.storyDocumentId}" }}")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error inserting story audio languages: ${e.message}")
                throw e
            }
        }

    suspend fun getStoryAudioLanguageByStoryDocumentId(storyDocumentId: String): StoryAudioLanguageLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🔍 Getting story audio language for story document ID: $storyDocumentId")
                storyAudioLanguageDao.getStoryAudioLanguageByStoryDocumentId(storyDocumentId)
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error getting story audio language: ${e.message}")
                null
            }
        }

    suspend fun getStoryAudioLanguageCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                storyAudioLanguageDao.getStoryAudioLanguageCount()
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error getting story audio language count: ${e.message}")
                0
            }
        }

    suspend fun deleteAllStoryAudioLanguages() =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🗑️ Deleting all story audio languages...")
                storyAudioLanguageDao.deleteAllStoryAudioLanguages()
                Log.d("LocalDataSource", "✅ All story audio languages deleted")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error deleting all story audio languages: ${e.message}")
                throw e
            }
        }

    // =====================================================
    // JOIN OPERATIONS (Story + StoryNameTranslation)
    // =====================================================

    suspend fun getStoryWithNameTranslation(documentId: String): com.naptune.lullabyandstory.data.local.dao.StoryWithNameTranslation? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "🔗 Getting story with name translation for ID: $documentId")
                storyNameTranslationDao.getStoryWithNameTranslation(documentId)
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error getting story with name translation: ${e.message}")
                null
            }
        }

    // =====================================================
    // FAVOURITE METADATA OPERATIONS (LIFO ordering support)
    // =====================================================

    /**
     * Insert favourite metadata when user favourites an item
     * This enables LIFO ordering for favourites
     */
    suspend fun insertFavouriteMetadata(itemId: String, itemType: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "❤️ Inserting favourite metadata: $itemId ($itemType)")
                val metadata = FavouriteMetadataEntity(
                    itemId = itemId,
                    itemType = itemType,
                    favouritedAt = System.currentTimeMillis()
                )
                favouriteMetadataDao.insertFavouriteMetadata(metadata)
                Log.d("LocalDataSource", "✅ Favourite metadata inserted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error inserting favourite metadata: ${e.message}")
                throw e
            }
        }

    /**
     * Delete favourite metadata when user unfavourites an item
     */
    suspend fun deleteFavouriteMetadata(itemId: String, itemType: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSource", "💔 Deleting favourite metadata: $itemId ($itemType)")
                favouriteMetadataDao.deleteFavouriteMetadata(itemId, itemType)
                Log.d("LocalDataSource", "✅ Favourite metadata deleted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error deleting favourite metadata: ${e.message}")
                throw e
            }
        }

    /**
     * Check if favourite metadata exists for an item
     */
    suspend fun hasFavouriteMetadata(itemId: String, itemType: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                favouriteMetadataDao.hasFavouriteMetadata(itemId, itemType)
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error checking favourite metadata: ${e.message}")
                false
            }
        }

    /**
     * Get favourite count
     */
    suspend fun getFavouriteCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                favouriteMetadataDao.getFavouriteCount()
            } catch (e: Exception) {
                Log.e("LocalDataSource", "❌ Error getting favourite count: ${e.message}")
                0
            }
        }
}
