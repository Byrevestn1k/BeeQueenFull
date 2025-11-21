package com.example.beequeen

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var btnLive: Button
    private lateinit var btnTraining: Button
    private lateinit var btnSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLive = findViewById(R.id.btnLive)
        btnTraining = findViewById(R.id.btnTraining)
        btnSettings = findViewById(R.id.btnSettings)

        // Перехід у LiveActivity
        btnLive.setOnClickListener {
            val intent = Intent(this, LiveActivity::class.java)
            startActivity(intent)
        }

        // Перехід у TrainingActivity
        btnTraining.setOnClickListener {
            try {
                val intent = Intent(this, TrainingActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "TrainingActivity не знайдено", Toast.LENGTH_SHORT).show()
            }
        }

        // Перехід у SettingsActivity
        btnSettings.setOnClickListener {
            try {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "SettingsActivity не знайдено", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
