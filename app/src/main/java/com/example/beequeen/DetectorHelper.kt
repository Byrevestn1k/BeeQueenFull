package com.example.beequeen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel

class DetectorHelper(
    context: Context,
    modelPath: String = "best_meta.tflite"
) {
    private val tflite: Interpreter
    private val inputSize  = 640
    private val numChannels= 8          // 4 box + 4 scores
    private val numPreds   = 8400
    private val classNames = listOf("beequeen", "drone", "mark_queen", "workbee")

    init {
        tflite = Interpreter(loadModelFile(context, modelPath))
    }

    private fun loadModelFile(context: Context, path: String) =
        context.assets.openFd(path).use { fd ->
            FileInputStream(fd.fileDescriptor).channel.map(
                FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength
            )
        }

    data class DetectionResult(val label: String, val score: Float, val box: RectF)

    fun detect(bitmap: Bitmap, confThresh: Float = 0.35f, iouThresh: Float = 0.45f): List<DetectionResult> {
        // 1. вхід 640×640 RGB 0-1
        val input = Array(1) { Array(inputSize) { Array(inputSize) { FloatArray(3) } } }
        val scaled = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val px = scaled.getPixel(x, y)
                input[0][y][x][0] = (px shr 16 and 0xFF) / 255.0f
                input[0][y][x][1] = (px shr 8  and 0xFF) / 255.0f
                input[0][y][x][2] = (px       and 0xFF) / 255.0f
            }
        }

        // 2. вихід
        val output = Array(1) { Array(numChannels) { FloatArray(numPreds) } }
        tflite.run(input, output)

        // 3. decode
        val wOrig = bitmap.width.toFloat()
        val hOrig = bitmap.height.toFloat()
        val candidates = mutableListOf<DetectionResult>()

        for (i in 0 until numPreds) {
            val classProbs = List(4) { ch -> output[0][4 + ch][i] }
            val maxEntry = classProbs.withIndex().maxByOrNull { it.value }!!
            val maxScore = maxEntry.value            // Float
            val labelIdx = maxEntry.index            // Int
            if (maxScore < confThresh) continue

            val x     = output[0][0][i]
            val y     = output[0][1][i]
            val w     = output[0][2][i]
            val h     = output[0][3][i]

            val left   = (x - w / 2) / inputSize * wOrig
            val top    = (y - h / 2) / inputSize * hOrig
            val right  = (x + w / 2) / inputSize * wOrig
            val bottom = (y + h / 2) / inputSize * hOrig


            candidates += DetectionResult(classNames[labelIdx], maxScore, RectF(left, top, right, bottom))
        }

        // 4. NMS
        val keep = mutableListOf<DetectionResult>()
        candidates.sortByDescending { it.score }
        for (c in candidates) {
            if (keep.any { IoU(c.box, it.box) > iouThresh }) continue
            keep += c
        }
        return keep
    }

    private fun IoU(a: RectF, b: RectF): Float {
        val interLeft   = maxOf(a.left, b.left)
        val interTop    = maxOf(a.top, b.top)
        val interRight  = minOf(a.right, b.right)
        val interBottom = minOf(a.bottom, b.bottom)
        val interArea = maxOf(0f, interRight - interLeft) * maxOf(0f, interBottom - interTop)
        val unionArea = a.width() * a.height() + b.width() * b.height() - interArea
        return if (unionArea > 0f) interArea / unionArea else 0f
    }
}