package com.whistle.phonefinder.tool

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.whistle.phonefinder.tool.Common.Add
import com.whistle.phonefinder.tool.Common.isExitBannerLoaded
import com.whistle.phonefinder.tool.Common.isMediaPlaying
import com.whistle.phonefinder.tool.Common.isPermissionok
import com.whistle.phonefinder.tool.Common.isSkip
import com.whistle.phonefinder.tool.Common.isSmartMode
import com.whistle.phonefinder.tool.Common.isSound1
import com.whistle.phonefinder.tool.Common.isSound2
import com.whistle.phonefinder.tool.Common.isSound3
import com.whistle.phonefinder.tool.Common.isVibration
import com.whistle.phonefinder.tool.Common.iscontinuousFlash
import com.whistle.phonefinder.tool.Common.iscontinuousVibration
import com.whistle.phonefinder.tool.Common.isheartBeatVibration
import com.whistle.phonefinder.tool.Common.isswitchFlash
import com.whistle.phonefinder.tool.Common.istoggleFlash
import com.whistle.phonefinder.tool.Common.mpbackground
import com.whistle.phonefinder.tool.Common.rewardearn
import com.whistle.phonefinder.tool.Common.ringsound
import com.whistle.phonefinder.tool.Common.ringtoneSound
import com.whistle.phonefinder.tool.common_ads.isShowingAppOpenAd
import com.whistle.phonefinder.tool.common_ads.rewardedInterstitialAd
import com.whistle.phonefinder.tool.databinding.ActivityMainBinding
import com.whistle.phonefinder.tool.databinding.DoneResultDialogBinding
import com.whistle.phonefinder.tool.databinding.ExitDialogBinding
import com.whistle.phonefinder.tool.databinding.ProgressLoadingDialogBinding
import com.whistle.phonefinder.tool.whistle.LanguageModel
import com.whistle.phonefinder.tool.whistle.WhistleBackgroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), OnUserEarnedRewardListener {
    private lateinit var exitDialogBinding: ExitDialogBinding
    private lateinit var exitDialog: AlertDialog
    lateinit var toast: Toast_Helper_Class
    lateinit var binding: ActivityMainBinding
    private lateinit var cameraId: String
    var vibrator: Vibrator? = null
    private var mRewardedAd: RewardedAd? = null
    private val TagRewarded = "Rewarded Ad"
    private lateinit var cameraManager: CameraManager
    private var langList: ArrayList<LanguageModel> = ArrayList()
    private lateinit var ContainerView: FrameLayout
    private final var TAG = "MainActivity"
    var PERMISSION_CODE = 123
    private var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager? = null
    var PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
        )
    } else {
        arrayOf(
            Manifest.permission.RECORD_AUDIO
        )
    }
    var check: Boolean = false
    private var mCountDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private lateinit var timerTextView: TextView
    private lateinit var skipButton: Button
    private lateinit var rewardedDialog: AlertDialog


    @SuppressLint("SuspiciousIndentation", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(this)
        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        window.statusBarColor = ContextCompat.getColor(this, R.color.statusbar_color)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.statusbar_color)
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        binding.drawerLayout.setDrawerListener(object : DrawerLayout.DrawerListener {
            private var last = 0f

            override fun onDrawerSlide(arg0: View, arg1: Float) {
                val opening = arg1 > last
                val closing = arg1 < last

                if (opening) {
                    Log.i("Drawer", "opening")
                } else if (closing) {
                    Log.i("Drawer", "closing")
                } else {
                    Log.i("Drawer", "doing nothing")
                }

                last = arg1
            }

            override fun onDrawerStateChanged(arg0: Int) {}
            override fun onDrawerOpened(arg0: View) {
                binding.adContainerView0.visibility=View.GONE
            }
            override fun onDrawerClosed(arg0: View) {
                binding.adContainerView0.visibility=View.VISIBLE
            }
        })

        Add++
        toast = Toast_Helper_Class(this)
        ContainerView = findViewById(R.id.adContainerView0)
        if (googleMobileAdsConsentManager!!.canRequestAds) {
            loadAdaptiveBanner(ContainerView)
            loadInterstailAd()
        }

        // Toast.makeText(this, googleMobileAdsConsentManager!!.canRequestAds.toString(), Toast.LENGTH_SHORT).show()
        if (Add == 1) {
            // binding.switchSmatmode.isClickable=false
            if (googleMobileAdsConsentManager!!.canRequestAds) {
                loadAd()
            }
        }

        val sharedpreferences = getSharedPreferences("sharedprefs", MODE_PRIVATE)
        val chkk = sharedpreferences.getBoolean("isCheck", false)
        if (chkk !== true) {
            showPrivacyDialog()
        }
        initializeExitDialog()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        val drawericon: ImageView = findViewById(R.id.drawer_icon)
        drawericon.setOnClickListener {
            binding.drawerLayout.open()
        }
        getFlashSharedpref()
        getVibrationSharedPref()
        getSmartSharedPref()
        getMainSharedPref()
        val sharedpreference = getSharedPreferences("adbtnprefs", AppCompatActivity.MODE_PRIVATE)
        check = sharedpreference.getBoolean("adprefs", false)
        interstitialSharedpref(false)
        if (getFlashSharedpref() == true) {
            binding.switchFlash.isChecked = true
        }
        if (getVibrationSharedPref() == true) {
            binding.switchVibration.isChecked = true
        }
        if (getSmartSharedPref() == true) {
            binding.switchSmatmode.isChecked = true
        }
        if (getMainSharedPref() == true) {
            binding.togglemain.isChecked = true
        }
        getinterstitialSharedpref()
        val btnState = binding.togglemain.isChecked
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        binding.seekbar.max = maxVolume
        val currVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        binding.seekbar.progress = currVolume
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0)
                Log.d("Numbers", seekBar.progress.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.switchFlash.setOnCheckedChangeListener { buttonView, isChecked ->
            if (binding.switchFlash.isChecked) {
                flashSharedpref(true)

            } else {
                flashSharedpref(false)

            }
        }
        binding.switchVibration.setOnCheckedChangeListener { buttonView, isChecked ->
            if (binding.switchVibration.isChecked) {
                vibrationSharedPref(true)
            } else {

                vibrator!!.cancel()
                vibrationSharedPref(false)
            }
        }
        binding.switchSmatmode.setOnCheckedChangeListener { buttonView, isChecked ->

            //  Toast.makeText(this, rewardedInterstitialAd.toString(), Toast.LENGTH_SHORT).show()
            if (binding.switchSmatmode.isChecked) {

                if (!isSkip) {
                    if (rewardedInterstitialAd != null) {
                        if (googleMobileAdsConsentManager!!.canRequestAds) {
                            showRewardedInterstitialDialogue()
                        }
                    }

                } else {
                    isSkip = false
                }

                isSmartMode = true
                smartSharedpref(true)
            } else {
                if (!isSkip) {
                    if (rewardedInterstitialAd != null) {
                        if (googleMobileAdsConsentManager!!.canRequestAds) {
                            showRewardedInterstitialDialogue()
                        }
                    }
                } else {
                    isSkip = false
                }
                isSmartMode = false
                smartSharedpref(false)
            }
        }
        binding.togglemain.setOnCheckedChangeListener { buttonView, isChecked ->
            try {
                if (!isMediaPlaying) {
                    if (binding.togglemain.isChecked) {
                        binding.togglemain.isEnabled = false
                        binding.togglemain.gravity = Gravity.CENTER;
                        if (currentAppLanguage(this) == "Arabic") {
                            binding.togglemain.setPadding(
                                resources.getDimension(R.dimen.left).toInt(),
                                0,
                                0,
                                0
                            );

                        } else if (currentAppLanguage(this) == "Persian") {
                            binding.togglemain.setPadding(
                                resources.getDimension(R.dimen.left).toInt(),
                                0,
                                0,
                                0
                            );
                        } else {
                            binding.togglemain.setPadding(
                                0,
                                0,
                                resources.getDimension(R.dimen.right).toInt(),
                                0
                            );
                        }

                        if (!hasRecordingPermissions(this@MainActivity, *PERMISSIONS)) {
                            checkForPermissions()
                        } else {
                            try {
                                showLoadingProgress(getString(R.string.loading))
                                delayHandler(1000) {
                                    val startIntent =
                                        Intent(
                                            this@MainActivity,
                                            WhistleBackgroundService::class.java
                                        )
                                    startIntent.action = "turnOn"
                                    startService(startIntent)
                                    mainSharedpref(true)
                                    /*  val sharedpreferences = getSharedPreferences("sharedprefs", MODE_PRIVATE)
                                      val editor = sharedpreferences.edit()
                                      editor.apply {
                                          putBoolean("StateCheck", true)
                                      }.apply()*/
                                    hideLoadingProgress()

                                    showDoneDialogue(
                                        getString(R.string.whistlePhoneFinderHaveBeenEnabled),
                                        this,
                                    ) {
                                        binding.togglemain.isEnabled = true
                                        loadInterstailAd()
                                        //   Toast.makeText(this, "done", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                println(e.toString())
                            }


                        }
                    } else {
                        if (!hasRecordingPermissions(this@MainActivity, *PERMISSIONS)) {
                            checkForPermissions()
                        } else {
                            try {
                                mpbackground?.isLooping = false
                                mpbackground?.stop()
                                ringtoneSound = false
                                ringsound?.stop()
                                binding.togglemain.gravity = Gravity.CENTER;

                                if (currentAppLanguage(this) == "Arabic") {
                                    binding.togglemain.setPadding(
                                        0,
                                        0,
                                        resources.getDimension(R.dimen.right).toInt(),
                                        0
                                    );

                                } else if (currentAppLanguage(this) == "Persian") {
                                    binding.togglemain.setPadding(
                                        0,
                                        0,
                                        resources.getDimension(R.dimen.right).toInt(),
                                        0
                                    );
                                } else {

                                    binding.togglemain.setPadding(
                                        resources.getDimension(R.dimen.left).toInt(),
                                        0,
                                        0,
                                        0
                                    );
                                }

                                //binding.togglemain.resources.getDimension(R.dimen.left)
                                mainSharedpref(false)
                                Thread.sleep(500)
                                val startIntent =
                                    Intent(this@MainActivity, WhistleBackgroundService::class.java)
                                startIntent.action = "turnOff"
                                startService(startIntent)

                            } catch (e: Exception) {
                                println(e.toString())
                            }

                        }
                    }
                } else {
                    binding.togglemain.isChecked = btnState
                }
            } catch (e: Exception) {
                println(e.toString())
            }

        }
        setUpDrawerControls()
       // getConsentStatus()

        if (currentAppLanguage(this) == "Arabic") {
            binding.togglemain.setBackgroundResource(R.drawable.switch_state_ar)
            binding.togglemain.gravity = Gravity.CENTER;
            binding.togglemain.setPadding(
                0,
                0,
                resources.getDimension(R.dimen.left).toInt(),
                0
            );
            binding.drawerView.ringtone.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ring_arrow_ar,
                0,
                R.drawable.ringtone_drawer_icon,
                0
            )
            binding.drawerView.flashlight.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.flash_arrow_ar,
                0,
                R.drawable.flashlight_icon,
                0
            )
            binding.drawerView.settings.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.setting_arrow_ar,
                0,
                R.drawable.setting_drawer_icon,
                0
            )
            binding.drawerView.vibration.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.vibration_arrow_ar,
                0,
                R.drawable.vibration_drawer_icon,
                0
            )
        } else if (currentAppLanguage(this) == "Persian") {
            binding.togglemain.gravity = Gravity.CENTER;
            binding.togglemain.setPadding(
                0,
                0,
                resources.getDimension(R.dimen.left).toInt(),
                0
            );
            binding.togglemain.setBackgroundResource(R.drawable.switch_state_ar)
            binding.drawerView.ringtone.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ring_arrow_ar,
                0,
                R.drawable.ringtone_drawer_icon,
                0
            )
            binding.drawerView.flashlight.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.flash_arrow_ar,
                0,
                R.drawable.flashlight_icon,
                0
            )
            binding.drawerView.settings.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.setting_arrow_ar,
                0,
                R.drawable.setting_drawer_icon,
                0
            )
            binding.drawerView.vibration.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.vibration_arrow_ar,
                0,
                R.drawable.vibration_drawer_icon,
                0
            )
        }

        if (currentAppLanguage(this) == "Czech") {
            binding.togglemain.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.czechSixe)
            )

        } else if (currentAppLanguage(this) == "Greek") {
            binding.togglemain.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.greek)
            )
        } else if (currentAppLanguage(this) == "Spanish") {
            binding.togglemain.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.spanish)
            )
        } else if (currentAppLanguage(this) == "Finish") {
            binding.togglemain.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.finish)
            )
        } else if (currentAppLanguage(this) == "French") {
            binding.togglemain.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.french)
            )
        } else if (currentAppLanguage(this) == "Italian") {
            binding.togglemain.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.italian)
            )
        } else if (currentAppLanguage(this) == "Malaysian") {
            binding.togglemain.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.malay)
            )
        } else if (currentAppLanguage(this) == "Polish") {
            binding.togglemain.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.polish)
            )
        } else if (currentAppLanguage(this) == "Portuguese") {
            binding.togglemain.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.portugese)
            )
        } else if (currentAppLanguage(this) == "Russian") {
            binding.togglemain.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.russian)
            )
        } else if (currentAppLanguage(this) == "Turkish") {
            binding.togglemain.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.turkish)
            )
        } else if (currentAppLanguage(this) == "Hungarian") {
            binding.togglemain.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.hungarian)
            )
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed() {
                if (isWifiStatus() || isMobileData()) {
                    Toast.makeText(this@MainActivity, isShowingAppOpenAd.toString(), Toast.LENGTH_SHORT).show()
                    if (isShowingAppOpenAd) {
                        //  Log.d("Appopen", isShowingAppOpenAd.toString())
                        binding.adContainerView0.visibility = View.GONE
                        Toast.makeText(this@MainActivity, "Visible", Toast.LENGTH_SHORT).show()
                    } else {
                        if (googleMobileAdsConsentManager!!.canRequestAds) {
                            loadAdaptiveBanner(binding.adContainerView0)
                        }
                    }
                } else {
                    binding.adContainerView0.visibility = View.GONE
                }
                // Toast.makeText(this, googleMobileAdsConsentManager!!.canRequestAds.toString(), Toast.LENGTH_SHORT).show()
                val rootView = findViewById<View>(android.R.id.content)
                val observer = rootView.viewTreeObserver
                observer.addOnPreDrawListener {
                    val heightDiff = rootView.rootView.height - rootView.height
                    if (heightDiff > dpToPx(this@MainActivity, 200)) {
                        // Keyboard is visible
                        // Toast.makeText(this@MainActivity, "Visible", Toast.LENGTH_SHORT).show()
                        binding.adContainerView0.visibility = View.GONE
                        // Do something when the keyboard is visible
                    } else {
                        /*binding.adContainerView0.visibility = View.VISIBLE*/
                        // Keyboard is not visible
                        // Do something when the keyboard is not visible
                    }
                    true
                }

            }

        })
    }

    override fun onBackPressed() {
        exitConfirmationLayout()
    }

    private fun loadAd() {
        if (googleMobileAdsConsentManager!!.canRequestAds) {
            RewardedInterstitialAd.load(this, getString(R.string.interstitialIdRewarded),
                AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        if (isWifiStatus() || isMobileData()) {
                            binding.adicon.visibility = View.VISIBLE
                        } else {
                            binding.adicon.visibility = View.INVISIBLE
                        }

                        binding.switchSmatmode.isClickable = true
                        rewardedInterstitialAd = ad
                        rewardedInterstitialAd?.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdClicked() {
                                    // Called when a click is recorded for an ad.
                                    Log.d(TAG, "Ad was clicked.")
                                }

                                override fun onAdDismissedFullScreenContent() {
                                    // Called when ad is dismissed.
                                    // Set the ad reference to null so you don't show the ad a second time.
                                    Log.d(TAG, "Ad dismissed fullscreen content.")
                                    rewardedInterstitialAd = null
                                    if (rewardearn) {
                                        rewardearn = false
                                    } else {
                                        loadAd()
                                        if (getSmartSharedPref() == true) {
                                            isSmartMode = false
                                            smartSharedpref(false)
                                            binding.switchSmatmode.isChecked = false
                                        } else {
                                            isSmartMode = true
                                            smartSharedpref(true)
                                            binding.switchSmatmode.isChecked = true
                                        }
                                    }
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    // Called when ad fails to show.
                                    Log.e(TAG, "Ad failed to show fullscreen content.")
                                    rewardedInterstitialAd = null
                                }

                                override fun onAdImpression() {
                                    // Called when an impression is recorded for an ad.
                                    Log.d(TAG, "Ad recorded an impression.")
                                }

                                override fun onAdShowedFullScreenContent() {
                                    // Called when ad is shown.
                                    Log.d(TAG, "Ad showed fullscreen content.")
                                }
                            }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        adError?.toString()?.let { Log.d(TAG, it) }

                        binding.adicon.visibility = View.INVISIBLE
                        rewardedInterstitialAd = null
                        Log.d("Rewardedinerstitial", adError.message)
                    }
                })
        }
    }

    private fun showRewardedInterstitialDialogue() {
        try {

            val factory = LayoutInflater.from(this)
            val mainDialogView: View = factory.inflate(R.layout.rewarded_interstitial_dialog, null)
            rewardedDialog = AlertDialog.Builder(this).create()
            rewardedDialog.window!!.setBackgroundDrawableResource(R.drawable.rounded_corner_bg)
            rewardedDialog.setView(mainDialogView)
            rewardedDialog.setCancelable(false)
            rewardedDialog.setCanceledOnTouchOutside(false)
            rewardedDialog.show()
            timerTextView = mainDialogView.findViewById<TextView>(R.id.start_timer_text_view)
            skipButton = mainDialogView.findViewById<Button>(R.id.skip_button)
            timerTextView.text = "5"
            startCountdown()
            skipButton.setOnClickListener {
                rewardedDialog.dismiss()
                stopCountdown()
                isSkip = true
                if (getSmartSharedPref() == true) {
                    isSmartMode = false
                    smartSharedpref(false)
                    binding.switchSmatmode.isChecked = false
                    //switchsmart.isChecked=false
                } else {
                    isSmartMode = true
                    smartSharedpref(true)
                    binding.switchSmatmode.isChecked = true
                    //switchsmart.isChecked=true
                }
                interstitialSharedpref(false)
            }

        } catch (e: Exception) {
            println(e.toString())
        }
    }

    private fun startCountdown() {
        if (isTimerRunning) return
        isTimerRunning = true
        mCountDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val time = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                timerTextView.text = "$time"

            }

            override fun onFinish() {
                timerTextView.text = "Skip ad ▸"
                isTimerRunning = false
                rewardedDialog.dismiss()
                interstitialSharedpref(true)
                rewardedInterstitialAd?.show(this@MainActivity, this@MainActivity)
            }
        }.start()
    }

    private fun stopCountdown() {
        if (mCountDownTimer != null) {
            mCountDownTimer?.cancel()
            timerTextView.text = "Skip ad ▸"
            isTimerRunning = false
        }
    }

    private fun loadAdaptiveBanner(adContainerView: FrameLayout) {
        if (googleMobileAdsConsentManager!!.canRequestAds) {
            var adSize: AdSize? = null
            adContainerView?.let {
                val activity = this as Activity
                val display = activity.windowManager.defaultDisplay
                val outMetrics = DisplayMetrics()
                display.getMetrics(outMetrics)

                val density = outMetrics.density

                var adWidthPixels = it.width.toFloat()
                if (adWidthPixels == 0f) {
                    adWidthPixels = outMetrics.widthPixels.toFloat()
                }

                val adWidth = (adWidthPixels / density).toInt()
                adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
            }
            val request: AdRequest = AdRequest.Builder().build()
            val adView = AdView(this)
            adView.setAdSize(adSize!!)
            adSize?.run {
                adView.adUnitId = this@MainActivity.resources.getString(R.string.bannerId)
                adView.loadAd(request)


            }
            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    adContainerView?.removeAllViews()
                    adContainerView?.addView(adView)
                    if (!exitDialog.isShowing) {
                        if (common_ads.isShowingAppOpenAd) {
                            adContainerView.visibility = View.GONE
                        } else {
                            if (googleMobileAdsConsentManager!!.canRequestAds) {
                                adContainerView.visibility = View.VISIBLE
                            }
                        }
                        //  adContainerView.visibility = View.VISIBLE
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adContainerView.visibility = View.GONE
                    Log.d("bannerload", adError.message)
                }

            }
        }
    }

    private lateinit var progressDialog: AlertDialog
    lateinit var progressLoadingView: ProgressLoadingDialogBinding
    fun showLoadingProgress(title: String) {
        hideLoadingProgress()
        progressLoadingView = ProgressLoadingDialogBinding.inflate(layoutInflater)
        progressLoadingView.tvTitleLoading.text = title

        progressDialog = AlertDialog.Builder(this).setView(progressLoadingView.root).show()
        progressDialog.setCancelable(false)

        progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }

    fun hideLoadingProgress() {
        if (::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }

    }

    private fun initializeExitDialog() {
        exitDialogBinding = ExitDialogBinding.inflate(layoutInflater)
        exitDialog = AlertDialog.Builder(this).create()
        exitDialog.setView(exitDialogBinding.root)
        exitDialog.window!!.setBackgroundDrawableResource(R.drawable.rounded_corner_bg)
    }

    private fun exitConfirmationLayout() {

        loadExitBanner()
        exitDialog.setCancelable(false)
        exitDialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            //Do something after 100ms
            try {
                exitDialogBinding.btnNo.setOnClickListener {
                    exitDialog.dismiss()
                }
                exitDialogBinding.btnYes.setOnClickListener {
                    exitDialog.dismiss()
                    finish()
                    moveTaskToBack(true)
                }
                exitDialog.setOnDismissListener {
                    if (isWifiStatus() || isMobileData()) {
                        binding.adContainerView0.visibility = View.VISIBLE
                    }

                }
            } catch (e: Exception) {
                println(e.toString())
            }
        }, 1000)
        // binding.adContainerView0.visibility = View.GONE
        //ad binding.adContainerView0.visibility = View.GONE
        //exitDialogBinding.adContainerView.isVisible = isExitBannerLoaded


    }

    private fun loadExitBanner() {
        //bannerRectangularAdsDefine(exitDialogBinding.adContainerView)
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
    private lateinit var consentInformation: ConsentInformation
    private lateinit var consentForm: ConsentForm
    lateinit var params: ConsentRequestParameters
    private fun getConsentStatus() {

        val debugSettings = ConsentDebugSettings.Builder(this).setDebugGeography(
            ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
        ).build()
        params = ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()
        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(this, params, {
            // The consent information state was updated.
            // You are now ready to check if a form is available.
            if (consentInformation.isConsentFormAvailable) {
                //  showToast("loading form")
                // Toast.makeText(this, "loading form", Toast.LENGTH_SHORT).show()
                loadForm()
            } else {
                // showToast("form not available")
                //  Toast.makeText(this, "form not available", Toast.LENGTH_SHORT).show()
            }
        }, {
            // Handle the error.
            //  showToast("checking form availability error")
            //Toast.makeText(this, "checking from availability error", Toast.LENGTH_SHORT).show()
        })

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

                    // Handle dismissal by reloading form.
                    loadForm()
                })
            }
        }, {
            // Handle the error.
        })
    }


    private fun bannerRectangularAdsDefine(adContainerView: FrameLayout) {
        var adView: AdView? = null
        var initialLayoutComplete = false
        adView = AdView(this)
        adContainerView.addView(adView)
        // Since we're loading the banner based on the adContainerView size, we need
        // to wait until this view is laid out before we can get the width.
        adContainerView.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true
                loadRectangularBanner(adContainerView)
            }
        }

    }

    private fun loadRectangularBanner(adContainerView: FrameLayout) {
        if (googleMobileAdsConsentManager!!.canRequestAds) {
            CoroutineScope(Dispatchers.Main).launch {

                val request: AdRequest = AdRequest.Builder().build()
                val adView = AdView(this@MainActivity)
                val adSize = AdSize(300, 250)

                adView.setAdSize(adSize)
                adSize?.run {
                    adView.adUnitId = resources.getString(R.string.bannerRectangularAdUnitId)
                    adView.loadAd(request)


                }
                adView.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        isExitBannerLoaded = true
                        adContainerView?.removeAllViews()
                        adContainerView?.addView(adView)
                        Log.d("banner", "Banner Ad Loaded")
                        if (googleMobileAdsConsentManager!!.canRequestAds) {
                            //exitDialogBinding.adContainerView.isVisible = isExitBannerLoaded
                        }
                        // exitDialogBinding.adContainerView.isVisible = isExitBannerLoaded

                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        Log.d("banner", "Banner Ad Failed Loaded")
                        isExitBannerLoaded = false

                        //exitDialogBinding.adContainerView.isVisible = isExitBannerLoaded
                    }

                }
            }
        }

    }

    private fun checkForInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val n = cm.activeNetwork
            if (n != null) {
                val nc = cm.getNetworkCapabilities(n)
                //It will check for both wifi and cellular network
                return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            }
            return false
        } else {
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showRingtoneActivity() {
        try {

            val factory = LayoutInflater.from(this)
            val mainDialogView: View = factory.inflate(R.layout.ringtone_activity, null)
            var dialog: AlertDialog = AlertDialog.Builder(this).create()
            dialog.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            );
            val dismissbutton = mainDialogView.findViewById<Button>(R.id.dismissbtn)
            val selectRingtone = mainDialogView.findViewById<TextView>(R.id.selectRingtone)
            val EditText =
                mainDialogView.findViewById<EditText>(R.id.editText)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setView(mainDialogView)


            val seekBar = mainDialogView.findViewById<SeekBar>(R.id.seekbar_ringtone)
            val Rb1 = mainDialogView.findViewById<RadioButton>(R.id.ringtoneRB1)
            val Rb2 = mainDialogView.findViewById<RadioButton>(R.id.ringtoneRB2)
            val Rb3 = mainDialogView.findViewById<RadioButton>(R.id.ringtoneRB3)
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            seekBar.max = maxVolume
            val currVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            seekBar.progress = currVolume
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0)
                    EditText.setText(seekBar.progress.toString())
                    binding.seekbar.progress = seekBar.progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    // Do Nothing
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    // Do Nothing
                    binding.seekbar.progress = seekBar.progress
                }
            })

            EditText.setText(seekBar.progress.toString())
            selectRingtone.setOnClickListener {
                val ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ringsound = RingtoneManager.getRingtone(applicationContext, ringtone)
                ringtoneSound = true
                isSound1 = false
                isSound2 = false
                isSound3 = false
            }

            Rb1.setOnClickListener {
                mpbackground?.isLooping = false
                mpbackground?.stop()
                if (mpbackground?.isPlaying == true) {
                    mpbackground?.stop()
                }
                if (Rb1.isChecked) {
                    Rb2.isChecked = false
                    Rb3.isChecked = false
                    isSound1 = true
                    isSound2 = false
                    isSound3 = false
                    ringtoneSound = false
                    ringsound?.stop()
                }
            }
            Rb2.setOnClickListener {
                mpbackground?.isLooping = false
                mpbackground?.stop()
                if (mpbackground?.isPlaying == true) {
                    mpbackground?.stop()
                }
                if (Rb2.isChecked) {
                    Rb1.isChecked = false
                    Rb3.isChecked = false
                    isSound2 = true
                    isSound1 = false
                    isSound3 = false
                    ringtoneSound = false
                    ringsound?.stop()
                }
            }
            Rb3.setOnClickListener {
                mpbackground?.isLooping = false
                mpbackground?.stop()
                if (mpbackground?.isPlaying == true) {
                    mpbackground?.stop()
                }
                if (Rb3.isChecked) {
                    isSound3 = true
                    Rb1.isChecked = false
                    Rb2.isChecked = false
                    isSound1 = false
                    isSound2 = false
                    ringtoneSound = false
                    ringsound?.stop()

                }
            }

            if (getMainSharedPref() == true) {
                if (Rb1.isChecked) {
                    isSound1 = true
                    isSound2 = false
                    isSound3 = false
                    ringtoneSound = false
                    ringsound?.stop()
                } else if (Rb2.isChecked) {
                    isSound2 = true
                    Rb1.isChecked = false
                    Rb3.isChecked = false
                    isSound1 = false
                    isSound3 = false
                    ringtoneSound = false
                    ringsound?.stop()


                } else if (Rb3.isChecked) {
                    isSound3 = true
                    Rb1.isChecked = false
                    Rb2.isChecked = false
                    isSound1 = false
                    isSound2 = false
                    ringtoneSound = false
                    ringsound?.stop()

                }

                if (checkSystemWritePermission()) {
                    selectRingtone.setOnClickListener {
                        val ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                        ringsound = RingtoneManager.getRingtone(applicationContext, ringtone)
                        ringtoneSound = true
                        isSound1 = false
                        isSound2 = false
                        isSound3 = false
                    }

                } else {

                    Toast.makeText(this, "Allow modify system settings ==> ON ", Toast.LENGTH_LONG)
                        .show()
                }
            }

            if (isSound1) {
                ringtoneSound = false
                ringsound?.stop()
                Rb1.isChecked = true
                Rb2.isChecked = false
                Rb3.isChecked = false
            } else if (isSound2) {
                ringtoneSound = false
                ringsound?.stop()
                Rb2.isChecked = true
                Rb1.isChecked = false
                Rb3.isChecked = false
            } else if (isSound3) {
                ringtoneSound = false
                ringsound?.stop()
                Rb3.isChecked = true
                Rb1.isChecked = false
                Rb2.isChecked = false
            } else {
                isSound1 = true
                Rb1.isChecked = true
            }

            dismissbutton.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        } catch (e: Exception) {
            println(e.toString())
        }
    }

    private fun checkSystemWritePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(this)) return true else openAndroidPermissionsMenu()
        }
        return false
    }

    private fun openAndroidPermissionsMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + this.getPackageName())
            this.startActivity(intent)
            isPermissionok = true
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showflashlightActivity() {
        try {

            val factory = LayoutInflater.from(this)
            val mainDialogView: View = factory.inflate(R.layout.flahlight_activity, null)
            var dialog: AlertDialog = AlertDialog.Builder(this).create()
            dialog.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            );
            dialog.setCanceledOnTouchOutside(false)
            dialog.setView(mainDialogView)
            cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            try {
                cameraId = cameraManager.cameraIdList[0] // Use the first available camera
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }

            val toggle = mainDialogView.findViewById<ToggleButton>(R.id.Flash_toggle)
            val Rb1 = mainDialogView.findViewById<RadioButton>(R.id.flashlightRB1)
            val Rb2 = mainDialogView.findViewById<RadioButton>(R.id.flashlightRB2)
            val Rb3 = mainDialogView.findViewById<RadioButton>(R.id.flashlightRB3)
            val dismissbutton = mainDialogView.findViewById<Button>(R.id.dismissbtn)
            dismissbutton.setOnClickListener {
                dialog.dismiss()
            }

            if (currentAppLanguage(this) == "Arabic") {
                toggle.setBackgroundResource(R.drawable.switch_state_ar)
                toggle.gravity = Gravity.CENTER;
                toggle.setPadding(
                    0,
                    0,
                    resources.getDimension(R.dimen.left).toInt(),
                    0
                );
            } else if (currentAppLanguage(this) == "Persian") {
                toggle.gravity = Gravity.CENTER;
                toggle.setPadding(
                    0,
                    0,
                    resources.getDimension(R.dimen.left).toInt(),
                    0
                );
                toggle.setBackgroundResource(R.drawable.switch_state_ar)
            }

            if (currentAppLanguage(this) == "Czech") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.czechSixe)
                )

            } else if (currentAppLanguage(this) == "Greek") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.greek)
                )
            } else if (currentAppLanguage(this) == "Spanish") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.spanish)
                )
            } else if (currentAppLanguage(this) == "Finish") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.finish)
                )
            } else if (currentAppLanguage(this) == "French") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.french)
                )
            } else if (currentAppLanguage(this) == "Italian") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.italian)
                )
            } else if (currentAppLanguage(this) == "Malaysian") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.malay)
                )
            } else if (currentAppLanguage(this) == "Polish") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.polish)
                )
            } else if (currentAppLanguage(this) == "Portuguese") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.portugese)
                )
            } else if (currentAppLanguage(this) == "Russian") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.russian)
                )
            } else if (currentAppLanguage(this) == "Turkish") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.turkish)
                )
            }
            toggle.setOnCheckedChangeListener { buttonView, isChecked ->
                if (toggle.isChecked) {

                    flashSharedpref(true)
                    binding.switchFlash.isChecked = true
                    toggle.gravity = Gravity.CENTER;
                    if (currentAppLanguage(this) == "Arabic") {
                        toggle.setPadding(
                            resources.getDimension(R.dimen.left).toInt(),
                            0,
                            0,
                            0
                        );

                    } else if (currentAppLanguage(this) == "Persian") {
                        toggle.setPadding(
                            resources.getDimension(R.dimen.left).toInt(),
                            0,
                            0,
                            0
                        );
                    } else {
                        toggle.setPadding(
                            0,
                            0,
                            resources.getDimension(R.dimen.right).toInt(),
                            0
                        );
                    }
                } else {

                    flashSharedpref(false)
                    binding.switchFlash.isChecked = false
                    toggle.gravity = Gravity.CENTER;
                    if (currentAppLanguage(this) == "Arabic") {
                        toggle.setPadding(
                            0,
                            0,
                            resources.getDimension(R.dimen.right).toInt(),
                            0
                        );

                    } else if (currentAppLanguage(this) == "Persian") {
                        toggle.setPadding(
                            0,
                            0,
                            resources.getDimension(R.dimen.right).toInt(),
                            0
                        );
                    } else {

                        toggle.setPadding(
                            resources.getDimension(R.dimen.left).toInt(),
                            0,
                            0,
                            0
                        );
                    }
                }

            }
            if (getFlashSharedpref() == true) {
                toggle.isChecked = true
                if (toggle.isChecked) {
                    toggle.gravity = Gravity.CENTER;
                    toggle.setPadding(0, 0, resources.getDimension(R.dimen.right).toInt(), 0);
                    if (Rb1.isChecked) {
                        Rb1.isChecked = true
                        iscontinuousFlash = false
                        isswitchFlash = false
                        istoggleFlash = true

                    } else if (Rb2.isChecked) {
                        iscontinuousFlash = true
                        istoggleFlash = false
                        isswitchFlash = false
                        Rb2.isChecked = true
                    } else if (Rb3.isChecked) {
                        Rb3.isChecked = true
                        istoggleFlash = false
                        iscontinuousFlash = false
                        isswitchFlash = true
                    }
                } else {
                    toggle.gravity = Gravity.CENTER;
                    toggle.setPadding(resources.getDimension(R.dimen.left).toInt(), 0, 0, 0);
                }

            }


            if (toggle.isChecked) {
                toggle.gravity = Gravity.CENTER;
                if (currentAppLanguage(this) == "Arabic") {
                    toggle.setPadding(
                        resources.getDimension(R.dimen.left).toInt(),
                        0,
                        0,
                        0
                    );

                } else if (currentAppLanguage(this) == "Persian") {
                    toggle.setPadding(
                        resources.getDimension(R.dimen.left).toInt(),
                        0,
                        0,
                        0
                    );
                } else {
                    toggle.setPadding(
                        0,
                        0,
                        resources.getDimension(R.dimen.right).toInt(),
                        0
                    );
                }
                Rb1.setOnClickListener {
                    if (Rb1.isChecked) {
                        istoggleFlash = true
                        isswitchFlash = false
                        iscontinuousFlash = false
                        Rb2.isChecked = false
                        Rb3.isChecked = false
                    }
                }
                Rb2.setOnClickListener {

                    if (Rb2.isChecked) {
                        iscontinuousFlash = true
                        istoggleFlash = false
                        isswitchFlash = false
                        Rb1.isChecked = false
                        Rb3.isChecked = false
                    }
                }
                Rb3.setOnClickListener {

                    if (Rb3.isChecked) {
                        istoggleFlash = false
                        iscontinuousFlash = false
                        isswitchFlash = true
                        Rb1.isChecked = false
                        Rb2.isChecked = false

                    }
                }

                if (istoggleFlash) {
                    Rb1.isChecked = true
                    Rb2.isChecked = false
                    Rb3.isChecked = false
                } else if (isswitchFlash) {
                    Rb3.isChecked = true
                    Rb1.isChecked = false
                    Rb2.isChecked = false
                } else if (iscontinuousFlash) {
                    Rb2.isChecked = true
                    Rb1.isChecked = false
                    Rb3.isChecked = false
                } else {
                    iscontinuousFlash = true
                    Rb2.isChecked = true
                }
            } else {
                Rb3.isChecked = true
                toggle.gravity = Gravity.CENTER;
                if (currentAppLanguage(this) == "Arabic") {
                    toggle.setPadding(
                        0,
                        0,
                        resources.getDimension(R.dimen.right).toInt(),
                        0
                    );

                } else if (currentAppLanguage(this) == "Persian") {
                    toggle.setPadding(
                        0,
                        0,
                        resources.getDimension(R.dimen.right).toInt(),
                        0
                    );
                } else {

                    toggle.setPadding(
                        resources.getDimension(R.dimen.left).toInt(),
                        0,
                        0,
                        0
                    );
                }
            }
            dialog.show()
        } catch (e: Exception) {
            println(e.toString())
        }
    }

    private fun showvibrationActivity() {
        try {
            val factory = LayoutInflater.from(this)
            val mainDialogView: View = factory.inflate(R.layout.vibration_activity, null)
            var dialog: AlertDialog = AlertDialog.Builder(this).create()
            dialog.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            );
            dialog.setCanceledOnTouchOutside(false)
            dialog.setView(mainDialogView)

            val toggle = mainDialogView.findViewById<ToggleButton>(R.id.vibration_toggle)
            val Rb1 = mainDialogView.findViewById<RadioButton>(R.id.vibrastionRb1)
            val Rb2 = mainDialogView.findViewById<RadioButton>(R.id.vibrastionRb2)
            val Rb3 = mainDialogView.findViewById<RadioButton>(R.id.vibrastionRb3)
            val dismissbutton = mainDialogView.findViewById<Button>(R.id.dismissbtn)
            dismissbutton.setOnClickListener {
                dialog.dismiss()
            }

            if (currentAppLanguage(this) == "Arabic") {
                toggle.setBackgroundResource(R.drawable.switch_state_ar)
                toggle.gravity = Gravity.CENTER;
                toggle.setPadding(
                    0,
                    0,
                    resources.getDimension(R.dimen.left).toInt(),
                    0
                );
            } else if (currentAppLanguage(this) == "Persian") {
                toggle.gravity = Gravity.CENTER;
                toggle.setPadding(
                    0,
                    0,
                    resources.getDimension(R.dimen.left).toInt(),
                    0
                );
                toggle.setBackgroundResource(R.drawable.switch_state_ar)
            }

            if (currentAppLanguage(this) == "Czech") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.czechSixe)
                )

            } else if (currentAppLanguage(this) == "Greek") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.greek)
                )
            } else if (currentAppLanguage(this) == "Spanish") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.spanish)
                )
            } else if (currentAppLanguage(this) == "Finish") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.finish)
                )
            } else if (currentAppLanguage(this) == "French") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.french)
                )
            } else if (currentAppLanguage(this) == "Italian") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.italian)
                )
            } else if (currentAppLanguage(this) == "Malaysian") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.malay)
                )
            } else if (currentAppLanguage(this) == "Polish") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.polish)
                )
            } else if (currentAppLanguage(this) == "Portuguese") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.portugese)
                )
            } else if (currentAppLanguage(this) == "Russian") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.russian)
                )
            } else if (currentAppLanguage(this) == "Turkish") {
                toggle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.turkish)
                )
            }

            toggle.setOnCheckedChangeListener { buttonView, isChecked ->
                if (toggle.isChecked) {
                    vibrationSharedPref(true)
                    binding.switchVibration.isChecked = true
                    toggle.gravity = Gravity.CENTER;
                    if (currentAppLanguage(this) == "Arabic") {
                        toggle.setPadding(
                            resources.getDimension(R.dimen.left).toInt(),
                            0,
                            0,
                            0
                        );

                    } else if (currentAppLanguage(this) == "Persian") {
                        toggle.setPadding(
                            resources.getDimension(R.dimen.left).toInt(),
                            0,
                            0,
                            0
                        );
                    } else {
                        toggle.setPadding(
                            0,
                            0,
                            resources.getDimension(R.dimen.right).toInt(),
                            0
                        );
                    }
                } else {

                    vibrationSharedPref(false)
                    binding.switchVibration.isChecked = false
                    toggle.gravity = Gravity.CENTER;
                    if (currentAppLanguage(this) == "Arabic") {
                        toggle.setPadding(
                            0,
                            0,
                            resources.getDimension(R.dimen.right).toInt(),
                            0
                        );

                    } else if (currentAppLanguage(this) == "Persian") {
                        toggle.setPadding(
                            0,
                            0,
                            resources.getDimension(R.dimen.right).toInt(),
                            0
                        );
                    } else {

                        toggle.setPadding(
                            resources.getDimension(R.dimen.left).toInt(),
                            0,
                            0,
                            0
                        );
                    }
                }
            }

            if (getVibrationSharedPref() == true) {
                toggle.isChecked = true
                if (toggle.isChecked) {
                    toggle.gravity = Gravity.CENTER;
                    toggle.setPadding(0, 0, resources.getDimension(R.dimen.right).toInt(), 0);
                    if (Rb1.isChecked) {
                        Rb1.isChecked = true
                        iscontinuousVibration = false
                        isVibration = false
                        isheartBeatVibration = true

                    } else if (Rb2.isChecked) {
                        Rb2.isChecked = true
                        iscontinuousVibration = true
                        isheartBeatVibration = false
                        isVibration = false
                    } else if (Rb3.isChecked) {
                        Rb3.isChecked = true
                        iscontinuousVibration = false
                        isheartBeatVibration = false
                        isVibration = true
                    } else {
                        iscontinuousVibration = true
                        Rb2.isChecked = true
                    }


                } else {
                    toggle.gravity = Gravity.CENTER;
                    toggle.setPadding(resources.getDimension(R.dimen.left).toInt(), 0, 0, 0);
                }
            }



            if (toggle.isChecked) {

                toggle.gravity = Gravity.CENTER;
                toggle.setPadding(
                    0,
                    0,
                    resources.getDimension(R.dimen.right).toInt(),
                    0
                );
                Rb1.setOnClickListener {
                    vibrator!!.cancel()
                    if (Rb1.isChecked) {
                        isVibration = false
                        iscontinuousVibration = false
                        isheartBeatVibration = true
                        Rb2.isChecked = false
                        Rb3.isChecked = false
                    }
                }
                Rb2.setOnClickListener {
                    vibrator!!.cancel()
                    if (Rb2.isChecked) {
                        isheartBeatVibration = false
                        isVibration = false
                        iscontinuousVibration = true
                        Rb1.isChecked = false
                        Rb3.isChecked = false
                    }
                }
                Rb3.setOnClickListener {
                    vibrator!!.cancel()

                    if (Rb3.isChecked) {
                        iscontinuousVibration = false
                        isheartBeatVibration = false
                        isVibration = true
                        Rb1.isChecked = false
                        Rb2.isChecked = false
                    }
                }

                if (isheartBeatVibration) {
                    isheartBeatVibration = true
                    isVibration = false
                    Rb1.isChecked = true
                    Rb2.isChecked = false
                    Rb3.isChecked = false
                } else if (isVibration) {
                    isVibration = true
                    isheartBeatVibration = false
                    Rb3.isChecked = true
                    Rb1.isChecked = false
                    Rb2.isChecked = false
                } else if (iscontinuousVibration) {
                    iscontinuousVibration = true
                    isheartBeatVibration = false
                    isVibration = false
                    Rb2.isChecked = true
                } else {
                    iscontinuousVibration = true
                    Rb2.isChecked = true
                }

            } else {
                Rb3.isChecked = true
                toggle.gravity = Gravity.CENTER;
                if (currentAppLanguage(this) == "Arabic") {
                    toggle.setPadding(
                        0,
                        0,
                        resources.getDimension(R.dimen.right).toInt(),
                        0
                    );

                } else if (currentAppLanguage(this) == "Persian") {
                    toggle.setPadding(
                        0,
                        0,
                        resources.getDimension(R.dimen.right).toInt(),
                        0
                    );
                } else {

                    toggle.setPadding(
                        resources.getDimension(R.dimen.left).toInt(),
                        0,
                        0,
                        0
                    );
                }
            }


            dialog.show()
        } catch (e: Exception) {
            println(e.toString())
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showsettingsActivity() {
        try {

            val factory = LayoutInflater.from(this)
            val mainDialogView: View = factory.inflate(R.layout.settings_activity, null)
            var dialog: AlertDialog = AlertDialog.Builder(this).create()
            dialog.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            );
            val dismissbutton = mainDialogView.findViewById<Button>(R.id.dismissSetting)
            val switchsmart = mainDialogView.findViewById<Switch>(R.id.switchsetting)
            val switchsDarkmode = mainDialogView.findViewById<Switch>(R.id.switchDarkmode)
            if (getSmartSharedPref() == true) {
                switchsmart.isChecked = true
            } else {
                switchsmart.isChecked = false
            }
            val Language = mainDialogView.findViewById<TextView>(R.id.language)
            Language.setOnClickListener {

                dialog.dismiss()
                val intent = Intent(this@MainActivity, Language_Activity::class.java)
                startActivity(intent)
                finish()
                //showLanguageDialog()
            }

            if (currentAppLanguage(this) == "Arabic") {

                Language.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.settings_arrow_ar,
                    0,
                    0,
                    0
                )
            } else if (currentAppLanguage(this) == "Persian") {
                Language.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.settings_arrow_ar,
                    0,
                    0,
                    0
                )
            }

            val sharedPreferences = getSharedPreferences("themeDetails", MODE_PRIVATE)
            val themeState = sharedPreferences.getString("ThemeState", "")
            if (themeState != "") {
                if (themeState == "true") {
                    switchsDarkmode.isChecked = true
                } else {
                    switchsDarkmode.isChecked = false
                }
            }

            switchsDarkmode.setOnCheckedChangeListener { buttonView, isChecked ->
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }

                if (switchsDarkmode.isChecked) {
                    saveThemeState("true")
                    HandelingNightMode(true)
                } else {
                    saveThemeState("false")
                    HandelingNightMode(false)
                }
                recreate();
            }

            switchsmart.setOnCheckedChangeListener { buttonView, isChecked ->
                if (switchsmart.isChecked) {
                    if (rewardedInterstitialAd != null) {
                        dialog.dismiss()
                    }
                    smartSharedpref(true)
                    binding.switchSmatmode.isChecked = true
                } else {
                    if (rewardedInterstitialAd != null) {
                        dialog.dismiss()
                    }
                    smartSharedpref(false)
                    binding.switchSmatmode.isChecked = false
                }
            }
            dismissbutton.setOnClickListener {
                dialog.dismiss()
            }
            dialog.setCanceledOnTouchOutside(false)
            dialog.setView(mainDialogView)
            if (getSmartSharedPref() == true) {
                switchsmart.isChecked = true
            }
            if (!isFinishing()) {
                // Show your dialog here
                dialog.show()
            }

        } catch (e: Exception) {
            println(e.toString())
        }
    }

    fun saveThemeState(str: String?) {
        val sharedPreferences = this.getSharedPreferences("themeDetails", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("ThemeState", str)
        editor.apply()
    }

    private fun HandelingNightMode(isChecked: Boolean) {
        if (!isChecked) {
            try {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                //triggerRebirth(this)
            } catch (e: Exception) {
                println(e.toString())
            }

        } else {

            try {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                // triggerRebirth(this)

            } catch (e: Exception) {
                println(e.toString())
            }

        }

    }

    @SuppressLint("MissingInflatedId")

    private fun loadInterstailAd() {
        if (googleMobileAdsConsentManager!!.canRequestAds) {
            InterstitialAd.load(this,
                getString(R.string.interstitialIdSuccess),
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(p0: InterstitialAd) {
                        successAd = p0
                        Log.d(AdTag,"loaded")
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        Log.d(AdTag, p0.message)
                    }
                })
        }
    }

    companion object {
        var successAd: InterstitialAd? = null

var AdTag="Interstitial"
        fun showSuccessAd(activity: Activity) {


            successAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdClicked() {
                        super.onAdClicked()
                        Log.d(AdTag, "On Ad clicked")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        Log.d(AdTag, "onAdDismissedFullScreenContent")


                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        Log.d(AdTag, "onAdFailedToShowFullScreenContentClicked${p0}")

                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        Log.d(AdTag, "onAdImpressionClicked")

                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        Log.d(AdTag, "onAdShowedFullScreenContent")

                    }

                }
            successAd?.show(activity)
        }

    }

    protected fun onPostExecute(result: Void?) {
        progressDialog.dismiss()
        // Handle the result
    }

    private fun setUpDrawerControls() = with(binding.drawerView) {
        val drawerView: View = findViewById(R.id.drawer_layout)
        if (drawerView != null && drawerView is DrawerLayout) {

            drawerView.setDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                   binding.adContainerView0.isVisible = false
                }

                override fun onDrawerOpened(drawerView: View) {
                    binding.adContainerView0.isVisible = false
                    // showToast(drawer_layout.isDrawerOpen(GravityCompat.START).toString())
                }

                override fun onDrawerClosed(drawerView: View) {
                    if (googleMobileAdsConsentManager!!.canRequestAds) {
                        loadAdaptiveBanner( binding.adContainerView0)
                    }
                    // showToast(drawer_layout.isDrawerOpen(GravityCompat.START).toString())
                }

                override fun onDrawerStateChanged(i: Int) {
                }
            })
        }

        ringtone.setOnClickListener {

            binding.drawerLayout.close()
            showRingtoneActivity()
        }
        flashlight.setOnClickListener {
            binding.drawerLayout.close()
            showflashlightActivity()
        }

        vibration.setOnClickListener {
            binding.drawerLayout.close()
            showvibrationActivity()
        }

        settings.setOnClickListener {
            binding.drawerLayout.close()
            showsettingsActivity()
        }

        removeAds.setOnClickListener {
            binding.drawerLayout.close()
            val intent = Intent(this@MainActivity, PremiumActivity::class.java)
            startActivity(intent)
        }

        moreapp.setOnClickListener {
            binding.drawerLayout.close()
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/dev?id=8304865013653518147")
                    )
                )
            } catch (e: Exception) {
                println(e.toString())
            }
        }

        shareapp.setOnClickListener {
            binding.drawerLayout.close()
            shareApp()
        }

        rateapp.setOnClickListener {
            binding.drawerLayout.close()
            showRatingDialogue()
        }

        privacy.setOnClickListener {
            binding.drawerLayout.close()
            //Setting()
            showPrivacyDialog()

        }

        feedback.setOnClickListener {
            binding.drawerLayout.close()
            contactUsDialog()
        }
    }

    private fun contactUsDialog() {
        try {
            val factory = LayoutInflater.from(this)
            val mainDialogView: View = factory.inflate(R.layout.feedback_dialog, null)
            val dialog: AlertDialog = AlertDialog.Builder(this).create()
            dialog.window!!.setBackgroundDrawableResource(R.drawable.rounded_corner_bg)
            dialog.setView(mainDialogView)
            dialog.setCanceledOnTouchOutside(false)

            val btn_feedback = mainDialogView.findViewById<TextView>(R.id.btn_feedback)
            val btn_cancel = mainDialogView.findViewById<TextView>(R.id.btn_cancel)
            val et_composeEmail = mainDialogView.findViewById<TextView>(R.id.et_composeEmail)
            btn_feedback.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_SENDTO) // it's not ACTION_SEND
                    val str =
                        "mailto:?subject= " + "${resources.getString(R.string.app_name)} User's Feedback" + " &to=Zubairbadal321@gmail.com" + "&body=" + et_composeEmail.text.toString()
                    intent.data = Uri.parse(
                        str
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // this will make such that when user returns to your app, your app is displayed, instead of the email app.
                    startActivity(intent)
                    dialog.dismiss()
                } catch (e: Exception) {
                    println(e.toString())
                }
            }
            btn_cancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            println(e.toString())
        }


    }

    private fun showRatingDialogue() {
        try {

            val factory = LayoutInflater.from(this)
            val mainDialogView: View = factory.inflate(R.layout.rating_dialog, null)
            val dialog: AlertDialog = AlertDialog.Builder(this).create()
            dialog.window!!.setBackgroundDrawableResource(R.drawable.rounded_corner_bg)
            dialog.setView(mainDialogView)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            val btn_done = mainDialogView.findViewById<LottieAnimationView>(R.id.btn_done)
            val btn_cancelRating =
                mainDialogView.findViewById<LottieAnimationView>(R.id.btn_cancelRating)

            btn_done.setOnClickListener {
                try {

                    askRatingsGooglePlay()

                    dialog.dismiss()

                } catch (e: Exception) {
                    println(e.toString())
                }
            }
            btn_cancelRating.setOnClickListener {
                try {
                    toast.toastDefine(resources.getString(R.string.thanksForRatingUs))
                    dialog.dismiss()

                } catch (e: Exception) {
                    println(e.toString())
                }
            }


        } catch (e: Exception) {
            println(e.toString())
        }
    }

    private fun askRatingsGooglePlay() {
        try {
            val uri: Uri = Uri.parse("market://details?id=$packageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }

        } catch (e: Exception) {
            println(e.toString())
        }
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name")
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage =
                """
                        ${shareMessage}https://play.google.com/store/apps/details?id=${packageName}
                        
                        
                        """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            //e.toString();
        }
    }

    private fun showPrivacyDialog() {
        val builder = AlertDialog.Builder(this)
        val layoutInflater = LayoutInflater.from(this@MainActivity)
        val viewDialog: View = layoutInflater.inflate(R.layout.activity_privacy, null)
        builder.setView(viewDialog)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
        val btn_save = viewDialog.findViewById<Button>(R.id.btn_save)
        btn_save.setOnClickListener {

            val sharedpreferences =
                getSharedPreferences("sharedprefs", MODE_PRIVATE)
            val editor = sharedpreferences.edit()
            editor.apply {

                putBoolean("isCheck", true)

                alertDialog.dismiss()


            }.apply()

        }
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

    private fun checkForPermissions() {
        if (!hasRecordingPermissions(this, *PERMISSIONS)) {
            requestPermissions(PERMISSIONS, PERMISSION_CODE)
        }
    }

    override fun onResume() {
        super.onResume()

        /*val rootView = findViewById<View>(android.R.id.content)
        val observer = rootView.viewTreeObserver

        observer.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val heightDiff = rootView.rootView.height - rootView.height
                if (heightDiff > dpToPx(this@MainActivity, 200)) {
                    // Keyboard is visible
                    // Toast.makeText(this@MainActivity, "Visible", Toast.LENGTH_SHORT).show()
                    binding.adContainerView0.visibility = View.GONE
                    // Do something when the keyboard is visible
                } else {
                    binding.adContainerView0.visibility = View.VISIBLE
                    // Keyboard is not visible
                    // Do something when the keyboard is not visible
                }
                return true
            }
        })*/

        if (getMainSharedPref() == true) {
            binding.togglemain.isChecked = true
        }else
        {
            binding.togglemain.isChecked = false
        }
        if (rewardedInterstitialAd != null) {
            binding.adicon.visibility = View.VISIBLE
        }
       // loadInterstailAd()
        if (binding.togglemain.isChecked) {
            binding.togglemain.gravity = Gravity.CENTER;
            if (currentAppLanguage(this) == "Arabic") {
                binding.togglemain.setPadding(
                    resources.getDimension(R.dimen.left).toInt(),
                    0,
                    0,
                    0
                );

            } else if (currentAppLanguage(this) == "Persian") {
                binding.togglemain.setPadding(
                    resources.getDimension(R.dimen.left).toInt(),
                    0,
                    0,
                    0
                );
            } else {
                binding.togglemain.setPadding(
                    0,
                    0,
                    resources.getDimension(R.dimen.right).toInt(),
                    0
                );
            }
            binding.togglemain.resources.getDimension(R.dimen.right)
        } else {
            binding.togglemain.gravity = Gravity.CENTER;
            if (currentAppLanguage(this) == "Arabic") {
                binding.togglemain.setPadding(
                    0,
                    0,
                    resources.getDimension(R.dimen.right).toInt(),
                    0
                );

            } else if (currentAppLanguage(this) == "Persian") {
                binding.togglemain.setPadding(
                    0,
                    0,
                    resources.getDimension(R.dimen.right).toInt(),
                    0
                );
            } else {

                binding.togglemain.setPadding(
                    resources.getDimension(R.dimen.left).toInt(),
                    0,
                    0,
                    0
                );
            }
        }

        if (isWifiStatus() || isMobileData()) {
            if (isShowingAppOpenAd) {
                binding.adContainerView0.visibility = View.GONE
            } else {
                binding.adContainerView0.visibility = View.VISIBLE
            }

        } else {
            binding.adicon.visibility = View.VISIBLE
            binding.adContainerView0.visibility = View.GONE
        }
    }
    private fun dpToPx(activity: AppCompatActivity, dp: Int): Int {
        val density = activity.resources.displayMetrics.density
        return (dp.toFloat() * density).toInt()
    }
    override fun onPause() {
        super.onPause()

        binding.adContainerView0.visibility = View.GONE
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {

        if (requestCode == PERMISSION_CODE) {
            if (mediaRecorderPermissionsGranted(grantResults)) {

                Toast.makeText(
                    this,
                    "allPermissionsGranted",
                    Toast.LENGTH_LONG
                ).show()
                binding.togglemain.isChecked = false
            } else {

                Toast.makeText(
                    this,
                    "permissionsNotGranted",
                    Toast.LENGTH_LONG
                ).show()
                binding.togglemain.isChecked = false
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

    override fun onUserEarnedReward(rewardItem: RewardItem) {
        Log.d(TAG, "User earned reward.")
        // TODO: Reward the user!

        val sharedpreference =
            getSharedPreferences("adbtnprefs", AppCompatActivity.MODE_PRIVATE)
        val editor = sharedpreference.edit()
        editor.apply {
            putBoolean("adprefs", true)
        }.apply()
        rewardearn = true
        binding.adicon.visibility = View.GONE


    }
}

fun Context.flashSharedpref(Ok: Boolean) {
    val sharedpreferences =
        getSharedPreferences("flashSharedpref", AppCompatActivity.MODE_PRIVATE)
    val editor = sharedpreferences.edit()
    editor.apply {

        putBoolean("isFlashCheck", Ok)

    }.apply()
}

fun Context.getFlashSharedpref(): Boolean {
    val sharedpreferences = getSharedPreferences("flashSharedpref", AppCompatActivity.MODE_PRIVATE)
    return sharedpreferences.getBoolean("isFlashCheck", false)
}

fun Context.interstitialSharedpref(Ok: Boolean) {
    val sharedpreferences =
        getSharedPreferences("interstitialSharedpref", AppCompatActivity.MODE_PRIVATE)
    val editor = sharedpreferences.edit()
    editor.apply {

        putBoolean("isinterstitialCheck", Ok)

    }.apply()
}

fun Context.getinterstitialSharedpref(): Boolean {
    val sharedpreferences =
        getSharedPreferences("interstitialSharedpref", AppCompatActivity.MODE_PRIVATE)
    return sharedpreferences.getBoolean("isinterstitialCheck", false)
}

fun Context.vibrationSharedPref(Ok: Boolean) {
    val sharedpreferences =
        getSharedPreferences("vibrationSharedPref", AppCompatActivity.MODE_PRIVATE)
    val editor = sharedpreferences.edit()
    editor.apply {

        putBoolean("isVibrationCheck", Ok)

    }.apply()
}

fun Context.getVibrationSharedPref(): Boolean {
    val sharedpreferences =
        getSharedPreferences("vibrationSharedPref", AppCompatActivity.MODE_PRIVATE)
    return sharedpreferences.getBoolean("isVibrationCheck", false)
}

fun Context.smartSharedpref(Ok: Boolean) {
    val sharedpreferences =
        getSharedPreferences("smartsharedPref", AppCompatActivity.MODE_PRIVATE)
    val editor = sharedpreferences.edit()
    editor.apply {
        putBoolean("isSmartCheck", Ok)
    }.apply()
}

fun Context.getSmartSharedPref(): Boolean {

    val sharedpreferences = getSharedPreferences("smartsharedPref", AppCompatActivity.MODE_PRIVATE)
    return sharedpreferences.getBoolean("isSmartCheck", false)
}

fun Context.getMainSharedPref(): Boolean {

    val sharedPreferences = getSharedPreferences("sharedprefs", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("StateCheck", false)
}

fun Context.mainSharedpref(Ok: Boolean) {

    val sharedpreferences = getSharedPreferences("sharedprefs", AppCompatActivity.MODE_PRIVATE)
    val editor = sharedpreferences.edit()
    editor.apply {
        putBoolean("StateCheck", Ok)
    }.apply()

}

fun Context.showDoneDialogue(title: String, context: Context, callbacksView: () -> Unit) {
    try {
        val layoutInflater = LayoutInflater.from(this)
        val binding = DoneResultDialogBinding.inflate(layoutInflater)

        val dialog: AlertDialog = AlertDialog.Builder(this).create()
        dialog.window!!.setBackgroundDrawableResource(R.drawable.transparent_bg)
        dialog.setView(binding.root)
        dialog.setCancelable(false)
        binding.title.text = title.toString()
        binding.btnOk.setOnClickListener {
            try {
                callbacksView.invoke()


                MainActivity.showSuccessAd(context as Activity)
                dialog.dismiss()
            } catch (e: Exception) {
                println(e.toString())
            }
        }

        dialog.show()
    } catch (e: Exception) {
        println(e.toString())
    }

}

fun Context.delayHandler(milliSeconds: Long, callbacks: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({
        //Do something after 100ms
        try {
            callbacks.invoke()
        } catch (e: Exception) {
            println(e.toString())
        }
    }, milliSeconds)
}

fun Context.triggerRebirth(context: Context) {
    Handler(Looper.getMainLooper()).postDelayed({
        //Do something after 100ms
        try {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            context.startActivity(intent)
        } catch (e: Exception) {
            println(e.toString())
        }
    }, 0)


}


private val android.content.Context.preferences: SharedPreferences
    get() = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

fun Context.currentAppLanguage(mContext: Context): String? {
    return this.preferences.getString("Locale", "English")
}

fun Context.setAppLanguage(mContext: Context, name: String) {
    mContext.preferences.edit {
        putString("Locale", name)
    }
}




