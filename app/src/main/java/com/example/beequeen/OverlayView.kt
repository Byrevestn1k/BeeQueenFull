package com.example.beequeen

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.hypot

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ---------- paints ----------
    private val queenPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.MAGENTA
        isAntiAlias = true
    }

    private val otherPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.RED
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 42f
        style = Paint.Style.FILL
        isAntiAlias = true
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

    // EMA по прямокутнику тільки для матки
    private val queenSmoother = QueenEmaSmoother()

    // ✅ EMA окремо для радіуса кола
    private var lastEmaQueenRadius: Float? = null
    private val radiusAlpha = 0.18f
    private val radiusMaxJumpRatio = 0.60f

    // Множник для mark_queen (бо бокс мітки значно менший за тіло матки)
    private val markedQueenRadiusMultiplier = 3.5f

    // ✅ Hold last queen circle (1 сек)
    private val queenHoldDurationMs = 1000L
    private var lastQueenCircleRect: RectF? = null
    private var lastQueenCircleRadius: Float? = null
    private var lastQueenScore: Float = 0f
    private var lastSeenQueenMs: Long = 0L

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

        // ❗ ВАЖЛИВО:
        // Якщо матка зникла — НЕ скидаємо EMA одразу.
        // Hold/Reset вирішуємо в onDraw() по таймеру.
        invalidate()
    }

    // =========================================================

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (imageWidth == 0 || imageHeight == 0) return

        val now = System.currentTimeMillis()
        val hasQueenNow = results.any { it.label == "beequeen" || it.label == "mark_queen" }

        // 1) Якщо матки зараз НЕМА — але є останнє коло і не пройшла 1 секунда:
        if (!hasQueenNow) {
            val rect = lastQueenCircleRect
            val radius = lastQueenCircleRadius

            if (rect != null && radius != null && (now - lastSeenQueenMs) <= queenHoldDurationMs) {
                queenPaint.color = classColors["beequeen"] ?: Color.MAGENTA
                canvas.drawCircle(rect.centerX(), rect.centerY(), radius, queenPaint)

                drawLabel(
                    canvas,
                    "queen",
                    lastQueenScore,
                    rect
                )
                return
            }

            // 2) Якщо час утримання вийшов — тоді вже скидаємо все по матці
            if (rect != null && (now - lastSeenQueenMs) > queenHoldDurationMs) {
                lastQueenCircleRect = null
                lastQueenCircleRadius = null
                lastQueenScore = 0f
                lastSeenQueenMs = 0L

                lastEmaQueenRadius = null
                queenSmoother.reset()
            }
        }

        // Якщо нема взагалі чого малювати — вихід
        if (results.isEmpty()) return

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

            val isQueen = result.label == "beequeen" || result.label == "mark_queen"

            if (isQueen) {
                drawQueenCircle(canvas, rect, result.score, result.label)
            } else {
                val paint = otherPaint.apply {
                    color = classColors[result.label] ?: Color.RED
                }
                canvas.drawRect(rect, paint)
                drawLabel(canvas, result.label, result.score, rect)
            }
        }
    }

    // ---------- QUEEN AS CIRCLE ----------
    private fun drawQueenCircle(
        canvas: Canvas,
        rect: RectF,
        score: Float,
        label: String
    ) {
        // EMA по прямокутнику (центр/розміри)
        val smoothRect = queenSmoother.smooth(rect)

        // центр прямокутника (перетин діагоналей)
        val cx = smoothRect.centerX()
        val cy = smoothRect.centerY()

        // діагональ
        val diag = hypot(
            smoothRect.width().toDouble(),
            smoothRect.height().toDouble()
        ).toFloat()

        // базове правило: діаметр = діагональ * 3 => radius = diag * 1.5
        var radius = (diag * 3f) / 2f

        // якщо це mark_queen (мітка) — радіус ще збільшуємо
        if (label == "mark_queen") {
            radius *= markedQueenRadiusMultiplier
        }

        // EMA по радіусу (щоб не "дихало")
        radius = smoothRadius(radius)

        // матка завжди одного кольору (beequeen)
        queenPaint.color = classColors["beequeen"] ?: Color.MAGENTA

        canvas.drawCircle(cx, cy, radius, queenPaint)

        // запамʼятовуємо останню матку для hold-режиму
        lastQueenCircleRadius = radius
        lastQueenCircleRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        lastQueenScore = score
        lastSeenQueenMs = System.currentTimeMillis()

        drawLabel(
            canvas,
            "queen",
            score,
            lastQueenCircleRect!!
        )
    }

    private fun smoothRadius(current: Float): Float {
        val prev = lastEmaQueenRadius
        if (prev == null) {
            lastEmaQueenRadius = current
            return current
        }

        // різкий стрибок — приймаємо як є
        if (isRadiusTooFar(prev, current)) {
            lastEmaQueenRadius = current
            return current
        }

        val smoothed = prev * (1f - radiusAlpha) + current * radiusAlpha
        lastEmaQueenRadius = smoothed
        return smoothed
    }

    private fun isRadiusTooFar(prev: Float, curr: Float): Boolean {
        val base = prev.coerceAtLeast(1f)
        val diffRatio = abs(curr - prev) / base
        return diffRatio > radiusMaxJumpRatio
    }

    // ---------- LABEL ----------
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
