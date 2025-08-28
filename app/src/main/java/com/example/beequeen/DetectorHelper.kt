package com.example.beequeen

import android.content.Context
import android.util.Log
import org.tensorflow.lite.task.vision.detector.ObjectDetector

object DetectorHelper {
    fun create(context: Context): ObjectDetector? {
        return try {
            val modelFile = java.io.File(context.filesDir, "bee_queen_model.tflite")
            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setScoreThreshold(0.45f)
                .setMaxResults(1)
                .build()
            if (modelFile.exists()) {
                ObjectDetector.createFromFileAndOptions(context, modelFile.path, options)
            } else {
                ObjectDetector.createFromFileAndOptions(context, "bee_queen_model.tflite", options)
            }
        } catch (e: Exception) {
            Log.e("DetectorHelper", "Model load failed: ${e.message}")
            null
        }
    }
}
