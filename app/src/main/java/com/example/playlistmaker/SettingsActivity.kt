package com.example.playlistmaker

import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val buttonBack = findViewById<ImageButton>(R.id.back)
        buttonBack.setOnClickListener {
            val buttonBackIntent = Intent(this, MainActivity::class.java)
            startActivity(buttonBackIntent)
        }


    }
}