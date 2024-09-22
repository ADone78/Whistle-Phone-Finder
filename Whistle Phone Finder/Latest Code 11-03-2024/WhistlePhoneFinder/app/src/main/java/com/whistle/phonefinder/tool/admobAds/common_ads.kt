package com.whistle.phonefinder.tool


import androidx.annotation.Keep
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd

@Keep
object common_ads {
    //Ads
    var mRewardedAd: RewardedAd? = null
    var appOpenAd: AppOpenAd? = null
    var isLoadingAppOpenAd = false
    var isShowingAppOpenAd = false
    var isShowingAppOpenAdDismissed = false
    var isShowingAppOpenAdStatus = false
    var loadTimeAppOpenAd: Long = 0
    var isAppOpenAdDismissed: Boolean = false
    var isInterstitialAdLoaded: Boolean = false
     var rewardedInterstitialAd: RewardedInterstitialAd? = null
}