package com.naptune.lullabyandstory.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.naptune.lullabyandstory.data.local.dao.FavouriteMetadataDao
import com.naptune.lullabyandstory.data.local.dao.LullabyDao
import com.naptune.lullabyandstory.data.local.dao.StoryDao
import com.naptune.lullabyandstory.data.local.dao.StoryAudioLanguageDao
import com.naptune.lullabyandstory.data.local.dao.StoryDescriptionTranslationDao
import com.naptune.lullabyandstory.data.local.dao.StoryNameTranslationDao
import com.naptune.lullabyandstory.data.local.dao.LullabyTranslationDao
import com.naptune.lullabyandstory.data.local.entity.FavouriteMetadataEntity
import com.naptune.lullabyandstory.data.local.entity.LullabyLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryAudioLanguageLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryDescriptionTranslationLocalEntity
import com.naptune.lullabyandstory.data.local.entity.StoryNameTranslationLocalEntity
import com.naptune.lullabyandstory.data.local.entity.TranslationLocalEntity

@Database(
    entities = [
        LullabyLocalEntity::class,
        StoryLocalEntity::class,
        StoryAudioLanguageLocalEntity::class,
        StoryDescriptionTranslationLocalEntity::class,
        StoryNameTranslationLocalEntity::class,
        TranslationLocalEntity::class,
        FavouriteMetadataEntity::class
    ],
    version = 6, // ✅ Version increment for new FavouriteMetadataEntity (LIFO ordering support)
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lullabyDao(): LullabyDao
    abstract fun storyDao(): StoryDao
    abstract fun storyAudioLanguageDao(): StoryAudioLanguageDao
    abstract fun storyDescriptionTranslationDao(): StoryDescriptionTranslationDao
    abstract fun storyNameTranslationDao(): StoryNameTranslationDao
    abstract fun translationDao(): LullabyTranslationDao
    abstract fun favouriteMetadataDao(): FavouriteMetadataDao

}