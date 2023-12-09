package com.example.playlistmaker

import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var titleField: TextView
//    private lateinit var track: Track
    private lateinit var artistField: TextView
    private lateinit var timeField: TextView
    private lateinit var albumField: TextView
    private lateinit var releaseField: TextView
    private lateinit var genreField: TextView
    private lateinit var countryField: TextView
    private lateinit var coverField: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        titleField = findViewById(R.id.name_track_field)
        artistField = findViewById(R.id.artist_track_field)
        timeField = findViewById(R.id.time_track_field)
        albumField = findViewById(R.id.album_track_field)
        releaseField = findViewById(R.id.year_track_field)
        genreField = findViewById(R.id.genre_track_field)
        countryField = findViewById(R.id.country_track_field)
        coverField = findViewById(R.id.cover_track)

        val track: Track? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("track", Track::class.java)
        } else {
            intent.getParcelableExtra("track")
        }

        val buttonBack = findViewById<ImageButton>(R.id.back)
        buttonBack.setOnClickListener { finish() }


       fillFields(track!!)


    }

    private fun fillFields(track: Track) {

        titleField.text = track.trackName
        artistField.text = track.artistName
        timeField.text = TrackProvider.dateFormat(track.trackTimeMillis)
        albumField.text = track.collectionName
        releaseField.text = track.releaseDate.substring(0, 4)
        genreField.text = track.primaryGenreName
        countryField.text = track.country

        val artworkUrl512 = track.artworkUrl512
        val cornerRadiusDp = 8
        val cornerRadiusPx = TrackProvider.dpToPx(cornerRadiusDp, this)

        Glide.with(this)
            .load(artworkUrl512)
            .placeholder(R.drawable.placeholder_312)
            .transform(CenterCrop(), RoundedCorners(cornerRadiusPx))
            .into(coverField)
    }
}

