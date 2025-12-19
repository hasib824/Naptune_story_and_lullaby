package com.naptune.lullabyandstory.data.repository

import android.util.Log
import com.naptune.lullabyandstory.data.local.source.lullaby.LullabyLocalDataSource
import com.naptune.lullabyandstory.data.local.source.lullaby.LullabyFavouriteDataSource
import com.naptune.lullabyandstory.domain.manager.LanguageStateManager
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.repository.LullabyFavouriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LullabyFavouriteRepository for favourite operations.
 * Follows Single Responsibility Principle (SRP) - handles only favourite operations.
 */
@Singleton
class LullabyFavouriteRepositoryImpl @Inject constructor(
    private val lullabyFavouriteDataSource: LullabyFavouriteDataSource,
    private val lullabyLocalDataSource: LullabyLocalDataSource,
    private val languageStateManager: LanguageStateManager
) : LullabyFavouriteRepository {

    override suspend fun toggleLullabyFavourite(lullabyId: String) {
        try {
            Log.d("LullabyFavouriteRepositoryImpl", "❤️ Toggling lullaby favourite for ID: $lullabyId")

            // Check current favourite status BEFORE toggling
            val lullaby = lullabyLocalDataSource.getLullabyById(lullabyId)
            val wasAlreadyFavourite = lullaby?.isFavourite ?: false

            Log.d("LullabyFavouriteRepositoryImpl", "📊 Current favourite status: $wasAlreadyFavourite")

            // Toggle the favourite boolean in lullaby table
            lullabyFavouriteDataSource.toggleLullabyFavourite(lullabyId)

            // Update metadata for LIFO ordering
            if (wasAlreadyFavourite) {
                // Was favourite, now unfavouriting -> DELETE metadata
                Log.d("LullabyFavouriteRepositoryImpl", "💔 Removing from favourites, deleting metadata")
                lullabyFavouriteDataSource.deleteFavouriteMetadata(lullabyId, "lullaby")
            } else {
                // Was not favourite, now favouriting -> INSERT metadata
                Log.d("LullabyFavouriteRepositoryImpl", "❤️ Adding to favourites, inserting metadata")
                lullabyFavouriteDataSource.insertFavouriteMetadata(lullabyId, "lullaby")
            }

            Log.d("LullabyFavouriteRepositoryImpl", "✅ Lullaby favourite toggled successfully with LIFO metadata")
        } catch (e: Exception) {
            Log.e("LullabyFavouriteRepositoryImpl", "❌ Error toggling lullaby favourite: ${e.message}")
            throw e
        }
    }

    override fun checkIfLullabyIsFavourite(lullabyId: String): Flow<Boolean> {
        Log.d("LullabyFavouriteRepositoryImpl", "🔍 Checking if lullaby is favourite for ID: $lullabyId")
        return lullabyFavouriteDataSource.isLullabyFavourite(lullabyId)
    }

    override fun getFavouriteLullabies(): Flow<List<LullabyDomainModel>> {
        Log.d("LullabyFavouriteRepositoryImpl", "❤️ Getting language-aware favourite lullabies")

        return languageStateManager.currentLanguage.flatMapLatest { currentLanguage ->
            Log.d("LullabyFavouriteRepositoryImpl", "🔄 Getting favourite lullabies for language: $currentLanguage")

            lullabyFavouriteDataSource.getFavouriteLullabiesWithLocalizedNames(currentLanguage)
                .map { favouriteLullabiesWithLocalizedNames ->
                    Log.d("LullabyFavouriteRepositoryImpl", "❤️ Favourite lullabies updated: ${favouriteLullabiesWithLocalizedNames.size} items")

                    favouriteLullabiesWithLocalizedNames.map { lullabyWithLocalizedName ->
                        val lullaby = lullabyWithLocalizedName.lullaby
                        val localizedName = lullabyWithLocalizedName.localizedMusicName

                        LullabyDomainModel(
                            documentId = lullaby.documentId,
                            id = lullaby.id,
                            musicName = localizedName,
                            musicPath = lullaby.musicPath,
                            musicLocalPath = lullaby.musicLocalPath,
                            musicSize = lullaby.musicSize,
                            imagePath = lullaby.imagePath,
                            musicLength = lullaby.musicLength,
                            isDownloaded = lullaby.isDownloaded,
                            isFavourite = lullaby.isFavourite,
                            popularity_count = lullaby.popularity_count,
                            isFree = lullaby.isFree,
                            translation = null
                        )
                    }
                }
        }
    }
}
