package com.whistle.phonefinder.tool.whistle

import android.app.Application
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.whistle.phonefinder.tool.Common.isServiceRunning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.dsl.module

class WhistleViewModel(application: Application) : AndroidViewModel(application),
    KoinComponent {

    private val _listeningComplete = MutableLiveData<Boolean>()
    val listeningEnd: LiveData<Boolean>
        get() = _listeningComplete
    private val updateLoopListeningHandler = Handler()
    private val handler = Handler()
    private val handlerStart = Handler()
    private val _inferenceOk = MutableLiveData<Boolean>()
    private var listeningRecorderObject: WhistleRecorder
    private var modelExecutor: WhistleExecutor
    var listeningRunning = false
    private val _listOfClasses = MutableLiveData<Pair<ArrayList<String>, ArrayList<Float>>>()
    val listOfClasses: LiveData<Pair<ArrayList<String>, ArrayList<Float>>>
        get() = _listOfClasses

    init {
        listeningRecorderObject = get()
        modelExecutor = get()
    }

    fun startListening() {
        listeningRunning = true
        listeningRecorderObject.startListening()
    }

    fun stopListening() {
        val stream = listeningRecorderObject.stopListening()
        val streamForInference = listeningRecorderObject.stopListeningForInference()
        listeningRunning = false
        viewModelScope.launch {
            doInference(streamForInference)
        }
    }

    private suspend fun doInference(
        arrayListShorts: ArrayList<Short>
    ) = withContext(Dispatchers.IO) {
        try {


            listeningRecorderObject.reInitializePcm()
            val floatsForInference = FloatArray(arrayListShorts.size)
            if (arrayListShorts != null) {
                for ((index, value) in arrayListShorts.withIndex()) {
                    floatsForInference[index] = (value / 32768F)
                }
                _inferenceOk.postValue(false)
                _listOfClasses.postValue(modelExecutor.startExecution(floatsForInference))
                _inferenceOk.postValue(true)
            }
        } catch (e: Exception) {
            println(e.message)
        }

    }

    fun setUpdateLoopListeningHandler() {
        updateLoopListeningHandler.postDelayed(updateLoopListeningRunnable, 0)
    }

    fun stopAllListening() {
        updateLoopListeningHandler.removeCallbacks(updateLoopListeningRunnable)
        handler.removeCallbacksAndMessages(null)
        handlerStart.removeCallbacksAndMessages(null)
        _listeningComplete.value = true
        listeningRecorderObject.stopListeningForInference()
    }

    private var updateLoopListeningRunnable: Runnable = Runnable {
        run {
            if (isServiceRunning) {
                Log.d("whistleViewModel", "updateLoopListeningRunnable")
                startListening()
                _listeningComplete.value = false
                val handler = Handler()
                handler.postDelayed({
                    stopListening()
                }, 2048L)
                updateLoopListeningHandler.postDelayed(
                    updateLoopListeningRunnable,
                    2048L
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAllListening()
        modelExecutor.closeExecution()

    }
}

val whistleModule = module {
    single { WhistleRecorder(0, get()) }
    factory { WhistleExecutor(get()) }
    viewModel {
        WhistleViewModel(get())
    }
}