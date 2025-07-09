package com.example.myot.ui.image

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.myot.R
import com.example.myot.model.ClothingItem
import com.example.myot.data.repository.ImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myot.util.DialogUtils


class SynthesisActivity : AppCompatActivity() {

    private lateinit var imageSynthesis: ImageView
    private lateinit var imageTop: ImageView
    private lateinit var imageBottom: ImageView
    private lateinit var textTop: TextView
    private lateinit var textBottom: TextView
    private lateinit var buttonGenerateImage: Button

    private var topItem: ClothingItem? = null
    private var bottomItem: ClothingItem? = null
    private var userImageUrl: String? = null
    private var recommendId: Int = -1

    private val SELECT_TOP = 1001
    private val SELECT_BOTTOM = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_synthesis)

        imageSynthesis = findViewById(R.id.imageSynthesis)
        imageTop = findViewById(R.id.imageTop)
        imageBottom = findViewById(R.id.imageBottom)
        textTop = findViewById(R.id.textTop)
        textBottom = findViewById(R.id.textBottom)
        buttonGenerateImage = findViewById(R.id.buttonGenerateImage)

        topItem = intent.getParcelableExtra("topItem")
        bottomItem = intent.getParcelableExtra("bottomItem")
        userImageUrl = intent.getStringExtra("userImageUrl")
        recommendId = intent.getIntExtra("recommendId", -1)

        Log.d("RecommendDebug", "SynthesisActivity: 전달받은 recommendId = $recommendId")

        val roundedOptions = RequestOptions().transform(RoundedCorners(24))

        topItem?.imageUri?.let {
            Glide.with(this).load(it).apply(roundedOptions).into(imageTop)
            textTop.visibility = View.GONE
        }

        bottomItem?.imageUri?.let {
            Glide.with(this).load(it).apply(roundedOptions).into(imageBottom)
            textBottom.visibility = View.GONE
        }

        loadUserImageIfNeeded()

        imageTop.setOnClickListener {
            val intent = Intent(this, com.example.myot.ui.recommend.ClosetSelectActivity::class.java)
            startActivityForResult(intent, SELECT_TOP)
        }

        imageBottom.setOnClickListener {
            val intent = Intent(this, com.example.myot.ui.recommend.ClosetSelectActivity::class.java)
            startActivityForResult(intent, SELECT_BOTTOM)
        }

        buttonGenerateImage.setOnClickListener {
            generateImage()
        }
    }

    private fun loadUserImageIfNeeded() {
        if (userImageUrl != null) {
            Glide.with(this).load(userImageUrl).into(imageSynthesis)
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val userImage = ImageRepository.fetchRecentImage()
                withContext(Dispatchers.Main) {
                    userImage?.let {
                        userImageUrl = it.image_url
                        Glide.with(this@SynthesisActivity).load(userImageUrl).into(imageSynthesis)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            val item = data.getParcelableExtra<ClothingItem>("selected_item") ?: return

            val roundedOptions = RequestOptions().transform(RoundedCorners(24))

            when (requestCode) {
                SELECT_TOP -> {
                    topItem = item
                    Glide.with(this).load(item.imageUri).apply(roundedOptions).into(imageTop)
                    textTop.visibility = View.GONE
                }
                SELECT_BOTTOM -> {
                    bottomItem = item
                    Glide.with(this).load(item.imageUri).apply(roundedOptions).into(imageBottom)
                    textBottom.visibility = View.GONE
                }
            }
        }
    }

    private fun generateImage() {
        val loadingDialog = DialogUtils.createLoadingDialog(this) // 로딩 다이얼로그 생성
        loadingDialog.show() // 로딩 시작

        CoroutineScope(Dispatchers.IO).launch {
            val resultUrl = ImageRepository.uploadAndSynthesizeImage(
                topItem?.id ?: return@launch,
                bottomItem?.id ?: return@launch,
                userImageUrl ?: return@launch
            )

            withContext(Dispatchers.Main) {
                loadingDialog.dismiss() // ✅ 로딩 종료

                if (resultUrl != null) {
                    Log.d("RecommendDebug", "SynthesisActivity: 이미지 생성 완료, recommendId = $recommendId")

                    val intent = Intent(this@SynthesisActivity, SynthesisResultActivity::class.java).apply {
                        putExtra("resultImageUrl", resultUrl)
                        putExtra("recommendId", recommendId)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@SynthesisActivity, "합성에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
