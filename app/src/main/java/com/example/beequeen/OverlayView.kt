package com.example.beequeen

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var results: List<DetectorHelper.DetectionResult> = emptyList()
    private var srcW = 0
    private var srcH = 0

    private val enabledClasses = mutableSetOf<String>()
    private val classColors = mutableMapOf<String, Int>()

    fun setFrameInfo(w: Int, h: Int) {
        srcW = w
        srcH = h
    }

    fun setResults(r: List<DetectorHelper.DetectionResult>) {
        results = r
        invalidate()
    }

    fun setClassEnabled(label: String, enabled: Boolean) {
        if (enabled) enabledClasses += label else enabledClasses -= label
        invalidate()
    }

    fun setClassColor(label: String, color: Int) {
        classColors[label] = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (srcW == 0 || srcH == 0) return

        val sx = width.toFloat() / srcW
        val sy = height.toFloat() / srcH

        for (r in results) {
            if (!enabledClasses.contains(r.label)) continue

            val paint = Paint().apply {
                color = classColors[r.label] ?: Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 6f
                isAntiAlias = true
            }

            canvas.drawRect(
                r.box.left * sx,
                r.box.top * sy,
                r.box.right * sx,
                r.box.bottom * sy,
                paint
            )
        }
    }
}
