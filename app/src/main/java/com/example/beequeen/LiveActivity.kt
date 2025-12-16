package com.example.beequeen

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable

@OptIn(ExperimentalGetImage::class) // ✅ фікс для imageProxy.image
class LiveActivity : AppCompatActivity() {

    // ---------- MODEL FOR UI ----------
    data class MarkerConfig(
        val id: String,
        val title: String,
        val defaultColor: Int,
        val defaultThreshold: Int,
        var enabled: Boolean = true,
        var color: Int = defaultColor,
        var threshold: Int = defaultThreshold
    )

    // ---------- MARKERS ----------
    private val markers = mutableListOf(
        MarkerConfig("beequeen", "Матка", Color.MAGENTA, 25),
        MarkerConfig("mark_queen", "Маркерована матка", Color.MAGENTA, 25),
        MarkerConfig("workbee", "Робочі бджоли", Color.parseColor("#66FF66"), 50),
        MarkerConfig("drone", "Трутні", Color.RED, 50)
    )

    // ---- FPS control / throttling ----
    private var lastDetectMs: Long = 0L
    private val MIN_DETECT_INTERVAL_MS = 120L  // 100..150 мс; 120 — хороший старт

    private lateinit var detector: DetectorHelper
    private lateinit var overlay: OverlayView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live)

        prefs = getSharedPreferences("markers", Context.MODE_PRIVATE)

        detector = DetectorHelper(this)
        overlay = findViewById(R.id.overlay)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // ✅ КРИТИЧНО: ініціалізація enabled+colors+thresholds (інакше рамки можуть “зникати”)
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

    // ---------- INITIAL STATE ----------
    private fun initMarkersState() {
        for (m in markers) {
            // load saved
            m.enabled = prefs.getBoolean("${m.id}_enabled", true)
            m.threshold = prefs.getInt("${m.id}_threshold", m.defaultThreshold)
            m.color = prefs.getInt("${m.id}_color", m.defaultColor)

            // apply to detector & overlay
            detector.setThreshold(m.id, m.threshold / 100f)
            overlay.setClassEnabled(m.id, m.enabled)
            overlay.setClassColor(m.id, m.color)
        }
    }

    // ---------- CAMERA ----------
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

        // ✅ детектимо не частіше, ніж раз на MIN_DETECT_INTERVAL_MS
        if (now - lastDetectMs < MIN_DETECT_INTERVAL_MS) {
            imageProxy.close()
            return
        }
        lastDetectMs = now

        try {
            val img = imageProxy.image
            if (img != null) {
                val bitmap = ImageUtils.imageToBitmap(
                    img,
                    imageProxy.imageInfo.rotationDegrees
                )

                val results = detector.detect(bitmap)

                // ✅ beequeen + mark_queen = одна матка (top-1 по score)
                val queen = results
                    .filter { it.label == "beequeen" || it.label == "mark_queen" }
                    .maxByOrNull { it.score }

                val filtered = buildList {
                    queen?.let { add(it) }
                    addAll(results.filter { it.label != "beequeen" && it.label != "mark_queen" })
                }

                runOnUiThread {
                    overlay.setFrameInfo(bitmap.width, bitmap.height)
                    overlay.setResults(filtered)
                }
            }
        } catch (e: Exception) {
            Log.e("LiveActivity", "processFrame error", e)
        } finally {
            imageProxy.close()
        }
    }


    // ---------- SETTINGS DIALOG ----------
    private fun showSettingsDialog() {
        val root = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null)
        val container = root.findViewById<LinearLayout>(R.id.containerMarkers)

        container.removeAllViews()

        for (marker in markers) {
            val row = layoutInflater.inflate(R.layout.item_marker_settings, container, false)

            val cb = row.findViewById<CheckBox>(R.id.cbEnabled)
            val tvTitle = row.findViewById<TextView>(R.id.tvTitle)
            val tvConf = row.findViewById<TextView>(R.id.tvConfidence)
            val seekThr = row.findViewById<SeekBar>(R.id.seekThreshold)
            val seekCol = row.findViewById<SeekBar>(R.id.seekColor)
            val colorPreview = row.findViewById<View>(R.id.viewColorPreview)
            val isMarkQueen = marker.id == "mark_queen"

            tvTitle.text = marker.title
            if (isMarkQueen) {
                // для маркованої матки: колір НЕ налаштовується
                seekCol.visibility = View.GONE
                colorPreview.visibility = View.GONE
            }

            cb.isChecked = marker.enabled

            // ✅ preview
            colorPreview.setBackgroundColor(marker.color)

            // long-press reset to defaults
            colorPreview.setOnLongClickListener {
                marker.color = marker.defaultColor
                val hue = getHue(marker.defaultColor)
                seekCol.progress = hue
                colorPreview.setBackgroundColor(marker.defaultColor)
                overlay.setClassColor(marker.id, marker.defaultColor)
                prefs.edit().putInt("${marker.id}_color", marker.defaultColor).apply()
                true
            }

            // ENABLE
            cb.setOnCheckedChangeListener { _, isChecked ->
                marker.enabled = isChecked
                overlay.setClassEnabled(marker.id, isChecked)
                prefs.edit().putBoolean("${marker.id}_enabled", isChecked).apply()
            }

            // THRESHOLD (10..90)
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

            // COLOR (HSV)
            if (!isMarkQueen) {
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

    // ---------- HSV GRADIENT ----------
    private fun applyHsvGradient(seekBar: SeekBar) {
        seekBar.post {
            val w = seekBar.width
            val h = seekBar.height
            if (w <= 0 || h <= 0) return@post

            // градієнт той самий (0..360), але малюємо його тонкою смугою в центрі
            val trackHeight = dpToPx(10f).coerceAtMost(h)
            val top = ((h - trackHeight) / 2f)
            val bottom = top + trackHeight

            val colors = IntArray(361) {
                Color.HSVToColor(floatArrayOf(it.toFloat(), 1f, 1f))
            }

            val shader = LinearGradient(
                0f, 0f, w.toFloat(), 0f,
                colors, null,
                Shader.TileMode.CLAMP
            )

            val paint = Paint().apply { this.shader = shader }
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp)

            // прозорий фон (щоб не було "плашки" на всю висоту)
            c.drawColor(Color.TRANSPARENT)

            // сам трек (тонкий)
            c.drawRect(0f, top, w.toFloat(), bottom, paint)

            seekBar.progressDrawable = BitmapDrawable(resources, bmp)
        }
    }

    private fun dpToPx(dp: Float): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }

    private fun getHue(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv[0].toInt().coerceIn(0, 360)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}
