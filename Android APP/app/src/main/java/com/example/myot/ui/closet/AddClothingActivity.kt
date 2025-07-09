package com.example.myot.ui.closet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.myot.R
import com.example.myot.data.repository.ClosetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AddClothingActivity : AppCompatActivity() {

    private lateinit var photoUri: Uri

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && ::photoUri.isInitialized) {
            analyzeAndForward(photoUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_clothing)

        val isCamera = intent.getBooleanExtra("startCamera", false)
        val galleryUri = intent.getParcelableExtra<Uri>("galleryImageUri")

        if (isCamera) startCamera()
        else if (galleryUri != null) analyzeAndForward(galleryUri)
        else {
            Toast.makeText(this, "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
            return
        }
        val file = File.createTempFile("IMG_${System.currentTimeMillis()}", ".jpg",
            getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        photoUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        takePictureLauncher.launch(photoUri)
    }

    private fun analyzeAndForward(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        if (inputStream == null) {
            Toast.makeText(this, "이미지를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ 파일 이름을 고유하게 설정하고 로그 추가
        val fileName = "upload_${System.currentTimeMillis()}.jpg"
        val tempFile = File(cacheDir, fileName)

        Log.d("AddClothing", "▶ 선택한 이미지 URI: $uri")
        Log.d("AddClothing", "▶ 복사된 임시 파일 경로: ${tempFile.absolutePath}")

        tempFile.outputStream().use { output -> inputStream.copyTo(output) }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val analyzedItem = ClosetRepository.analyzeClothing(tempFile)

                withContext(Dispatchers.Main) {
                    if (analyzedItem != null) {
                        Log.d("AddClothing", "✅ 분석 결과 item: id=${analyzedItem.id}, uri=${analyzedItem.imageUri}")

                        val intent = Intent(this@AddClothingActivity, EditClothingActivity::class.java).apply {
                            putExtra("clothingItem", analyzedItem)
                            putExtra("localImageUri", uri.toString()) // 필요한 경우를 위해 같이 전달
                        }
                        setResult(RESULT_OK)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@AddClothingActivity, "서버 분석 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddClothingActivity, "AI 분석 중 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
