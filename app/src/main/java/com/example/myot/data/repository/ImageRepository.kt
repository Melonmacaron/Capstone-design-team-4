package com.example.myot.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myot.data.remote.ApiClient
import com.example.myot.model.GenerateImageRequest
import com.example.myot.data.remote.SynthesisImageResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

object ImageRepository {

    // ✅ 사용자 이미지 기반 합성 요청
    suspend fun uploadAndSynthesizeImage(
        topId: Long,
        bottomId: Long,
        backgroundUrl: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val filename = backgroundUrl.substringAfterLast("/")
            Log.d("ImageRepository", "Parsed filename from URL: $filename")

            val baseId = filename.removePrefix("user_").removeSuffix(".jpg")
            val userImageId = "user_$baseId" // ✅ DB에 저장된 형식에 맞춰줌

            if (userImageId.isEmpty()) {
                Log.e("ImageRepository", "userImageId 추출 실패! URL: $backgroundUrl")
                return@withContext null
            }

            Log.d("ImageRepository", "userImageId 최종 요청값: $userImageId")

            val request = GenerateImageRequest(
                user_image_id = userImageId,
                top_id = topId.toInt(),
                bottom_id = bottomId.toInt()
            )

            Log.d("ImageRepository", "합성 요청: $request")

            val response = ApiClient.apiService.generateImage(request)

            if (response.generated_image_url != null) {
                Log.d("ImageRepository", "합성 결과 URL: ${response.generated_image_url}")
            } else {
                Log.e("ImageRepository", "합성 실패, 결과 URL 없음")
            }

            response.generated_image_url
        } catch (e: Exception) {
            Log.e("ImageRepository", "합성 요청 중 예외 발생", e)
            null
        }
    }


    // ✅ 사용자 이미지 업로드 (Uri → File)
    suspend fun uploadUserImage(context: Context, imageUri: Uri): SynthesisImageResponse? = withContext(Dispatchers.IO) {
        try {
            val file = uriToFile(context, imageUri) ?: return@withContext null
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

            ApiClient.apiService.uploadUserImage(imagePart)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ✅ Uri → 실제 파일 경로
    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ✅ 최근 사용자 이미지 가져오기
    suspend fun fetchRecentImage(): SynthesisImageResponse? = withContext(Dispatchers.IO) {
        return@withContext try {
            ApiClient.apiService.getRecentImage()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
