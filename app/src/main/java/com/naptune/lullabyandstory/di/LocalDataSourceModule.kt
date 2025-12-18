package com.naptune.lullabyandstory.di

import com.naptune.lullabyandstory.data.local.source.lullaby.LullabyLocalDataSource
import com.naptune.lullabyandstory.data.local.source.lullaby.LullabyLocalDataSourceImpl
import com.naptune.lullabyandstory.data.local.source.story.StoryLocalDataSource
import com.naptune.lullabyandstory.data.local.source.story.StoryLocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataSourceModule {

    @Binds
    abstract fun bindLullabyLocalDataSource(dataSourceImpl: LullabyLocalDataSourceImpl): LullabyLocalDataSource

    @Binds
    abstract fun bindStoryLocalDataSource(dataSourceImpl: StoryLocalDataSourceImpl): StoryLocalDataSource


}