package com.naptune.lullabyandstory.di

import com.google.firebase.messaging.FirebaseMessaging
import com.naptune.lullabyandstory.BuildConfig
import com.naptune.lullabyandstory.data.network.fcm.FcmApiService
import com.naptune.lullabyandstory.data.repository.fcm.FcmRepositoryImpl
import com.naptune.lullabyandstory.domain.repository.fcm.FcmRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Hilt Dependency Injection Module for FCM
 * Provides FCM-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object FcmModule {

    /**
     * Qualifier for FCM-specific Retrofit instance
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class FcmRetrofit

    /**
     * Qualifier for FCM-specific OkHttpClient
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class FcmOkHttpClient

    /**
     * Provide FirebaseMessaging instance
     */
    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    /**
     * Provide OkHttpClient for FCM API
     * Separate client with logging for FCM requests
     */
    @Provides
    @Singleton
    @FcmOkHttpClient
    fun provideFcmOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provide Retrofit instance for FCM API
     * Base URL: https://notifier.appswave.xyz/
     */
    @Provides
    @Singleton
    @FcmRetrofit
    fun provideFcmRetrofit(
        @FcmOkHttpClient okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.FCM_SERVER_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provide FcmApiService
     */
    @Provides
    @Singleton
    fun provideFcmApiService(
        @FcmRetrofit retrofit: Retrofit
    ): FcmApiService {
        return retrofit.create(FcmApiService::class.java)
    }

    /**
     * Provide FcmRepository implementation
     * Binds FcmRepositoryImpl to FcmRepository interface
     */
    @Provides
    @Singleton
    fun provideFcmRepository(
        fcmRepositoryImpl: FcmRepositoryImpl
    ): FcmRepository {
        return fcmRepositoryImpl
    }
}
