package com.example.beequeen

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var rects: List<Pair<RectF, String>> = emptyList()

    private val boxPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val textPaint = Paint().apply {
        color = Color.YELLOW
        textSize = 40f
        style = Paint.Style.FILL
    }

    fun setRectF(newRects: List<Pair<RectF, String>>) {
        rects = newRects
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for ((rect, label) in rects) {
            canvas.drawRect(rect, boxPaint)
            canvas.drawText(label, rect.left, rect.top - 10, textPaint)
        }
    }
}
