package com.cs4530.lifestyleapp

import com.google.gson.annotations.SerializedName

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "weather_table")
data class WeatherTable(
    @field:ColumnInfo(name = "id")
    @field:PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @field:ColumnInfo(name = "current_temperature")
    @SerializedName("temp")
    var temperature: Int? = null,

    @field:ColumnInfo(name = "max_temperature")
    @SerializedName("temp_max")
    var tempHigh: Int? = null,

    @field:ColumnInfo(name = "min_temperature")
    @SerializedName("temp_min")
    var tempLow: Int? = null,

    @field:ColumnInfo(name = "humidity")
    var humidity: Int? = null,

    @field:ColumnInfo(name = "icon")
    var icon: String? = null,
)