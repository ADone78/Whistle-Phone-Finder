package com.whistle.phonefinder.tool

import android.content.Context
import android.widget.Toast

    class Toast_Helper_Class
        (var context: Context) {
        fun toastDefine(text: String) {
            Toast.makeText(
                context,
                text.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }
}