package com.example.playlistmaker

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Search : AppCompatActivity() {

    private var input: String = ""
    private lateinit var inputEditText: EditText
    lateinit var recycler: RecyclerView
    lateinit var trackAdapter: TrackAdapter
    private var trackList: MutableList<Track> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        recycler = findViewById(R.id.recyclerView)
        val tracksBaseUrl = "https://itunes.apple.com"
        trackAdapter = TrackAdapter(trackList)
        recycler.adapter = trackAdapter

        val buttonBack = findViewById<ImageButton>(R.id.back)
        val clearButton = findViewById<ImageView>(R.id.clearIcon)
        inputEditText = findViewById(R.id.searchView)

        val imageErrorView = findViewById<ImageView>(R.id.img_error_server)
        val textErrorView = findViewById<TextView>(R.id.text_error_server)
        val buttonUpdateView = findViewById<Button>(R.id.button_update_view)

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(tracksBaseUrl).client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val tracksService = retrofit.create(TrackApi::class.java)

        fun showErrorConnect() {
            when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> imageErrorView.setImageResource(
                    R.drawable.error_internet_dark
                )

                Configuration.UI_MODE_NIGHT_NO -> imageErrorView.setImageResource(
                    R.drawable.error_internet_light
                )
            }
            textErrorView.setText(R.string.error_internet_text)
            textErrorView.visibility = View.VISIBLE
            imageErrorView.visibility = View.VISIBLE
            buttonUpdateView.visibility = View.VISIBLE
            recycler.visibility = View.GONE
        }

        fun showErrorEmpty() {
            when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> imageErrorView.setImageResource(
                    R.drawable.error_search_dark
                )

                Configuration.UI_MODE_NIGHT_NO -> imageErrorView.setImageResource(
                    R.drawable.error_search_light
                )
            }
            textErrorView.setText(R.string.error_server_text)
            textErrorView.visibility = View.VISIBLE
            imageErrorView.visibility = View.VISIBLE
            recycler.visibility = View.GONE
        }

        fun search() {
            tracksService.getTrackModel(inputEditText.getText().toString())
                .enqueue(object : Callback<Tracks> {
                    override fun onResponse(call: Call<Tracks>, response: Response<Tracks>) {
                        if (response.isSuccessful) {
                            trackList.clear()
                            if (recycler.visibility == View.GONE) {
                                recycler.visibility = View.VISIBLE
                            }
                            if (textErrorView.visibility == View.VISIBLE) {
                                textErrorView.visibility = View.GONE
                            }
                            if (imageErrorView.visibility == View.VISIBLE) {
                                imageErrorView.visibility = View.GONE
                            }
                            if (buttonUpdateView.visibility == View.VISIBLE) {
                                buttonUpdateView.visibility = View.GONE
                            }
                            if (response.body()?.results?.isNotEmpty() == true) {
                                trackList.addAll(response.body()?.results!!)
                                trackAdapter.notifyDataSetChanged()
                            } else {
                                showErrorEmpty()
                            }

                        } else {
                            // Сервер отклонил наш запрос с ошибкой
                            showErrorConnect()
                        }
                    }

                    override fun onFailure(call: Call<Tracks>, t: Throwable) {
                        // Не смогли соединиться с сервером
                        // Выводим ошибку в лог, что-то пошло не так
                        showErrorConnect()
                        t.printStackTrace()
                    }
                })
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                search()
                true
            }
            false
        }

        buttonUpdateView.setOnClickListener {
            search()
        }

        buttonBack.setOnClickListener {
            finish()
        }

        clearButton.setOnClickListener {
            inputEditText.setText("")
            // Скрыть клавиатуру
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0)

            if (imageErrorView.visibility == View.VISIBLE) {
                imageErrorView.visibility = View.GONE
            }

            if (textErrorView.visibility == View.VISIBLE) {
                textErrorView.visibility = View.GONE
            }

            if (buttonUpdateView.visibility == View.VISIBLE) {
                buttonUpdateView.visibility = View.GONE
            }
            trackAdapter.listClear()
            trackAdapter.notifyDataSetChanged()
        }


        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    clearButton.visibility = View.GONE
                } else {
                    input += s.toString()
                    clearButton.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        inputEditText.addTextChangedListener(simpleTextWatcher)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("INPUT_SEARCH", input)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        input = savedInstanceState.getString("INPUT_SEARCH") ?: ""
    }

    private fun isDarkTheme(activity: Activity): Boolean {
        return activity.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}