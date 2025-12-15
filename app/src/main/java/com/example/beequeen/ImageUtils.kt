package com.example.beequeen

import android.graphics.*
import android.media.Image

import java.io.ByteArrayOutputStream

object ImageUtils {

    fun imageToBitmap(image: Image, rotationDegrees: Int): Bitmap {
        require(image.format == ImageFormat.YUV_420_888) {
            "Unsupported image format: ${image.format}"
        }

        val yBuffer = image.planes[0].buffer   // Y
        val uBuffer = image.planes[1].buffer   // U
        val vBuffer = image.planes[2].buffer   // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Y
        yBuffer[nv21, 0, ySize]

        // UV – важливо: ітеруємо з урахуванням stride
        val uvStride = image.planes[1].rowStride
        val pxStride = image.planes[1].pixelStride
        var offset = ySize
        for (row in 0 until image.height / 2) {
            for (col in 0 until image.width / 2) {
                val uIndex = row * uvStride + col * pxStride
                val vIndex = row * image.planes[2].rowStride + col * image.planes[2].pixelStride
                nv21[offset++] = vBuffer[vIndex]
                nv21[offset++] = uBuffer[uIndex]
            }
        }

        val yuv = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuv.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val jpeg = out.toByteArray()
        var bmp = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)

        if (rotationDegrees != 0) {
            val m = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
        }
        return bmp
    }
}