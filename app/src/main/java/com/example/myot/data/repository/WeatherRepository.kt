package com.example.myot.data.repository

import com.example.myot.data.remote.ApiClient
import com.example.myot.model.WeatherResponse
import retrofit2.Response

object WeatherRepository {
    suspend fun fetchWeather(): Response<WeatherResponse> {
        return ApiClient.apiService.getWeather()
    }
}
