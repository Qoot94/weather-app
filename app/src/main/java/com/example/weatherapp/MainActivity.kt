package com.example.weatherapp

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.loader.content.AsyncTaskLoader
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.Charsets.UTF_8

class MainActivity : AppCompatActivity() {
//    lateinit var binding: ActivityMainBinding
    lateinit var updateButton: FloatingActionButton
    var city = "Washington,US"
    val api = "631f9b2051a9e88d1454e16219622841"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
        updateButton = findViewById(R.id.btUpdate)
        val input = findViewById<EditText>(R.id.etInput)
        supportActionBar?.hide()
        requestAPI("dhaka,bd")

        updateButton.setOnClickListener {
            requestAPI(input.text.toString())
        }
    }


    fun fetchData(city: String = "Washington,US"): String {
        var response = ""
        try {
            response =
                URL("https://api.openweathermap.org/data/2.5/weather?q=$city%20&units=metric&appid=06c921750b9a82d8f5d1294e1586276f").readText()
        } catch (e: Exception) {
            Log.d("MAIN", "ISSUE: $e")
        }
        // our response is saved as a string and returned
        return response
    }

    fun requestAPI(city: String = "Washington,US") {
        // we use Coroutines to fetch the data, then update the Recycler View if the data is valid
        CoroutineScope(Dispatchers.IO).launch {
            // we fetch the data

            val data = async { fetchData(city) }.await()
            // once the data comes back, we populate our Recycler View
            if (data.isNotEmpty()) {
                populateWeather(data)
            } else {
                Log.d("MAIN", "Unable to get data")
            }
        }
    }

    suspend fun populateWeather(result: String) {
        withContext(Dispatchers.Main) {
            val jsonObj = JSONObject(result)
            val main = jsonObj.getJSONObject("main")
            val system = jsonObj.getJSONObject("sys")
            val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
            val wind = jsonObj.getJSONObject("wind")
            val pressure = main.getString("pressure")
            val humidity = main.getString("humidity")
            val temp = "${main.getString("temp")} °c"
            val lowTemp = "Low: ${main.getString("temp_min")} °c"
            val highTemp = "High: ${main.getString("temp_max")} °c"

            val dateTime = jsonObj.getLong("dt")
            val updatedAtStr = "Updated at ${
                SimpleDateFormat("dd/mm/yyyy hh:mm a", Locale.ENGLISH).format(
                    Date(dateTime * 1000)
                )
            }"

            val sunrise = system.getLong("sunrise")
            val sunset = system.getLong("sunset")
            val windSpeed = wind.getString("speed")
            val weatherStr = weather.getString("description")
            val location = "${jsonObj.getString("name")}, ${system.getString("country")}"

            findViewById<TextView>(R.id.tvCityName).text = location
            findViewById<TextView>(R.id.tvDateTime).text = updatedAtStr
            findViewById<TextView>(R.id.tvStatus).text = weatherStr.capitalize()
            findViewById<TextView>(R.id.tvTemp).text = temp
            findViewById<TextView>(R.id.tvLow).text = lowTemp
            findViewById<TextView>(R.id.tvHigh).text = highTemp
            findViewById<TextView>(R.id.tvSunrise).text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(
                    Date(sunrise * 1000)
                )
            findViewById<TextView>(R.id.tvSunset).text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(
                    Date(sunset * 1000)
                )
            findViewById<TextView>(R.id.tvPressure).text = pressure
            findViewById<TextView>(R.id.tvWind).text = windSpeed
            findViewById<TextView>(R.id.tvHumidity).text = humidity
        }
    }
}
