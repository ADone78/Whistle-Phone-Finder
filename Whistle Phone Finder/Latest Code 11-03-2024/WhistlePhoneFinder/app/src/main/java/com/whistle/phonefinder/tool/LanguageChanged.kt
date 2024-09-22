package com.whistle.phonefinder.tool

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import java.util.*


const val LOCAL_PREF = "local_pref"
const val KEY_LOCAL = "local"

object LanguageChanged {

    @JvmStatic
    fun wrapContext(context: Context): Context {
        // as part of creating a new context that contains the new locale we also need to override the default locale.
        val savedLocale = createLocaleFromSavedLanguage(context)
        // as part of creating a new context that contains the new locale we also need to override the default locale.

        Log.d("locale", "current Locale $savedLocale")
        Locale.setDefault(savedLocale)
        //create new configuration with the saved locale
        val newConfig = Configuration(context.resources.configuration)
        newConfig.setLocale(savedLocale)

        return context.createConfigurationContext(newConfig)
    }

    @JvmStatic
    fun createLocaleFromSavedLanguage(mContext: Context): Locale {
        val local = mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
            .getString(KEY_LOCAL, "en")!!
        return Locale(local)
    }

    @JvmStatic
    fun changeLanguage(mContext: Context, langCode: String) {
        setLanguage(mContext, langCode)

    }

    private fun setLanguage(mContext: Context, text: String) {

        when (text) {
            "Chinese" -> {
                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "zh")
                        apply()
                    }
            }
            "Russian" -> {
                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "ru")
                        apply()
                    }
            }
            "English" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "en")
                        apply()
                    }
            }
            "Arabic" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "ar")
                        apply()
                    }

            }
            "French" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "fr")
                        apply()
                    }
            }
            "Italian" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "it")
                        apply()
                    }
            }
            "Spanish" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "es")
                        apply()
                    }
            }
            "Portuguese" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "pt")
                        apply()
                    }
            }
            "German" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "de")
                        apply()
                    }
            }
            "Indonesian" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "in")
                        apply()
                    }
            }
            "Malaysian" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "ms")
                        apply()
                    }
            }
            "Persian" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "fa")
                        apply()
                    }
            }
            "Turkish" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "tr")
                        apply()
                    }
            }
            "Thai" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "th")
                        apply()
                    }
            }
            "Vietnamese" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "vi")
                        apply()
                    }
            }
            "Polish" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "pl")
                        apply()
                    }
            }
            "Greek" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "el")
                        apply()
                    }
            }
            "Czech" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "cs")
                        apply()
                    }
            }
            "Swedish" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "sv")
                        apply()
                    }
            }
            "Hungarian" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "hu")
                        apply()
                    }
            }
            "Norwegian" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "no")
                        apply()
                    }
            }
            "Japanese" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "ja")
                        apply()
                    }
            }
            "Hindi" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "hi")
                        apply()
                    }
            }
            "Danish" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "da")
                        apply()
                    }
            }
            "Korean" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "ko")
                        apply()
                    }
            }
            "Finish" -> {

                mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
                    .edit().apply {
                        putString(KEY_LOCAL, "fi")
                        apply()
                    }
            }
        }


    }


}