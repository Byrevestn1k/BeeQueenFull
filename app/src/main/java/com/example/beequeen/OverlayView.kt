package com.example.beequeen

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min
class OverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // кола для кожного класу
    private val paints = mapOf(
        "queenbee"  to Paint().apply { color = Color.MAGENTA; style = Paint.Style.STROKE; strokeWidth = 8f },
        "drone" to Paint().apply { color = Color.GREEN;   style = Paint.Style.STROKE; strokeWidth = 8f },
        "mark_queen"  to Paint().apply { color = Color.RED;     style = Paint.Style.STROKE; strokeWidth = 8f },
        "workbee" to Paint().apply { color = Color.YELLOW;  style = Paint.Style.STROKE; strokeWidth = 8f }
    )

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        style = Paint.Style.FILL
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private var results: List<DetectorHelper.DetectionResult> = emptyList()

    fun setResults(results: List<DetectorHelper.DetectionResult>) {
        this.results = results
        invalidate()          // перемалювати
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (results.isEmpty()) return

        val viewW = width.toFloat()
        val viewH = height.toFloat()

        // модель завжди 640×640
        val modelW = 640f
        val modelH = 640f

        // 1. КАК МОДЕЛЬ ВМІЩАЄТЬСЯ В КАМЕРУ (реальну картинку)
        // Масштаб для letterbox (беремо мінімальний → додає padding)
        val scale = min(viewW / modelW, viewH / modelH)

        // 2. Letterbox padding
        val padX = (viewW - modelW * scale) / 2f
        val padY = (viewH - modelH * scale) / 2f

        for (r in results) {

            val paint = paints[r.label] ?: paints["queenbee"]!!

            // YOLO нормалізує координати відносно 640x640
            val x1 = r.box.left * modelW
            val y1 = r.box.top * modelH
            val x2 = r.box.right * modelW
            val y2 = r.box.bottom * modelH  // може бути >640 → норм

            // масштабування + додавання padding
            val screenBox = RectF(
                x1 * scale + padX,
                y1 * scale + padY,
                x2 * scale + padX,
                y2 * scale + padY
            )

            canvas.drawRect(screenBox, paint)
            canvas.drawText(
                "${r.label} ${(r.score * 100).toInt()}%",
                screenBox.left,
                screenBox.top - 10,
                textPaint
            )
        }
    }



}