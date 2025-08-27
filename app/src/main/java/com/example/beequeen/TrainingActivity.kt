package com.example.beequeen
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.beequeen.databinding.ActivityTrainingBinding
import java.io.File
class TrainingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTrainingBinding
    private lateinit var adapter: ImageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnImportModel.setOnClickListener { importModel() }
        binding.btnExportDataset.setOnClickListener { exportDataset() }
        adapter = ImageAdapter()
        binding.rvImages.layoutManager = GridLayoutManager(this, 3)
        binding.rvImages.adapter = adapter
        loadImages()
    }
    private fun importModel() {
        // Use system picker to import .tflite (not implemented here for brevity)
        Toast.makeText(this, "Use file manager to copy bee_queen_model.tflite into app files (Training screen) in this demo.", Toast.LENGTH_LONG).show()
    }
    private fun exportDataset() {
        Toast.makeText(this, "Export created: ${'$'}{zipDataset()}", Toast.LENGTH_SHORT).show()
    }
    private fun loadImages() {
        val dir = File(getExternalFilesDir(null), "train_data")
        val files = dir.listFiles() ?: arrayOf()
        adapter.setFiles(files.toList())
        binding.tvInfo.text = "Frames: ${'$'}{files.size}"

    }
    private fun zipDataset(): String {
        val dir = File(getExternalFilesDir(null), "train_data")
        val zip = File(getExternalFilesDir(null), "train_dataset.zip")
        java.util.zip.ZipOutputStream(zip.outputStream()).use { zos ->
            dir.listFiles()?.forEach { f -> zos.putNextEntry(java.util.zip.ZipEntry(f.name)); f.inputStream().use { it.copyTo(zos) }; zos.closeEntry() }
        }
        return zip.absolutePath
    }
    inner class ImageAdapter : RecyclerView.Adapter<ImageAdapter.VH>() {
        private var files: List<File> = emptyList()
        fun setFiles(list: List<File>) { files = list; notifyDataSetChanged() }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val iv = ImageView(parent.context); iv.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300); iv.scaleType = ImageView.ScaleType.CENTER_CROP
            return VH(iv)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            val f = files[position]; holder.imageView.setImageURI(android.net.Uri.fromFile(f))
            holder.imageView.setOnClickListener { if (f.delete()) { Toast.makeText(this@TrainingActivity, "Deleted", Toast.LENGTH_SHORT).show(); setFiles(files.filter { it.exists() }) } }
        }
        override fun getItemCount() = files.size
        inner class VH(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
    }
}
