package com.whistle.phonefinder.tool

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.whistle.phonefinder.tool.databinding.ActivityPremiumBinding
import com.whistle.phonefinder.tool.billing.InAppPurchase
import com.whistle.phonefinder.tool.billing.OnPurchaseListener
import kotlinx.coroutines.launch


class PremiumActivity : AppCompatActivity(), OnPurchaseListener {
    lateinit var binding: ActivityPremiumBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // inAppPurchase
        inAppPurchase.onPurchaseListener = this@PremiumActivity

        window.statusBarColor = ContextCompat.getColor(this, R.color.statusbar_color)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.statusbar_color)
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        binding.btnBuy.setOnClickListener {
            startPaymentRequest()
        }
        binding.back.setOnClickListener {
            val intent = Intent(this@PremiumActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (currentAppLanguage(this)=="Arabic")
        {
            binding.back.setBackgroundResource(R.drawable.back_ar)

        }
        else  if (currentAppLanguage(this)=="Persian")
        {
            binding.back.setBackgroundResource(R.drawable.back_ar)
        }
        else
        {
            binding.back.setBackgroundResource(R.drawable.back)

        }


    }
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LanguageChanged.wrapContext(base!!))
    }

    override fun onResume() {
        super.onResume()
        updatePriceUI()
    }

    private fun triggerRebirth(context: Context) {
        try {
            val packageManager: PackageManager = context.packageManager
            val intent: Intent? = packageManager.getLaunchIntentForPackage(context.packageName)
            val componentName: ComponentName? = intent!!.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
            context.startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        } catch (e: Exception) {
            println(e.toString())
        }
    }

    private fun paymentSuccessfulLayout(text: String) {
        try {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            /* val factory = LayoutInflater.from(this)
             val mainDialogView: View = factory.inflate(R.layout.payment_success_dialog, null)
             val Dialog: AlertDialog = AlertDialog.Builder(this).create()
             Dialog.setView(mainDialogView)
             Dialog.setCancelable(true)
             Dialog.window!!.setBackgroundDrawableResource(R.drawable.rounded_corner_bg);
             Dialog.show()
             Dialog.setOnDismissListener {}
             mainDialogView.tv_titleSuccess.text = text.toString()
             mainDialogView.btn_restart.setOnClickListener {
                 delay.handlerDefine(0) {

                     triggerRebirth(this)
                 }
                 Dialog.dismiss()
             }
             mainDialogView.btn_later.setOnClickListener {
                 Dialog.dismiss()
             }*/
        } catch (e: Exception) {
            println(e.toString())
        }

    }

    private fun startPaymentRequest() {
        val productDetailsList = inAppPurchase.productDetailsList
        for (it in productDetailsList) {
            inAppPurchase.startPurchase(this, it)
        }
    }


    private fun showPurchaseSuccessAlert() {
        //TODO show purchase success dialog
        paymentSuccessfulLayout(getString(R.string.purchasedSuccessfully))
    }

    val productInfo = HashMap<String, ProductDetails>()
    private fun updatePriceUI() {
        val productInfoList = inAppPurchase.productDetailsList
        if (productInfoList.isEmpty()) return

        productInfoList.forEach { productInfo[it.productId] = it }

        val removeAdsProduct = productInfo[InAppPurchase.REMOVE_ADS_PURCHASED]

        val price =
            removeAdsProduct?.oneTimePurchaseOfferDetails?.formattedPrice.toString()

        if (isRemoveAdsPurchased(this)) {
            binding.btnBuy.text = resources.getString(R.string.bought)
            binding.btnBuy.isEnabled = false
        } else {
            binding.btnBuy.text = "${resources.getString(R.string.buyFor)}  ${price}"
            binding.btnBuy.isEnabled = true
        }


    }


    override fun onPurchaseComplete() = runOnUiThread {

        lifecycleScope.launch {
            removeAdsPurchasedStatus(this@PremiumActivity, true)
        }

        showPurchaseSuccessAlert()
    }


    override fun onPurchaseFailed(msg: Int) = runOnUiThread {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}
private val android.content.Context.preferences: SharedPreferences
    get() = getSharedPreferences("inAppPurchase_preferences", Context.MODE_PRIVATE)
fun Context.isRemoveAdsPurchased(mContext: Context): Boolean {
    return this.preferences.getBoolean("inAppPurchase", false)
}

fun Context.removeAdsPurchasedStatus(mContext: Context, bool: Boolean) {
    mContext.preferences.edit {
        putBoolean("inAppPurchase", bool)
    }
}