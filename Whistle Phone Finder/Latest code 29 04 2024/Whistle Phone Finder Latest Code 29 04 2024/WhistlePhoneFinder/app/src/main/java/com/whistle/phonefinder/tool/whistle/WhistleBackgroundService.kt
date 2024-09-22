package com.whistle.phonefinder.tool.whistle


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.whistle.phonefinder.tool.Common
import com.whistle.phonefinder.tool.Common.isPhoneScreenOn
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
import com.whistle.phonefinder.tool.Common.ringsound
import com.whistle.phonefinder.tool.Common.ringtoneSound
import com.whistle.phonefinder.tool.R
import com.whistle.phonefinder.tool.getFlashSharedpref
import com.whistle.phonefinder.tool.getMainSharedPref
import com.whistle.phonefinder.tool.getVibrationSharedPref
import com.whistle.phonefinder.tool.mainSharedpref
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis


class WhistleBackgroundService : LifecycleService() {
    lateinit var context: Context
    var handler: Handler? = null
    lateinit var viewModel: WhistleViewModel
    private lateinit var cameraId: String
    var vibrator: Vibrator? = null
    private lateinit var cameraManager: CameraManager
    private lateinit var predictedVoices: ArrayList<String>
    private var screenStateReceiver: ScreenStateReceiver? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val action = intent?.action
        context = applicationContext as Context

        viewModel = WhistleViewModel(application)
        setUpObservers()
        when (action) {
            "turnOn" -> turnOnListening()
            "turnOff" -> turnOffListening()
        }

