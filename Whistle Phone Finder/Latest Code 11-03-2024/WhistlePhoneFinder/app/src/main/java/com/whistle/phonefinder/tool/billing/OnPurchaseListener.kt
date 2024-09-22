package com.whistle.phonefinder.tool.billing

import androidx.annotation.MainThread
import androidx.annotation.StringRes

interface OnPurchaseListener {
    @MainThread
    fun onPurchaseComplete()
    @MainThread
    fun onPurchaseFailed(@StringRes msg:Int)
}