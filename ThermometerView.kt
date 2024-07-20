package com.temp.lifestylegps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class ThermometerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var temperature: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()

        // Draw the outer thermometer
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        canvas.drawRect(width * 0.4f, height * 0.1f, width * 0.6f, height * 0.9f, paint)
        canvas.drawCircle(width * 0.5f, height * 0.9f, width * 0.1f, paint)

        // Draw the mercury
        paint.color = Color.RED
        paint.style = Paint.Style.FILL
        val mercuryHeight = height * 0.1f + (height * 0.8f * (temperature / 100f))
        canvas.drawRect(width * 0.41f, mercuryHeight, width * 0.59f, height * 0.9f, paint)
        canvas.drawCircle(width * 0.5f, height * 0.9f, width * 0.09f, paint)

        // Draw the temperature text
        paint.color = Color.BLACK
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("$temperature°C", width * 0.5f, height * 0.05f, paint)

        // Draw the thermometer scale and labels
        drawScale(canvas, width, height)
    }

    private fun drawScale(canvas: Canvas, width: Float, height: Float) {
        paint.color = Color.BLACK
        paint.textSize = 30f
        paint.textAlign = Paint.Align.LEFT

        val startX = width * 0.65f
        val endX = width * 0.7f
        val textX = width * 0.75f

        val scaleStep = height * 0.8f / 10
        for (i in 0..10) {
            val y = height * 0.1f + i * scaleStep
            canvas.drawLine(startX, y, endX, y, paint)
            canvas.drawText("${10 - i}0°C", textX, y + 10f, paint)
        }
    }
}