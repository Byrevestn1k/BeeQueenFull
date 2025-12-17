package com.example.beequeen

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
class LiveActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val PREFS_NAME = "beequeen_prefs"
        private const val KEY_STICKY_QUEEN = "sticky_queen"
    }

    data class MarkerConfig(
        val id: String,
        val title: String,
        val defaultColor: Int,
        val defaultThreshold: Int,
        var enabled: Boolean = true,
        var color: Int = defaultColor,
        var threshold: Int = defaultThreshold
    )

    private val markers = mutableListOf(
        MarkerConfig("beequeen", "Матка", Color.MAGENTA, 25),
        MarkerConfig("mark_queen", "Маркерована матка", Color.MAGENTA, 25),
        MarkerConfig("workbee", "Робочі бджоли", Color.GREEN, 50),
        MarkerConfig("drone", "Трутні", Color.RED, 50)
    )

    private lateinit var detector: DetectorHelper
    private lateinit var overlay: OverlayView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var prefs: SharedPreferences

    private var lastDetectMs = 0L
    private val MIN_DETECT_INTERVAL_MS = 120L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        detector = DetectorHelper(this)
        overlay = findViewById(R.id.overlay)
        cameraExecutor = Executors.newSingleThreadExecutor()

        initMarkersState()

        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            showSettingsDialog()
        }

        if (allPermissionsGranted()) startCamera()
        else ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CODE_PERMISSIONS
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun initMarkersState() {
        for (m in markers) {
            m.enabled = prefs.getBoolean("${m.id}_enabled", true)

            // threshold: safe (Int or legacy Float)
            val keyThr = "${m.id}_threshold"
            m.threshold = when {
                prefs.contains(keyThr) -> {
                    try {
                        prefs.getInt(keyThr, m.defaultThreshold)
                    } catch (_: ClassCastException) {
                        (prefs.getFloat(keyThr, m.defaultThreshold / 100f) * 100).toInt()
                    }
                }
                else -> m.defaultThreshold
            }

            m.color = prefs.getInt("${m.id}_color", m.defaultColor)

            detector.setThreshold(m.id, m.threshold / 100f)
            overlay.setClassEnabled(m.id, m.enabled)
            overlay.setClassColor(m.id, m.color)
        }
    }

    private fun allPermissionsGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val previewView = findViewById<PreviewView>(R.id.viewFinder)

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processFrame(imageProxy)
            }

            provider.unbindAll()
            provider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis
            )
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processFrame(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()
        if (now - lastDetectMs < MIN_DETECT_INTERVAL_MS) {
            imageProxy.close()
            return
        }
        lastDetectMs = now

        try {
            val img = imageProxy.image ?: return
            val bitmap = ImageUtils.imageToBitmap(
                img,
                imageProxy.imageInfo.rotationDegrees
            )

            val results = detector.detect(bitmap)

            // ✅ ВАЖЛИВО: передаємо ВСІ детекції.
            // Top-1 queen та sticky вибір робить OverlayView.
            runOnUiThread {
                overlay.setFrameInfo(bitmap.width, bitmap.height)
                overlay.setResults(results)
            }
        } catch (e: Exception) {
            Log.e("LiveActivity", "processFrame error", e)
        } finally {
            imageProxy.close()
        }
    }

    private fun showSettingsDialog() {
        val root = LayoutInflater.from(this)
            .inflate(R.layout.dialog_settings, null)

        val container =
            root.findViewById<LinearLayout>(R.id.containerMarkers)
        container.removeAllViews()

        val cbSticky = CheckBox(this).apply {
            text = "Sticky queen (tracker)"
            isChecked = prefs.getBoolean(KEY_STICKY_QUEEN, false)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(KEY_STICKY_QUEEN, checked).apply()
            }
        }
        container.addView(cbSticky)

        for (marker in markers) {
            val row = layoutInflater.inflate(
                R.layout.item_marker_settings,
                container,
                false
            )

            val cbEnabled = row.findViewById<CheckBox>(R.id.checkEnabled)
            val tvTitle = row.findViewById<TextView>(R.id.tvTitle)
            val tvConf = row.findViewById<TextView>(R.id.tvConfidence)
            val seekThr = row.findViewById<SeekBar>(R.id.seekThreshold)
            val seekCol = row.findViewById<SeekBar>(R.id.seekColor)
            val colorPreview = row.findViewById<View>(R.id.viewColorPreview)
            val tvColorLabel = row.findViewById<TextView?>(R.id.tvColorLabel)

            tvTitle.text = marker.title
            cbEnabled.isChecked = marker.enabled
            colorPreview.setBackgroundColor(marker.color)

            cbEnabled.setOnCheckedChangeListener { _, isChecked ->
                marker.enabled = isChecked
                overlay.setClassEnabled(marker.id, isChecked)
                prefs.edit().putBoolean("${marker.id}_enabled", isChecked).apply()
            }

            tvConf.text = "Впевненість: ${marker.threshold}%"
            seekThr.progress = (marker.threshold - 10).coerceIn(0, 80)

            seekThr.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, f: Boolean) {
                    val value = (p + 10).coerceIn(10, 90)
                    marker.threshold = value
                    tvConf.text = "Впевненість: $value%"
                    detector.setThreshold(marker.id, value / 100f)
                    prefs.edit().putInt("${marker.id}_threshold", value).apply()
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })

            // ✅ mark_queen — без вибору кольору
            if (marker.id == "mark_queen") {
                seekCol.visibility = View.GONE
                colorPreview.visibility = View.GONE
                tvColorLabel?.visibility = View.GONE
            } else {
                applyHsvGradient(seekCol)
                seekCol.progress = getHue(marker.color)
                seekCol.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, p: Int, f: Boolean) {
                        val hue = p.coerceIn(0, 360)
                        val color = Color.HSVToColor(floatArrayOf(hue.toFloat(), 1f, 1f))
                        marker.color = color
                        colorPreview.setBackgroundColor(color)
                        overlay.setClassColor(marker.id, color)
                        prefs.edit().putInt("${marker.id}_color", color).apply()
                    }
                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })
            }

            container.addView(row)
        }

        AlertDialog.Builder(this)
            .setView(root)
            .setCancelable(true)
            .show()
    }

    private fun applyHsvGradient(seekBar: SeekBar) {
        seekBar.post {
            val w = seekBar.width
            val h = seekBar.height
            if (w <= 0 || h <= 0) return@post

            val colors = IntArray(361) {
                Color.HSVToColor(floatArrayOf(it.toFloat(), 1f, 1f))
            }

            val shader = android.graphics.LinearGradient(
                0f, 0f, w.toFloat(), 0f,
                colors, null,
                android.graphics.Shader.TileMode.CLAMP
            )

            val paint = android.graphics.Paint().apply { this.shader = shader }
            val bmp = android.graphics.Bitmap.createBitmap(
                w, h, android.graphics.Bitmap.Config.ARGB_8888
            )
            android.graphics.Canvas(bmp).drawRect(
                0f, 0f, w.toFloat(), h.toFloat(), paint
            )

            seekBar.progressDrawable =
                android.graphics.drawable.BitmapDrawable(resources, bmp)
        }
    }

    private fun getHue(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv[0].toInt().coerceIn(0, 360)
    }
}
