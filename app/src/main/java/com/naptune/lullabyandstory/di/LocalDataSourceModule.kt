package com.naptune.lullabyandstory.di

import com.naptune.lullabyandstory.data.local.source.favourite.FavouriteDataSource
import com.naptune.lullabyandstory.data.local.source.favourite.FavouriteDataSourceImpl
import com.naptune.lullabyandstory.data.local.source.lullaby.LullabyLocalDataSource
import com.naptune.lullabyandstory.data.local.source.lullaby.LullabyLocalDataSourceImpl
import com.naptune.lullabyandstory.data.local.source.lullaby.LullabyFavouriteDataSource
import com.naptune.lullabyandstory.data.local.source.lullaby.LullabyFavouriteDataSourceImpl
import com.naptune.lullabyandstory.data.local.source.lullaby.LullabyTranslationDataSource
import com.naptune.lullabyandstory.data.local.source.lullaby.LullabyTranslationDataSourceImpl
import com.naptune.lullabyandstory.data.local.source.story.StoryLocalDataSource
import com.naptune.lullabyandstory.data.local.source.story.StoryLocalDataSourceImpl
import com.naptune.lullabyandstory.data.local.source.story.StoryTranslationDataSource
import com.naptune.lullabyandstory.data.local.source.story.StoryTranslationDataSourceImpl
import com.naptune.lullabyandstory.data.local.source.story.StoryAudioLanguageDataSource
import com.naptune.lullabyandstory.data.local.source.story.StoryAudioLanguageDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataSourceModule {

    @Binds
    abstract fun bindLullabyLocalDataSource(dataSourceImpl: LullabyLocalDataSourceImpl): LullabyLocalDataSource

    // =====================================================
    // Story Data Source
    // =====================================================

    @Binds
    abstract fun bindStoryLocalDataSource(dataSourceImpl: StoryLocalDataSourceImpl): StoryLocalDataSource

    @Binds
    abstract fun bindStoryTranslationDataSource(
        dataSourceImpl: StoryTranslationDataSourceImpl
    ): StoryTranslationDataSource

    @Binds
    abstract fun bindStoryAudioLanguageDataSource(
        dataSourceImpl: StoryAudioLanguageDataSourceImpl
    ): StoryAudioLanguageDataSource

    // =====================================================
    // Shared Favourite Data Source
    // =====================================================

    @Binds
    abstract fun bindFavouriteDataSource(
        dataSourceImpl: FavouriteDataSourceImpl
    ): FavouriteDataSource

    // =====================================================
    // NEW: Split Lullaby Data Sources (ISP Compliant)
    // =====================================================

    @Binds
    abstract fun bindLullabyFavouriteDataSource(
        dataSourceImpl: LullabyFavouriteDataSourceImpl
    ): LullabyFavouriteDataSource

    @Binds
    abstract fun bindLullabyTranslationDataSource(
        dataSourceImpl: LullabyTranslationDataSourceImpl
    ): LullabyTranslationDataSource

    // =====================================================
    // OLD: Legacy binding (to be removed after migration)
    // =====================================================


}