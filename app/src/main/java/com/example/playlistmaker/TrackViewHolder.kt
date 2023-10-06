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

    private val trackNameView: TextView = itemView.findViewById(R.id.trackName)
    private val trackArtistView: TextView = itemView.findViewById(R.id.trackArtist)
    private val trackTimeView: TextView = itemView.findViewById(R.id.trackTime)
    private val trackImage: ImageView = itemView.findViewById(R.id.trackImage)

    @RequiresApi(Build.VERSION_CODES.S)
    fun bind(model: Track) {

        val cornerRadiusDp = 2
        val cornerRadiusPx = dpToPx(cornerRadiusDp, itemView.context)

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


    private fun dpToPx(dp: Int, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

}