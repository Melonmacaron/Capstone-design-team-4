package com.example.myot.model

data class WeatherResponse(
    val weather: String,
    val temperature: Double,
    val humidity: Int,
    val icon: String
)
