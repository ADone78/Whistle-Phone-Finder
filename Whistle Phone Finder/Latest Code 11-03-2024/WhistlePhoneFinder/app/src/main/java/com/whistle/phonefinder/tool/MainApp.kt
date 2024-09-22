package com.whistle.phonefinder.tool

import android.app.Activity
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.MobileAds
import com.whistle.phonefinder.tool.billing.InAppPurchase
import com.whistle.phonefinder.tool.common_ads.isShowingAppOpenAd
import com.whistle.phonefinder.tool.whistle.whistleModule

import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
class MainApp : Application(), Application.ActivityLifecycleCallbacks {

    lateinit var appOpenManager: AppOpenManager
    private lateinit var diff: DefaultLifecycleObserver
    private var currentActivity: Activity? = null
    private lateinit var _inAppPurchase: InAppPurchase
    val inAppPurchase: InAppPurchase get() = _inAppPurchase
    private var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager? = null

    override fun onCreate() {
        super.onCreate()
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(this)
        startKoin {
            // Context
            androidContext(this@MainApp)
            modules(
                whistleModule
            )
        }
        MobileAds.initialize(this) { initStatus ->
            Log.d(ContentValues.TAG, "onCreate: $initStatus")
        }
        registerActivityLifecycleCallbacks(this)
        appOpenManager = AppOpenManager(this)
        diff = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                try {
                    Thread.sleep(80)
                } catch (e: java.lang.Exception) {
                }
                currentActivity?.let {
                    // Toast.makeText(this@MainApp, "called", Toast.LENGTH_SHORT).show()
                    if (googleMobileAdsConsentManager!!.canRequestAds)
                    {
                        appOpenManager.showAdIfAvailable(currentActivity!!,false)
                    }
                }
            }
           /* override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                currentActivity?.let {
                    // Toast.makeText(this@MainApp, "called", Toast.LENGTH_SHORT).show()

                }
            }*/
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(diff)
        themeDetails()

        try {
            _inAppPurchase = InAppPurchase(this)
        } catch (e: Exception) {
            println(e.message)
        }
    }

    private fun themeDetails() {
        val sharedPreferences = getSharedPreferences("themeDetails", MODE_PRIVATE)
        val themeState = sharedPreferences.getString("ThemeState", "")
        if (themeState != "") {
            if (themeState == "true") {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            }

        }

    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        // Updating the currentActivity only when an ad is not showing.
        if (!isShowingAppOpenAd) {
            currentActivity = activity

        }
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}


}
val Context.inAppPurchase get() = (applicationContext as MainApp).inAppPurchase