package com.whistle.phonefinder.tool.whistle

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.io.ByteArrayOutputStream

class WhistleRecorder(
    numberRecordings: Int,
    context: Context

) {
    private var recording: Boolean = true
    private var thread: Thread? = null
    private val lengths: DoubleArray
    private var Samples: Int
    private val mContext: Context
    private var done = false
    private val SR = 16000
    private val CH_M = AudioFormat.CHANNEL_IN_MONO
    private val ENCOD = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER = AudioRecord.getMinBufferSize(SR, CH_M, ENCOD)
    var buffer = ShortArray(BUFFER)
    var bufferForInference: ArrayList<Short>
    var mPcmStreams: ByteArrayOutputStream
    private var recorder: AudioRecord? = null
    private val AUD_S = MediaRecorder.AudioSource.VOICE_RECOGNITION
    private val AUD_F =
        AudioFormat.Builder().setEncoding(ENCOD)
            .setSampleRate(SR)
            .setChannelMask(CH_M)
            .build()

    init {
        mPcmStreams = ByteArrayOutputStream()
        bufferForInference = arrayListOf()
        recording = false
        lengths = DoubleArray(numberRecordings)
        Samples = 0
        mContext = context
    }


    fun startListening() {
        recorder = AudioRecord.Builder().setAudioSource(AUD_S)
            .setAudioFormat(AUD_F)
            .setBufferSizeInBytes(BUFFER)
            .build()
        done = false
        recording = true
        recorder?.startRecording()
        thread = Thread(readVoice)
        thread!!.start()

    }

    fun stopListening(): ByteArrayOutputStream {
        if (recorder != null && recorder!!.state == AudioRecord.STATE_INITIALIZED) {
            recording = false
            recorder?.stop()
            recorder?.release()
            recorder = null
            done = true
        }
        return mPcmStreams
    }

    fun stopListeningForInference(): ArrayList<Short> {
        return bufferForInference
    }

    private val readVoice = Runnable {
        var readBytes: Int
        buffer = ShortArray(BUFFER)
        while (recording) {
            if (recorder!!.state == AudioRecord.STATE_INITIALIZED) {
                try {
                    readBytes = recorder!!.read(buffer, 0, BUFFER)
                    if (readBytes > 0) {
                        for (i in 0 until readBytes) {
                            buffer[i] = (buffer[i] * 6.7).toInt().coerceAtMost(Short.MAX_VALUE.toInt())
                                .toShort()
                        }
                    }
                    if (readBytes != AudioRecord.ERROR_INVALID_OPERATION) {
                        try {
                            for (s in buffer) {
                                bufferForInference.add(s)
                                writeShort(mPcmStreams, s)
                            }
                        } catch (e: Exception) {

                        }
                    } else {
                        Log.d("vr", "ERROR_INVALID_OPERATION")
                    }
                } catch (e: Exception) {
                    println(e.message)
                }

            }

        }

    }

    private fun writeShort(
        output: ByteArrayOutputStream,
        value: Short
    ) {
        output.write(value.toInt())
        output.write(value.toInt() shr 8)
    }

    fun reInitializePcm() {
        mPcmStreams = ByteArrayOutputStream()
        bufferForInference = arrayListOf()
    }

}