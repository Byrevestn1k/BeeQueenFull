package com.example.beequeen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.beequeen.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var vb: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)

        vb.btnLive.setOnClickListener { startActivity(Intent(this, LiveActivity::class.java)) }
        vb.btnTraining.setOnClickListener { startActivity(Intent(this, TrainingActivity::class.java)) }
        vb.btnSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }
}
