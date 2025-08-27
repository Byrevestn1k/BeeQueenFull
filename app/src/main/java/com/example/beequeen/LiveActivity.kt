package com.example.beequeen
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.util.Size
import android.graphics.RectF
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import org.tensorflow.lite.support.image.TensorImage
import java.text.SimpleDateFormat
import java.util.*
class LiveActivity : AppCompatActivity() {
    private lateinit var previewView: androidx.camera.view.PreviewView
    private lateinit var overlay: OverlayView
    private lateinit var btnCorrect: Button
    private lateinit var btnIncorrect: Button
    private val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    private var detector = null as org.tensorflow.lite.task.vision.detector.ObjectDetector?
    private val yuvConverter = YuvToRgbConverter()
    private var lastBitmap: android.graphics.Bitmap? = null
    private var lastBox: RectF? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live)
        previewView = findViewById(R.id.previewView)
        overlay = findViewById(R.id.overlay)
        btnCorrect = findViewById(R.id.btnCorrect)
        btnIncorrect = findViewById(R.id.btnIncorrect)
        detector = DetectorHelper.create(this)
        if (detector == null) Toast.makeText(this, getString(R.string.no_model), Toast.LENGTH_LONG).show()
        startCamera()
        btnCorrect.setOnClickListener {
            if (lastBitmap != null && lastBox != null) {
                saveLabeledFrame(lastBitmap!!, lastBox!!, "queen")
                Toast.makeText(this, "Saved as queen", Toast.LENGTH_SHORT).show()
            }
        }
        btnIncorrect.setOnClickListener {
            if (lastBitmap != null && lastBox != null) {
                saveLabeledFrame(lastBitmap!!, lastBox!!, "incorrect")
                Toast.makeText(this, "Saved as incorrect", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val selector = CameraSelector.DEFAULT_BACK_CAMERA
            val analyzer = ImageAnalysis.Builder().setTargetResolution(Size(640,480)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
            analyzer.setAnalyzer(ContextCompat.getMainExecutor(this)) { image -> analyze(image) }
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, selector, preview, analyzer)
        }, ContextCompat.getMainExecutor(this))
    }
    private fun analyze(image: ImageProxy) {
        try {
            val bmp = yuvConverter.toBitmap(image)
            lastBitmap = bmp
            val tensor = TensorImage.fromBitmap(bmp)
            val det = detector
            if (det != null) {
                val results = det.detect(tensor)
                if (results.isNotEmpty()) {
                    val r = results[0].boundingBox
                    val rectF = RectF(r.left.toFloat(), r.top.toFloat(), r.right.toFloat(), r.bottom.toFloat())
                    lastBox = rectF
                    runOnUiThread {
                        overlay.setRectF(rectF)
                        btnCorrect.visibility = Button.VISIBLE
                        btnIncorrect.visibility = Button.VISIBLE
                        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                    }
                } else {
                    runOnUiThread {
                        overlay.setRectF(null)
                        btnCorrect.visibility = Button.GONE
                        btnIncorrect.visibility = Button.GONE
                    }
                }
            }
        } catch (e: Exception) { }
        finally { image.close() }
    }
    private fun saveLabeledFrame(bmp: android.graphics.Bitmap, box: RectF, label: String) {
        val outDir = java.io.File(getExternalFilesDir(null), "train_data")
        if (!outDir.exists()) outDir.mkdirs()
        val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = java.io.File(outDir, "${label}_$${time}.jpg")
        val mutable = bmp.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(mutable)
        val p = android.graphics.Paint().apply { color = android.graphics.Color.RED; style = android.graphics.Paint.Style.STROKE; strokeWidth = 6f }
        canvas.drawRect(box, p)
        file.outputStream().use { mutable.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, it) }
    }
}
