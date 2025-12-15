package com.example.beequeen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min

class DetectorHelper(
    context: Context,
    modelPath: String = "best_meta.tflite"
) {

    private val inputSize = 640
    private val numPreds = 8400
    private val numChannels = 8
    private val classNames = listOf("beequeen", "drone", "mark_queen", "workbee")

    private val tflite =
        Interpreter(loadModelFile(context, modelPath))

    /** ПОРОГИ, КЕРОВАНІ З UI */
    private val classThresholds = mutableMapOf<String, Float>().apply {
        put("beequeen", 0.25f)
        put("mark_queen", 0.25f)
        put("workbee", 0.50f)
        put("drone", 0.50f)
    }

    fun setThreshold(label: String, value01: Float) {
        classThresholds[label] = value01
    }

    fun getClassNames(): List<String> = classNames

    data class DetectionResult(
        val label: String,
        val score: Float,
        val box: RectF
    )

    fun detect(bitmap: Bitmap): List<DetectionResult> {

        val input = Array(1) { Array(inputSize) { Array(inputSize) { FloatArray(3) } } }
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        for (y in 0 until inputSize)
            for (x in 0 until inputSize) {
                val p = resized.getPixel(x, y)
                input[0][y][x][0] = ((p shr 16) and 0xFF) / 255f
                input[0][y][x][1] = ((p shr 8) and 0xFF) / 255f
                input[0][y][x][2] = (p and 0xFF) / 255f
            }

        val output = Array(1) { Array(numChannels) { FloatArray(numPreds) } }
        tflite.run(input, output)

        val w = bitmap.width.toFloat()
        val h = bitmap.height.toFloat()

        val candidates = mutableListOf<DetectionResult>()

        for (i in 0 until numPreds) {
            var bestScore = 0f
            var bestClass = -1
            for (c in 0 until 4) {
                val s = output[0][4 + c][i]
                if (s > bestScore) {
                    bestScore = s
                    bestClass = c
                }
            }
            if (bestClass < 0) continue

            val label = classNames[bestClass]
            val thresh = classThresholds[label] ?: 0.5f
            if (bestScore < thresh) continue

            val cx = output[0][0][i]
            val cy = output[0][1][i]
            val bw = output[0][2][i]
            val bh = output[0][3][i]

            val box = RectF(
                (cx - bw / 2) * w,
                (cy - bh / 2) * h,
                (cx + bw / 2) * w,
                (cy + bh / 2) * h
            )

            candidates += DetectionResult(label, bestScore, box)
        }

        return nms(candidates, 0.45f)
    }

    private fun nms(src: List<DetectionResult>, iouThresh: Float): List<DetectionResult> {
        val out = mutableListOf<DetectionResult>()
        src.sortedByDescending { it.score }.forEach { c ->
            if (out.none { iou(it.box, c.box) > iouThresh }) out += c
        }
        return out
    }

    private fun iou(a: RectF, b: RectF): Float {
        val l = max(a.left, b.left)
        val t = max(a.top, b.top)
        val r = min(a.right, b.right)
        val bt = min(a.bottom, b.bottom)
        val inter = max(0f, r - l) * max(0f, bt - t)
        val union = a.width() * a.height() + b.width() * b.height() - inter
        return if (union > 0f) inter / union else 0f
    }

    private fun loadModelFile(context: Context, path: String) =
        context.assets.openFd(path).use { fd ->
            FileInputStream(fd.fileDescriptor).channel.map(
                FileChannel.MapMode.READ_ONLY,
                fd.startOffset,
                fd.declaredLength
            )
        }
}
