package com.example.beequeen

import android.graphics.RectF
import kotlin.math.abs

class QueenEmaSmoother(
    private val alpha: Float = 0.25f,
    private val maxJumpRatio: Float = 0.6f
) {

    private var lastRect: RectF? = null

    fun reset() {
        lastRect = null
    }

    fun smooth(current: RectF): RectF {
        val prev = lastRect
        if (prev == null) {
            lastRect = RectF(current)
            return current
        }

        if (isTooFar(prev, current)) {
            lastRect = RectF(current)
            return current
        }

        val smoothed = RectF(
            lerp(prev.left, current.left),
            lerp(prev.top, current.top),
            lerp(prev.right, current.right),
            lerp(prev.bottom, current.bottom)
        )

        lastRect = smoothed
        return smoothed
    }

    private fun lerp(prev: Float, curr: Float): Float {
        return prev * (1f - alpha) + curr * alpha
    }

    private fun isTooFar(a: RectF, b: RectF): Boolean {
        val w = a.width()
        val h = a.height()
        return abs(a.centerX() - b.centerX()) > w * maxJumpRatio ||
                abs(a.centerY() - b.centerY()) > h * maxJumpRatio
    }
}
