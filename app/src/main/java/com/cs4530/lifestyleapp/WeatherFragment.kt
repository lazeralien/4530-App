package com.cs4530.lifestyleapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.squareup.picasso.Picasso

class WeatherFragment: Fragment() {
    private var temperatureTile : TextView? = null
    private var highTempTextView : TextView? = null
    private var lowTempTextView : TextView? = null
    private var humidityTextView : TextView? = null
    private var iconImageView : ImageView? = null

    // Get the view model
    private val mViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weather_view, container, false)
        temperatureTile = view.findViewById(R.id.weather_tile)
        highTempTextView = view.findViewById(R.id.high_temp_value)
        lowTempTextView = view.findViewById(R.id.low_temp_value)
        humidityTextView = view.findViewById(R.id.humidity_value)
        iconImageView = view.findViewById(R.id.weather_icon)

        mViewModel.dataWeather.observe(requireActivity(), liveDataObserver)

        return view
    }

    //create an observer that watches the LiveData<WeatherData> object
    private val liveDataObserver: Observer<WeatherTable> =
        Observer { weatherData -> // Update the UI if this data variable changes
            if (weatherData != null) {
                temperatureTile!!.text = "" + weatherData.temperature + "º F"
                highTempTextView!!.text = "" + weatherData.tempHigh + "º F"
                lowTempTextView!!.text = "" + weatherData.tempLow + "º F"
                humidityTextView!!.text = "" + weatherData.humidity + "%"
                Picasso.get()
                    .load(weatherData.icon)
                    .resize(200, 200)
                    .error(R.drawable.baseline_wb_sunny_18)
                    .into(iconImageView)
            }
        }
}