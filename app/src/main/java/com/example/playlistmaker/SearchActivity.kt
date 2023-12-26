package com.example.playlistmaker

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity(), TrackAdapter.TrackListener {

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    private lateinit var searchRunnable: Runnable
    private val handler = Handler(Looper.getMainLooper())
    private var isClickAllowed: Boolean = true
    private var input: String = ""
    private lateinit var inputEditText: EditText
    lateinit var recycler: RecyclerView
    lateinit var trackAdapter: TrackAdapter
    private var trackList: MutableList<Track> = mutableListOf()
    lateinit var searchHint: TextView
    lateinit var buttonClearHistory: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var historyTracks: History
    lateinit var trackHistoryAdapter: TrackAdapter
    lateinit var imageErrorView: ImageView
    lateinit var textErrorView: TextView
    lateinit var buttonUpdateView: Button
    lateinit var clearButton: ImageView
    lateinit var progressBar: CircularProgressIndicator
    private val tracksBaseUrl = "https://itunes.apple.com"
    private val retrofit = Retrofit.Builder()
        .baseUrl(tracksBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val tracksService = retrofit.create(TrackApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        recycler = findViewById(R.id.recyclerView)
        trackAdapter = TrackAdapter(trackList, this)
        sharedPreferences = getSharedPreferences(MY_PREF, MODE_PRIVATE)
        historyTracks = History(sharedPreferences)
        historyTracks.readSharePreference()
        trackHistoryAdapter = TrackAdapter(historyTracks.getHistoryList().toMutableList(), this)

        val buttonBack = findViewById<ImageButton>(R.id.back)
        clearButton = findViewById(R.id.clearIcon)
        inputEditText = findViewById(R.id.searchView)

        progressBar = findViewById(R.id.progressBar)
        imageErrorView = findViewById(R.id.img_error_server)
        textErrorView = findViewById(R.id.text_error_server)
        buttonUpdateView = findViewById(R.id.button_update_view)
        searchHint = findViewById(R.id.searchHint)
        buttonClearHistory = findViewById(R.id.button_clear_history)

        buttonClearHistory.setOnClickListener {
            historyTracks.clearTrackListHistory()
            trackHistoryAdapter.listClear()
            trackHistoryAdapter.notifyDataSetChanged()
            searchHint.visibility = View.GONE
            buttonClearHistory.visibility = View.GONE
        }

        searchRunnable = Runnable { search() }

        buttonUpdateView.setOnClickListener { search() }

        buttonBack.setOnClickListener { finish() }

        clearButton.setOnClickListener {
            inputEditText.setText("")
            // Скрыть клавиатуру
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0)
            imageErrorView.visibility = View.GONE
            textErrorView.visibility = View.GONE
            buttonUpdateView.visibility = View.GONE
            searchHint.visibility = View.GONE
            buttonClearHistory.visibility = View.GONE
            progressBar.visibility = View.GONE
            trackAdapter.listClear()
            trackAdapter.notifyDataSetChanged()
            trackHistoryAdapter.listClear()
            trackAdapter.notifyDataSetChanged()
            inputEditText.clearFocus()
        }

        inputEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && inputEditText.text.isEmpty()) {
                startRecyclerHistory()
            }
        }


        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    clearButton.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    searchHint.visibility = View.GONE
                    buttonClearHistory.visibility = View.GONE
                    clearButton.visibility = View.GONE
                    textErrorView.visibility = View.GONE
                    imageErrorView.visibility = View.GONE
                    startRecyclerHistory()
                } else {
                    searchHint.visibility = View.GONE
                    clearButton.visibility = View.GONE
                    buttonClearHistory.visibility = View.GONE
                    trackHistoryAdapter.listClear()
                    trackHistoryAdapter.notifyDataSetChanged()
                    searchDebounce()
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
        buttonClearHistory.visibility = View.GONE
        progressBar.visibility = View.GONE
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
        buttonClearHistory.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    fun search() {
        if (inputEditText.text.isNotEmpty()) {
            searchHint.visibility = View.GONE
            recycler.visibility = View.GONE
            textErrorView.visibility = View.GONE
            imageErrorView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE


            tracksService.getTrackModel(inputEditText.getText().toString())
                .enqueue(object : Callback<Tracks> {
                    override fun onResponse(call: Call<Tracks>, response: Response<Tracks>) {
                        progressBar.visibility = View.GONE
                        if (response.isSuccessful) {
                            trackList.clear()
                            trackAdapter.listClear()

                            recycler.visibility = View.VISIBLE
                            textErrorView.visibility = View.GONE
                            imageErrorView.visibility = View.GONE
                            buttonUpdateView.visibility = View.GONE

                            val results = response.body()?.results

                            if (results?.isNotEmpty() == true) {
                                trackList.addAll(results!!)
                                recycler.adapter = trackAdapter
                                trackAdapter.notifyDataSetChanged()
                            } else {
                                showErrorEmpty()
                            }

                        } else {
                            showErrorConnect()
                        }
                    }

                    override fun onFailure(call: Call<Tracks>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        showErrorConnect()
                        t.printStackTrace()
                    }
                })
        }
    }

    override fun onClick(track: Track) {
        historyTracks.addSharePreference(track)
        if (clickDebounce()) {
            val intent = Intent(this, AudioPlayerActivity::class.java)
            intent.putExtra("track", track)
            startActivity(intent)
        }
    }

    fun startRecyclerHistory() {
        if (historyTracks.getHistoryList().isNotEmpty()) {
            searchHint.visibility = View.VISIBLE
            buttonClearHistory.visibility = View.VISIBLE
            trackHistoryAdapter.listClear()
            trackHistoryAdapter.notifyDataSetChanged()
            trackHistoryAdapter = TrackAdapter(historyTracks.getHistoryList().toMutableList(), this)
            recycler.adapter = trackHistoryAdapter
            trackHistoryAdapter.notifyDataSetChanged()
        }
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

}