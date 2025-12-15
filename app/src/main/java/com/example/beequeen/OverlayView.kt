package com.example.beequeen

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var results: List<DetectorHelper.DetectionResult> = emptyList()
    private var srcWidth = 0
    private var srcHeight = 0

    private val boxPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        style = Paint.Style.FILL
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    fun setFrameInfo(w: Int, h: Int) {
        srcWidth = w
        srcHeight = h
    }

    fun setResults(results: List<DetectorHelper.DetectionResult>) {
        this.results = results
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (srcWidth == 0 || srcHeight == 0) return
        if (results.isEmpty()) return

        val viewW = width.toFloat()
        val viewH = height.toFloat()

        val scaleX = viewW / srcWidth
        val scaleY = viewH / srcHeight

        for (r in results) {
            val left   = r.box.left   * scaleX
            val top    = r.box.top    * scaleY
            val right  = r.box.right  * scaleX
            val bottom = r.box.bottom * scaleY

            canvas.drawRect(left, top, right, bottom, boxPaint)

            val label = "${r.label} ${(r.score * 100).toInt()}%"
            canvas.drawText(label, left, top - 10f, textPaint)
        }
    }
}
