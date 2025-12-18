package com.naptune.lullabyandstory.di

// Android Settings > Apps > Your App > Storage > Clear Data
// অথবা


import android.content.Context
import androidx.room.Room
import com.naptune.lullabyandstory.data.local.dao.FavouriteMetadataDao
import com.naptune.lullabyandstory.data.local.dao.LullabyDao
import com.naptune.lullabyandstory.data.local.dao.StoryDao
import com.naptune.lullabyandstory.data.local.dao.StoryAudioLanguageDao
import com.naptune.lullabyandstory.data.local.dao.StoryDescriptionTranslationDao
import com.naptune.lullabyandstory.data.local.dao.StoryNameTranslationDao
import com.naptune.lullabyandstory.data.local.dao.LullabyTranslationDao
import com.naptune.lullabyandstory.data.local.database.AppDatabase
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lullaby_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideLullabyDao(database: AppDatabase): LullabyDao {
        return database.lullabyDao()
    }

    @Provides
    fun provideStoryDao(database: AppDatabase): StoryDao {
        return database.storyDao()
    }

    @Provides
    fun provideStoryAudioLanguageDao(database: AppDatabase): StoryAudioLanguageDao {
        return database.storyAudioLanguageDao()
    }

    @Provides
    fun provideStoryDescriptionTranslationDao(database: AppDatabase): StoryDescriptionTranslationDao {
        return database.storyDescriptionTranslationDao()
    }

    @Provides
    fun provideStoryNameTranslationDao(database: AppDatabase): StoryNameTranslationDao {
        return database.storyNameTranslationDao()
    }

    @Provides
    fun provideTranslationDao(database: AppDatabase): LullabyTranslationDao {
        return database.translationDao()
    }

    @Provides
    fun provideFavouriteMetadataDao(database: AppDatabase): FavouriteMetadataDao {
        return database.favouriteMetadataDao()
    }

/*
    @Provides
    @Singleton
    fun provideLocalDataSource(
        lullabyDao: LullabyDao,
        storyDao: StoryDao,
        storyAudioLanguageDao: StoryAudioLanguageDao,
        storyDescriptionTranslationDao: StoryDescriptionTranslationDao,
        storyNameTranslationDao: StoryNameTranslationDao,
        translationDao: LullabyTranslationDao,
        favouriteMetadataDao: FavouriteMetadataDao
    ): LullabyLocalDataSourceImpl {
        return LullabyLocalDataSourceImpl(lullabyDao, storyDao, storyAudioLanguageDao, storyDescriptionTranslationDao, storyNameTranslationDao, translationDao, favouriteMetadataDao)
    }
*/

    @Provides
    @Singleton
    fun provideAppPreferences(
        @ApplicationContext context: Context
    ): AppPreferences {
        return AppPreferences(context)
    }
}