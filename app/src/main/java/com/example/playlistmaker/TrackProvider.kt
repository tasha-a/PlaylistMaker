package com.example.playlistmaker

import android.content.Context
import android.util.TypedValue
import java.text.SimpleDateFormat
import java.util.Locale

object TrackProvider {
    private var track: Track? = null

    fun setTrack(track: Track) {
        this.track = track
    }

    fun getTrack(): Track? {
        return track
    }

    fun dateFormat(mlsec: Int): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(mlsec)
    }

    fun dpToPx(dp: Int, context: Context): Int {

        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

}