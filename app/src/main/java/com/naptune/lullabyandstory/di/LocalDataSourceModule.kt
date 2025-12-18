package com.naptune.lullabyandstory.di

import com.naptune.lullabyandstory.data.local.source.LocalDataSource
import com.naptune.lullabyandstory.data.local.source.LocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataSourceModule {

    @Binds
    abstract fun bindLocalDataSource(dataSourceImpl: LocalDataSourceImpl): LocalDataSource

}