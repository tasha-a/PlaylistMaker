package com.example.playlistmaker

import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.databinding.ActivityAudioPlayerBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val TIMER_DELAY = 1000L
        private const val SECONDS_COUNT = 29000
    }

    private var mainThreadHandler: Handler? = null
    private var playerState = STATE_DEFAULT
    private lateinit var binding: ActivityAudioPlayerBinding
    private lateinit var urlSound: String
    private var mediaPlayer = MediaPlayer()
    private var trackMls: Int = 0
    private var currentPosition = 0
    private var run: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainThreadHandler = Handler(Looper.getMainLooper())

        val track: Track? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("track", Track::class.java)
        } else {
            intent.getParcelableExtra("track")
        }

        binding.back.setOnClickListener {
            mediaPlayer.stop()
            finish()
        }

        fillFields(track!!)

        preparePlayer()
        binding.playButton.setOnClickListener {
            playbackControl()
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    private fun fillFields(track: Track) {

        trackMls = track.trackTimeMillis
        binding.nameTrackField.text = track.trackName
        binding.artistTrackField.text = track.artistName
        binding.timeTrackField.text = track.dateFormat(track.trackTimeMillis)
        binding.timerTrack.text = track.dateFormat(track.trackTimeMillis)
        binding.albumTrackField.text = track.collectionName
        binding.yearTrackField.text = track.releaseDate.substring(0, 4)
        binding.genreTrackField.text = track.primaryGenreName
        binding.countryTrackField.text = track.country
        urlSound = track.previewUrl


        val artworkUrl512 = track.artworkUrl512
        val cornerRadiusDp = 8
        val cornerRadiusPx = track.dpToPx(cornerRadiusDp, this)

        Glide.with(this)
            .load(artworkUrl512)
            .placeholder(R.drawable.placeholder_312)
            .transform(CenterCrop(), RoundedCorners(cornerRadiusPx))
            .into(binding.coverTrack)
    }

    private fun preparePlayer() {
        mediaPlayer.setDataSource(urlSound)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            playerState = STATE_PREPARED
        }
        mediaPlayer.setOnCompletionListener {
            binding.timerTrack.text = dateFormat(0)
            playImg()
            run = null
            playerState = STATE_PREPARED
        }
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }

            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
            }
        }
    }


    private fun startPlayer() {
        mainThreadHandler?.post(startTimer())
        mediaPlayer.start()
        pauseImg()
        playerState = STATE_PLAYING
    }

    private fun pausePlayer() {
        run?.let {
            mainThreadHandler?.removeCallbacks(it)
        }
        mediaPlayer.pause()
        playImg()
        playerState = STATE_PAUSED
    }


    private fun startTimer(): Runnable {
        run = object : Runnable {
            override fun run() {
                if (mediaPlayer.currentPosition < SECONDS_COUNT) {
                    binding.timerTrack.text = dateFormat(mediaPlayer.currentPosition)
                    mainThreadHandler?.postDelayed(this, TIMER_DELAY / 2)
                }
            }
        }
        return run!!
    }


    private fun dateFormat(mlsec: Int): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(mlsec)
    }

    private fun pauseImg() {
        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> binding.playButton.setImageResource(R.drawable.pause_dark)

            Configuration.UI_MODE_NIGHT_NO -> binding.playButton.setImageResource(R.drawable.pause_light)
        }
    }

    private fun playImg() {
        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> binding.playButton.setImageResource(R.drawable.play_dark)

            Configuration.UI_MODE_NIGHT_NO -> binding.playButton.setImageResource(R.drawable.play_light)
        }
    }
}