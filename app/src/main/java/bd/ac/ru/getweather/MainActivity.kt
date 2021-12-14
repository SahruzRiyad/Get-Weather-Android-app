package bd.ac.ru.getweather

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.squareup.picasso.Picasso
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.net.URL

class MainActivity : AppCompatActivity() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var City:String
    lateinit var Country:String
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val weaButton: Button = findViewById(R.id.getWea_button)
        weaButton.setOnClickListener {
            get_location()
        }
    }

    private fun get_location() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
        var task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener {

            findViewById<TextView>(R.id.lati).text = it.latitude.toString()
            findViewById<TextView>(R.id.longi).text = it.longitude.toString()

            var lat: Double = it.latitude
            var lon:Double = it.longitude
            var city_country = getAddress(lat,lon)
            findViewById<TextView>(R.id.city_country).text = city_country
            get_weather()

        }.addOnFailureListener{
            Toast.makeText(this,"Failure to get the current location",
                Toast.LENGTH_SHORT).show()
        }

    }

    private fun getAddress(lat: Double, lng: Double): String {
        // Function to get location city name using geocoder
        var geocoder = Geocoder(this) // initializing geocoder for the context
        var list = geocoder.getFromLocation(lat, lng, 1)
        City = list[0].locality
        Country = list[0].countryName
        var Location = City +", "+ Country // getting location using lat long
        return Location  // as it returns a list object we fetch only the local name
    }

    private val client = OkHttpClient()
    private fun get_weather() {

        var appKey = "c1eac5a9939755cf8aa71f0acb891cdd"
        val url: String = "https://api.openweathermap.org/data/2.5/weather?q=${City}&units=metric&appid=${appKey}"

        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }
            override fun onResponse(call: Call, response: Response) {

                var jsonString = response.body?.string()
                val reader = JSONObject(jsonString)

                val main = reader.getJSONObject("main")
                val weather = reader.getJSONArray("weather")
                val wind = reader.getJSONObject("wind")
                val weather_icon = weather.getJSONObject(0).getString("icon")


                findViewById<TextView>(R.id.wea_des).text = weather.getJSONObject(0)
                    .getString("description")
                findViewById<TextView>(R.id.temperature).text = main.getString("temp")+"°C"
                findViewById<TextView>(R.id.feel_temp).text = main.getString("feels_like")+"°C"
                findViewById<TextView>(R.id.wisSpeed).text = wind.getString("speed")+" m/s"
                findViewById<TextView>(R.id.hum).text =  main.getString("humidity")+"%"

                runOnUiThread{
                    val weatherImage : ImageView = findViewById<ImageView>(R.id.wea_icon)
                    Picasso.get().load("https://openweathermap.org/img/wn/${weather_icon}@2x.png").into(weatherImage)
                    weatherImage.visibility = View.VISIBLE
                }
            }
        })
    }

}