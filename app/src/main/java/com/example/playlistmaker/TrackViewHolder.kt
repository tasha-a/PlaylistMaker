package com.example.playlistmaker

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val trackNameView: TextView = itemView.findViewById(R.id.trackName)
    private val trackArtistView: TextView = itemView.findViewById(R.id.trackArtist)
    private val trackTimeView: TextView = itemView.findViewById(R.id.trackTime)
    private val trackImage: ImageView = itemView.findViewById(R.id.trackImage)

    fun bind(model: Track, listener: TrackAdapter.TrackListener) {

        val cornerRadiusDp = 2
        val cornerRadiusPx = model.dpToPx(cornerRadiusDp, itemView.context)
        val timeTrackMs = model.dateFormat(model.trackTimeMillis)

        itemView.setOnClickListener {
            listener.onClick(model)
        }

        trackNameView.text = model.trackName
        trackArtistView.text = model.artistName
        trackTimeView.text = timeTrackMs
        Glide.with(itemView)
            .load(model.artworkUrl100)
            .placeholder(R.drawable.placeholder)
            .transform(CenterCrop(), RoundedCorners(cornerRadiusPx))
            .into(trackImage)

    }

}