package com.example.beequeen
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
class OverlayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint().apply { color = Color.RED; style = Paint.Style.STROKE; strokeWidth = 6f }
    private var rect: RectF? = null
    fun setRectF(r: RectF?) { rect = r; invalidate() }
    override fun onDraw(canvas: Canvas) { super.onDraw(canvas); rect?.let { canvas.drawRect(it, paint) } }
}
