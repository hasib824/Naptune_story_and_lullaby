package com.naptune.lullabyandstory.data.local.source

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.naptune.lullabyandstory.data.local.dao.FavouriteMetadataDao
import com.naptune.lullabyandstory.data.local.dao.LullabyDao
import com.naptune.lullabyandstory.data.local.dao.LullabyWithLocalizedName
import com.naptune.lullabyandstory.data.local.dao.StoryDao
import com.naptune.lullabyandstory.data.local.dao.StoryAudioLanguageDao
import com.naptune.lullabyandstory.data.local.dao.StoryDescriptionTranslationDao
import com.naptune.lullabyandstory.data.local.dao.StoryNameTranslationDao
import com.naptune.lullabyandstory.data.local.dao.TranslationDao
import com.naptune.lullabyandstory.data.local.dao.LullabyWithTranslation
import com.naptune.lullabyandstory.data.local.dao.StoryWithFullLocalization
import com.naptune.lullabyandstory.data.local.dao.StoryWithLocalizedName
import com.naptune.lullabyandstory.data.local.dao.StoryWithNameTranslation
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


@Singleton
class LocalDataSourceImpl @Inject constructor(
    private val lullabyDao: LullabyDao,
    private val storyDao: StoryDao,
    private val storyAudioLanguageDao: StoryAudioLanguageDao,
    private val storyDescriptionTranslationDao: StoryDescriptionTranslationDao,
    private val storyNameTranslationDao: StoryNameTranslationDao,
    private val translationDao: TranslationDao,
    private val favouriteMetadataDao: FavouriteMetadataDao
): LocalDataSource {

      // =====================================================
     // LULLABY OPERATIONS
    // =====================================================

    override fun getAllLullabies(): Flow<List<LullabyLocalEntity>> = lullabyDao.getAllLullabies()

    override suspend fun getLullabyById(documentId: String): LullabyLocalEntity? =
        withContext(Dispatchers.IO) {
            lullabyDao.getLullabyById(documentId)
        }

    override fun searchLullabies(query: String): Flow<List<LullabyLocalEntity>> =
        lullabyDao.searchLullabies(query)

    override suspend fun insertLullaby(lullaby: LullabyLocalEntity) =
        withContext(Dispatchers.IO) {
            lullabyDao.insertLullaby(lullaby)
        }

    override suspend fun insertAllLullabies(lullabies: List<LullabyLocalEntity>) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "💾 Inserting ${lullabies.size} lullabies...")
                lullabyDao.insertAllLullabies(lullabies)
                Log.d("LocalDataSourceImpl", "✅ Lullabies inserted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error inserting lullabies: ${e.message}")
                throw e
            }
        }

    override suspend fun updateLullaby(lullaby: LullabyLocalEntity) =
        withContext(Dispatchers.IO) {
            lullabyDao.updateLullaby(lullaby)
        }

    override suspend fun updateLocalPath(musicLocalPath: String, documentId: String) =
        withContext(Dispatchers.IO) {
            lullabyDao.updateLocalPath(musicLocalPath, documentId)
        }

    override suspend fun markAsDownloaded(documentId: String): Int =
        withContext(Dispatchers.IO) {
            Log.d("LocalDataSourceImpl", "💾 Marking lullaby as downloaded: $documentId")
            lullabyDao.updateIsDownloaded(documentId, true)
        }

    override suspend fun deleteLullaby(lullaby: LullabyLocalEntity) =
        withContext(Dispatchers.IO) {
            lullabyDao.deleteLullaby(lullaby)
        }

    override suspend fun deleteAllLullabies() =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🗑️ Deleting all lullabies...")
                lullabyDao.deleteAllLullabies()
                Log.d("LocalDataSourceImpl", "✅ All lullabies deleted")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error deleting lullabies: ${e.message}")
                throw e
            }
        }

    override suspend fun getLullabyCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                val count = lullabyDao.getLullabyCount()
                Log.d("LocalDataSourceImpl", "📊 Lullaby count: $count")
                count
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error getting lullaby count: ${e.message}")
                0
            }
        }

    override suspend fun getLullabiesPaginated(limit: Int, offset: Int): List<LullabyLocalEntity> =
        withContext(Dispatchers.IO) {
            lullabyDao.getLullabiesPaginated(limit, offset)
        }

    // =====================================================
    // STORY OPERATIONS  
    // =====================================================

    override suspend fun insertAllStories(stories: List<StoryLocalEntity>) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "💾 Inserting ${stories.size} stories...")
                storyDao.insertAllStories(stories)
                Log.d("LocalDataSourceImpl", "✅ Stories inserted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error inserting stories: ${e.message}")
                throw e
            }
        }

    override fun getAllStories(): Flow<List<StoryLocalEntity>> {
        Log.d("LocalDataSourceImpl", "📖 Getting all stories from database...")
        return storyDao.getAllStories()
    }

    override suspend fun getStoriesCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                val count = storyDao.getStoriesCount()
                Log.d("LocalDataSourceImpl", "📊 Story count: $count")
                count
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error getting story count: ${e.message}")
                0
            }
        }

    override suspend fun toggleStoryFavourite(documentId: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "❤️ Toggling story favourite: $documentId")
                storyDao.toggleStoryFavourite(documentId)
                Log.d("LocalDataSourceImpl", "✅ Story favourite toggled successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error toggling story favourite: ${e.message}")
                throw e
            }
        }

    override fun checkIfItemIsFavourite(documentId: String): Flow<Boolean> {
        Log.d("LocalDataSourceImpl", "🔍 Checking if story is favourite: $documentId")
        return storyDao.checkIfItemIsFavourite(documentId)
    }

    override suspend fun deleteAllStories() =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🗑️ Deleting all stories...")
                storyDao.deleteAllStories()
                Log.d("LocalDataSourceImpl", "✅ All stories deleted")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error deleting stories: ${e.message}")
                throw e
            }
        }
    
    // =====================================================
    // FAVOURITE OPERATIONS
    // =====================================================
    
    override suspend fun toggleLullabyFavourite(lullabyId: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "❤️ Toggling lullaby favourite: $lullabyId")
                lullabyDao.toggleLullabyFavourite(lullabyId)
                Log.d("LocalDataSourceImpl", "✅ Lullaby favourite toggled successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error toggling lullaby favourite: ${e.message}")
                throw e
            }
        }
    
    override fun isLullabyFavourite(lullabyId: String): Flow<Boolean> {
        Log.d("LocalDataSourceImpl", "🔍 Checking if lullaby is favourite: $lullabyId")
        return lullabyDao.isLullabyFavourite(lullabyId)
    }
    
    override fun getFavouriteLullabies(): Flow<List<LullabyLocalEntity>> {
        Log.d("LocalDataSourceImpl", "❤️ Getting favourite lullabies")
        return lullabyDao.getFavouriteLullabies()
    }

    // ✅ NEW: Language-aware favourite lullabies
    override fun getFavouriteLullabiesWithLocalizedNames(languageCode: String): Flow<List<LullabyWithLocalizedName>> {
        Log.d("LocalDataSourceImpl", "❤️ Getting favourite lullabies with localized names for language: $languageCode")
        return translationDao.getFavouriteLullabiesWithTranslations(languageCode)
    }
    
    override fun getFavouriteStories(): Flow<List<StoryLocalEntity>> {
        Log.d("LocalDataSourceImpl", "❤️ Getting favourite stories")
        return storyDao.getFavouriteStories()
    }

    // ✅ NEW: Language-aware favourite stories
    override fun getFavouriteStoriesWithLocalizedNames(languageCode: String): Flow<List<StoryWithLocalizedName>> {
        Log.d("LocalDataSourceImpl", "❤️ Getting favourite stories with localized names for language: $languageCode")
        return storyNameTranslationDao.getFavouriteStoriesWithNameTranslations(languageCode)
    }

    // =====================================================
    // TRANSLATION OPERATIONS
    // =====================================================

    override fun getAllTranslations(): Flow<List<TranslationLocalEntity>> {
        Log.d("LocalDataSourceImpl", "🌍 Getting all translations")
        return translationDao.getAllTranslations()
    }

    override suspend fun getTranslationByLullabyDocumentId(lullabyDocumentId: String): TranslationLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🔍 Getting translation for lullaby document ID: $lullabyDocumentId")
                translationDao.getTranslationByLullabyDocumentId(lullabyDocumentId)
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error getting translation: ${e.message}")
                null
            }
        }

    override suspend fun getTranslationByLullabyId(lullabyId: String): TranslationLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🔍 Getting translation for lullaby ID: $lullabyId")
                translationDao.getTranslationByLullabyId(lullabyId)
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error getting translation by lullaby ID: ${e.message}")
                null
            }
        }

    override suspend fun insertTranslation(translation: TranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "💾 Inserting translation: ${translation.lullabyId}")
                translationDao.insertTranslation(translation)
                Log.d("LocalDataSourceImpl", "✅ Translation inserted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error inserting translation: ${e.message}")
                throw e
            }
        }

    override suspend fun insertAllTranslations(translations: List<TranslationLocalEntity>) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "💾 Inserting ${translations.size} translations...")
                translationDao.insertAllTranslations(translations)
                Log.d("LocalDataSourceImpl", "✅ Translations inserted successfully")
                Log.d("LocalDataSourceImpl", "🔗 Translation relationships: ${translations.map { "${it.lullabyId}->${it.lullabyDocumentId}" }}")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error inserting translations: ${e.message}")
                throw e
            }
        }

    override suspend fun updateTranslation(translation: TranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🔄 Updating translation: ${translation.lullabyId}")
                translationDao.updateTranslation(translation)
                Log.d("LocalDataSourceImpl", "✅ Translation updated successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error updating translation: ${e.message}")
                throw e
            }
        }

    override suspend fun deleteTranslation(translation: TranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🗑️ Deleting translation: ${translation.lullabyId}")
                translationDao.deleteTranslation(translation)
                Log.d("LocalDataSourceImpl", "✅ Translation deleted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error deleting translation: ${e.message}")
                throw e
            }
        }

    override suspend fun deleteAllTranslations() =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🗑️ Deleting all translations...")
                translationDao.deleteAllTranslations()
                Log.d("LocalDataSourceImpl", "✅ All translations deleted")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error deleting all translations: ${e.message}")
                throw e
            }
        }

    override suspend fun getTranslationCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                val count = translationDao.getTranslationCount()
                Log.d("LocalDataSourceImpl", "📊 Translation count: $count")
                count
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error getting translation count: ${e.message}")
                0
            }
        }

    // =====================================================
    // JOIN OPERATIONS (Lullaby + Translation)
    // =====================================================

    override  suspend fun getLullabyWithTranslation(documentId: String): LullabyWithTranslation? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🔗 Getting lullaby with translation for ID: $documentId")
                translationDao.getLullabyWithTranslation(documentId)
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error getting lullaby with translation: ${e.message}")
                null
            }
        }

    // ✅ OPTIMIZED: Single method for language-specific lullabies
    override   fun getAllLullabiesWithLocalizedNames(languageCode: String): Flow<List<LullabyWithLocalizedName>> {
        Log.d("LocalDataSourceImpl", "🚀 Getting all lullabies with localized names for language: $languageCode")
        return translationDao.getAllLullabiesWithTranslations(languageCode)
    }

    // =====================================================
    // STORY NAME TRANSLATION OPERATIONS
    // =====================================================

    override  fun getAllStoryNameTranslations(): Flow<List<StoryNameTranslationLocalEntity>> {
        Log.d("LocalDataSourceImpl", "🌍 Getting all story name translations")
        return storyNameTranslationDao.getAllStoryNameTranslations()
    }

    override   suspend fun getStoryNameTranslationByDocumentId(storyDocumentId: String): StoryNameTranslationLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🔍 Getting story name translation for document ID: $storyDocumentId")
                storyNameTranslationDao.getStoryNameTranslationByDocumentId(storyDocumentId)
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error getting story name translation: ${e.message}")
                null
            }
        }

    override  suspend fun getStoryNameTranslationById(storyId: String): StoryNameTranslationLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🔍 Getting story name translation for story ID: $storyId")
                storyNameTranslationDao.getStoryNameTranslationById(storyId)
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error getting story name translation by story ID: ${e.message}")
                null
            }
        }

    override suspend fun insertStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "💾 Inserting story name translation: ${storyNameTranslation.storyId}")
                storyNameTranslationDao.insertStoryNameTranslation(storyNameTranslation)
                Log.d("LocalDataSourceImpl", "✅ Story name translation inserted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error inserting story name translation: ${e.message}")
                throw e
            }
        }

    override   suspend fun insertAllStoryNameTranslations(storyNameTranslations: List<StoryNameTranslationLocalEntity>) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "💾 Inserting ${storyNameTranslations.size} story name translations...")
                storyNameTranslationDao.insertAllStoryNameTranslations(storyNameTranslations)
                Log.d("LocalDataSourceImpl", "✅ Story name translations inserted successfully")
                Log.d("LocalDataSourceImpl", "🔗 Story name translation relationships: ${storyNameTranslations.map { "${it.storyId}->${it.storyDocumentId}" }}")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error inserting story name translations: ${e.message}")
                throw e
            }
        }

    override  suspend fun updateStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🔄 Updating story name translation: ${storyNameTranslation.storyId}")
                storyNameTranslationDao.updateStoryNameTranslation(storyNameTranslation)
                Log.d("LocalDataSourceImpl", "✅ Story name translation updated successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error updating story name translation: ${e.message}")
                throw e
            }
        }

    override   suspend fun deleteStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🗑️ Deleting story name translation: ${storyNameTranslation.storyId}")
                storyNameTranslationDao.deleteStoryNameTranslation(storyNameTranslation)
                Log.d("LocalDataSourceImpl", "✅ Story name translation deleted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error deleting story name translation: ${e.message}")
                throw e
            }
        }

    override  suspend fun deleteAllStoryNameTranslations() =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🗑️ Deleting all story name translations...")
                storyNameTranslationDao.deleteAllStoryNameTranslations()
                Log.d("LocalDataSourceImpl", "✅ All story name translations deleted")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error deleting all story name translations: ${e.message}")
                throw e
            }
        }

    override   suspend fun getStoryNameTranslationCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                val count = storyNameTranslationDao.getStoryNameTranslationCount()
                Log.d("LocalDataSourceImpl", "📊 Story name translation count: $count")
                count
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error getting story name translation count: ${e.message}")
                0
            }
        }

    // ✅ OPTIMIZED: Single method for language-specific stories
    override  fun getAllStoriesWithLocalizedNames(languageCode: String): Flow<List<StoryWithLocalizedName>> {
        Log.d("LocalDataSourceImpl", "🚀 Getting all stories with localized names for language: $languageCode")
        return storyNameTranslationDao.getAllStoriesWithNameTranslations(languageCode)
    }

    // 🚀 ULTRA OPTIMIZED: Single method for FULL story localization (Name + Description)
    override   fun getAllStoriesWithFullLocalization(languageCode: String): Flow<List<StoryWithFullLocalization>> {
        Log.d("LocalDataSourceImpl", "🚀 ULTRA OPTIMIZED: Getting all stories with FULL localization for language: $languageCode")
        return storyNameTranslationDao.getAllStoriesWithFullLocalization(languageCode)
    }

    // 🚀 ULTRA OPTIMIZED: Single method for FULL favourite story localization (Name + Description)
    override   fun getFavouriteStoriesWithFullLocalization(languageCode: String): Flow<List<StoryWithFullLocalization>> {
        Log.d("LocalDataSourceImpl", "❤️ ULTRA OPTIMIZED: Getting favourite stories with FULL localization for language: $languageCode")
        return storyNameTranslationDao.getFavouriteStoriesWithFullLocalization(languageCode)
    }

    // =====================================================
    // STORY DESCRIPTION TRANSLATION OPERATIONS
    // =====================================================

    override   suspend fun insertAllStoryDescriptionTranslations(storyDescriptionTranslations: List<StoryDescriptionTranslationLocalEntity>) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "💾 Inserting ${storyDescriptionTranslations.size} story description translations...")
                storyDescriptionTranslationDao.insertAllStoryDescriptionTranslations(storyDescriptionTranslations)
                Log.d("LocalDataSourceImpl", "✅ Story description translations inserted successfully")
                Log.d("LocalDataSourceImpl", "🔗 Story description translation relationships: ${storyDescriptionTranslations.map { "${it.storyId}->${it.storyDocumentId}" }}")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error inserting story description translations: ${e.message}")
                throw e
            }
        }

    override   suspend fun deleteAllStoryDescriptionTranslations() =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🗑️ Deleting all story description translations...")
                storyDescriptionTranslationDao.deleteAllStoryDescriptionTranslations()
                Log.d("LocalDataSourceImpl", "✅ All story description translations deleted")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error deleting all story description translations: ${e.message}")
                throw e
            }
        }

    // =====================================================
    // STORY AUDIO LANGUAGE OPERATIONS
    // =====================================================

    override  suspend fun insertAllStoryAudioLanguages(storyAudioLanguages: List<StoryAudioLanguageLocalEntity>) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🎵 Inserting ${storyAudioLanguages.size} story audio languages...")
                storyAudioLanguageDao.insertAllStoryAudioLanguages(storyAudioLanguages)
                Log.d("LocalDataSourceImpl", "✅ Story audio languages inserted successfully")
                Log.d("LocalDataSourceImpl", "🔗 Story audio language relationships: ${storyAudioLanguages.map { "${it.storyDocumentId}" }}")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error inserting story audio languages: ${e.message}")
                throw e
            }
        }

    override  suspend fun getStoryAudioLanguageByStoryDocumentId(storyDocumentId: String): StoryAudioLanguageLocalEntity? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🔍 Getting story audio language for story document ID: $storyDocumentId")
                storyAudioLanguageDao.getStoryAudioLanguageByStoryDocumentId(storyDocumentId)
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error getting story audio language: ${e.message}")
                null
            }
        }

    override  suspend fun getStoryAudioLanguageCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                storyAudioLanguageDao.getStoryAudioLanguageCount()
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error getting story audio language count: ${e.message}")
                0
            }
        }

    override  suspend fun deleteAllStoryAudioLanguages() =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🗑️ Deleting all story audio languages...")
                storyAudioLanguageDao.deleteAllStoryAudioLanguages()
                Log.d("LocalDataSourceImpl", "✅ All story audio languages deleted")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error deleting all story audio languages: ${e.message}")
                throw e
            }
        }

    // =====================================================
    // JOIN OPERATIONS (Story + StoryNameTranslation)
    // =====================================================

    override  suspend fun getStoryWithNameTranslation(documentId: String): StoryWithNameTranslation? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "🔗 Getting story with name translation for ID: $documentId")
                storyNameTranslationDao.getStoryWithNameTranslation(documentId)
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error getting story with name translation: ${e.message}")
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
    override  suspend fun insertFavouriteMetadata(itemId: String, itemType: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "❤️ Inserting favourite metadata: $itemId ($itemType)")
                val metadata = FavouriteMetadataEntity(
                    itemId = itemId,
                    itemType = itemType,
                    favouritedAt = System.currentTimeMillis()
                )
                favouriteMetadataDao.insertFavouriteMetadata(metadata)
                Log.d("LocalDataSourceImpl", "✅ Favourite metadata inserted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error inserting favourite metadata: ${e.message}")
                throw e
            }
        }

    /**
     * Delete favourite metadata when user unfavourites an item
     */
    override  suspend fun deleteFavouriteMetadata(itemId: String, itemType: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LocalDataSourceImpl", "💔 Deleting favourite metadata: $itemId ($itemType)")
                favouriteMetadataDao.deleteFavouriteMetadata(itemId, itemType)
                Log.d("LocalDataSourceImpl", "✅ Favourite metadata deleted successfully")
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error deleting favourite metadata: ${e.message}")
                throw e
            }
        }

    /**
     * Check if favourite metadata exists for an item
     */
    override   suspend fun hasFavouriteMetadata(itemId: String, itemType: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                favouriteMetadataDao.hasFavouriteMetadata(itemId, itemType)
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error checking favourite metadata: ${e.message}")
                false
            }
        }

    /**
     * Get favourite count
     */
    override  suspend fun getFavouriteCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                favouriteMetadataDao.getFavouriteCount()
            } catch (e: Exception) {
                Log.e("LocalDataSourceImpl", "❌ Error getting favourite count: ${e.message}")
                0
            }
        }
}
