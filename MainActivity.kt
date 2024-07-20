package com.temp.lifestylegps

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import java.util.TimerTask


class MainActivity : AppCompatActivity() {

    private lateinit var apiservice: ApiService
    private lateinit var apiservicekey : ApiServiceKey
    private lateinit var setting: ImageView
    private lateinit var temperatureTextView: TextView
    private lateinit var temperatureTextViewsecond: TextView
    private lateinit var spinner: Spinner
    private lateinit var spinnersecond: Spinner
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var timerTextView: TextView
    private lateinit var refresh : ImageButton
    private var timer: Timer? = null
    private lateinit var handler :Handler

    private lateinit var selectedName: String
    private var selectedPosition = 0
    private var seconds = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("com.example.lifestylegps", Context.MODE_PRIVATE)

        setting = findViewById(R.id.setting)
        temperatureTextView = findViewById(R.id.txt3)
        temperatureTextViewsecond = findViewById(R.id.txt32)
        spinner = findViewById(R.id.spinner)
        spinnersecond = findViewById(R.id.spinnersecond)
        apiservice = RetrofitInstance.retrofit.create(ApiService::class.java)
        apiservicekey = RetrofitInstanceKey.retrofitLifestyleGPS.create(ApiServiceKey::class.java)
        timerTextView = findViewById(R.id.timer_textview)
        refresh = findViewById(R.id.refresh)
        handler = Handler()
        selectedName = ""

        refresh.setOnClickListener {
            seconds = 10
            timerTextView.text = "Timer: 0:${String.format("%02d", seconds)}"
            handler.removeCallbacksAndMessages(null)
            updateTimerTextView()
        }

        fetchNames()
        fetchApiKey()

        if (isNetworkAvailable(this)) {
            startTimer()
        } else {
            stopTimer()
            Toast.makeText(this, "Mobile data is off, timer stopped", Toast.LENGTH_SHORT).show()
        }


