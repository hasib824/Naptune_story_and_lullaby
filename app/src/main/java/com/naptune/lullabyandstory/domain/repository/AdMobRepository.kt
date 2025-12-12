package com.naptune.lullabyandstory.domain.repository

import android.app.Activity
import com.naptune.lullabyandstory.domain.model.AdLoadResult
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.domain.model.RewardedAdLoadResult
import com.naptune.lullabyandstory.domain.model.RewardedAdShowResult
import kotlinx.coroutines.flow.Flow

interface AdMobRepository {
    
    suspend fun initializeMobileAds()
    
    suspend fun loadBannerAd(
        adUnitId: String,
        adSizeType: AdSizeType
    ): Flow<AdLoadResult>
    
    suspend fun destroyBannerAd(adUnitId: String)
    
    suspend fun loadRewardedAd(adUnitId: String): Flow<RewardedAdLoadResult>
    
    suspend fun showRewardedAd(
        adUnitId: String,
        activity: Activity
    ): Flow<RewardedAdShowResult>
    
    fun isRewardedAdLoaded(adUnitId: String): Boolean
    
    fun isAdMobInitialized(): Boolean
}