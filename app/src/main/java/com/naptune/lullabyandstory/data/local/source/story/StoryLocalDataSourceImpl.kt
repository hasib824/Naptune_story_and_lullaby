package com.naptune.lullabyandstory.data.local.source.story

import android.util.Log
import com.naptune.lullabyandstory.data.local.dao.FavouriteMetadataDao
import com.naptune.lullabyandstory.data.local.dao.StoryAudioLanguageDao
import com.naptune.lullabyandstory.data.local.dao.StoryDao
import com.naptune.lullabyandstory.data.local.dao.StoryDescriptionTranslationDao
import com.naptune.lullabyandstory.data.local.dao.StoryNameTranslationDao
import com.naptune.lullabyandstory.data.local.dao.StoryWithFullLocalization
import com.naptune.lullabyandstory.data.local.dao.StoryWithLocalizedName
import com.naptune.lullabyandstory.data.local.dao.StoryWithNameTranslation
import com.naptune.lullabyandstory.data.local.entity.FavouriteMetadataEntity
import com.naptune.lullabyandstory.data.local.entity.StoryAudioLanguageLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryDescriptionTranslationLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryNameTranslationLocalEntity
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StoryLocalDataSourceImpl @Inject constructor(
    private val storyDao: StoryDao,
    private val storyAudioLanguageDao: StoryAudioLanguageDao,
    private val storyNameTranslationDao: StoryNameTranslationDao,
    private val storyDescriptionTranslationDao: StoryDescriptionTranslationDao,
    private val favouriteMetadataDao: FavouriteMetadataDao
) : StoryLocalDataSource {
    // =====================================================
    // STORY OPERATIONS
    // =====================================================

    override suspend fun insertAllStories(stories: List<StoryLocalEntity>) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "💾 Inserting ${stories.size} stories...")
                storyDao.insertAllStories(stories)
                Log.d("LullabyLocalDataSourceImpl", "✅ Stories inserted successfully")
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error inserting stories: ${e.message}")
                throw e
            }
        }

    override fun getAllStories(): Flow<List<StoryLocalEntity>> {
        Log.d("LullabyLocalDataSourceImpl", "📖 Getting all stories from database...")
        return storyDao.getAllStories()
    }

    override suspend fun getStoriesCount(): Int =
        withContext(Dispatchers.IO) {
            try {
                val count = storyDao.getStoriesCount()
                Log.d("LullabyLocalDataSourceImpl", "📊 Story count: $count")
                count
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error getting story count: ${e.message}")
                0
            }
        }

    // ✅ NEW: Language-aware favourite stories
    override fun getFavouriteStoriesWithLocalizedNames(languageCode: String): Flow<List<StoryWithLocalizedName>> {
        Log.d(
            "LullabyLocalDataSourceImpl",
            "❤️ Getting favourite stories with localized names for language: $languageCode"
        )
        return storyNameTranslationDao.getFavouriteStoriesWithNameTranslations(languageCode)
    }

    override suspend fun toggleStoryFavourite(documentId: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "❤️ Toggling story favourite: $documentId")
                storyDao.toggleStoryFavourite(documentId)
                Log.d("LullabyLocalDataSourceImpl", "✅ Story favourite toggled successfully")
            } catch (e: Exception) {
                Log.e(
                    "LullabyLocalDataSourceImpl",
                    "❌ Error toggling story favourite: ${e.message}"
                )
                throw e
            }
        }

    override fun checkIfItemIsFavourite(documentId: String): Flow<Boolean> {
        Log.d("LullabyLocalDataSourceImpl", "🔍 Checking if story is favourite: $documentId")
        return storyDao.checkIfItemIsFavourite(documentId)
    }

    override suspend fun deleteAllStories() =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "🗑️ Deleting all stories...")
                storyDao.deleteAllStories()
                Log.d("LullabyLocalDataSourceImpl", "✅ All stories deleted")
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error deleting stories: ${e.message}")
                throw e
            }
        }

    override fun getFavouriteStories(): Flow<List<StoryLocalEntity>> {
        Log.d("LullabyLocalDataSourceImpl", "❤️ Getting favourite stories")
        return storyDao.getFavouriteStories()
    }


        ///


        // =====================================================
        // STORY NAME TRANSLATION OPERATIONS
        // =====================================================

        override  fun getAllStoryNameTranslations(): Flow<List<StoryNameTranslationLocalEntity>> {
            Log.d("LullabyLocalDataSourceImpl", "🌍 Getting all story name translations")
            return storyNameTranslationDao.getAllStoryNameTranslations()
        }

        override   suspend fun getStoryNameTranslationByDocumentId(storyDocumentId: String): StoryNameTranslationLocalEntity? =
            withContext(Dispatchers.IO) {
                try {
                    Log.d("LullabyLocalDataSourceImpl", "🔍 Getting story name translation for document ID: $storyDocumentId")
                    storyNameTranslationDao.getStoryNameTranslationByDocumentId(storyDocumentId)
                } catch (e: Exception) {
                    Log.e("LullabyLocalDataSourceImpl", "❌ Error getting story name translation: ${e.message}")
                    null
                }
            }

        override  suspend fun getStoryNameTranslationById(storyId: String): StoryNameTranslationLocalEntity? =
            withContext(Dispatchers.IO) {
                try {
                    Log.d("LullabyLocalDataSourceImpl", "🔍 Getting story name translation for story ID: $storyId")
                    storyNameTranslationDao.getStoryNameTranslationById(storyId)
                } catch (e: Exception) {
                    Log.e("LullabyLocalDataSourceImpl", "❌ Error getting story name translation by story ID: ${e.message}")
                    null
                }
            }

        override suspend fun insertStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity) =
            withContext(Dispatchers.IO) {
                try {
                    Log.d("LullabyLocalDataSourceImpl", "💾 Inserting story name translation: ${storyNameTranslation.storyId}")
                    storyNameTranslationDao.insertStoryNameTranslation(storyNameTranslation)
                    Log.d("LullabyLocalDataSourceImpl", "✅ Story name translation inserted successfully")
                } catch (e: Exception) {
                    Log.e("LullabyLocalDataSourceImpl", "❌ Error inserting story name translation: ${e.message}")
                    throw e
                }
            }

        override   suspend fun insertAllStoryNameTranslations(storyNameTranslations: List<StoryNameTranslationLocalEntity>) =
            withContext(Dispatchers.IO) {
                try {
                    Log.d("LullabyLocalDataSourceImpl", "💾 Inserting ${storyNameTranslations.size} story name translations...")
                    storyNameTranslationDao.insertAllStoryNameTranslations(storyNameTranslations)
                    Log.d("LullabyLocalDataSourceImpl", "✅ Story name translations inserted successfully")
                    Log.d("LullabyLocalDataSourceImpl", "🔗 Story name translation relationships: ${storyNameTranslations.map { "${it.storyId}->${it.storyDocumentId}" }}")
                } catch (e: Exception) {
                    Log.e("LullabyLocalDataSourceImpl", "❌ Error inserting story name translations: ${e.message}")
                    throw e
                }
            }

        override  suspend fun updateStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity) =
            withContext(Dispatchers.IO) {
                try {
                    Log.d("LullabyLocalDataSourceImpl", "🔄 Updating story name translation: ${storyNameTranslation.storyId}")
                    storyNameTranslationDao.updateStoryNameTranslation(storyNameTranslation)
                    Log.d("LullabyLocalDataSourceImpl", "✅ Story name translation updated successfully")
                } catch (e: Exception) {
                    Log.e("LullabyLocalDataSourceImpl", "❌ Error updating story name translation: ${e.message}")
                    throw e
                }
            }

        override   suspend fun deleteStoryNameTranslation(storyNameTranslation: StoryNameTranslationLocalEntity) =
            withContext(Dispatchers.IO) {
                try {
                    Log.d("LullabyLocalDataSourceImpl", "🗑️ Deleting story name translation: ${storyNameTranslation.storyId}")
                    storyNameTranslationDao.deleteStoryNameTranslation(storyNameTranslation)
                    Log.d("LullabyLocalDataSourceImpl", "✅ Story name translation deleted successfully")
                } catch (e: Exception) {
                    Log.e("LullabyLocalDataSourceImpl", "❌ Error deleting story name translation: ${e.message}")
                    throw e
                }
            }

        override  suspend fun deleteAllStoryNameTranslations() =
            withContext(Dispatchers.IO) {
                try {
                    Log.d("LullabyLocalDataSourceImpl", "🗑️ Deleting all story name translations...")
                    storyNameTranslationDao.deleteAllStoryNameTranslations()
                    Log.d("LullabyLocalDataSourceImpl", "✅ All story name translations deleted")
                } catch (e: Exception) {
                    Log.e("LullabyLocalDataSourceImpl", "❌ Error deleting all story name translations: ${e.message}")
                    throw e
                }
            }

        override   suspend fun getStoryNameTranslationCount(): Int =
            withContext(Dispatchers.IO) {
                try {
                    val count = storyNameTranslationDao.getStoryNameTranslationCount()
                    Log.d("LullabyLocalDataSourceImpl", "📊 Story name translation count: $count")
                    count
                } catch (e: Exception) {
                    Log.e("LullabyLocalDataSourceImpl", "❌ Error getting story name translation count: ${e.message}")
                    0
                }
            }

        // ✅ OPTIMIZED: Single method for language-specific stories
        override  fun getAllStoriesWithLocalizedNames(languageCode: String): Flow<List<StoryWithLocalizedName>> {
            Log.d("LullabyLocalDataSourceImpl", "🚀 Getting all stories with localized names for language: $languageCode")
            return storyNameTranslationDao.getAllStoriesWithNameTranslations(languageCode)
        }

        // 🚀 ULTRA OPTIMIZED: Single method for FULL story localization (Name + Description)
        override   fun getAllStoriesWithFullLocalization(languageCode: String): Flow<List<StoryWithFullLocalization>> {
            Log.d("LullabyLocalDataSourceImpl", "🚀 ULTRA OPTIMIZED: Getting all stories with FULL localization for language: $languageCode")
            return storyNameTranslationDao.getAllStoriesWithFullLocalization(languageCode)
        }

        // 🚀 ULTRA OPTIMIZED: Single method for FULL favourite story localization (Name + Description)
        override   fun getFavouriteStoriesWithFullLocalization(languageCode: String): Flow<List<StoryWithFullLocalization>> {
            Log.d("LullabyLocalDataSourceImpl", "❤️ ULTRA OPTIMIZED: Getting favourite stories with FULL localization for language: $languageCode")
            return storyNameTranslationDao.getFavouriteStoriesWithFullLocalization(languageCode)
        }

        // =====================================================
        // STORY DESCRIPTION TRANSLATION OPERATIONS
        // =====================================================

        override   suspend fun insertAllStoryDescriptionTranslations(storyDescriptionTranslations: List<StoryDescriptionTranslationLocalEntity>) =
            withContext(Dispatchers.IO) {
                try {
                    Log.d("LullabyLocalDataSourceImpl", "💾 Inserting ${storyDescriptionTranslations.size} story description translations...")
                    storyDescriptionTranslationDao.insertAllStoryDescriptionTranslations(storyDescriptionTranslations)
                    Log.d("LullabyLocalDataSourceImpl", "✅ Story description translations inserted successfully")
                    Log.d("LullabyLocalDataSourceImpl", "🔗 Story description translation relationships: ${storyDescriptionTranslations.map { "${it.storyId}->${it.storyDocumentId}" }}")
                } catch (e: Exception) {
                    Log.e("LullabyLocalDataSourceImpl", "❌ Error inserting story description translations: ${e.message}")
                    throw e
                }
            }

        override   suspend fun deleteAllStoryDescriptionTranslations() =
            withContext(Dispatchers.IO) {
                try {
                    Log.d("LullabyLocalDataSourceImpl", "🗑️ Deleting all story description translations...")
                    storyDescriptionTranslationDao.deleteAllStoryDescriptionTranslations()
                    Log.d("LullabyLocalDataSourceImpl", "✅ All story description translations deleted")
                } catch (e: Exception) {
                    Log.e("LullabyLocalDataSourceImpl", "❌ Error deleting all story description translations: ${e.message}")
                    throw e
                }
            }

        // =====================================================
        // STORY AUDIO LANGUAGE OPERATIONS
        // =====================================================

        override  suspend fun insertAllStoryAudioLanguages(storyAudioLanguages: List<StoryAudioLanguageLocalEntity>) =
            withContext(Dispatchers.IO) {
                try {
                    Log.d("LullabyLocalDataSourceImpl", "🎵 Inserting ${storyAudioLanguages.size} story audio languages...")
                    storyAudioLanguageDao.insertAllStoryAudioLanguages(storyAudioLanguages)
                    Log.d("LullabyLocalDataSourceImpl", "✅ Story audio languages inserted successfully")
                    Log.d("LullabyLocalDataSourceImpl", "🔗 Story audio language relationships: ${storyAudioLanguages.map { "${it.storyDocumentId}" }}")
                } catch (e: Exception) {
                    Log.e("LullabyLocalDataSourceImpl", "❌ Error inserting story audio languages: ${e.message}")
                    throw e
                }
            }

        override  suspend fun getStoryAudioLanguageByStoryDocumentId(storyDocumentId: String): StoryAudioLanguageLocalEntity? =
            withContext(Dispatchers.IO) {
                try {
                    Log.d("LullabyLocalDataSourceImpl", "🔍 Getting story audio language for story document ID: $storyDocumentId")
                    storyAudioLanguageDao.getStoryAudioLanguageByStoryDocumentId(storyDocumentId)
                } catch (e: Exception) {
                    Log.e("LullabyLocalDataSourceImpl", "❌ Error getting story audio language: ${e.message}")
                    null
                }
            }

        override  suspend fun getStoryAudioLanguageCount(): Int =
            withContext(Dispatchers.IO) {
                try {
                    storyAudioLanguageDao.getStoryAudioLanguageCount()
                } catch (e: Exception) {
                    Log.e("LullabyLocalDataSourceImpl", "❌ Error getting story audio language count: ${e.message}")
                    0
                }
            }

        override  suspend fun deleteAllStoryAudioLanguages() =
            withContext(Dispatchers.IO) {
                try {
                    Log.d("LullabyLocalDataSourceImpl", "🗑️ Deleting all story audio languages...")
                    storyAudioLanguageDao.deleteAllStoryAudioLanguages()
                    Log.d("LullabyLocalDataSourceImpl", "✅ All story audio languages deleted")
                } catch (e: Exception) {
                    Log.e("LullabyLocalDataSourceImpl", "❌ Error deleting all story audio languages: ${e.message}")
                    throw e
                }
            }

        // =====================================================
        // JOIN OPERATIONS (Story + StoryNameTranslation)
        // =====================================================

        override  suspend fun getStoryWithNameTranslation(documentId: String): StoryWithNameTranslation? =
            withContext(Dispatchers.IO) {
                try {
                    Log.d("LullabyLocalDataSourceImpl", "🔗 Getting story with name translation for ID: $documentId")
                    storyNameTranslationDao.getStoryWithNameTranslation(documentId)
                } catch (e: Exception) {
                    Log.e("LullabyLocalDataSourceImpl", "❌ Error getting story with name translation: ${e.message}")
                    null
                }
            }


    override  suspend fun insertFavouriteMetadata(itemId: String, itemType: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "❤️ Inserting favourite metadata: $itemId ($itemType)")
                val metadata = FavouriteMetadataEntity(
                    itemId = itemId,
                    itemType = itemType,
                    favouritedAt = System.currentTimeMillis()
                )
                favouriteMetadataDao.insertFavouriteMetadata(metadata)
                Log.d("LullabyLocalDataSourceImpl", "✅ Favourite metadata inserted successfully")
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error inserting favourite metadata: ${e.message}")
                throw e
            }
        }

    /**
     * Delete favourite metadata when user unfavourites an item
     */
    override  suspend fun deleteFavouriteMetadata(itemId: String, itemType: String) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("LullabyLocalDataSourceImpl", "💔 Deleting favourite metadata: $itemId ($itemType)")
                favouriteMetadataDao.deleteFavouriteMetadata(itemId, itemType)
                Log.d("LullabyLocalDataSourceImpl", "✅ Favourite metadata deleted successfully")
            } catch (e: Exception) {
                Log.e("LullabyLocalDataSourceImpl", "❌ Error deleting favourite metadata: ${e.message}")
                throw e
            }
        }



    }
