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
        private const val KEY_SEARCH_HOLD_MS = "search_hold_ms"
    }

    // ===================== Marker config =====================
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

    // ===================== Core =====================
    private lateinit var detector: DetectorHelper
    private lateinit var overlay: OverlayView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var prefs: SharedPreferences
    private val stickerGap = 12f
    private val percentGap = 8f

    // ===================== Lifecycle =====================
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

    // ===================== Init =====================
    private fun initMarkersState() {
        for (m in markers) {

            // enabled
            m.enabled = prefs.getBoolean("${m.id}_enabled", true)

            // threshold (SAFE: Int or legacy Float)
            val thrKey = "${m.id}_threshold"
            m.threshold = when {
                prefs.contains(thrKey) -> {
                    try {
                        // новий формат (Int, 10..90)
                        prefs.getInt(thrKey, m.defaultThreshold)
                    } catch (e: ClassCastException) {
                        // старий формат (Float, 0.0..1.0)
                        val legacy = prefs.getFloat(
                            thrKey,
                            m.defaultThreshold / 100f
                        )
                        val converted = (legacy * 100).toInt().coerceIn(10, 90)

                        // 🔧 МІГРАЦІЯ: перезаписуємо в Int
                        prefs.edit().putInt(thrKey, converted).apply()

                        converted
                    }
                }
                else -> m.defaultThreshold
            }

            // color
            m.color = prefs.getInt("${m.id}_color", m.defaultColor)

            // apply to detector & overlay
            detector.setThreshold(m.id, m.threshold / 100f)
            overlay.setClassEnabled(m.id, m.enabled)
            overlay.setClassColor(m.id, m.color)
        }
    }


    // ===================== Camera =====================
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
        try {
            val img = imageProxy.image ?: return
            val bitmap = ImageUtils.imageToBitmap(
                img,
                imageProxy.imageInfo.rotationDegrees
            )

            val results = detector.detect(bitmap)

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

    // ===================== Settings dialog =====================
    private fun showSettingsDialog() {
        val root = LayoutInflater.from(this)
            .inflate(R.layout.dialog_settings, null)

        val container = root.findViewById<LinearLayout>(R.id.containerMarkers)
        container.removeAllViews()

        /* ---------- Sticky checkbox ---------- */
        val cbSticky = CheckBox(this).apply {
            text = "Sticky queen (tracker)"
            isChecked = prefs.getBoolean(KEY_STICKY_QUEEN, false)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(KEY_STICKY_QUEEN, checked).apply()
            }
        }
        container.addView(cbSticky)

        /* ---------- SEARCH hold slider ---------- */
        val tvHold = TextView(this).apply {
            setPadding(0, 24, 0, 0)
            textSize = 14f
        }

        val seekHold = SeekBar(this).apply {
            max = 20 // 0..10 сек (крок 0.5)
        }

        val currentMs = prefs.getLong(KEY_SEARCH_HOLD_MS, 3000L)
        seekHold.progress = (currentMs / 500L).toInt()
        tvHold.text = "Час пошуку матки: ${currentMs / 1000f} с"

        seekHold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, f: Boolean) {
                val ms = p * 500L
                prefs.edit().putLong(KEY_SEARCH_HOLD_MS, ms).apply()
                tvHold.text = "Час пошуку матки: ${ms / 1000f} с"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        container.addView(tvHold)
        container.addView(seekHold)

        /* ---------- Marker settings ---------- */
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

    // ===================== Helpers =====================
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
