package com.example.beequeen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

object TrainingUtils {

    private fun outDir(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "train_data")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun listItems(context: Context): List<ImageItem> {
        val dir = outDir(context)
        return dir.listFiles()?.filter { it.isFile && (it.name.endsWith(".jpg") || it.name.endsWith(".jpeg")) }
            ?.map {
                val labelFile = File(it.parentFile, it.nameWithoutExtension + ".txt")
                val label = if (labelFile.exists()) labelFile.readText() else "unknown"
                ImageItem(Uri.fromFile(it), label)
            } ?: emptyList()
    }

    fun saveFromUri(context: Context, uri: Uri): ImageItem {
        val dir = outDir(context)
        val name = "img_${System.currentTimeMillis()}.jpg"
        val dst = File(dir, name)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dst).use { output -> input.copyTo(output) }
        }
        val labelFile = File(dir, dst.nameWithoutExtension + ".txt")
        labelFile.writeText("unknown")
        return ImageItem(Uri.fromFile(dst), "unknown")
    }

    fun updateLabelFile(imageItem: ImageItem, newLabel: String) {
        val file = File(imageItem.uri.path ?: return)
        val labelFile = File(file.parentFile, file.nameWithoutExtension + ".txt")
        labelFile.writeText(newLabel)
    }
}
