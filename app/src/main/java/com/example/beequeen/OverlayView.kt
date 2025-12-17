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
    private fun stickyEnabled() = prefs.getBoolean(KEY_STICKY_QUEEN, false)

    // ================= Paints =================
    private val queenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = Color.MAGENTA
    }

    private val otherPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.RED
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val labelBgPaint = Paint().apply {
        color = Color.BLACK
        alpha = 160
    }

    private val statePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // ================= State =================
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

    private val queenHoldMs = 3000L
    private var lastQueenRect: RectF? = null
    private var lastQueenRadius: Float? = null
    private var lastQueenScore = 0f
    private var lastSeenMs = 0L

    private var locked = false
    private var lastQueenCx: Float? = null
    private var lastQueenCy: Float? = null

    private enum class TrackState { LOCK, SEARCH, LOST }
    private var trackState: TrackState? = null

    // ================= API =================
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

    // ================= Draw =================
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (imageWidth == 0 || imageHeight == 0) return

        val sticky = stickyEnabled()
        val now = System.currentTimeMillis()

        if (!sticky) trackState = null

        if (results.isEmpty()) {
            drawHold(canvas, now, sticky)
            return
        }

        val sx = width.toFloat() / imageWidth
        val sy = height.toFloat() / imageHeight

        val queens = mutableListOf<Pair<DetectorHelper.DetectionResult, RectF>>()
        val others = mutableListOf<Pair<DetectorHelper.DetectionResult, RectF>>()

        for (r in results) {
            if (enabledClasses[r.label] == false) continue
            val rect = RectF(
                r.box.left * sx,
                r.box.top * sy,
                r.box.right * sx,
                r.box.bottom * sy
            )
            if (r.label == "beequeen" || r.label == "mark_queen") queens += r to rect
            else others += r to rect
        }

        for ((r, rect) in others) {
            otherPaint.color = classColors[r.label] ?: Color.RED
            canvas.drawRect(rect, otherPaint)
        }

        val selected = selectQueen(queens, sticky)
        if (selected == null) {
            drawHold(canvas, now, sticky)
            return
        }

        val (qr, rect) = selected
        drawQueen(canvas, rect, qr.score, qr.label, sticky)
    }

    private fun selectQueen(
        queens: List<Pair<DetectorHelper.DetectionResult, RectF>>,
        sticky: Boolean
    ): Pair<DetectorHelper.DetectionResult, RectF>? {
        if (queens.isEmpty()) return null
        if (!sticky || !locked || lastQueenCx == null) {
            locked = sticky
            return queens.maxByOrNull { it.first.score }
        }
        return queens.minByOrNull {
            val dx = it.second.centerX() - lastQueenCx!!
            val dy = it.second.centerY() - lastQueenCy!!
            dx * dx + dy * dy
        }
    }

    private fun drawQueen(
        canvas: Canvas,
        rect: RectF,
        score: Float,
        label: String,
        sticky: Boolean
    ) {
        val smooth = queenSmoother.smooth(rect)
        val cx = smooth.centerX()
        val cy = smooth.centerY()

        val diag = hypot(smooth.width(), smooth.height())
        var radius = (diag * 3f) / 2f
        if (label == "mark_queen") radius *= markedQueenRadiusMultiplier


         fun clampRadiusToScreen(radius: Float): Float {
            val minSide = width.coerceAtMost(height).toFloat()
            val maxDiameter = minSide * 0.75f
            val maxRadius = maxDiameter / 2f
            return radius.coerceAtMost(maxRadius)
        }

        radius = smoothRadius(radius)
        radius = clampRadiusToScreen(radius)

        queenPaint.color = classColors["beequeen"] ?: Color.MAGENTA
        canvas.drawCircle(cx, cy, radius, queenPaint)

        lastQueenCx = cx
        lastQueenCy = cy
        lastQueenRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        lastQueenRadius = radius
        lastQueenScore = score
        lastSeenMs = System.currentTimeMillis()

        if (sticky) trackState = TrackState.LOCK

        drawStateSquare(canvas, cx, cy, radius)
        drawPercent(canvas, cx, cy, sticky)
    }

    private fun drawHold(canvas: Canvas, now: Long, sticky: Boolean) {
        val rect = lastQueenRect ?: return
        val r = lastQueenRadius ?: return

        val elapsed = now - lastSeenMs
        if (elapsed <= queenHoldMs) {
            queenPaint.alpha =
                ((1f - elapsed.toFloat() / queenHoldMs) * 255).toInt()
            canvas.drawCircle(rect.centerX(), rect.centerY(), r, queenPaint)
            if (sticky) trackState = TrackState.SEARCH
            drawStateSquare(canvas, rect.centerX(), rect.centerY(), r)
            drawPercent(canvas, rect.centerX(), rect.centerY(), sticky)
        } else {
            if (sticky) trackState = TrackState.LOST
            clearLock()
        }
    }

    // ================= UI helpers =================
    private fun drawStateSquare(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val state = trackState ?: return

        statePaint.color = when (state) {
            TrackState.LOCK -> Color.GREEN
            TrackState.SEARCH -> Color.YELLOW
            TrackState.LOST -> Color.RED
        }

        val size = r * 0.25f
        val left = cx + r * 0.7f
        val top = cy - r * 0.7f

        canvas.drawRect(
            left,
            top,
            left + size,
            top + size,
            statePaint
        )
    }

    private fun drawPercent(canvas: Canvas, cx: Float, cy: Float, sticky: Boolean) {
        if (trackState == TrackState.LOST) return

        val text = "${(lastQueenScore * 100).toInt()}%"
        val w = labelPaint.measureText(text)
        val h = labelPaint.textSize

        val x = cx - w / 2
        val y = cy + h / 2

        val bg = RectF(
            x - 8,
            y - h,
            x + w + 8,
            y + 6
        )

        canvas.drawRect(bg, labelBgPaint)
        canvas.drawText(text, x, y, labelPaint)
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
}
