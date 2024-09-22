package com.whistle.phonefinder.tool

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.whistle.phonefinder.tool.common_ads.appOpenAd
import com.whistle.phonefinder.tool.common_ads.isAppOpenAdDismissed
import com.whistle.phonefinder.tool.common_ads.isLoadingAppOpenAd
import com.whistle.phonefinder.tool.common_ads.isShowingAppOpenAd
import com.whistle.phonefinder.tool.common_ads.isShowingAppOpenAdDismissed
import com.whistle.phonefinder.tool.common_ads.isShowingAppOpenAdStatus
import com.whistle.phonefinder.tool.common_ads.loadTimeAppOpenAd
import java.util.*

class AppOpenManager(
    var context: Context,
    var AD_UNIT_IDAppOpen: String = "",
    var LOG_TAGAppOpen: String = "AppOpenAd",
) {
    var PERMISSION_CODE = 123
    var PERMISSIONS =
    arrayOf(
        Manifest.permission.RECORD_AUDIO
    )
    var PERMISSIONSNotification = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS

    )

    init {
        MobileAds.initialize(context) {}
        AD_UNIT_IDAppOpen =
            context.resources.getString(
                R.string.admobAppId
            )
    }
    private var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager? = null
    init {
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context)
    }

    /* APP OPEN ADD*/
    fun loadAppOpenAd(context: Context) {

        if (googleMobileAdsConsentManager!!.canRequestAds) {
            //Toast.makeText(context, googleMobileAdsConsentManager!!.canRequestAds.toString(), Toast.LENGTH_SHORT).show()
            //Toast.makeText(context, "loading request", Toast.LENGTH_SHORT).show()
            if (isLoadingAppOpenAd || isAppOpenAdAvailable()) {
                return
            }

            isLoadingAppOpenAd = true
            val request = AdRequest.Builder().build()
            AppOpenAd.load(context,
                context.resources.getString(
                    R.string.appOpenId
                ),
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {

                    override fun onAdLoaded(ad: AppOpenAd) {
                        // Called when an app open ad has loaded.
                        Log.d(LOG_TAGAppOpen, "Ad was loaded.")
                        appOpenAd = ad
                        isLoadingAppOpenAd = false
                        loadTimeAppOpenAd = Date().time

                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Called when an app open ad has failed to load.
                        Log.d(LOG_TAGAppOpen, "AppOpen: ${loadAdError.message}")
                        isLoadingAppOpenAd = false;
                    }
                })
        }
        else
        {
            //Toast.makeText(context, "not loading app open", Toast.LENGTH_SHORT).show()
        }
    }

    fun isAppOpenAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTimeAppOpenAd
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    //Shows the ad if one isn't already showing. /
    fun showAdIfAvailable(
        activity: Activity,isFirst: Boolean
    ) {
        if (googleMobileAdsConsentManager!!.canRequestAds) {
        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAppOpenAd) {
            Log.d(LOG_TAGAppOpen, "The app open ad is already showing.")
            //  toast.toastDefine("app open is already   showing")
            isShowingAppOpenAdDismissed = false
            return
        }

        // If the app open ad is not available yet, invoke the callback then load the ad.
        // toast.toastDefine(isAppOpenAdAvailable().toString())
        isShowingAppOpenAdStatus = isAppOpenAdAvailable()
        if (!isAppOpenAdAvailable()) {
            Log.d(LOG_TAGAppOpen, "The app open ad is not ready yet.")
            // onShowAdCompleteListener.onShowAdComplete()
            //  toast.toastDefine("app open is not ready to  showing")
            loadAppOpenAd(activity)
            isShowingAppOpenAdDismissed = true
            return
        } else {
            isShowingAppOpenAdDismissed = false

        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdDismissedFullScreenContent() {
                // Called when full screen content is dismissed.
                // Set the reference to null so isAdAvailable() returns false.
                Log.d(LOG_TAGAppOpen, "Ad dismissed fullscreen content.")
                appOpenAd = null
                isShowingAppOpenAd = false
                isAppOpenAdDismissed = true
                isShowingAppOpenAdDismissed = true

                loadAppOpenAd(activity)

                //toast.toastDefine("app open dismissed to  showing")
                try {
                    if (isFirst) {
                        if (hasRecordingPermissions(context,*PERMISSIONS,*PERMISSIONSNotification))
                        {
                            if (checkSystemWritePermission()) {

                                val intent = Intent(context, MainActivity::class.java)
                                context.startActivity(intent)
                                //finish()
                            }
                        }
                        else
                        {
                            val intent = Intent(context, Permission_activity::class.java)
                            context.startActivity(intent)
                            //finish()
                        }
                       /* val i = Intent(
                            context, MainActivity::class.java
                        )
                        context.startActivity(i)*/
                    }


                } catch (e: Exception) {
                    println(e.toString())
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when fullscreen content failed to show.
                // Set the reference to null so isAdAvailable() returns false.
                Log.d(LOG_TAGAppOpen, adError.message)
                appOpenAd = null
                isShowingAppOpenAd = false
                Log.d(LOG_TAGAppOpen, "onAdFailedToShowFullScreenContent")
                //   onShowAdCompleteListener.onShowAdComplete()
                loadAppOpenAd(activity)
                isAppOpenAdDismissed = true
                isShowingAppOpenAdDismissed = true

            }

            override fun onAdShowedFullScreenContent() {
                // Called when fullscreen content is shown.
                isShowingAppOpenAd = true
                isShowingAppOpenAdDismissed = false

                Log.d(LOG_TAGAppOpen, "Ad showed fullscreen content.")
                //  toast.toastDefine("app open showing")

            }
        }

        appOpenAd?.show(activity)}
    }

    private fun checkSystemWritePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(context)
        }
        return false
    }

    private fun hasRecordingPermissions(
        context: Context?,
        vararg permissions: String?,
    ): Boolean {
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission!!
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

   /* override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {

        if (requestCode == PERMISSION_CODE) {
            if (mediaRecorderPermissionsGranted(grantResults)) {

                if (hasRecordingPermissions(context,*PERMISSIONS,*PERMISSIONSNotification))
                {
                    val intent = Intent(context, MainActivity::class.java)
                   context.startActivity(intent)
                    //finish()
                }
                else
                {
                    val intent = Intent(context, Permission_activity::class.java)
                    context.startActivity(intent)
                   // finish()
                }
            } else {
                Toast.makeText(
                    context,
                    "permissionsNotGranted",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }*/
    private fun mediaRecorderPermissionsGranted(grantResults: IntArray) = grantResults.all {
        it == PackageManager.PERMISSION_GRANTED
    }
}