package com.example.beequeen

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetectorTestActivity : AppCompatActivity() {

    private lateinit var detectorHelper: DetectorHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detector_test)

        detectorHelper = DetectorHelper(this)

        val testButton: Button = findViewById(R.id.btnTest)
        val resultText: TextView = findViewById(R.id.txtResult)
        val imageView: ImageView = findViewById(R.id.imageView)

        testButton.setOnClickListener {
            // Тестове зображення з drawable
            val testBitmap = BitmapFactory.decodeResource(resources, R.drawable.test_bee)

            imageView.setImageBitmap(testBitmap)

            val results = detectorHelper.detect(testBitmap)

            if (results.isEmpty()) {
                resultText.text = "Нічого не виявлено"
            } else {
                val sb = StringBuilder()
                results.forEach {
                    sb.append("Об’єкт: ${it.label}, ймовірність: ${(it.score * 100).toInt()}%\n")
                }
                resultText.text = sb.toString()
            }
        }
    }
}
