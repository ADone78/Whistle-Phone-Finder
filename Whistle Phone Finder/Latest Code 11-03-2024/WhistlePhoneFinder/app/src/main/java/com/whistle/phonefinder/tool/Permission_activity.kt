package com.whistle.phonefinder.tool

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.whistle.phonefinder.tool.Common.isPermissionok
import com.whistle.phonefinder.tool.Common.retVal
import com.whistle.phonefinder.tool.common_ads.appOpenAd
import com.whistle.phonefinder.tool.common_ads.isShowingAppOpenAd
import com.whistle.phonefinder.tool.databinding.ActivityPermissionBinding

class Permission_activity : AppCompatActivity() {
    lateinit var binding: ActivityPermissionBinding
    var PERMISSION_CODE = 123

  /*  var PERMISSIONSNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {

    }*/
    var PERMISSIONS =
        arrayOf(
            Manifest.permission.RECORD_AUDIO
        )
    var PERMISSIONSNotification = arrayOf(
                Manifest.permission.POST_NOTIFICATIONS
            )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.statusbar_color)
        window.navigationBarColor=ContextCompat.getColor(this,R.color.statusbar_color)
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        if (hasRecordingPermissions(this,*PERMISSIONS,))
        {
            binding.tvmediaaAllow.visibility=View.GONE
            binding.tvmediaAllowed.visibility=View.VISIBLE
        }
        else
        {
            binding.tvmediaaAllow.visibility=View.VISIBLE
            binding.tvmediaAllowed.visibility=View.GONE
        }
        if (hasRecordingPermissions(this,*PERMISSIONSNotification))
        {
            binding.tvNotificationAllow.visibility=View.GONE
            binding.tvNotificationAllowed.visibility=View.VISIBLE
        }
        else
        {
            binding.tvNotificationAllow.visibility=View.VISIBLE
            binding.tvNotificationAllowed.visibility=View.GONE
        }
        if (checkSystemWritePermission()) {
            binding.tvModifyAllow.visibility=View.GONE
            binding.tvModifyAllowed.visibility=View.VISIBLE
        } else {
            binding.tvModifyAllow.visibility=View.VISIBLE
            binding.tvModifyAllowed.visibility=View.GONE
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.notificationLayout.visibility=View.VISIBLE
        }
        else
        {
            binding.notificationLayout.visibility=View.GONE
        }

        binding.tvmediaaAllow.setOnClickListener {
            if (!hasRecordingPermissions(this@Permission_activity, *PERMISSIONS)) {
                checkForPermissions()
            }
        }
        binding.tvNotificationAllow.setOnClickListener {
            if (!hasRecordingPermissions(this@Permission_activity, *PERMISSIONSNotification )) {
                checkForPermissionsNotification()
            }
        }
        binding.tvModifyAllow.setOnClickListener {
            isShowingAppOpenAd = true
            if (checkSystemWriteSettings()==true) {
                binding.tvModifyAllow.visibility= View.GONE
                binding.tvModifyAllowed.visibility= View.VISIBLE
            } else {

                Toast.makeText(this, "Allow modify system settings ==> ON ", Toast.LENGTH_LONG)
                    .show()
            }
        }
        binding.forward.setOnClickListener {

            if (hasRecordingPermissions(this@Permission_activity, *PERMISSIONS)) {
                if (hasRecordingPermissions(this@Permission_activity, *PERMISSIONSNotification )) {
                    if (checkSystemWriteSettings()) {
                        val intent = Intent(this@Permission_activity, MainActivity::class.java)
                        startActivity(intent)
                    }
                    else
                    {
                        Toast.makeText(this, "Please Grant Allow Modify  Permission ", Toast.LENGTH_SHORT).show()
                    }

                }
                else
                {
                    Toast.makeText(this, "Please Grant Notification Permission ", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(this, "Please Grant Media Recorder Permission ", Toast.LENGTH_SHORT).show()
            }



        }
    }

    private fun checkSystemWritePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(this)
        }
        return false
    }

    private fun checkSystemWriteSettings(): Boolean {
        retVal = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(this)
            Log.d("TAG", "Can Write Settings: $retVal")
            if (retVal) {
                binding.tvModifyAllow.visibility= View.GONE
                binding.tvModifyAllowed.visibility= View.VISIBLE

            } else {
                isShowingAppOpenAd = true
                binding.tvModifyAllow.visibility= View.VISIBLE
                binding.tvModifyAllowed.visibility= View.GONE

                openAndroidPermissionsMenu()
            }
        }
        return retVal
    }
    private fun openAndroidPermissionsMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + this.getPackageName())
            this.startActivity(intent)

            isPermissionok =true
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
    private fun checkForPermissionsNotification() {
        if (!hasRecordingPermissions(this, *PERMISSIONSNotification)) {
            requestPermissions(PERMISSIONSNotification , PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {

        if (requestCode == PERMISSION_CODE) {
            if (mediaRecorderPermissionsGranted(grantResults)) {

                if (hasRecordingPermissions(this,*PERMISSIONS,))
                {
                    binding.tvmediaaAllow.visibility=View.GONE
                    binding.tvmediaAllowed.visibility=View.VISIBLE
                  /*  Toast.makeText(
                        this,
                        "allPermissionsGranted",
                        Toast.LENGTH_LONG
                    ).show()*/
                }
                else
                {
                    binding.tvmediaaAllow.visibility=View.VISIBLE
                    binding.tvmediaAllowed.visibility=View.GONE
                }
                if (hasRecordingPermissions(this,*PERMISSIONSNotification))
                {
                    binding.tvNotificationAllow.visibility=View.GONE
                    binding.tvNotificationAllowed.visibility=View.VISIBLE
                }
                else
                {
                    binding.tvNotificationAllow.visibility=View.VISIBLE
                    binding.tvNotificationAllowed.visibility=View.GONE
                }
                if (checkSystemWritePermission()) {
                    binding.tvModifyAllow.visibility=View.GONE
                    binding.tvModifyAllowed.visibility=View.VISIBLE
                } else {

                    binding.tvModifyAllow.visibility=View.VISIBLE
                    binding.tvModifyAllowed.visibility=View.GONE
                }



            } else {

                Toast.makeText(
                    this,
                    "permissions Not Granted",
                    Toast.LENGTH_LONG
                ).show()

            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkSystemWritePermission()) {
            //checkSystemWriteSettings()
            binding.tvModifyAllow.visibility= View.GONE
            binding.tvModifyAllowed.visibility= View.VISIBLE
        } else {
            binding.tvModifyAllow.visibility= View.VISIBLE
            binding.tvModifyAllowed.visibility= View.GONE
            /*Toast.makeText(this, "Allow modify system settings ==> ON ", Toast.LENGTH_LONG)
                .show()*/
        }
    }
    private fun mediaRecorderPermissionsGranted(grantResults: IntArray) = grantResults.all {
        it == PackageManager.PERMISSION_GRANTED
    }
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LanguageChanged.wrapContext(base!!))
    }

    override fun onBackPressed() {
        finish()
        moveTaskToBack(true)
    }
}