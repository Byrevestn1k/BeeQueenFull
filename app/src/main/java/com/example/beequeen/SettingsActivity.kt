package com.example.beequeen
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val sw = findViewById<Switch>(R.id.switchCloud)
        sw.setOnCheckedChangeListener { _, isChecked ->
            // placeholder: save preference
            getSharedPreferences("bee_prefs", MODE_PRIVATE).edit().putBoolean("cloud", isChecked).apply()
        }
        sw.isChecked = getSharedPreferences("bee_prefs", MODE_PRIVATE).getBoolean("cloud", false)
    }
}
