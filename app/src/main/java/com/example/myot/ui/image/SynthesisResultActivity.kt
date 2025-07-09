package com.example.myot.ui.image

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.data.repository.RecommendRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SynthesisResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_synthesis_result)

        val imageResult = findViewById<ImageView>(R.id.imageSynthesisResult)
        val buttonReplaceImage = findViewById<Button>(R.id.buttonReplaceImage)

        val resultImageUrl = intent.getStringExtra("resultImageUrl") ?: ""
        val recommendId = intent.getIntExtra("recommendId", -1)

        // 🔍 recommendId 디버깅용 로그
        Log.d("RecommendDebug", "SynthesisResultActivity에서 받은 recommendId = $recommendId")

        Glide.with(this).load(resultImageUrl).into(imageResult)

        if (recommendId != -1) {
            buttonReplaceImage.visibility = View.VISIBLE
            buttonReplaceImage.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    // 🔍 서버 요청 직전 recommendId 확인
                    Log.d("RecommendDebug", "서버로 전송할 recommendId = $recommendId")

                    val updated = RecommendRepository.updateImage(recommendId, resultImageUrl)
                    withContext(Dispatchers.Main) {
                        if (updated != null) {
                            Toast.makeText(
                                this@SynthesisResultActivity,
                                "대표 이미지가 변경되었습니다.",
                                Toast.LENGTH_SHORT
                            ).show()

                            // ✅ RecommendResultActivity로 결과 전달
                            val resultIntent = Intent().apply {
                                putExtra("imageReplaced", true)
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        } else {
                            Toast.makeText(this@SynthesisResultActivity, "변경 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } else {
            Log.e("RecommendDebug", "recommendId가 유효하지 않음 (recommendId = -1)")
            buttonReplaceImage.visibility = View.GONE
        }
    }
}
