package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson

const val HISTORY_LIST = "history_list"
const val MAX_SIZE_LIST = 10

class History(val sharedPreferences: SharedPreferences) {

    private val sharePrefer = sharedPreferences
    private var historyTrackList: MutableList<Track>? = mutableListOf()

    fun clearTrackListHistory() {
        sharePrefer.edit().clear().apply()
        historyTrackList?.clear()
    }

    fun getHistoryList() : List<Track> {
        if (historyTrackList != null) return historyTrackList!!
        else return emptyList()
    }

    fun addSharePreference(track: Track) {
        historyTrackList?.removeIf { it.trackId == track.trackId }
        historyTrackList?.add(0, track)
        if (historyTrackList!!.size > MAX_SIZE_LIST) {
            historyTrackList = historyTrackList!!.subList(0, MAX_SIZE_LIST)
        }
        writeSharePreference(historyTrackList!!)
    }

    private fun writeSharePreference(tracks: MutableList<Track>) {
        val json = Gson().toJson(tracks)
        sharePrefer.edit()
            .putString(HISTORY_LIST, json)
            .apply()
    }

    fun readSharePreference() {
        val json = sharePrefer.getString(HISTORY_LIST, null)
        if (json != null) {
            historyTrackList = Gson().fromJson(json, Array<Track>::class.java).toMutableList()
        }
    }

}