        setting.setOnClickListener {
//            showPopupWindow(it)
        }

    }
    private fun startTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                handler.post {
                    if (isNetworkAvailable(this@MainActivity)) {
                        fetchNames()
                        fetchTempData(selectedName, temperatureTextViewsecond, "spinnersecond_selection", selectedPosition)
                        updateTimerTextView()
                    } else {
                        stopTimer()
                        Toast.makeText(this@MainActivity, "Mobile data is off, timer stopped", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }, 0, 10000)
    }
    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }


    private fun fetchNames() {
        if (!isNetworkAvailable(this)) {
            Toast.makeText(this, "Your mobile internet is not working", Toast.LENGTH_SHORT).show()
            return
        }

        val call = apiservice.getTempData("user", "4100C5D677E4053B6850E0F444E9BBEE", "USER_GET_OBJECTS")
        Log.d("API Call", "URL: ${call.request().url()}")
        call.enqueue(object : Callback<List<TempData>> {
            override fun onResponse(call: Call<List<TempData>>, response: Response<List<TempData>>) {
                Log.d("API Response", "Code: ${response.code()}")
                if (response.isSuccessful) {
                    val tempDataList = response.body()
                    if (tempDataList != null && tempDataList.isNotEmpty()) {
                        val names = tempDataList.map { it.name }

                        // Setup spinner adapters
                        val adapter = CustomSpinnerAdapter(this@MainActivity, names)
                        spinner.adapter = adapter

                        val adapterSecond = CustomSpinnerAdapter(this@MainActivity, names)
                        spinnersecond.adapter = adapterSecond

                        // Retrieve saved spinner positions
                        val savedSpinnerPosition = sharedPreferences.getInt("spinner_selection", 0)
                        if (savedSpinnerPosition in 0 until names.size) {
                            spinner.setSelection(savedSpinnerPosition)
                        }

                        val savedSpinnerSecondPosition = sharedPreferences.getInt("spinnersecond_selection", 0)
                        if (savedSpinnerSecondPosition in 0 until names.size) {
                            spinnersecond.setSelection(savedSpinnerSecondPosition)
                        }

                        // Set spinner selection listeners
                        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                if (position >= 0 && position < names.size) {
                                    val selectedName = names[position]
                                    fetchTempData(selectedName, temperatureTextView, "spinner_selection", position)
                                }
                            }
                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }

                        spinnersecond.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                if (position >= 0 && position < names.size) {
                                    val selectedName = names[position]
                                    fetchTempData(selectedName, temperatureTextViewsecond, "spinnersecond_selection", position)
                                }
                            }
                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                    } else {
                        Log.e("API Error", "Empty response body")
                        Toast.makeText(this@MainActivity, "No data available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<List<TempData>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Your mobile internet is not working", Toast.LENGTH_SHORT).show()
                Log.e("API Error", "Failure: ${t.message}", t)
            }
        })
    }



    private fun fetchTempData(selectedName: String, temperatureTextView: TextView, prefKey: String, position: Int) {
        if (!isNetworkAvailable(this)) {
            Toast.makeText(this, "Your mobile internet is not working", Toast.LENGTH_SHORT).show()
            return
        }
        val call = apiservice.getTempData(
            "user",
            "4100C5D677E4053B6850E0F444E9BBEE",
            "USER_GET_OBJECTS"
        )


        call.enqueue(object : Callback<List<TempData>> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<List<TempData>>, response: Response<List<TempData>>) {
                if (response.isSuccessful) {
                    val tempDataList = response.body()
                    Log.d("TempDataList", tempDataList.toString())
                    val selectedTempData = tempDataList?.find { it.name == selectedName }
                    Log.d("selectedTempData", selectedTempData.toString())

                    if (selectedTempData!= null && tempDataList.isNotEmpty()) {
                        val temperature = selectedTempData.params.temp1
                        Log.d("params",selectedTempData.params.toString())
                        // assuming you have a temperature property in TempData
                        // Update the temperature value here, inside the onResponse callback
                        temperatureTextView.text = "$temperature°C"
                        temperatureTextView.visibility = View.VISIBLE

                        // Save the selected item in SharedPreferences
                        sharedPreferences.edit().putInt(prefKey, position).apply()
                    }
                }
            }

            override fun onFailure(call: Call<List<TempData>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "API call failed", Toast.LENGTH_SHORT).show()
                Log.e("API Error", "Failure: ${t.message}", t)
            }
        })
    }
    private fun fetchApiKey() = lifecycleScope.launch {
        try {
            val apiKeyResponse = apiservicekey.getApiKey()
            // Store the API key in SharedPreferences
            val sharedPreferences = getSharedPreferences("api_key_prefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("api_key", apiKeyResponse.toString())
            editor.apply()

            Log.d("API Key", apiKeyResponse.toString())
            Toast.makeText(this@MainActivity, "API key stored successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "API call failed2", Toast.LENGTH_SHORT).show()
            Log.e("API Error Check", "Failure: ${e.message}", e)
        }
    }

    private fun getApiKeyFromStorage(): String? {
        val sharedPreferences = getSharedPreferences("api_key_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("api_key", null)
    }
    private fun handleErrorResponse(response: Response<*>) {
        when (response.code()) {
            404 -> {
                Log.e("API Error", "Not Found")
                Toast.makeText(this@MainActivity, "API endpoint not found", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Log.e("API Error", "Response Code: ${response.code()} - ${response.message()}")
                response.errorBody()?.let {
                    Log.e("API Error", "Error body: ${it.string()}")
                }
                Toast.makeText(this@MainActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
            }
        }
    }




    private fun updateTimerTextView() {
        var seconds = 10 // Initial countdown value in seconds
        timerTextView.text = "Refresh : 0:${String.format("%02d", seconds)}" // Update TextView with initial value

        val countdownRunnable = object : Runnable {
            override fun run() {
                seconds-- // Decrement seconds
                if (seconds >= 0) {
                    val minutes = seconds / 60
                    val remainingSeconds = seconds % 60
                    timerTextView.text = "Refresh : ${String.format("%02d", minutes)}:${String.format("%02d", remainingSeconds)}" // Update TextView with current countdown value
                    handler.postDelayed(this, 1000) // Schedule the next update after 1 second
                } else {
                    timerTextView.text = "Refresh : 0:10" // Reset the timer if needed
                }
            }
        }

        handler.post(countdownRunnable) // Start the countdown
    }
    class CustomSpinnerAdapter(context: Context, private val items: List<String>) :
        ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items) {

        // Override getDropDownView to customize dropdown item appearance
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent)
            val textView = view.findViewById<TextView>(android.R.id.text1)
            textView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.black
                )
            ) // Set text color
            textView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            ) // Set background color
            textView.setPadding(20, 20, 20, 20) // Set padding if needed
            return view
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel() // Cancel the timer when the activity is destroyed
    }


}
@SuppressLint("ServiceCast")
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}