package com.example.myot.ui.recommend

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.myot.R
import com.example.myot.model.RecommendItem
import com.example.myot.data.repository.ClosetRepository
import com.example.myot.data.repository.ImageRepository
import com.example.myot.data.repository.RecommendRepository
import com.example.myot.model.ClothingItem
import com.example.myot.ui.image.SynthesisActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecommendResultActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RECOMMEND_ITEM = "recommend_item"
        const val REQUEST_SYNTHESIZE = 1001
    }

    private lateinit var imageClothes: ImageView
    private lateinit var textDescription: TextView
    private lateinit var editDescription: EditText
    private lateinit var buttonEditDescription: ImageButton
    private lateinit var buttonSave: Button
    private lateinit var resultContainer: LinearLayout
    private lateinit var buttonSynthesize: Button

    private var currentItem: RecommendItem? = null
    private var topItem: ClothingItem? = null
    private var bottomItem: ClothingItem? = null
    private var userImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommend_result)

        imageClothes = findViewById(R.id.imageClothes)
        textDescription = findViewById(R.id.textRecommendDescription)
        editDescription = findViewById(R.id.editRecommendDescription)
        buttonEditDescription = findViewById(R.id.buttonEditDescription)
        buttonSave = findViewById(R.id.buttonSave)
        resultContainer = findViewById(R.id.resultContainer)
        buttonSynthesize = findViewById(R.id.buttonSynthesize)

        currentItem = intent.getSerializableExtra(EXTRA_RECOMMEND_ITEM) as? RecommendItem
        if (currentItem == null) {
            Toast.makeText(this, "추천 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        updateUI()

        buttonEditDescription.setOnClickListener {
            textDescription.visibility = View.GONE
            editDescription.visibility = View.VISIBLE
            buttonSave.visibility = View.VISIBLE
            editDescription.requestFocus()
        }

        buttonSave.setOnClickListener {
            val updatedDesc = editDescription.text.toString()
            currentItem?.let { item ->
                CoroutineScope(Dispatchers.IO).launch {
                    val result = RecommendRepository.updateDescription(item.id.toInt(), updatedDesc)
                    withContext(Dispatchers.Main) {
                        if (result != null) {
                            item.description = result.description
                            updateUI()
                            Toast.makeText(this@RecommendResultActivity, "설명이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@RecommendResultActivity, "저장 실패", Toast.LENGTH_SHORT).show()
                        }
                        textDescription.visibility = View.VISIBLE
                        editDescription.visibility = View.GONE
                        buttonSave.visibility = View.GONE
                    }
                }
            }
        }

        buttonSynthesize.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val clothes = ClosetRepository.getClothes()
                val matched = clothes.filter { currentItem?.clothingIds?.contains(it.id) == true }

                topItem = matched.firstOrNull { it.mainCategory == "상의" }
                bottomItem = matched.firstOrNull { it.mainCategory == "하의" }
                userImageUrl = ImageRepository.fetchRecentImage()?.image_url

                val recommendIntId = currentItem?.id?.toInt() ?: -1
                Log.d("RecommendDebug", "전달할 recommendId: $recommendIntId")

                withContext(Dispatchers.Main) {
                    val intent = Intent(this@RecommendResultActivity, SynthesisActivity::class.java).apply {
                        putExtra("userImageUrl", userImageUrl)
                        putExtra("topItem", topItem)
                        putExtra("bottomItem", bottomItem)
                        putExtra("recommendId", recommendIntId)
                    }
                    startActivityForResult(intent, REQUEST_SYNTHESIZE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("RecommendResult", "onActivityResult called: requestCode=$requestCode, resultCode=$resultCode")

        if (requestCode == REQUEST_SYNTHESIZE && resultCode == Activity.RESULT_OK) {
            val replaced = data?.getBooleanExtra("imageReplaced", false) ?: false
            Log.d("RecommendResult", "imageReplaced=$replaced")

            if (replaced && currentItem != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val updated = RecommendRepository.getRecommendById(currentItem!!.id.toInt())
                    Log.d("RecommendResult", "updated imageUrl=${updated?.imageUrl}")

                    withContext(Dispatchers.Main) {
                        if (updated != null) {
                            currentItem = updated
                            updateUI()
                        }
                    }
                }
            }
        }
    }

    private fun updateUI() {
        currentItem?.let { item ->
            Glide.with(this)
                .load(item.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageClothes)

            textDescription.text = item.description
            editDescription.setText(item.description)
            showRecommendedItems()
        }
    }

    private fun showRecommendedItems() {
        resultContainer.removeAllViews()
        CoroutineScope(Dispatchers.IO).launch {
            val clothes = ClosetRepository.getClothes()
            val matched = clothes.filter { currentItem?.clothingIds?.contains(it.id) == true }
            withContext(Dispatchers.Main) {
                matched.forEach { item ->
                    val view = layoutInflater.inflate(R.layout.item_recommend_result, resultContainer, false)
                    val image = view.findViewById<ImageView>(R.id.imageClothing)
                    val text = view.findViewById<TextView>(R.id.textInfo)

                    if (item.imageUri != null) {
                        Glide.with(this@RecommendResultActivity).load(item.imageUri).into(image)
                    } else {
                        image.setImageResource(item.imageResId)
                    }

                    text.text = buildString {
                        append("대분류: ${item.mainCategory}\n")
                        append("카테고리: ${item.category}\n")
                        append("기장: ${item.length}\n")
                        append("색상: ${item.color}\n")
                        append("핏: ${item.fit}\n")
                        append("소재: ${item.material}\n")
                        if (item.mainCategory == "상의" || item.mainCategory == "아우터") {
                            append("소매기장: ${item.sleeveLength ?: "-"}\n")
                        }
                    }

                    resultContainer.addView(view)
                }
            }
        }
    }
}
