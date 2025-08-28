package com.example.beequeen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class TrainingActivity : AppCompatActivity() {
    private lateinit var rv: RecyclerView
    private lateinit var tvInfo: TextView
    private lateinit var adapter: ImageAdapter
    private var currentPhotoFile: File? = null

    private val REQ_PICK = 1001
    private val REQ_CAPTURE = 1002
    private val REQ_IMPORT_ZIP = 1003
    private val REQ_IMPORT_MODEL = 1004

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        rv = findViewById(R.id.rvImages)
        tvInfo = findViewById(R.id.tvInfo)

        adapter = ImageAdapter(TrainingUtils.listItems(this).toMutableList()) { file ->
            val next = nextLabel(TrainingUtils.readLabel(file))
            TrainingUtils.updateLabel(file, next)
            refresh()
            Toast.makeText(this, "Label: $next", Toast.LENGTH_SHORT).show()
        }
        rv.layoutManager = GridLayoutManager(this, 3)
        rv.adapter = adapter

        findViewById<Button>(R.id.btnPick).setOnClickListener { pickImages() }
        findViewById<Button>(R.id.btnCapture).setOnClickListener { capturePhoto() }
        findViewById<Button>(R.id.btnImportZip).setOnClickListener { importZip() }
        findViewById<Button>(R.id.btnExportZip).setOnClickListener {
            val zip = TrainingUtils.exportZip(this)
            Toast.makeText(this, "Exported: ${zip.absolutePath}", Toast.LENGTH_LONG).show()
        }
        findViewById<Button>(R.id.btnImportModel).setOnClickListener { importModel() }
        findViewById<Button>(R.id.btnHowToTrain).setOnClickListener {
            Toast.makeText(this, "Export ZIP -> train on PC/cloud -> import bee_queen_model.tflite back via Import .tflite", Toast.LENGTH_LONG).show()
        }

        refresh()
    }

    private fun nextLabel(cur: String): String {
        val order = listOf("unknown","queen","worker","drone","varroa","negative")
        val idx = order.indexOf(cur).takeIf { it >= 0 } ?: 0
        return order[(idx + 1) % order.size]
    }

    private fun refresh() {
        val items = TrainingUtils.listItems(this)
        adapter.setData(items)
        tvInfo.text = "Frames: ${items.size}  (tap image to cycle label)"
    }

    private fun pickImages() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQ_PICK)
    }

    private fun capturePhoto() {
        val dir = getExternalFilesDir(null)!!
        currentPhotoFile = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(this, "com.example.beequeen.provider", currentPhotoFile!!)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { putExtra(MediaStore.EXTRA_OUTPUT, uri) }
        startActivityForResult(intent, REQ_CAPTURE)
    }

    private fun importZip() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "application/zip"; addCategory(Intent.CATEGORY_OPENABLE) }
        startActivityForResult(intent, REQ_IMPORT_ZIP)
    }

    private fun importModel() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "*/*"; addCategory(Intent.CATEGORY_OPENABLE) }
        startActivityForResult(intent, REQ_IMPORT_MODEL)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQ_PICK -> {
                if (data?.clipData != null) {
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val uri = data.clipData!!.getItemAt(i).uri
                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        TrainingUtils.saveFromUri(this, uri)
                    }
                } else data?.data?.let { uri ->
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    TrainingUtils.saveFromUri(this, uri)
                }
                refresh()
            }
            REQ_CAPTURE -> {
                currentPhotoFile?.let { f ->
                    TrainingUtils.saveCaptured(this, f)
                    f.delete()
                }
                refresh()
            }
            REQ_IMPORT_ZIP -> {
                data?.data?.let { uri -> TrainingUtils.importZip(this, uri) }
                refresh()
            }
            REQ_IMPORT_MODEL -> {
                data?.data?.let { uri ->
                    val dst = File(filesDir, "bee_queen_model.tflite")
                    contentResolver.openInputStream(uri)?.use { input -> dst.outputStream().use { input.copyTo(it) } }
                    Toast.makeText(this, "Model imported to app files", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
