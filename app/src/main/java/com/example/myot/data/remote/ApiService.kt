package com.example.myot.data.remote

import com.example.myot.data.model.ClothesUpdateRequest
import com.example.myot.model.ClothingItem
import com.example.myot.model.RecommendItem
import com.example.myot.data.remote.SynthesisResponse
import com.example.myot.model.GenerateImageRequest
import com.example.myot.model.GenerateImageResponse
import com.example.myot.model.PingResponse
import com.example.myot.model.RecommendCreateRequest
import com.example.myot.model.RecommendEditRequest
import com.example.myot.model.WeatherResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import retrofit2.Response


interface ApiService {
    @GET("/ping")
    suspend fun ping(): retrofit2.Response<PingResponse>

    @GET("/closet/frequent")
    suspend fun getFrequentClothes(): List<ClothingItem>

    // 1. 옷장 전체 가져오기
    @GET("/closet")
    suspend fun getAllClothes(): List<ClothingItem>

    // ✅ 분석 전용 API (이미지만 전달)
    @Multipart
    @POST("/closet/upload")
    suspend fun analyzeClothing(
        @Part image: MultipartBody.Part
    ): ClothingItem

    // 3. 옷 수정
    @POST("/closet/edit/{id}")
    suspend fun editClothing(
        @Path("id") id: Long,
        @Body clothingItem: ClothesUpdateRequest
    ): ClothingItem

    //옷 삭제
    @DELETE("/closet/{id}")
    suspend fun deleteClothing(@Path("id") id: Int): Response<Unit>

    // 4. 추천 생성
    @POST("/recommend")
    suspend fun createRecommendation(
        @Body request: RecommendCreateRequest
    ): RecommendItem

    // 5. 추천 내역 조회
    @GET("/recommend/history")
    suspend fun getRecommendHistory(): List<RecommendItem>

    // 6. 추천 설명 수정
    @POST("/recommend/edit/{recommendId}")
    suspend fun editRecommendDescription(
        @Path("recommendId") id: Int,
        @Body body: RecommendEditRequest
    ): Response<RecommendItem>

    //현재 합성에 사용될 이미지 불러오기
    // ApiService.kt
    @GET("/user/recentimage")
    suspend fun getRecentImage(): SynthesisImageResponse

    //기본 이미지 수정
    @Multipart
    @POST("/user/image")
    suspend fun uploadUserImage(
        @Part image: MultipartBody.Part
    ): SynthesisImageResponse

    // 7. 이미지 합성 요청
    @POST("/generate-image")
    suspend fun generateImage(@Body request: GenerateImageRequest): GenerateImageResponse

    @POST("/recommend/update-image/{recommend_id}")
    suspend fun updateRecommendImage(
        @Path("recommend_id") id: Int,
        @Body imageUrl: Map<String, String>
    ): Response<RecommendItem>

    //이미지만 불러오는
    @GET("/recommend/{id}")
    suspend fun getRecommendById(@Path("id") id: Int): RecommendItem

    //날씨
    @GET("/weather")
    suspend fun getWeather(): Response<WeatherResponse>
}
