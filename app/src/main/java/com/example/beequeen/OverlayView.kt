package com.example.beequeen

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ---------- paints ----------
    private val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.RED
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 42f
        style = Paint.Style.FILL
    }

    private val textBgPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        alpha = 160
    }

    // ---------- state ----------
    private var results: List<DetectorHelper.DetectionResult> = emptyList()

    private var imageWidth = 0
    private var imageHeight = 0

    private val enabledClasses = mutableMapOf<String, Boolean>()
    private val classColors = mutableMapOf<String, Int>()

    // EMA тільки для матки
    private val queenSmoother = QueenEmaSmoother()

    // =========================================================
    // === API, ЯКИЙ ЧЕКАЄ LiveActivity (НЕ МІНЯЄМО!) ===========
    // =========================================================

    fun setFrameInfo(imageWidth: Int, imageHeight: Int) {
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
    }

    fun setClassEnabled(label: String, enabled: Boolean) {
        enabledClasses[label] = enabled
        invalidate()
    }

    fun setClassColor(label: String, color: Int) {
        classColors[label] = color
        invalidate()
    }

    fun setResults(detectionResults: List<DetectorHelper.DetectionResult>) {
        this.results = detectionResults

        // якщо матки нема — скидаємо EMA
        if (detectionResults.none { it.label == "beequeen" }) {
            queenSmoother.reset()
        }

        invalidate()
    }

    // =========================================================

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (results.isEmpty() || imageWidth == 0 || imageHeight == 0) return

        val scaleX = width.toFloat() / imageWidth
        val scaleY = height.toFloat() / imageHeight

        for (result in results) {

            if (enabledClasses[result.label] == false) continue

            val src = result.box
            val rect = RectF(
                src.left * scaleX,
                src.top * scaleY,
                src.right * scaleX,
                src.bottom * scaleY
            )

            val rectToDraw =
                if (result.label == "beequeen") {
                    queenSmoother.smooth(rect)
                } else {
                    rect
                }

            boxPaint.color =
                classColors[result.label]
                    ?: if (result.label == "beequeen") Color.MAGENTA else Color.RED

            canvas.drawRect(rectToDraw, boxPaint)
            drawLabel(canvas, result.label, result.score, rectToDraw)
        }
    }

    private fun drawLabel(
        canvas: Canvas,
        label: String,
        score: Float,
        rect: RectF
    ) {
        val text = "$label ${(score * 100).toInt()}%"

        val textWidth = textPaint.measureText(text)
        val textHeight = textPaint.textSize

        val left = rect.left
        val top = rect.top - textHeight - 8f

        val bgRect = RectF(
            left,
            top,
            left + textWidth + 16f,
            top + textHeight + 12f
        )

        canvas.drawRect(bgRect, textBgPaint)
        canvas.drawText(text, left + 8f, top + textHeight, textPaint)
    }
}
