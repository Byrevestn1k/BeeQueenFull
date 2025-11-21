package com.example.beequeen

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.SeekBar

class VerticalSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.seekBarStyle
) : SeekBar(context, attrs, defStyle) {

    // Поверхність вимірюємо, міняючи місцями width і height
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // swap specs when measuring so that height becomes width and vice versa
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    // also swap size when changed
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    // малюємо повернувши canvas
    override fun onDraw(canvas: Canvas) {
        // повертаємо та зсовуємо, щоб стандартний горизонтальний трек намалювався вертикально
        canvas.rotate(-90f)
        canvas.translate(-height.toFloat(), 0f)
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                isPressed = true
            }
            MotionEvent.ACTION_MOVE -> {
                // nothing extra
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isPressed = false
                performClick()
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }

        // Обчислюємо прогрес: зверху -> max, знизу -> 0
        val newProgress = max - (max * event.y / height).toInt()
        progress = newProgress.coerceIn(0, max)

        // оновити відображення
        onSizeChanged(width, height, 0, 0)
        invalidate()
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
