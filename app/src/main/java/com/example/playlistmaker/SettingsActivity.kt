package com.example.playlistmaker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.net.Uri


class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val buttonBack = findViewById<ImageButton>(R.id.back)
        val buttonShare = findViewById<Button>(R.id.shareButton)
        val buttonSupport = findViewById<Button>(R.id.supportButton)
        val buttonForward = findViewById<Button>(R.id.forwardButton)

        buttonBack.setOnClickListener {
            finish()
        }

        buttonShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            var link = getResources().getString(R.string.link)
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                link
            )
            startActivity(Intent.createChooser(shareIntent, "Поделиться"))
        }

        buttonSupport.setOnClickListener {
            val supportIntent = Intent(Intent.ACTION_SENDTO)
            val email = resources.getString(R.string.email)
            val subject = resources.getString(R.string.subject)
            val text = resources.getString(R.string.text_mail)

            supportIntent.data = Uri.parse("mailto:")
            supportIntent.putExtra(Intent.EXTRA_EMAIL, email)
            supportIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            supportIntent.putExtra(Intent.EXTRA_TEXT, text)
            startActivity(supportIntent)
        }

        buttonForward.setOnClickListener {
            val link_share = getResources().getString(R.string.link_share)
            val forwardtIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(link_share ))
            startActivity(forwardtIntent)
        }


    }
}