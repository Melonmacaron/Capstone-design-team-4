package com.example.myot.data.repository

import com.example.myot.model.RecommendItem
import com.example.myot.model.RecommendEditRequest
import com.example.myot.data.remote.ApiClient
import com.example.myot.model.RecommendCreateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RecommendRepository {

    // ✅ 이미지 대체
    suspend fun updateImage(id: Int, newImageUrl: String): RecommendItem? {
        return try {
            val response = ApiClient.apiService.updateRecommendImage(id, mapOf("imageUrl" to newImageUrl))
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    // ✅ 추천 생성
    suspend fun createRecommendation(tpo: String, selectedItems: List<Long>): RecommendItem? {
        return try {
            val request = RecommendCreateRequest(
                tpo = tpo,
                selected_item_id = selectedItems.first().toInt()
            )
            withContext(Dispatchers.IO) {
                ApiClient.apiService.createRecommendation(request)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ✅ 추천 히스토리 조회
    suspend fun getRecommendHistory(): List<RecommendItem> {
        return try {
            withContext(Dispatchers.IO) {
                ApiClient.apiService.getRecommendHistory()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ✅ 추천 설명 수정
    suspend fun updateDescription(id: Int, description: String): RecommendItem? {
        return try {
            val body = RecommendEditRequest(description)
            val response = ApiClient.apiService.editRecommendDescription(id, body)
            if (response.isSuccessful) {
                response.body()
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ✅ 추천 ID로 상세 정보 다시 가져오기
    suspend fun getRecommendById(id: Int): RecommendItem? {
        return try {
            withContext(Dispatchers.IO) {
                ApiClient.apiService.getRecommendById(id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
