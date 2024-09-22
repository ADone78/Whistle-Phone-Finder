package com.whistle.phonefinder.tool

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.Gravity.RIGHT
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class Splash_activity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var pbProcessing: ProgressBar
    var handler: Handler? = null
    var PERMISSION_CODE = 123
    private lateinit var appOpenManager: AppOpenManager

    private var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager? = null
    var PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )
    var PERMISSIONSNotification = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS

    )

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        gMACManager()
        window.statusBarColor = ContextCompat.getColor(this, R.color.statusbar_color)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.statusbar_color)
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT
        )
        common_ads.isShowingAppOpenAd = false
        common_ads.appOpenAd=null
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(this)
        val sharedpreferences = getSharedPreferences("sharedprefs", MODE_PRIVATE)
        val chkk = sharedpreferences.getBoolean("isCheck", false)
        button = findViewById(R.id.getstarted)
        button.visibility=View.GONE
        pbProcessing = findViewById(R.id.pbProcessing)
        getConsentStatus()
        if (currentAppLanguage(this) == "Arabic") {
            button.setBackgroundResource(R.drawable.get_started_ar)
            button.gravity = Gravity.CENTER or Gravity.LEFT;
            button.setPadding(
                resources.getDimension(R.dimen.leftsplash).toInt(), 0, 0, 0
            );
        } else if (currentAppLanguage(this) == "Persian") {
            button.setBackgroundResource(R.drawable.get_started_ar)
            button.gravity = Gravity.CENTER or Gravity.LEFT;
            button.setPadding(
                resources.getDimension(R.dimen.leftsplash).toInt(), 0, 0, 0
            );
        } else {
            button.gravity = Gravity.CENTER or RIGHT;
            button.setPadding(
                0, 0, resources.getDimension(R.dimen.rightsplash).toInt(), 0
            );
        }

        button.setOnClickListener {
            if (chkk !== true) {
                button.visibility = View.GONE
                showPrivacyDialog()
            } else {
                appOpenManager = AppOpenManager(this)
                if (appOpenManager.isAppOpenAdAvailable()) {
                    appOpenManager.showAdIfAvailable(this@Splash_activity, true)
                } else {
                    if (hasRecordingPermissions(this, *PERMISSIONS, *PERMISSIONSNotification)) {
                        if (checkSystemWritePermission()) {

                            val intent = Intent(this@Splash_activity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        val intent = Intent(this@Splash_activity, Permission_activity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private lateinit var consentInformation: ConsentInformation
    private lateinit var consentForm: ConsentForm
    lateinit var params: ConsentRequestParameters
    private fun getConsentStatus() {
        if ((isWifiStatus() || isMobileData()))
        {
            val debugSettings = ConsentDebugSettings.Builder(this).setDebugGeography(
                ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
            ).build()
            params =
                ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()
            consentInformation = UserMessagingPlatform.getConsentInformation(this)
            consentInformation.requestConsentInfoUpdate(this, params, {
                if (consentInformation.isConsentFormAvailable) {
                    //   Toast.makeText(this, "loading form", Toast.LENGTH_SHORT).show()
                    // showToast("loading form")
                    pbProcessing.isIndeterminate = true

                    loadForm()
                    Thread {
                        var i = 0
                        while (i != -1) {
                            i += 1
                            try {
                                Thread.sleep(100)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }

                            if (googleMobileAdsConsentManager!!.canRequestAds) {

                                runOnUiThread {

                                    val sharedpreferences =
                                        getSharedPreferences("sharedprefs", MODE_PRIVATE)
                                    val chkk = sharedpreferences.getBoolean("isCheck", false)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        //Do something after 100ms
                                        try {
                                            appOpenManager = AppOpenManager(this)
                                            if (googleMobileAdsConsentManager!!.canRequestAds) {
                                                appOpenManager.loadAppOpenAd(this@Splash_activity)
                                            }
                                            //  showPrivacyDialog()
                                        } catch (e: Exception) {
                                            println(e.toString())
                                        }
                                    }, 1000)
                                    if (chkk !== true) {
                                        button.visibility = View.GONE
                                    }
                                    pbProcessing.isIndeterminate = false

                                    var count = 0
                                    object : CountDownTimer(6700, 60) {
                                        // Callback function, fired on regular interval
                                        override fun onTick(millisUntilFinished: Long) {
                                            count += 1
                                            pbProcessing.progress = count
                                        }

                                        override fun onFinish() {
                                            pbProcessing.visibility = View.GONE
                                            button.visibility = View.VISIBLE
                                            // binding.animationView.cancelAnimation()
                                            slideUp(button)
                                            // loadInterstailAd()

                                        }
                                    }.start()
                                }
                                i = -1
                            }
                        }
                    }.start()
                } else {
                    // Toast.makeText(this, "form not available", Toast.LENGTH_SHORT).show()
                    // showMessege("form not available")
                    pbProcessing.isIndeterminate = false
                    var count = 20
                    object : CountDownTimer(6700, 60) {
                        // Callback function, fired on regular interval
                        override fun onTick(millisUntilFinished: Long) {
                            count += 1
                            pbProcessing.progress = count
                        }

                        override fun onFinish() {
                            pbProcessing.visibility = View.GONE
                            button.visibility = View.VISIBLE
                            // binding.animationView.cancelAnimation()
                            slideUp(button)
                        }
                    }.start()
                    Handler(Looper.getMainLooper()).postDelayed({
                        // loadInterstailAd()
                        val sharedpreferences = getSharedPreferences("sharedprefs", MODE_PRIVATE)
                        val chkk = sharedpreferences.getBoolean("isCheck", false)
                        Handler(Looper.getMainLooper()).postDelayed({
                            //Do something after 100ms
                            try {
                                appOpenManager = AppOpenManager(this)
                                if (googleMobileAdsConsentManager!!.canRequestAds) {
                                    appOpenManager.loadAppOpenAd(this@Splash_activity)
                                }
                                //showPrivacyDialog()
                            } catch (e: Exception) {
                                println(e.toString())
                            }
                        }, 1000)
                        if (chkk !== true) {
                            button.visibility = View.GONE
                        }
                    }, 4000)
                }
            }, {
                // Handle the error.
                //  showToast("checking form availability error")
            })
        } else {
            //   Toast.makeText(this, "No conscent", Toast.LENGTH_SHORT).show()
            pbProcessing.isIndeterminate = false
            var count = 0
            object : CountDownTimer(6700, 70) {
                // Callback function, fired on regular interval
                override fun onTick(millisUntilFinished: Long) {
                    count += 1
                    pbProcessing.progress = count
                }

                override fun onFinish() {
                    pbProcessing.visibility = View.GONE
                    button.visibility = View.VISIBLE
                    // binding.animationView.cancelAnimation()
                    slideUp(button)
                }
            }.start()
        }
    }

    private fun slideUp(view: View) {
        ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 300f, 0f).apply {
            duration = 4000
            start()
        }.doOnEnd {}
    }

    private fun loadForm() {
        // Loads a consent form. Must be called on the main thread.
        UserMessagingPlatform.loadConsentForm(this, {
            consentForm = it
            if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                consentForm.show(this, ConsentForm.OnConsentFormDismissedListener {
                    if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                        // App can start requesting ads.
                    }
                    loadForm()
                })
            }
        }, {
            // Handle the error.
        })
    }

    @SuppressLint("RestrictedApi")
    private fun gMACManager() {
        googleMobileAdsConsentManager =
            GoogleMobileAdsConsentManager.getInstance(applicationContext)
        googleMobileAdsConsentManager!!.gatherConsent(this) { consentError ->
            if (consentError != null) {
                // Consent not obtained in current session.
                Log.w(
                    PackageManagerCompat.LOG_TAG,
                    String.format("%s: %s", consentError.errorCode, consentError.message)
                )
            }
            Log.d(
                PackageManagerCompat.LOG_TAG,
                googleMobileAdsConsentManager!!.canRequestAds.toString()
            )
            // showMessege(googleMobileAdsConsentManager!!.canRequestAds.toString())
            if (googleMobileAdsConsentManager!!.canRequestAds) {
                MobileAds.initialize(this) {}
            }
        }
    }

    fun isWifiStatus(): Boolean {
        val DEBUG_TAG = "NetworkStatusExample"

        val connMgr: ConnectivityManager? =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        var isWifiConn = false
        var isMobileConn = false
        for (network in connMgr!!.getAllNetworks()) {
            val networkInfo: NetworkInfo? = connMgr.getNetworkInfo(network)
            if (networkInfo!!.type == ConnectivityManager.TYPE_WIFI) {
                isWifiConn = isWifiConn or networkInfo.isConnected()
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                isMobileConn = isMobileConn or networkInfo.isConnected()
            }
        }
        Log.d(DEBUG_TAG, "Wifi connected: $isWifiConn")
        Log.d(DEBUG_TAG, "Mobile connected: $isMobileConn")
        return isWifiConn
    }

    fun isMobileData(): Boolean {

        val DEBUG_TAG = "NetworkStatusExample"
        val connMgr: ConnectivityManager? =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        var isWifiConn = false
        var isMobileConn = false
        for (network in connMgr!!.getAllNetworks()) {
            val networkInfo: NetworkInfo? = connMgr.getNetworkInfo(network)
            if (networkInfo!!.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiConn = isWifiConn or networkInfo.isConnected()
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                isMobileConn = isMobileConn or networkInfo.isConnected()
            }
        }
        Log.d(DEBUG_TAG, "Wifi connected: $isWifiConn")
        Log.d(DEBUG_TAG, "Mobile connected: $isMobileConn")
        return isMobileConn
    }

    private fun showPrivacyDialog() {
        val builder = AlertDialog.Builder(this)
        val layoutInflater = LayoutInflater.from(this@Splash_activity)
        val viewDialog: View = layoutInflater.inflate(R.layout.activity_privacy, null)
        builder.setView(viewDialog)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
        val btn_save = viewDialog.findViewById<Button>(R.id.btn_save)
        btn_save.setOnClickListener {
            val sharedpreferences = getSharedPreferences("sharedprefs", MODE_PRIVATE)
            val editor = sharedpreferences.edit()
            editor.apply {
                putBoolean("isCheck", true)
                alertDialog.dismiss()
                if (appOpenManager.isAppOpenAdAvailable()) {
                    appOpenManager.showAdIfAvailable(this@Splash_activity, true)
                } else {
                    if (hasRecordingPermissions(
                            this@Splash_activity, *PERMISSIONS, *PERMISSIONSNotification
                        )
                    ) {
                        if (checkSystemWritePermission()) {

                            val intent = Intent(this@Splash_activity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        val intent = Intent(this@Splash_activity, Permission_activity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }.apply()
        }
    }

    private fun checkSystemWritePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(this)
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
                        context, permission!!
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                   // Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {

        if (requestCode == PERMISSION_CODE) {
            if (mediaRecorderPermissionsGranted(grantResults)) {

                if (hasRecordingPermissions(this, *PERMISSIONS, *PERMISSIONSNotification)) {
                    val intent = Intent(this@Splash_activity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@Splash_activity, Permission_activity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(
                    this, "permissionsNotGranted", Toast.LENGTH_LONG
                ).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun mediaRecorderPermissionsGranted(grantResults: IntArray) = grantResults.all {
        it == PackageManager.PERMISSION_GRANTED
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LanguageChanged.wrapContext(base!!))
    }

}