package com.naptune.lullabyandstory.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class BannerAdDomainModel(
    val adUnitId: String,
    val adSize: AdSize,
    val isLoaded: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

@Immutable
data class RewardedAdDomainModel(
    val adUnitId: String,
    val isLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val reward: RewardDomainModel? = null
)

@Immutable
data class RewardDomainModel(
    val type: String,
    val amount: Int
)

@Immutable
data class AdSize(
    val width: Int,
    val height: Int,
    val type: AdSizeType
)

enum class AdSizeType {
    BANNER,
    LARGE_BANNER,
    SMART_BANNER,
    ADAPTIVE_BANNER,
    ANCHORED_ADAPTIVE_BANNER,
    INLINE_ADAPTIVE_BANNER,
    MEDIUM_RECTANGLE

}

sealed class AdLoadResult {
    object Loading : AdLoadResult()
    data class Success(val bannerAd: BannerAdDomainModel) : AdLoadResult()
    data class Error(val message: String) : AdLoadResult()
}

sealed class RewardedAdLoadResult {
    object Loading : RewardedAdLoadResult()
    data class Success(val rewardedAd: RewardedAdDomainModel) : RewardedAdLoadResult()
    data class Error(val message: String) : RewardedAdLoadResult()
}

sealed class RewardedAdShowResult {
    object Loading : RewardedAdShowResult()
    data class Success(val reward: RewardDomainModel) : RewardedAdShowResult()
    data class Dismissed(val reason: String) : RewardedAdShowResult()
    data class Error(val message: String) : RewardedAdShowResult()
}