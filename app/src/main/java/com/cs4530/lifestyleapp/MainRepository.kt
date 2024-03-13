package com.cs4530.lifestyleapp

import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONException

class MainRepository private constructor(weatherDao: WeatherDao, userDao: UserDao) {
    // Weather fields
    val weatherData = MutableLiveData<WeatherTable>()
    private var mWeatherDao: WeatherDao = weatherDao
    private var mJsonWeatherData: String? = null
    private var temperature: Int? = null
    private var humidity: Int? = null
    private var tempHigh: Int? = null
    private var tempLow: Int? = null
    private var weatherIcon: String? = null

    val userData = MutableLiveData<UserTable>()
    private var mUserDao: UserDao = userDao

    private var mLocation: String? = null

    fun setWeather(location: String) {
        // First cache the location
        mLocation = location

        // Everything within the scope happens logically sequentially
        mScope.launch(Dispatchers.IO){
            //fetch data on a worker thread
            fetchWeatherData(location)

            // After the suspend function returns, Update the View THEN insert into db
            if(mJsonWeatherData!=null) {
                try {
                    val gson = GsonBuilder()
                        .registerTypeAdapter(WeatherTable::class.java, JSONWeatherUtils)
                        .create()

                    val data = gson.fromJson(mJsonWeatherData, WeatherTable::class.java)
                    temperature = data!!.temperature
                    humidity = data!!.humidity
                    tempLow = data!!.tempLow
                    tempHigh = data!!.tempHigh
                    weatherIcon = data!!.icon

                    weatherData.postValue(data)

                    insertWeather()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setUserData(newUser: UserTable) {
        mScope.launch {
            try {
                userData.postValue(newUser)

                insertUser(newUser)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    fun getUserData(userId: Long) : UserTable? {
        var user: UserTable? = null
        mScope.launch(Dispatchers.IO){
            try {
                user = getUser(userId)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return user
    }

    @WorkerThread
    suspend fun insertUser(newUser: UserTable): Long {
        return mUserDao.insert(newUser)
    }

    @WorkerThread
    suspend fun getUser(userId: Long) : UserTable? {
        return mUserDao.loadById(userId)
    }

    @WorkerThread
    suspend fun getUserData() : List<UserTable?> {
        return mUserDao.getAll()
    }

    @WorkerThread
    suspend fun insertWeather() {
        if (temperature != null && tempHigh !=null && tempLow !=null && humidity !=null) {
            mWeatherDao.insert(WeatherTable(temperature = temperature!!, tempHigh = tempHigh!!, tempLow = tempLow!!, humidity = humidity!!, icon = weatherIcon!!))
        }
    }

    @WorkerThread
    suspend fun fetchWeatherData(location: String) {
        val weatherDataURL = WeatherNetworkUtils.buildURLFromString(location)
        if(weatherDataURL!=null) {
            // This is actually a blocking call unless you're using an
            // asynchronous IO library (which we're not). However, it is a blocking
            // call on a background thread, not on the UI thread
            val jsonWeatherData = WeatherNetworkUtils.getDataFromURL(weatherDataURL)
            if (jsonWeatherData != null) {
                mJsonWeatherData = jsonWeatherData
            }
        }
    }

    // Make the repository singleton.
    companion object {
        private var mInstance: MainRepository? = null
        private lateinit var mScope: CoroutineScope
        @Synchronized
        fun getInstance(weatherDao: WeatherDao,
                        userDao: UserDao,
                        scope: CoroutineScope
        ): MainRepository {
            mScope = scope
            return mInstance?: synchronized(this){
                val instance = MainRepository(weatherDao, userDao)
                mInstance = instance
                instance
            }
        }
    }
}