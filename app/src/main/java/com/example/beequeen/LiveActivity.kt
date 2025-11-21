@file:OptIn(ExperimentalGetImage::class)

package com.example.beequeen

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LiveActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var detectorHelper: DetectorHelper
    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView
    private lateinit var seekBar: VerticalSeekBar
    private lateinit var percentText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live)

        detectorHelper = DetectorHelper(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        previewView = findViewById(R.id.viewFinder)
        overlayView = findViewById(R.id.overlay)
        seekBar       = findViewById(R.id.seekBarVertical)
        percentText   = findViewById(R.id.percentText)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val savedProgress = prefs.getInt("seek_progress", 50)
        seekBar.progress = savedProgress
        percentText.text = "$savedProgress %"

        seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                percentText.text = "$progress %"
                prefs.edit().putInt("seek_progress", progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun allPermissionsGranted() =
        REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) startCamera()
            else finish()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val analysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImage(imageProxy)
            }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis
                )
            } catch (exc: Exception) {
                Log.e("LiveActivity", "Camera binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImage(imageProxy: ImageProxy) {
        val image   = imageProxy.image
        if (image == null) {
            imageProxy.close()
            return
        }
        val bitmap  = ImageUtils.imageToBitmap(image, imageProxy.imageInfo.rotationDegrees)
        if (bitmap == null || bitmap.width == 0) {
            imageProxy.close()
            return
        }

        val results = detectorHelper.detect(bitmap)
        val message = when {
            results.isEmpty() -> "Об'єкти відсутні"
            else              -> "Знайдено: ${results.joinToString { it.label }}"
        }
        Log.d("YOLO", message)

        runOnUiThread { overlayView.setResults(results) }
        imageProxy.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}