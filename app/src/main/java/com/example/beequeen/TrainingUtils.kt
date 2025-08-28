package com.example.beequeen

import android.content.Context
import android.graphics.*
import android.net.Uri
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object TrainingUtils {
    private fun outDir(context: Context): File = File(context.getExternalFilesDir(null), "train_data").apply { if (!exists()) mkdirs() }

    fun listItems(context: Context): List<File> =
        outDir(context).listFiles()?.filter { it.isFile && (it.name.endsWith(".jpg", true) || it.name.endsWith(".jpeg", true)) }?.sortedBy { it.name } ?: emptyList()

    fun labelsFileFor(image: File): File = File(image.parentFile, image.nameWithoutExtension + ".json")

    fun saveFromUri(context: Context, uri: Uri): File {
        val name = "img_" + System.currentTimeMillis() + ".jpg"
        val dst = File(outDir(context), name)
        context.contentResolver.openInputStream(uri)?.use { input -> dst.outputStream().use { input.copyTo(it) } }
        FileWriter(labelsFileFor(dst)).use { it.write("{\"label\":\"unknown\"}") }
        return dst
    }

    fun saveCaptured(context: Context, src: File): File {
        val dst = File(outDir(context), src.name)
        src.inputStream().use { input -> dst.outputStream().use { input.copyTo(it) } }
        FileWriter(labelsFileFor(dst)).use { it.write("{\"label\":\"unknown\"}") }
        return dst
    }

    fun importZip(context: Context, uri: Uri) {
        context.contentResolver.openInputStream(uri)?.use { ins ->
            ZipInputStream(ins).use { zis ->
                var entry: ZipEntry?
                val dir = outDir(context)
                while (zis.nextEntry.also { entry = it } != null) {
                    entry?.let { e ->
                        if (!e.isDirectory) {
                            val out = File(dir, File(e.name).name)
                            FileOutputStream(out).use { zos -> zis.copyTo(zos) }
                        }
                    }
                }
            }
        }
    }

    fun exportZip(context: Context): File {
        val dir = outDir(context)
        val zip = File(context.getExternalFilesDir(null), "training_export_${System.currentTimeMillis()}.zip")
        ZipOutputStream(FileOutputStream(zip)).use { zos ->
            dir.listFiles()?.forEach { f ->
                zos.putNextEntry(ZipEntry(f.name))
                FileInputStream(f).use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
        return zip
    }

    fun saveLabeledFrame(context: Context, bmp: Bitmap, box: RectF, label: String) {
        val dir = outDir(context)
        val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "${label}_${time}.jpg")
        val mutable = bmp.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)
        val p = Paint().apply { color = Color.RED; style = Paint.Style.STROKE; strokeWidth = 6f }
        canvas.drawRect(box, p)
        FileOutputStream(file).use { mutable.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        FileWriter(labelsFileFor(file)).use { it.write("{\"label\":\"$label\"}") }
    }

    fun updateLabel(image: File, newLabel: String) {
        FileWriter(labelsFileFor(image)).use { it.write("{\"label\":\"$newLabel\"}") }
    }

    fun readLabel(image: File): String {
        return try {
            BufferedReader(FileReader(labelsFileFor(image))).use { br ->
                val txt = br.readText()
                Regex("\"label\"\\s*:\\s*\"([^\"]+)\"").find(txt)?.groupValues?.get(1) ?: "unknown"
            }
        } catch (_: Exception) { "unknown" }
    }
}
