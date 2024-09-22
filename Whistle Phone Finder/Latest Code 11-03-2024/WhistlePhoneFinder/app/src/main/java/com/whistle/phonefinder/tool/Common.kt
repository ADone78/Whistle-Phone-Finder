package com.whistle.phonefinder.tool

import android.media.MediaPlayer
import android.media.Ringtone

object Common {

  var istoggleFlash:Boolean=false
  var isswitchFlash:Boolean=false
  var iscontinuousFlash:Boolean=false
  var isVibration:Boolean=false
  var isheartBeatVibration:Boolean=false
  var iscontinuousVibration:Boolean=false
  var isExitBannerLoaded:Boolean=false
  var isPhoneScreenOn: Boolean = true
  var isSmartMode: Boolean = false
  var rewardEarned: Boolean = false
  var isSound1: Boolean = false
  var isSound2: Boolean = false
  var isSound3: Boolean = false
  var ringtoneSound: Boolean = false
  var isMediaPlaying: Boolean = false
  var isServiceRunning: Boolean = true
  var isPermissionok: Boolean = false
  var mpbackground: MediaPlayer? = null
  var rewardearn: Boolean = false
  var isSkip: Boolean = false
  var Add =0

  var retVal = false

   var ringsound:Ringtone?=null
}