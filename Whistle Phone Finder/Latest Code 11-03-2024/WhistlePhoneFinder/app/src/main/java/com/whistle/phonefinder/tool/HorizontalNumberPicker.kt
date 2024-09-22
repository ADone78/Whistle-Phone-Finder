package com.whistle.phonefinder.tool

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

class HorizontalNumberPicker(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint()
    private val minValue = 0
    private val maxValue = 100
    private val stepSize = 10
    private var currentValue = 10

    init {
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val text = currentValue.toString()
        val textX = width / 2f
        val textY = height / 2f

        canvas.drawText(text, textX, textY, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val newValue = calculateValueFromX(event.x)
                if (newValue in minValue..maxValue && newValue % stepSize == 0) {
                    currentValue = newValue
                    if (newValue==currentValue) {
                        Toast.makeText(context, newValue.toString(), Toast.LENGTH_SHORT).show()
                    }
                    invalidate()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun calculateValueFromX(x: Float): Int {
        val range = (maxValue - minValue) / stepSize
        val relativeX = x / width
        return (minValue + (relativeX * range * stepSize)).toInt()
    }

    fun setValue(value: Int, i: Int) {
        if (value in minValue..maxValue && value % stepSize == 0) {
            currentValue = value
            invalidate()
        }
    }
}