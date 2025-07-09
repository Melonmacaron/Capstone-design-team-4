package com.example.myot.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://13.125.42.2:8000"

    // ✅ 타임아웃 설정 추가
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(600 , TimeUnit.SECONDS)  // 서버 연결 최대 대기 시간
        .readTimeout(600, TimeUnit.SECONDS)     // 서버 응답 대기 시간
        .writeTimeout(600, TimeUnit.SECONDS)    // 서버로 전송 최대 시간
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // 🔑 타임아웃 적용된 클라이언트 사용
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
