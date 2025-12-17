package com.example.beequeen

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.hypot

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    companion object {
        private const val PREFS_NAME = "beequeen_prefs"
        private const val KEY_STICKY_QUEEN = "sticky_queen"
        private const val KEY_SEARCH_HOLD_MS = "search_hold_ms"
    }

    // ================= Preferences =================
    private val prefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun stickyEnabled(): Boolean =
        prefs.getBoolean(KEY_STICKY_QUEEN, false)

    private fun getSearchHoldMs(): Long =
        prefs.getLong(KEY_SEARCH_HOLD_MS, 3000L)

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

    private val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val percentBgPaint = Paint().apply {
        color = Color.BLACK
        alpha = 160
    }

    // ================= Eye drawable =================
    private val eyeDrawable by lazy {
        ContextCompat.getDrawable(context, R.drawable.ic_eye)!!
    }

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

    private var lastQueenRect: RectF? = null
    private var lastQueenRadius: Float? = null
    private var lastQueenScore = 0f
    private var lastSeenMs = 0L

    private var locked = false
    private var lastQueenCx: Float? = null
    private var lastQueenCy: Float? = null

    private enum class TrackState { LOCK, SEARCH, LOST }
    private var trackState: TrackState? = null

    // layout gaps
    private val stickerGap = 12f
    private val percentGap = 8f

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

        if (!sticky || !locked || lastQueenCx == null || lastQueenCy == null) {
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
        radius = smoothRadius(radius)
        radius = clampRadiusToScreen(radius)

        queenPaint.color = classColors["beequeen"] ?: Color.MAGENTA
        queenPaint.alpha = 255
        canvas.drawCircle(cx, cy, radius, queenPaint)

        lastQueenCx = cx
        lastQueenCy = cy
        lastQueenRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        lastQueenRadius = radius
        lastQueenScore = score
        lastSeenMs = System.currentTimeMillis()

        if (sticky) trackState = TrackState.LOCK

        drawConfidencePercent(canvas, cx, cy, radius)
        drawEyeSticker(canvas, cx, cy, radius)
    }

    private fun drawHold(canvas: Canvas, now: Long, sticky: Boolean) {
        val rect = lastQueenRect ?: return
        val r = lastQueenRadius ?: return

        val elapsed = now - lastSeenMs
        if (elapsed <= getSearchHoldMs()) {
            queenPaint.alpha =
                ((1f - elapsed.toFloat() / getSearchHoldMs()) * 255).toInt()
            canvas.drawCircle(rect.centerX(), rect.centerY(), r, queenPaint)

            if (sticky) trackState = TrackState.SEARCH

            drawConfidencePercent(canvas, rect.centerX(), rect.centerY(), r)
            drawEyeSticker(canvas, rect.centerX(), rect.centerY(), r)
        } else {
            trackState = TrackState.LOST
            clearLock()
        }
    }

    // ================= UI helpers =================
    private fun drawEyeSticker(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float
    ) {
        val state = trackState ?: return
        if (state == TrackState.LOST) return

        val tint = when (state) {
            TrackState.LOCK -> Color.GREEN
            TrackState.SEARCH -> Color.YELLOW
            TrackState.LOST -> return
        }

        val size = (radius * 0.34f).toInt()
        val left = (cx + radius + stickerGap).toInt()
        val top = (cy - size / 2).toInt()

        eyeDrawable.setBounds(left, top, left + size, top + size)
        eyeDrawable.setTint(tint)
        eyeDrawable.alpha = 200
        eyeDrawable.draw(canvas)
    }

    private fun drawConfidencePercent(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float
    ) {
        if (trackState == TrackState.LOST) return

        val text = "${(lastQueenScore * 100).toInt()}%"
        val textW = percentPaint.measureText(text)
        val textH = percentPaint.textSize

        val x = cx + radius + stickerGap - percentGap - textW
        val y = cy + textH / 2

        val bg = RectF(
            x - 8,
            y - textH,
            x + textW + 8,
            y + 6
        )

        canvas.drawRect(bg, percentBgPaint)
        canvas.drawText(text, x, y, percentPaint)
    }

    // ================= Utils =================
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

    private fun clampRadiusToScreen(radius: Float): Float {
        val minSide = width.coerceAtMost(height).toFloat()
        val maxDiameter = minSide * 0.75f
        return radius.coerceAtMost(maxDiameter / 2f)
    }

    private fun clearLock() {
        locked = false
        lastQueenCx = null
        lastQueenCy = null
    }
}
