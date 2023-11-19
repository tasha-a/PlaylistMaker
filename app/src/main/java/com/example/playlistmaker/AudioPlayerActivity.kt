package com.example.playlistmaker

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class AudioPlayerActivity : AppCompatActivity() {

    lateinit var titleField: TextView
    lateinit var track: Track
    lateinit var artistField: TextView
    lateinit var timeField: TextView
    lateinit var albumField: TextView
    lateinit var releaseField: TextView
    lateinit var genreField: TextView
    lateinit var countryField: TextView
    lateinit var coverField: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        val buttonBack = findViewById<ImageButton>(R.id.back)
        buttonBack.setOnClickListener() { finish() }

        titleField = findViewById(R.id.name_track_field)
        artistField = findViewById(R.id.artist_track_field)
        timeField = findViewById(R.id.time_track_field)
        albumField = findViewById(R.id.album_track_field)
        releaseField = findViewById(R.id.year_track_field)
        genreField = findViewById(R.id.genre_track_field)
        countryField = findViewById(R.id.country_track_field)
        coverField = findViewById(R.id.cover_track)

        if (TrackProvider.getTrack() != null) {
            track = TrackProvider.getTrack()!!
            fillFields(track)
        }
    }

    private fun fillFields(track: Track) {

        titleField.text = track.trackName
        artistField.text = track.artistName
        timeField.text = TrackProvider.dateFormat(track.trackTimeMillis)
        albumField.text = track.collectionName
        releaseField.text = track.releaseDate.substring(0, 4)
        genreField.text = track.primaryGenreName
        countryField.text = track.country

        val artworkUrl512 = TrackProvider.getCoverArtwork(track.artworkUrl100)
        val cornerRadiusDp = 8
        val cornerRadiusPx = TrackProvider.dpToPx(cornerRadiusDp, this)

        Glide.with(this)
            .load(artworkUrl512)
            .placeholder(R.drawable.placeholder_312)
            .transform(CenterCrop(), RoundedCorners(cornerRadiusPx))
            .into(coverField)
    }
}