package com.example.playlistmaker

import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val trackNameView: TextView
    private val trackArtistView: TextView
    private val trackTimeView: TextView
    private val trackImage: ImageView

    init {
        trackNameView = itemView.findViewById(R.id.trackName)
        trackArtistView = itemView.findViewById(R.id.trackArtist)
        trackTimeView = itemView.findViewById(R.id.trackTime)
        trackImage = itemView.findViewById(R.id.trackImage)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun bind(model: Track) {

        trackNameView.text = model.trackName
        trackArtistView.text = model.artistName
        trackTimeView.text = model.trackTime
        Glide.with(itemView)
            .load(model.artworkUrl100)
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .apply(RequestOptions.bitmapTransform(RoundedCorners(cornerRadiusPx)))
            .into(trackImage)
    }

    val cornerRadiusDp = 2
    val cornerRadiusPx = dpToPx(cornerRadiusDp, itemView.context) // Переводим радиус из dp в px

    private fun dpToPx(dp: Int, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

}