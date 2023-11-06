package com.example.playlistmaker

import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson


const val HISTORY_LIST = "history_list"
const val MAX_SIZE_LIST = 10

class History(val sharedPreferences: SharedPreferences) {

    private val sharePrefer = sharedPreferences

    fun clearTrackListHistory() {
        sharePrefer.edit().clear().apply()
    }

    fun addSharePreference(track: Track) {
        var currentHistoryList = readSharePreference().toMutableList()
        currentHistoryList.removeIf { it.trackId == track.trackId }
        currentHistoryList.add(0, track)
        if (currentHistoryList.size == MAX_SIZE_LIST) {
            currentHistoryList.removeAt(0)
        }
        writeSharePreference(currentHistoryList)
    }

    private fun writeSharePreference(tracks: MutableList<Track>) {
        val json = Gson().toJson(tracks)
        sharePrefer.edit()
            .putString(HISTORY_LIST, json)
            .apply()
    }

    fun readSharePreference(): List<Track> {
        val json = sharePrefer.getString(HISTORY_LIST, null) ?: return emptyList()
        return Gson().fromJson(json, Array<Track>::class.java).toList()
    }

}
