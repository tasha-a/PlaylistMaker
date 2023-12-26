package com.example.playlistmaker

import android.content.Context
import android.os.Parcelable
import android.util.TypedValue
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Locale

@Parcelize
data class Track(
    val trackId: Int,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Int,
    val artworkUrl100: String,
    val collectionName: String,
    val releaseDate: String,
    val primaryGenreName: String,
    val country: String,
    val previewUrl: String,
) : Parcelable {
    val artworkUrl512
        get() = artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

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