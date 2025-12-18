package com.naptune.lullabyandstory.di

import com.naptune.lullabyandstory.data.network.source.lullaby.LullabyRemoteDataSource
import com.naptune.lullabyandstory.data.network.source.story.StoryRemoteDataSource
import com.naptune.lullabyandstory.data.network.source.lullaby.LullabyRemoteDataSourceImpl
import com.naptune.lullabyandstory.data.network.source.story.StoryRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {

    @Binds
    abstract fun bindLullabyRemoteDataSource(remoteDataSourceImpl: LullabyRemoteDataSourceImpl): LullabyRemoteDataSource

    @Binds
    abstract fun bindStoryRemoteDataSource(remoteDataSourceImpl: StoryRemoteDataSourceImpl): StoryRemoteDataSource

}