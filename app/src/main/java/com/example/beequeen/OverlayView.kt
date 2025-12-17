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

    companion object {
        private const val PREFS_NAME = "beequeen_prefs"
        private const val KEY_STICKY_QUEEN = "sticky_queen"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun stickyEnabled(): Boolean =
        prefs.getBoolean(KEY_STICKY_QUEEN, false)

    // ===============================
    // Paints
    // ===============================
    private val queenPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
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
        isAntiAlias = true
    }

    private val textBgPaint = Paint().apply {
        color = Color.BLACK
        alpha = 160
    }

    // ===============================
    // State
    // ===============================
    private var results: List<DetectorHelper.DetectionResult> = emptyList()
    private var imageWidth = 0
    private var imageHeight = 0

    private val enabledClasses = mutableMapOf<String, Boolean>()
    private val classColors = mutableMapOf<String, Int>()

    private val queenSmoother = QueenEmaSmoother()

    private var lastEmaRadius: Float? = null
    private val radiusAlpha = 0.18f
    private val radiusMaxJumpRatio = 0.6f
    private val markedQueenRadiusMultiplier = 3.5f

    private val queenHoldDurationMs = 1000L
    private var lastQueenRect: RectF? = null
    private var lastQueenRadius: Float? = null
    private var lastQueenScore = 0f
    private var lastSeenMs = 0L

    // Sticky tracking state
    private var lastStickyEnabled = false
    private var locked = false
    private var lastQueenCx: Float? = null
    private var lastQueenCy: Float? = null

    // ===============================
    // API (НЕ МІНЯЄМО)
    // ===============================
    fun setFrameInfo(w: Int, h: Int) {
        imageWidth = w
        imageHeight = h
    }

    fun setClassEnabled(label: String, enabled: Boolean) {
        enabledClasses[label] = enabled
        invalidate()
    }

    fun setClassColor(label: String, color: Int) {
        classColors[label] = color
        invalidate()
    }

    fun setResults(r: List<DetectorHelper.DetectionResult>) {
        results = r
        invalidate()
    }

    // ===============================
    // Draw
    // ===============================
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (imageWidth == 0 || imageHeight == 0) return

        val now = System.currentTimeMillis()
        val sticky = stickyEnabled()

        // якщо sticky вимкнули — скидаємо lock
        if (lastStickyEnabled && !sticky) {
            clearLock()
        }
        lastStickyEnabled = sticky

        if (results.isEmpty()) {
            // якщо детекцій нема, але є last queen — відмалюємо hold/fade (v1.5)
            drawHoldFadeIfNeeded(canvas, now, sticky)
            return
        }

        val sx = width.toFloat() / imageWidth
        val sy = height.toFloat() / imageHeight

        val queens = ArrayList<Pair<DetectorHelper.DetectionResult, RectF>>()
        val others = ArrayList<Pair<DetectorHelper.DetectionResult, RectF>>()

        for (r in results) {
            if (enabledClasses[r.label] == false) continue

            val rect = RectF(
                r.box.left * sx,
                r.box.top * sy,
                r.box.right * sx,
                r.box.bottom * sy
            )

            if (r.label == "beequeen" || r.label == "mark_queen") {
                queens.add(r to rect)
            } else {
                others.add(r to rect)
            }
        }

        // спершу малюємо НЕ-матку
        for ((r, rect) in others) {
            otherPaint.color = classColors[r.label] ?: Color.RED
            canvas.drawRect(rect, otherPaint)
            drawLabel(canvas, r.label, r.score, rect)
        }

        // далі — матка (тільки ОДНА)
        val selected = selectQueen(queens, sticky)
        if (selected == null) {
            drawHoldFadeIfNeeded(canvas, now, sticky)
            return
        }

        val (qr, qrect) = selected
        drawQueen(canvas, qrect, qr.score, qr.label)
    }

    private fun selectQueen(
        queens: List<Pair<DetectorHelper.DetectionResult, RectF>>,
        sticky: Boolean
    ): Pair<DetectorHelper.DetectionResult, RectF>? {
        if (queens.isEmpty()) return null

        // Sticky OFF → як v1.5: top-1 queen
        if (!sticky) {
            return queens.maxByOrNull { it.first.score }
        }

        // Sticky ON:
        // 1) якщо ще не locked — lock на top-1
        if (!locked || lastQueenCx == null || lastQueenCy == null) {
            val top = queens.maxByOrNull { it.first.score } ?: return null
            locked = true
            return top
        }

        // 2) якщо locked — беремо найближчу матку до попереднього центру
        val cx = lastQueenCx!!
        val cy = lastQueenCy!!

        return queens.minByOrNull { (_, rect) ->
            val dx = rect.centerX() - cx
            val dy = rect.centerY() - cy
            dx * dx + dy * dy
        }
    }

    private fun drawHoldFadeIfNeeded(canvas: Canvas, now: Long, sticky: Boolean) {
        if (lastQueenRect == null || lastQueenRadius == null) return

        val elapsed = now - lastSeenMs
        if (elapsed <= queenHoldDurationMs) {
            val alpha = ((1f - elapsed.toFloat() / queenHoldDurationMs) * 255)
                .toInt().coerceIn(0, 255)
            queenPaint.alpha = alpha

            canvas.drawCircle(
                lastQueenRect!!.centerX(),
                lastQueenRect!!.centerY(),
                lastQueenRadius!!,
                queenPaint
            )
            drawLabel(canvas, "queen", lastQueenScore, lastQueenRect!!)
        } else {
            resetQueen()
            if (sticky) clearLock()
        }
    }

    private fun drawQueen(canvas: Canvas, rect: RectF, score: Float, label: String) {
        val smooth = queenSmoother.smooth(rect)
        val cx = smooth.centerX()
        val cy = smooth.centerY()

        val diag = hypot(smooth.width(), smooth.height())
        var radius = (diag * 3f) / 2f
        if (label == "mark_queen") radius *= markedQueenRadiusMultiplier
        radius = smoothRadius(radius)

        queenPaint.alpha = 255
        queenPaint.color = classColors["beequeen"] ?: Color.MAGENTA
        canvas.drawCircle(cx, cy, radius, queenPaint)

        // оновлюємо “останню матку”
        lastQueenCx = cx
        lastQueenCy = cy
        lastQueenRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        lastQueenRadius = radius
        lastQueenScore = score
        lastSeenMs = System.currentTimeMillis()

        drawLabel(canvas, "queen", score, lastQueenRect!!)
    }

    private fun smoothRadius(curr: Float): Float {
        val prev = lastEmaRadius ?: run {
            lastEmaRadius = curr
            return curr
        }
        val jump = abs(curr - prev) / prev.coerceAtLeast(1f)
        if (jump > radiusMaxJumpRatio) {
            lastEmaRadius = curr
            return curr
        }
        val smoothed = prev * (1f - radiusAlpha) + curr * radiusAlpha
        lastEmaRadius = smoothed
        return smoothed
    }

    private fun clearLock() {
        locked = false
        lastQueenCx = null
        lastQueenCy = null
    }

    private fun resetQueen() {
        lastQueenRect = null
        lastQueenRadius = null
        lastQueenScore = 0f
        lastSeenMs = 0L
        lastEmaRadius = null
        queenSmoother.reset()
        queenPaint.alpha = 255
    }

    private fun drawLabel(canvas: Canvas, label: String, score: Float, rect: RectF) {
        val text = "$label ${(score * 100).toInt()}%"
        val w = textPaint.measureText(text)
        val h = textPaint.textSize
        val bg = RectF(rect.left, rect.top - h - 8, rect.left + w + 16, rect.top + 4)
        canvas.drawRect(bg, textBgPaint)
        canvas.drawText(text, rect.left + 8, rect.top - 8, textPaint)
    }
}
