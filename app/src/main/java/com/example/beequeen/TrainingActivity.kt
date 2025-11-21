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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TrainingActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var tvInfo: TextView
    private lateinit var adapter: ImageAdapter

    private val REQ_PICK = 1001
    private val REQ_CAPTURE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        rv = findViewById(R.id.rvImages)
        tvInfo = findViewById(R.id.tvInfo)

        adapter = ImageAdapter(TrainingUtils.listItems(this).toMutableList()) { image ->
            val next = nextLabel(image.label)
            TrainingUtils.updateLabelFile(image, next)
            refresh()
            Toast.makeText(this, "Label: $next", Toast.LENGTH_SHORT).show()
        }

        rv.layoutManager = GridLayoutManager(this, 3)
        rv.adapter = adapter

        findViewById<Button>(R.id.btnPick).setOnClickListener { pickImages() }

        refresh()
    }

    private fun nextLabel(cur: String): String {
        val order = listOf("unknown", "queen", "worker", "drone", "varroa", "negative")
        val idx = order.indexOf(cur).takeIf { it >= 0 } ?: 0
        return order[(idx + 1) % order.size]
    }

    private fun refresh() {
        val items = TrainingUtils.listItems(this)
        adapter.setData(items)
        tvInfo.text = "Frames: ${items.size}"
    }

    private fun pickImages() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQ_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQ_PICK -> {
                data?.clipData?.let { clip ->
                    for (i in 0 until clip.itemCount) {
                        val uri = clip.getItemAt(i).uri
                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        TrainingUtils.saveFromUri(this, uri)
                    }
                } ?: data?.data?.let { uri ->
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    TrainingUtils.saveFromUri(this, uri)
                }
                refresh()
            }
        }
    }
}
