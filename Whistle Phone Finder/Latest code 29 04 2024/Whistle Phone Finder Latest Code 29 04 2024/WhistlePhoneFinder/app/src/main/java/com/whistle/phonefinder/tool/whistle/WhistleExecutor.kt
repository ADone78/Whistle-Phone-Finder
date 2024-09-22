package com.whistle.phonefinder.tool.whistle

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class WhistleExecutor(
    context: Context
) {
    private var predictionTime = 0L
    private var labels_class: List<String>
    private var nThreads = 2
    private var interpreter: Interpreter

    init {
        interpreter = getInterpreters(context, "whistle_model.tflite")
        labels_class = FileUtil.loadLabels(context, "available_classes.txt")
    }

    fun startExecution(floatingInputs: FloatArray): Pair<ArrayList<String>, ArrayList<Float>> {
        predictionTime = System.currentTimeMillis()
        val inputValues = floatingInputs
        val inputsArray = arrayOf<Any>(inputValues)
        val outputsArray = HashMap<Int, Any>()
        val scores = Array(4) { FloatArray(521) { 0f } }
        val embeddings = Array(4) { FloatArray(1024) { 0f } }
        val spectograms = Array(240) { FloatArray(64) { 0f } }

        outputsArray[0] = scores
        outputsArray[1] = embeddings
        outputsArray[2] = spectograms
        Log.d("exp", outputsArray.toString())
        Log.d("exp", inputsArray.toString())
        try {
            interpreter.runForMultipleInputsOutputs(inputsArray, outputsArray)
        } catch (e: Exception) {
            Log.e("EXCEPTION", e.toString())
        }

        val arrayMeanScores = FloatArray(521) { 0f }
        for (i in 0 until 521) {
            // Find the average of the 4 arrays at axis = 0
            arrayMeanScores[i] = arrayListOf(
                scores[0][i],
                scores[1][i],
                scores[2][i],
                scores[3][i]
            ).average().toFloat()
        }

        val listOfArrayMeanScores = arrayMeanScores.toCollection(ArrayList())

        val listOfMaximumValues = arrayListOf<Float>()
        for (i in 0 until 10) {
            val number = listOfArrayMeanScores.max()
            listOfMaximumValues.add(number)
            listOfArrayMeanScores.remove(number)
        }

        val listOfMaxIndices = arrayListOf<Int>()
        for (i in 0 until 10) {
            for (k in arrayMeanScores.indices) {
                if (listOfMaximumValues[i] == arrayMeanScores[k]) {
                    listOfMaxIndices.add(k)
                }
            }
        }

        val finalListOfOutputs = arrayListOf<String>()
        for (i in listOfMaxIndices.indices) {
            finalListOfOutputs.add(labels_class[listOfMaxIndices[i]])
        }

        return Pair(finalListOfOutputs, listOfMaximumValues)
    }

    @Throws(IOException::class)
    private fun loadingModelFiles(context: Context, modelFile: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelFile)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val retFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        fileDescriptor.close()
        return retFile
    }

    @Throws(IOException::class)
    private fun getInterpreters(
        context: Context,
        modelName: String
    ): Interpreter {
        val tfliteOptions = Interpreter.Options()
        tfliteOptions.setNumThreads(nThreads)
        return Interpreter(loadingModelFiles(context, modelName), tfliteOptions)
    }

    fun closeExecution() {
        interpreter.close()
    }
}