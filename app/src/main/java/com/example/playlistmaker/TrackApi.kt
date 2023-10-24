package com.example.playlistmaker

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TrackApi {

    @GET("/search")
    fun getTrackModel(@Query("term") text: String) : Call<Tracks>
}