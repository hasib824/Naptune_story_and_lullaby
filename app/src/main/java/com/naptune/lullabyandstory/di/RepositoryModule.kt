package com.naptune.lullabyandstory.di

import com.naptune.lullabyandstory.data.repository.LullabyDataRepositoryImpl
import com.naptune.lullabyandstory.data.repository.LullabyFavouriteRepositoryImpl
import com.naptune.lullabyandstory.data.repository.LullabyRepositoryImpl
import com.naptune.lullabyandstory.data.repository.LullabyTranslationRepositoryImpl
import com.naptune.lullabyandstory.data.repository.ProfileRepositoryImpl
import com.naptune.lullabyandstory.data.repository.StoryRepositoryImpl
import com.naptune.lullabyandstory.domain.repository.LullabyDataRepository
import com.naptune.lullabyandstory.domain.repository.LullabyFavouriteRepository
import com.naptune.lullabyandstory.domain.repository.LullabyRepository
import com.naptune.lullabyandstory.domain.repository.LullabyTranslationRepository
import com.naptune.lullabyandstory.domain.repository.ProfileRepository
import com.naptune.lullabyandstory.domain.repository.StoryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // =====================================================
    // NEW: Split Lullaby Repositories (ISP Compliant)
    // =====================================================

    @Binds
    abstract fun bindLullabyDataRepository(
        repositoryImpl: LullabyDataRepositoryImpl
    ): LullabyDataRepository

    @Binds
    abstract fun bindLullabyFavouriteRepository(
        repositoryImpl: LullabyFavouriteRepositoryImpl
    ): LullabyFavouriteRepository

    @Binds
    abstract fun bindLullabyTranslationRepository(
        repositoryImpl: LullabyTranslationRepositoryImpl
    ): LullabyTranslationRepository

    // =====================================================
    // OLD: Legacy binding (to be removed after migration)
    // =====================================================

    @Binds
    abstract fun bindLullabyRepository(lullabyRepositoryImpl: LullabyRepositoryImpl): LullabyRepository

    // =====================================================
    // Other Repositories
    // =====================================================

    @Binds
    abstract fun bindStroyRepository(storyRepositoryImpl: StoryRepositoryImpl): StoryRepository

    @Binds
    abstract fun bindProfileRepository(profileRepositoryImpl: ProfileRepositoryImpl): ProfileRepository
}