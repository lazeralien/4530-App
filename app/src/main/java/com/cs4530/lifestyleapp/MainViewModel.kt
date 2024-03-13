package com.cs4530.lifestyleapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainViewModel(repository: MainRepository) :  ViewModel(){
    // Connect a live data object to the current bit of weather info
    private val weatherData: LiveData<WeatherTable> = repository.weatherData
    private val userData: LiveData<UserTable> = repository.userData

    private var mRepository: MainRepository = repository

    fun setWeather(location: String) {
        // Simply pass the location to the repository
        mRepository.setWeather(location)
    }

    // Returns the data contained in the live data object
    val dataWeather: LiveData<WeatherTable>
        get() = weatherData

    // Returns the data contained in the live data object
    val dataUser: LiveData<UserTable>
        get() = userData

    fun setUserData(user: UserTable) {
        mRepository.setUserData(user)
    }


    suspend fun getUserData() : List<UserTable?> {
        return mRepository.getUserData()
    }
}

// This factory class allows us to define custom constructors for the view model
class MainViewModelFactory(private val repository: MainRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}