        return START_NOT_STICKY
    }
    override fun onDestroy() {
        Log.d("services", "service destoryed")

        try {
            this.unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {

        }
        super.onDestroy()
    }

    private fun turnOnListening() {
        Common.isServiceRunning = true
        Log.d("services", "service on")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val CHANNELID = "Foreground Service ID"
            val channel = NotificationChannel(
                CHANNELID, CHANNELID, NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

            val notificationLayout = RemoteViews(packageName, R.layout.notification_layout)

            val notification = NotificationCompat.Builder(this, CHANNELID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCustomBigContentView(notificationLayout)
            val notificationSwitchIntent = Intent(this, WhistleReceiver::class.java)
            notificationSwitchIntent.action = "off_action"

            val notificationSwitchPendingIntent = PendingIntent.getBroadcast(
                this, 0, notificationSwitchIntent, PendingIntent.FLAG_IMMUTABLE
            )

            notificationLayout.setOnClickPendingIntent(
                R.id.notificationSwitchOn, notificationSwitchPendingIntent
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    1001, notification.build(), FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            }

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val CHANNELID = "Foreground Service ID"
                val channel = NotificationChannel(
                    CHANNELID, CHANNELID, NotificationManager.IMPORTANCE_LOW
                )
                getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
                val notificationLayout = RemoteViews(packageName, R.layout.notification_layout)
                val notification = NotificationCompat.Builder(this, CHANNELID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setCustomBigContentView(notificationLayout)
                val notificationSwitchIntent = Intent(this, WhistleReceiver::class.java)
                notificationSwitchIntent.action = "off_action"
                val notificationSwitchPendingIntent = PendingIntent.getBroadcast(
                    this, 0, notificationSwitchIntent, PendingIntent.FLAG_IMMUTABLE
                )
                notificationLayout.setOnClickPendingIntent(
                    R.id.notificationSwitchOn, notificationSwitchPendingIntent
                )
                startForeground(
                    1001, notification.build()
                )
            }
        }
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        viewModel.setUpdateLoopListeningHandler()

        // Register the broadcast receiver to listen for screen state changes

        try {
            try {
                Log.d("ScreenStateRegister", "registering...")
                val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
                filter.addAction(Intent.ACTION_SCREEN_OFF)
                screenStateReceiver = ScreenStateReceiver()
                registerReceiver(screenStateReceiver, filter)

            } catch (e: Exception) {
                // this.unregisterReceiver(screenStateReceiver)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        mainSharedpref(true)
        /*val sharedPreferences = getSharedPreferences("sharedprefs", Context.MODE_PRIVATE)
        val editorr = sharedPreferences.edit()
        editorr.apply {
            putBoolean("StateCheck", true)
        }.apply()*/
    }

    private fun turnOffListening() {
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        Log.d("services", "service off")

        vibrator?.cancel()
        mainSharedpref(false)

        toggleFlashlight(false)
        switchFlashLight(false)
        continuousFlashLight(false)
        vibration(false)
        heartBeatVibration(false)
        continuousvibration(false)
        mpbackground?.isLooping=false
        mpbackground?.stop()
        if (mpbackground?.isPlaying==true) {
            mpbackground?.stop()
        }
        ringsound?.stop()
        Common.isServiceRunning = false
        stopForeground(true)
        stopSelf()
        listeningStopped()


    }

    private fun listeningStopped() {
        viewModel.stopAllListening()
    }

    private fun setUpObservers() {
        viewModel.listeningEnd.observe(this, Observer { end ->
            if (end) {

            } else {

            }
        })

        viewModel.listOfClasses.observe(this, Observer { pair ->
            val (stringList, _) = pair
            predictedVoices = stringList
            Log.d("services", "Whistling...")
            if (predictedVoices[0] == "Whistling" || predictedVoices[0] == "Whistle" || predictedVoices[0] == "Steam whistle") {
                manageWhistleMedia()
            }
            if (predictedVoices[1] == "Whistling" || predictedVoices[1] == "Whistle" || predictedVoices[1] == "Steam whistle") {
                manageWhistleMedia()
            }
            if (predictedVoices[2] == "Whistling" || predictedVoices[2] == "Whistle" || predictedVoices[2] == "Steam whistle") {
                manageWhistleMedia()
            }
            if (predictedVoices[3] == "Whistling" || predictedVoices[3] == "Whistle" || predictedVoices[3] == "Steam whistle") {
                manageWhistleMedia()
            }
            if (predictedVoices[4] == "Whistling" || predictedVoices[4] == "Whistle" || predictedVoices[4] == "Steam whistle") {
                manageWhistleMedia()
            }

        })
    }

    private fun manageWhistleMedia() {
        lifecycleScope.launch {
            val time = measureTimeMillis {
                // call your function here
                Log.d(
                    "TakenTime",
                    "Zero time: ${System.currentTimeMillis()}  ${Common.isMediaPlaying}"
                )
                val job1 = async {
                    Common.isMediaPlaying = true
                    Log.d("services", "Media Playing started")
                    if (!isSmartMode) {
                        Log.d("services", "smartMode ${isSmartMode}")
                        Log.d("services", "Whistle")

                        if (context.getMainSharedPref() == true) {
                            if (context.getFlashSharedpref() == true) {

                                if (istoggleFlash) {

                                    switchFlashLight(false)
                                    continuousFlashLight(false)
                                    toggleFlashlight(true)
                                } else if (isswitchFlash) {
                                    toggleFlashlight(false)
                                    continuousFlashLight(false)
                                    switchFlashLight(true)
                                }
                                else if (iscontinuousFlash)
                                {
                                    switchFlashLight(false)
                                    toggleFlashlight(false)
                                    continuousFlashLight(true)
                                }
                                else{
                                    iscontinuousFlash=true
                                }
                            }
                            if (getVibrationSharedPref()) {

                                if (isheartBeatVibration) {
                                    continuousvibration(false)
                                    vibration(false)
                                    heartBeatVibration(true)
                                } else if (isVibration) {
                                    continuousvibration(false)
                                    heartBeatVibration(false)
                                    vibration(true)
                                }
                                else if (iscontinuousVibration)
                                {
                                    heartBeatVibration(false)
                                    vibration(false)
                                    continuousvibration(true)
                                }
                                else{
                                    iscontinuousVibration=true
                                }
                            }
                            if (isSound1) {
                                mpbackground?.isLooping=false
                                mpbackground?.stop()
                                ringsound?.stop()
                                if (mpbackground?.isPlaying==true) {
                                    mpbackground?.stop()
                                }
                                mpbackground?.setVolume(1F, 1F)
                                mpbackground = MediaPlayer.create(
                                    this@WhistleBackgroundService,
                                    R.raw.custom1
                                )
                                mpbackground?.start()
                                mpbackground?.isLooping=true
                                mpbackground?.setOnCompletionListener {

                                    toggleFlashlight(false)
                                    switchFlashLight(false)
                                    continuousFlashLight(false)
                                    vibration(false)
                                    heartBeatVibration(false)
                                    continuousvibration(false)
                                }

                            }
                            else if (isSound2) {
                                mpbackground?.isLooping=false
                                mpbackground?.stop()
                                ringsound?.stop()
                                if (mpbackground?.isPlaying==true) {
                                    mpbackground?.stop()
                                }
                                mpbackground?.setVolume(1F, 1F)
                                mpbackground =
                                    MediaPlayer.create(this@WhistleBackgroundService, R.raw.custom2)
                                mpbackground?.start()
                                mpbackground?.isLooping=true
                                mpbackground?.setOnCompletionListener {

                                    toggleFlashlight(false)
                                    switchFlashLight(false)
                                    continuousFlashLight(false)
                                    vibration(false)
                                    heartBeatVibration(false)
                                    continuousvibration(false)
                                }
                            }
                            else if (isSound3) {
                                ringsound?.stop()
                                if (mpbackground?.isPlaying==true) {
                                    mpbackground?.stop()

                                }
                                mpbackground?.setVolume(1F, 1F)
                                mpbackground = MediaPlayer.create(
                                    this@WhistleBackgroundService,
                                    R.raw.custom3
                                )
                                mpbackground?.start()
                                mpbackground?.isLooping=true
                                mpbackground?.setOnCompletionListener {

                                    toggleFlashlight(false)
                                    switchFlashLight(false)
                                    continuousFlashLight(false)
                                    vibration(false)
                                    heartBeatVibration(false)
                                    continuousvibration(false)
                                }
                            }
                            else if (ringtoneSound)
                            {
                                toggleFlashlight(false)
                                switchFlashLight(false)
                                continuousFlashLight(false)
                                vibration(false)
                                heartBeatVibration(false)
                                continuousvibration(false)
                                mpbackground?.isLooping=false
                                mpbackground?.stop()
                                ringsound?.play()

                            }
                            else{
                                isSound3=true
                            }
                        }
                        else
                        {
                            toggleFlashlight(false)
                            switchFlashLight(false)
                            continuousFlashLight(false)
                            vibration(false)
                            heartBeatVibration(false)
                            continuousvibration(false)
                            vibrator!!.cancel()
                            mpbackground?.isLooping=false
                            mpbackground?.stop()
                            if (mpbackground?.isPlaying==true) {
                                mpbackground?.stop()
                            }
                            ringsound?.stop()
                        }
                    } else {
                        Log.d("services", "smartMode ${isSmartMode}")
                        if (isPhoneScreenOn) {
                            Log.d("services", "locked mode")
                            Log.d("services", "phone screen is on")
                        } else {
                            Log.d("services", "no locked mode")
                            Log.d("services", "Whistle")

                            if (context.getMainSharedPref() == true) {
                                if (context.getFlashSharedpref() == true) {

                                    if (istoggleFlash) {

                                        switchFlashLight(false)
                                        continuousFlashLight(false)
                                        toggleFlashlight(true)
                                    } else if (isswitchFlash) {
                                        toggleFlashlight(false)
                                        continuousFlashLight(false)
                                        switchFlashLight(true)
                                    }
                                    else if (iscontinuousFlash)
                                    {
                                        switchFlashLight(false)
                                        toggleFlashlight(false)
                                        continuousFlashLight(true)
                                    }
                                    else{
                                        iscontinuousFlash=true
                                    }
                                }
                                if (getVibrationSharedPref()) {

                                    if (isheartBeatVibration) {
                                        continuousvibration(false)
                                        vibration(false)
                                        heartBeatVibration(true)
                                    } else if (isVibration) {
                                        continuousvibration(false)
                                        heartBeatVibration(false)
                                        vibration(true)
                                    }
                                    else if (iscontinuousVibration)
                                    {
                                        heartBeatVibration(false)
                                        vibration(false)
                                        continuousvibration(true)
                                    }
                                    else{
                                        iscontinuousVibration=true
                                    }
                                }
                                if (isSound1) {
                                    mpbackground?.isLooping=false
                                    mpbackground?.stop()
                                    ringsound?.stop()
                                    if (mpbackground?.isPlaying==true) {
                                        mpbackground?.stop()
                                    }
                                    mpbackground?.setVolume(1F, 1F)
                                    mpbackground = MediaPlayer.create(
                                        this@WhistleBackgroundService,
                                        R.raw.custom1
                                    )
                                    mpbackground?.start()
                                    mpbackground?.isLooping=true
                                    mpbackground?.setOnCompletionListener {

                                        toggleFlashlight(false)
                                        switchFlashLight(false)
                                        continuousFlashLight(false)
                                        vibration(false)
                                        heartBeatVibration(false)
                                        continuousvibration(false)
                                    }

                                }
                                else if (isSound2) {
                                    mpbackground?.isLooping=false
                                    mpbackground?.stop()
                                    ringsound?.stop()
                                    if (mpbackground?.isPlaying==true) {
                                        mpbackground?.stop()
                                    }
                                    mpbackground?.setVolume(1F, 1F)
                                    mpbackground =
                                        MediaPlayer.create(this@WhistleBackgroundService, R.raw.custom2)
                                    mpbackground?.start()
                                    mpbackground?.isLooping=true
                                    mpbackground?.setOnCompletionListener {

                                        toggleFlashlight(false)
                                        switchFlashLight(false)
                                        continuousFlashLight(false)
                                        vibration(false)
                                        heartBeatVibration(false)
                                        continuousvibration(false)
                                    }
                                }
                                else if (isSound3) {
                                    ringsound?.stop()
                                    if (mpbackground?.isPlaying==true) {
                                        mpbackground?.stop()

                                    }
                                    mpbackground?.setVolume(1F, 1F)
                                    mpbackground = MediaPlayer.create(
                                        this@WhistleBackgroundService,
                                        R.raw.custom3
                                    )
                                    mpbackground?.start()
                                    mpbackground?.isLooping=true
                                    mpbackground?.setOnCompletionListener {

                                        toggleFlashlight(false)
                                        switchFlashLight(false)
                                        continuousFlashLight(false)
                                        vibration(false)
                                        heartBeatVibration(false)
                                        continuousvibration(false)
                                    }
                                }
                                else if (ringtoneSound)
                                {
                                    toggleFlashlight(false)
                                    switchFlashLight(false)
                                    continuousFlashLight(false)
                                    vibration(false)
                                    heartBeatVibration(false)
                                    continuousvibration(false)
                                    mpbackground?.isLooping=false
                                    mpbackground?.stop()
                                    ringsound?.play()

                                }
                                else{
                                    isSound3=true
                                }
                            }
                            else
                            {
                                toggleFlashlight(false)
                                switchFlashLight(false)
                                continuousFlashLight(false)
                                vibration(false)
                                heartBeatVibration(false)
                                continuousvibration(false)
                                vibrator!!.cancel()
                                mpbackground?.isLooping=false
                                mpbackground?.stop()
                                if (mpbackground?.isPlaying==true) {
                                    mpbackground?.stop()
                                }
                                ringsound?.stop()
                            }
                        }
                    }

                    Log.d(
                        "TakenTime",
                        "First time: ${System.currentTimeMillis()}  ${Common.isMediaPlaying}"
                    )
                }
                Log.d("TakenTime", "took time1: ${job1.await()}")
                val job2 = async {
                    Common.isMediaPlaying = false
                    Log.d("services", "Media Playing finished")
                    Log.d(
                        "TakenTime",
                        "2nd time: ${System.currentTimeMillis()}  ${Common.isMediaPlaying}"
                    )
                }
                Log.d("TakenTime", "took time2: ${job2.await()}")

            }
            Log.d("TakenTime", "took time ALl: ${time}")
        }
    }

    private class ScreenStateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null) {
                if (intent.action == Intent.ACTION_SCREEN_ON) {
                    // Screen is turned on
                    Log.d("services", "Screen on")
                    isPhoneScreenOn = true
                    // Do something
                } else if (intent.action == Intent.ACTION_SCREEN_OFF) {
                    // Screen is turned off
                    Log.d("services", "Screen off")
                    isPhoneScreenOn = false
                    // Do something else
                }
            }
        }

    }

    private fun heartBeatVibration(status: Boolean) {
        if (status) {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val vibrationPattern = longArrayOf(0, 100, 200, 150)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator!!.vibrate(
                    VibrationEffect.createWaveform(
                        vibrationPattern,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                //deprecated in API 26
                vibrator!!.vibrate(vibrationPattern, -1)
            }
        }

    }

    private fun vibration(status: Boolean) {
        if (status) {
            if (vibrator!!.hasVibrator()) { // Vibrator availability checking
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator!!.vibrate(
                        VibrationEffect.createOneShot(
                            3000, VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    ) // New vibrate method for API Level 26 or higher
                }
            }
        }

    }
    private fun continuousvibration(status: Boolean) {
        if (status) {
            if (vibrator!!.hasVibrator()) { // Vibrator availability checking
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator!!.vibrate(
                        VibrationEffect.createOneShot(
                            900000000, VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    ) // New vibrate method for API Level 26 or higher
                }
            }
        }

    }

    private fun toggleFlashlight(Ok: Boolean) {

        if (Ok) {
            var light: Boolean
            val s = "10101010101010101010101010"
            for (i: Int in s.indices) {
                light = s[i] == '1'
                cameraManager.setTorchMode(cameraId, light)
                Thread.sleep(90)
            }
        }
    }

    private fun switchFlashLight(status: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, status)
            }
            handler = Handler()
            handler!!.postDelayed(Runnable {
                switchFlashLight(false)
            }, 9000)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
    private fun continuousFlashLight(status: Boolean) {

        try {

            if (status)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cameraManager.setTorchMode(cameraId, true)
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
}
