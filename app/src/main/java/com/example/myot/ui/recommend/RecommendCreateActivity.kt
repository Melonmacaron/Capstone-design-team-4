package com.example.myot.ui.recommend

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.myot.R
import com.example.myot.data.repository.RecommendRepository
import com.example.myot.model.ClothingItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecommendCreateActivity : AppCompatActivity() {

    private lateinit var imageSelectClothing: ImageButton
    private lateinit var textSelectClothing: TextView
    private lateinit var buttonShowRecommendation: Button
    private lateinit var radioGroupLine1: RadioGroup
    private lateinit var radioGroupLine2: RadioGroup

    private var selectedClothingItem: ClothingItem? = null

    private val selectClothingLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedItem = result.data?.getParcelableExtra<ClothingItem>("selected_item")

            if (selectedItem != null) {
                selectedClothingItem = selectedItem

                if (selectedItem.imageUri != null) {
                    Glide.with(this)
                        .load(Uri.parse(selectedItem.imageUri))
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(32)))
                        .into(imageSelectClothing)
                } else {
                    Glide.with(this)
                        .load(selectedItem.imageResId)
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(32)))
                        .into(imageSelectClothing)
                }

                textSelectClothing.text = ""
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommend_create)

        imageSelectClothing = findViewById(R.id.buttonSelectClothing)
        textSelectClothing = findViewById(R.id.textSelectClothing)
        buttonShowRecommendation = findViewById(R.id.buttonShowRecommendation)
        radioGroupLine1 = findViewById(R.id.radioGroupTpoLine1)
        radioGroupLine2 = findViewById(R.id.radioGroupTpoLine2)

        val clearOther = { selectedGroup: RadioGroup ->
            if (selectedGroup == radioGroupLine1) radioGroupLine2.clearCheck()
            else radioGroupLine1.clearCheck()
        }

        radioGroupLine1.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) clearOther(radioGroupLine1)
        }

        radioGroupLine2.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) clearOther(radioGroupLine2)
        }

        imageSelectClothing.setOnClickListener {
            val intent = Intent(this, ClosetSelectActivity::class.java)
            selectClothingLauncher.launch(intent)
        }

        buttonShowRecommendation.setOnClickListener {
            val clothing = selectedClothingItem
            val tpo = findSelectedTpo()

            if (clothing == null) {
                Toast.makeText(this, "옷을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (tpo == null) {
                Toast.makeText(this, "TPO를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔽 서버 요청
            CoroutineScope(Dispatchers.IO).launch {
                val result = RecommendRepository.createRecommendation(tpo, listOf(clothing.id))
                withContext(Dispatchers.Main) {
                    if (result != null) {
                        val intent = Intent(this@RecommendCreateActivity, RecommendResultActivity::class.java)
                        intent.putExtra("recommend_item", result)
                        startActivity(intent)
                        finish()  // ✅ RecommendCreateActivity 종료
                    } else {
                        Toast.makeText(this@RecommendCreateActivity, "추천 요청 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun findSelectedTpo(): String? {
        val checkedId1 = radioGroupLine1.checkedRadioButtonId
        val checkedId2 = radioGroupLine2.checkedRadioButtonId

        val selectedId = if (checkedId1 != -1) checkedId1 else checkedId2
        return if (selectedId != -1) {
            findViewById<RadioButton>(selectedId)?.text?.toString()
        } else null
    }
}
