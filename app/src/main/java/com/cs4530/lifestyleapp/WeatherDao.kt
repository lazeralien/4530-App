package com.cs4530.lifestyleapp

import androidx.room.*

@Dao
interface WeatherDao {
    // Insert ignore
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(weatherTable: WeatherTable)

    // Delete all
    @Query("DELETE FROM weather_table")
    suspend fun deleteAll()

}