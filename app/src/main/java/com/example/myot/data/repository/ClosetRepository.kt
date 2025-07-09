package com.example.myot.data.repository

import android.util.Log
import com.example.myot.data.model.ClothesUpdateRequest
import com.example.myot.data.remote.ApiClient
import com.example.myot.model.ClothingItem
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object ClosetRepository {

    private var cachedClothes: List<ClothingItem> = emptyList()

    // 전체 옷 가져오기
    suspend fun getClothes(): List<ClothingItem> {
        return try {
            val result = ApiClient.apiService.getAllClothes()
            Log.d("ClosetRepository", "받은 옷 수: ${result.size}")
            result.forEach { item ->
                Log.d("ClosetRepository", "옷 ID=${item.id}, 대분류=${item.mainCategory}, 카테고리=${item.category}")
            }
            cachedClothes = result
            result
        } catch (e: Exception) {
            Log.e("ClosetRepository", "옷 가져오기 실패", e)
            cachedClothes
        }
    }



    // ✅ 단일 옷 조회 (서버에서 직접 요청)
    suspend fun getClothingById(id: Long): ClothingItem? {
        return try {
            val result = ApiClient.apiService.getAllClothes()
            result.find { it.id == id }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 서버에 옷 이미지 분석 요청
    suspend fun analyzeClothing(file: File): ClothingItem? {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

        return try {
            ApiClient.apiService.analyzeClothing(imagePart)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


// 옷 정보 수정 요청
    suspend fun editClothing(item: ClothesUpdateRequest): ClothingItem? {
        return try {
            ApiClient.apiService.editClothing(item.id, item)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    // 최근 자주 입은 옷 가져오기
    suspend fun getFrequentClothes(): List<ClothingItem> {
        return try {
            ApiClient.apiService.getFrequentClothes()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun deleteClothing(id: Int): Boolean {
        return try {
            val response = ApiClient.apiService.deleteClothing(id)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

}
