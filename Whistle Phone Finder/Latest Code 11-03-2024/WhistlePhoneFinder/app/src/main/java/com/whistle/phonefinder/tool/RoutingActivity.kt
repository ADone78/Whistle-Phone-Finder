package com.whistle.phonefinder.tool

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class RoutingActivity : AppCompatActivity() {
    var handler: Handler? = null
    var PERMISSION_CODE = 123
    var PERMISSIONS =
        arrayOf(
            Manifest.permission.RECORD_AUDIO
        )
    var PERMISSIONSNotification = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS
    )
    private lateinit var appOpen: AppOpenManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appOpen = AppOpenManager(this)
        installSplashScreen().apply {
            setOnExitAnimationListener { splashScreen ->
                if (hasRecordingPermissions(this@RoutingActivity,*PERMISSIONS,*PERMISSIONSNotification))
                {
                    if (checkSystemWritePermission()) {

                        val intent = Intent(this@RoutingActivity, MainActivity::class.java)
                        startActivity(intent)
                    }
                    else
                    {
                        val intent = Intent(this@RoutingActivity, Permission_activity::class.java)
                        startActivity(intent)
                    }

                }
                else
                {
                    val intent = Intent(this@RoutingActivity, Permission_activity::class.java)
                    startActivity(intent)
                }
            }
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
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {

        if (requestCode == PERMISSION_CODE) {
            if (mediaRecorderPermissionsGranted(grantResults)) {

                if (hasRecordingPermissions(this,*PERMISSIONS,*PERMISSIONSNotification))
                {
                    val intent = Intent(this@RoutingActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    val intent = Intent(this@RoutingActivity, Permission_activity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(
                    this,
                    "permissionsNotGranted",
                    Toast.LENGTH_LONG
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
/*    private fun appSetup() {
        if (getLanguageStatus(this@RoutingActivity)) {
            if (hasCameraPermissions(this@RoutingActivity) && hasPhotoMediaPermissions(
                    this@RoutingActivity
                ) && hasLocationPermissions(this@RoutingActivity)
            ) {



                delayHandler(10) {
                    startActivity(
                        Intent(
                            this@RoutingActivity, MainActivity::class.java
                        )
                    )
                }
            } else {
                delayHandler(10) {
                    startActivity(
                        Intent(
                            this@RoutingActivity, PermissionActivity::class.java
                        )
                    )
                }
            }
        } else {
            startActivity(
                Intent(
                    this@RoutingActivity, LanguageSelectionActivity::class.java
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (isAppFinished) {
            isAppFinished = false

            delayHandler(10) {
                appSetup()
            }
        }

    }*/
}