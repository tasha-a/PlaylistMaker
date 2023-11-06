package com.example.playlistmaker

import android.app.Activity
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val SEARCH_PREF = "search_pref"

class Search : AppCompatActivity(), TrackAdapter.TrackListener {
    private var input: String = ""
    private lateinit var inputEditText: EditText
    lateinit var recycler: RecyclerView
    lateinit var trackAdapter: TrackAdapter
    private var trackList: MutableList<Track> = mutableListOf()
    private var trackListHistory: MutableList<Track> = mutableListOf()
    lateinit var searchHint: TextView
    lateinit var buttonClearHistory: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var historyTracks: History
    lateinit var trackHistoryAdapter: TrackAdapter
    lateinit var imageErrorView: ImageView
    lateinit var textErrorView: TextView
    lateinit var buttonUpdateView: Button
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
        trackHistoryAdapter = TrackAdapter(trackListHistory, this)
        sharedPreferences = getSharedPreferences(SEARCH_PREF, MODE_PRIVATE)
        historyTracks = History(sharedPreferences)

        val buttonBack = findViewById<ImageButton>(R.id.back)
        val clearButton = findViewById<ImageView>(R.id.clearIcon)
        inputEditText = findViewById(R.id.searchView)

        imageErrorView = findViewById(R.id.img_error_server)
        textErrorView = findViewById(R.id.text_error_server)
        buttonUpdateView = findViewById(R.id.button_update_view)
        searchHint = findViewById(R.id.searchHint)
        buttonClearHistory = findViewById(R.id.button_clear_history)


        buttonClearHistory.setOnClickListener {
            historyTracks.clearTrackListHistory()
            trackHistoryAdapter.listClear()
            trackHistoryAdapter.notifyDataSetChanged()
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                search()
                true
            }
            false
        }

        buttonUpdateView.setOnClickListener { search() }

        buttonBack.setOnClickListener { finish() }

        clearButton.setOnClickListener {
            inputEditText.setText("")
            // Скрыть клавиатуру
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0)

            buttonClearHistory.visibility = View.GONE

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


        inputEditText.setOnFocusChangeListener { view, hasFocus ->
            searchHint.visibility =
                if (hasFocus && inputEditText.text.isEmpty()) View.VISIBLE else View.GONE

            if (historyTracks.readSharePreference().isNotEmpty()) {
                buttonClearHistory.visibility = View.VISIBLE
                trackListHistory.addAll(historyTracks.readSharePreference()!!)
                trackHistoryAdapter = TrackAdapter(trackListHistory.toMutableList(), this)
                recycler.adapter = trackHistoryAdapter
                trackHistoryAdapter.notifyItemRangeChanged(0, MAX_SIZE_LIST)
            }
        }


        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchHint.visibility =
                    if (inputEditText.hasFocus() && s?.isEmpty() == true) View.VISIBLE else View.GONE
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
                        trackAdapter.listClear()
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
                    showErrorConnect()
                    t.printStackTrace()
                }
            })
    }

    override fun onClick(track: Track) {
        historyTracks.addSharePreference(track)
    }
}