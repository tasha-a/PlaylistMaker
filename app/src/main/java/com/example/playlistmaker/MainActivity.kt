package com.example.playlistmaker

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var checkedDarkMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(MY_PREF, MODE_PRIVATE)
        checkedDarkMode = sharedPreferences.getBoolean(DARK_MODE, false)
        (applicationContext as App).switchTheme(checkedDarkMode)

        val buttonSearch = findViewById<Button>(R.id.searchButton)
        buttonSearch.setOnClickListener {
            val searchIntent = Intent(this, Search::class.java)
            startActivity(searchIntent)
        }

        val mediaButton = findViewById<Button>(R.id.media)
        mediaButton.setOnClickListener {
            val mediaIntent = Intent(this, MediaLibrary::class.java)
            startActivity(mediaIntent)
        }

        val settingsButton = findViewById<Button>(R.id.settings)
        settingsButton.setOnClickListener {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }
    }
}