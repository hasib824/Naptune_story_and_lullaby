package com.naptune.lullabyandstory.data.repository

import android.app.Activity
import android.util.Log
import com.naptune.lullabyandstory.data.network.admob.AdMobDataSource
import com.naptune.lullabyandstory.domain.model.AdLoadResult
import com.naptune.lullabyandstory.domain.model.AdSizeType
import com.naptune.lullabyandstory.domain.model.RewardedAdLoadResult
import com.naptune.lullabyandstory.domain.model.RewardedAdShowResult
import com.naptune.lullabyandstory.domain.repository.AdMobRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobRepositoryImpl @Inject constructor(
    private val adMobDataSource: AdMobDataSource
) : AdMobRepository {
    
    companion object {
        private const val TAG = "AdMobRepositoryImpl"
    }
    
    override suspend fun initializeMobileAds() {
        Log.d(TAG, "🚀 Initializing Mobile Ads through repository...")
        adMobDataSource.initializeMobileAds()
    }
    
    override suspend fun loadBannerAd(
        adUnitId: String,
        adSizeType: AdSizeType
    ): Flow<AdLoadResult> = flow {
        Log.d(TAG, "📢 Loading banner ad through repository - Unit: $adUnitId, Size: $adSizeType")
         emitAll(adMobDataSource.loadBannerAd(adUnitId, adSizeType))
    }
    
    override suspend fun destroyBannerAd(adUnitId: String) {
        Log.d(TAG, "🗑️ Destroying banner ad through repository - Unit: $adUnitId")
        adMobDataSource.destroyBannerAd(adUnitId)
    }
    
    override suspend fun loadRewardedAd(adUnitId: String): Flow<RewardedAdLoadResult> {
        Log.d(TAG, "🎁 Loading rewarded ad through repository - Unit: $adUnitId")
        return adMobDataSource.loadRewardedAd(adUnitId)
    }
    
    override suspend fun showRewardedAd(
        adUnitId: String,
        activity: Activity
    ): Flow<RewardedAdShowResult> {
        Log.d(TAG, "🎬 Showing rewarded ad through repository - Unit: $adUnitId")
        return adMobDataSource.showRewardedAd(adUnitId, activity)
    }
    
    override fun isRewardedAdLoaded(adUnitId: String): Boolean {
        return adMobDataSource.isRewardedAdLoaded(adUnitId)
    }
    
    override fun isAdMobInitialized(): Boolean {
        return adMobDataSource.isAdMobInitialized()
    }
}