package com.whistle.phonefinder.tool.billing


import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.android.billingclient.api.QueryPurchasesParams
import com.whistle.phonefinder.tool.R

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.whistle.phonefinder.tool.removeAdsPurchasedStatus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class InAppPurchase(private val mContext: Context) : PurchasesUpdatedListener,
    BillingClientStateListener, CoroutineScope {

    private val _productDetailsList = ArrayList<ProductDetails>()
    val productDetailsList: List<ProductDetails> get() = _productDetailsList
    var onQueryProductsListener: OnQueryProductsListener? = null
    var onPurchaseListener: OnPurchaseListener? = null


    private val billingClient =
        BillingClient.newBuilder(mContext).enablePendingPurchases().setListener(this).build()

    init {
        billingClient.startConnection(this)
        initPreferences()
    }

    fun startPurchase(activity: Activity, productDetails: ProductDetails) {
        val billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails).build()
            )
        ).build()
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    private fun initPreferences() {


    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult, purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingResponseCode.USER_CANCELED) {
            onPurchaseListener?.onPurchaseFailed(R.string.user_canceled)
            return
        }
        if (billingResult.responseCode != BillingResponseCode.OK || purchases.isNullOrEmpty()) {
            onPurchaseListener?.onPurchaseFailed(R.string.purchase_failed)
            return
        }
        val purchase = purchases.first()
        handlePurchase(purchase)

    }

    private fun handlePurchase(purchase: Purchase) {
        val purchaseToken = purchase.purchaseToken
        val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
        billingClient.consumeAsync(consumeParams) { billingResult, _ ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                onPurchaseListener?.onPurchaseComplete()
                updateSubscriptionStatus()
            }
        }
    }

    override fun onBillingServiceDisconnected() {

    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingResponseCode.OK) {
            updateSubscriptionStatus()
            queryProducts()
        }
    }

    private fun updateSubscriptionStatus() = launch {
        val purchaseList = getPurchases()
        Log.d(TAG, "purchaseListing: $purchaseList")
        val purchased = purchaseList.any {
            it.purchaseState == PurchaseState.PURCHASED
        }
        if (purchased.not() && purchaseList.isNotEmpty()) {
            val purchase = purchaseList.first()
            if (purchaseList.first().purchaseState == PurchaseState.PENDING) {
                handlePurchase(purchase)
            }
        }
        for (it in purchaseList) {
            Log.d(TAG, "purchaseList: $it")
            if (it.products[0].toString() != "") {
                Log.d(TAG, it.products[0].toString())
                mContext.removeAdsPurchasedStatus(mContext, true)
            }
        }
    }

    private suspend fun getPurchases() = suspendCoroutine<List<Purchase>> { continuation ->
        val queryPurchasesParams =
            QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP).build()
        billingClient.queryPurchasesAsync(queryPurchasesParams) { billingResult, detailsList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                continuation.resume(detailsList)
            } else {
                continuation.resume(emptyList())
            }
        }

    }

    private fun queryProducts() = launch {
        val queriedProducts = getQueriedProducts()
        Log.d(TAG, "queryProducts: $queriedProducts")
        _productDetailsList.addAll(queriedProducts)
        onQueryProductsListener?.onQueryProducts(queriedProducts)
    }


    private suspend fun getQueriedProducts() =
        suspendCoroutine<List<ProductDetails>> { continuation ->
            val queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder().setProductList(getProductList()).build()
            billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, detailsList ->
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    if (isActive) {
                        continuation.resume(detailsList)
                    }
                } else {
                    if (isActive) {
                        continuation.resume(emptyList())
                    }
                }
            }
        }

    private fun getProductList(): List<Product> {
        return listOf(
            Product.newBuilder().setProductId(REMOVE_ADS_PURCHASED)
                .setProductType(ProductType.INAPP).build()
        )
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    companion object {
        private const val TAG = "InAppPurchase"
        const val REMOVE_ADS_PURCHASED = "com.whistle.phone.finder.remove.ads"
    }


}