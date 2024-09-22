package com.whistle.phonefinder.tool.billing

import androidx.annotation.MainThread
import com.android.billingclient.api.ProductDetails

interface OnQueryProductsListener {
    @MainThread
    fun onQueryProducts(products: List<ProductDetails>)
}