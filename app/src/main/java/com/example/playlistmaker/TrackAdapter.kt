package com.example.playlistmaker

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter (
    private var tracks: MutableList<Track>,
    private val listener: TrackListener
)
: RecyclerView.Adapter<TrackViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tracks_view, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position], listener)

    }

    override fun getItemCount() = tracks.size

    fun listClear() {
        tracks.clear()
    }

    interface TrackListener {
        fun onClick(track: Track)
    }

}

