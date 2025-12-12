package com.naptune.lullabyandstory.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.naptune.lullabyandstory.data.appwrite.AppwriteBaseClient
import com.naptune.lullabyandstory.data.repository.AppInitializationRepository
import com.naptune.lullabyandstory.data.datastore.TimerPreferences
import com.naptune.lullabyandstory.domain.manager.LanguageStateManager
import com.naptune.lullabyandstory.utils.LanguageManager
import com.naptune.lullabyandstory.presentation.player.timermodal.operations.TimerAlarmManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.appwrite.Client
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideAppwriteBaseClient(
        @ApplicationContext context: Context
    ): AppwriteBaseClient {
        return AppwriteBaseClient(context)
    }

    @Provides
    @Singleton
    fun provideAppInitializationRepository(): AppInitializationRepository {
        return AppInitializationRepository()
    }



    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideLanguageStateManager(
        languageManager: LanguageManager
    ): LanguageStateManager {
        return LanguageStateManager(languageManager)
    }

    @Provides
    @Singleton
    fun provideTimerPreferences(
        @ApplicationContext context: Context
    ): TimerPreferences {
        return TimerPreferences(context)
    }

    @Provides
    @Singleton
    fun provideTimerAlarmManager(
        @ApplicationContext context: Context,
        timerPreferences: TimerPreferences
    ): TimerAlarmManager {
        return TimerAlarmManager(context, timerPreferences)
    }
}
