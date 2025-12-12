package com.naptune.lullabyandstory.di

import com.naptune.lullabyandstory.data.repository.LullabyRepositoryImpl
import com.naptune.lullabyandstory.data.repository.ProfileRepositoryImpl
import com.naptune.lullabyandstory.data.repository.StoryRepositoryImpl
import com.naptune.lullabyandstory.domain.repository.LullabyRepository
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

    @Binds
    abstract fun bindStroyRepository(storyRepositoryImpl: StoryRepositoryImpl): StoryRepository

    @Binds
    abstract fun bindLullabyRepository(lullabyRepositoryImpl: LullabyRepositoryImpl): LullabyRepository

    @Binds
    abstract fun bindProfileRepository(profileRepositoryImpl: ProfileRepositoryImpl): ProfileRepository

}