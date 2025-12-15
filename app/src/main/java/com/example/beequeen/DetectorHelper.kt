package com.example.beequeen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel

class DetectorHelper(
    context: Context,
    modelPath: String = "best_meta.tflite"
) {

    companion object {
        private const val TAG = "YOLO"
    }

    private val tflite: Interpreter

    private val inputSize = 640
    private val numChannels = 8   // 4 bbox + 4 class scores
    private val numPreds = 8400

    private val classNames = listOf(
        "beequeen",
        "drone",
        "mark_queen",
        "workbee"
    )

    init {
        tflite = Interpreter(loadModelFile(context, modelPath))
    }

    private fun loadModelFile(context: Context, path: String) =
        context.assets.openFd(path).use { fd ->
            FileInputStream(fd.fileDescriptor).channel.map(
                FileChannel.MapMode.READ_ONLY,
                fd.startOffset,
                fd.declaredLength
            )
        }

    data class DetectionResult(
        val label: String,
        val score: Float,
        val box: RectF
    )

    fun detect(
        bitmap: Bitmap,
        confThresh: Float = 0.35f,
        iouThresh: Float = 0.45f
    ): List<DetectionResult> {

        Log.d(TAG, "detect start, bitmap ${bitmap.width}x${bitmap.height}")

        val wOrig = bitmap.width.toFloat()
        val hOrig = bitmap.height.toFloat()

        // ===== Input tensor =====
        val input = Array(1) {
            Array(inputSize) {
                Array(inputSize) {
                    FloatArray(3)
                }
            }
        }

        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val px = resized.getPixel(x, y)
                input[0][y][x][0] = ((px shr 16) and 0xFF) / 255f
                input[0][y][x][1] = ((px shr 8) and 0xFF) / 255f
                input[0][y][x][2] = (px and 0xFF) / 255f
            }
        }

        val output = Array(1) {
            Array(numChannels) {
                FloatArray(numPreds)
            }
        }

        tflite.run(input, output)
        Log.d(TAG, "inference finished")

        val candidates = mutableListOf<DetectionResult>()

        for (i in 0 until numPreds) {

            var bestScore = 0f
            var bestClass = -1

            for (c in 0 until 4) {
                val score = output[0][4 + c][i]
                if (score > bestScore) {
                    bestScore = score
                    bestClass = c
                }
            }

            if (bestScore < confThresh) continue

            // 🔑 YOLO NORMALIZED COORDS (0..1)
            val cx = output[0][0][i]
            val cy = output[0][1][i]
            val w = output[0][2][i]
            val h = output[0][3][i]

            val left   = (cx - w / 2f) * wOrig
            val top    = (cy - h / 2f) * hOrig
            val right  = (cx + w / 2f) * wOrig
            val bottom = (cy + h / 2f) * hOrig

            val box = RectF(
                left.coerceIn(0f, wOrig),
                top.coerceIn(0f, hOrig),
                right.coerceIn(0f, wOrig),
                bottom.coerceIn(0f, hOrig)
            )

            Log.d(TAG, "raw box $i: score=$bestScore px=$box")

            candidates += DetectionResult(
                classNames[bestClass],
                bestScore,
                box
            )
        }

        // ===== NMS =====
        val results = mutableListOf<DetectionResult>()
        candidates.sortByDescending { it.score }

        for (det in candidates) {
            if (results.any { iou(det.box, it.box) > iouThresh }) continue
            results += det
        }

        Log.d(TAG, "final boxes: ${results.size}")
        return results
    }

    private fun iou(a: RectF, b: RectF): Float {
        val interLeft = maxOf(a.left, b.left)
        val interTop = maxOf(a.top, b.top)
        val interRight = minOf(a.right, b.right)
        val interBottom = minOf(a.bottom, b.bottom)

        val interArea =
            maxOf(0f, interRight - interLeft) *
                    maxOf(0f, interBottom - interTop)

        val unionArea =
            a.width() * a.height() +
                    b.width() * b.height() -
                    interArea

        return if (unionArea > 0f) interArea / unionArea else 0f
    }
}
