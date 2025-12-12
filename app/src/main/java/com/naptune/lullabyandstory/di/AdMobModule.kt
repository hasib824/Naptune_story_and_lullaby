package com.naptune.lullabyandstory.di

import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.data.repository.AdMobRepositoryImpl
import com.naptune.lullabyandstory.domain.repository.AdMobRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdMobModule {

    @Binds
    @Singleton
    abstract fun bindAdMobRepository(
        adMobRepositoryImpl: AdMobRepositoryImpl
    ): AdMobRepository
}