package com.example.myot.ui.closet

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.data.model.ClothesUpdateRequest
import com.example.myot.data.repository.ClosetRepository
import com.example.myot.model.ClothingItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditClothingActivity : AppCompatActivity() {

    private lateinit var imageClothing: ImageView
    private lateinit var textMainCategory: TextView
    private lateinit var textSubCategory: TextView
    private lateinit var textSubLength: TextView
    private lateinit var textColor: TextView
    private lateinit var textFit: TextView
    private lateinit var textMaterial: TextView
    private lateinit var labelSleeve: TextView
    private lateinit var textSleeve: TextView
    private lateinit var labelCollar: TextView
    private lateinit var textCollar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_clothing)

        imageClothing = findViewById(R.id.imageClothing)
        textMainCategory = findViewById(R.id.textMainCategory)
        textSubCategory = findViewById(R.id.textSubCategory)
        textSubLength = findViewById(R.id.textSubLength)
        textColor = findViewById(R.id.textColor)
        textFit = findViewById(R.id.textFit)
        textMaterial = findViewById(R.id.textMaterial)
        labelSleeve = findViewById(R.id.labelSleeve)
        textSleeve = findViewById(R.id.textSleeve)
        labelCollar = findViewById(R.id.labelCollar)
        textCollar = findViewById(R.id.textCollar)

        val item = intent.getParcelableExtra<ClothingItem>("clothingItem")
        Log.d("EditClothing", "✅ 받은 아이템: id=${item?.id}, uri=${item?.imageUri}")

        if (item == null) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Glide.with(this)
            .load(item.imageUri?.replace("localhost", "10.0.2.2"))
            .placeholder(R.drawable.default_clothes)
            .skipMemoryCache(true) // ✅ 캐시 회피
            .into(imageClothing)

        textMainCategory.text = item.mainCategory
        textSubCategory.text = item.category
        textSubLength.text = item.length
        textColor.text = item.color
        textFit.text = item.fit
        textMaterial.text = item.material
        textSleeve.text = item.sleeveLength ?: ""
        textCollar.text = item.collar ?: ""

        if (item.mainCategory == "상의" || item.mainCategory == "아우터") {
            labelSleeve.visibility = TextView.VISIBLE
            textSleeve.visibility = TextView.VISIBLE

        } else {
            labelSleeve.visibility = TextView.GONE
            textSleeve.visibility = TextView.GONE

        }
        labelCollar.visibility = TextView.GONE
        textCollar.visibility = TextView.GONE
        findViewById<TextView>(R.id.buttonSave).setOnClickListener {
            val updatedRequest = ClothesUpdateRequest(
                id = item.id,
                imageResId = item.imageResId,
                imageUri = item.imageUri,
                maincategory = textMainCategory.text.toString(),
                category = textSubCategory.text.toString(),
                length = textSubLength.text.toString(),
                color = textColor.text.toString(),
                fit = textFit.text.toString(),
                material = textMaterial.text.toString(),
                sleeveLength = textSleeve.text.toString().takeIf { textSleeve.visibility == TextView.VISIBLE },
                collar = textCollar.text.toString().takeIf { textCollar.visibility == TextView.VISIBLE }
            )
            Log.d("EditClothing", "🚨 서버로 보낼 updatedItem: id=${updatedRequest.id}, uri=${updatedRequest.imageUri}")
            CoroutineScope(Dispatchers.IO).launch {
                val result = ClosetRepository.editClothing(updatedRequest)
                runOnUiThread {
                    if (result != null) {
                        Toast.makeText(this@EditClothingActivity, "옷 정보가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@EditClothingActivity, "수정 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 필드 선택 리스너
        textMainCategory.setOnClickListener {
            val mainOptions = categoryMap.keys.toTypedArray()
            showOptionDialog("대분류 선택", mainOptions) { selected ->
                textMainCategory.text = selected
                textSubCategory.text = "소분류 선택"
                textSubLength.text = "기장 선택"
                if (selected == "상의" || selected == "아우터") {
                    labelSleeve.visibility = TextView.VISIBLE
                    textSleeve.visibility = TextView.VISIBLE

                } else {
                    labelSleeve.visibility = TextView.GONE
                    textSleeve.visibility = TextView.GONE

                }
            }
        }
        labelCollar.visibility = TextView.GONE
        textCollar.visibility = TextView.GONE
        textSubCategory.setOnClickListener {
            val selectedMain = textMainCategory.text.toString()
            val subOptions = categoryMap[selectedMain]
            if (subOptions == null) {
                Toast.makeText(this, "먼저 대분류를 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showOptionDialog("소분류 선택", subOptions.toTypedArray()) { selected ->
                textSubCategory.text = selected
            }
        }

        textSubLength.setOnClickListener {
            val selectedMain = textMainCategory.text.toString()
            val subOptions = lengthMap[selectedMain]
            if (subOptions == null) {
                Toast.makeText(this, "먼저 대분류를 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showOptionDialog("기장 선택", subOptions) { selected ->
                textSubLength.text = selected
            }
        }

        textColor.setOnClickListener {
            val options = arrayOf("그레이", "퍼플", "화이트", "블랙", "블루", "핑크", "베이지", "브라운", "네온", "라벤더", "실버", "그린", "와인", "민트", "레드", "옐로우", "네이비", "오렌지", "골드", "스카이블루", "카키")
            showOptionDialog("색상", options) { textColor.text = it }
        }

        textFit.setOnClickListener {
            val options = arrayOf("오버사이즈", "와이드", "스키니", "타이트", "루즈", "노멀", "벨보텀")
            showOptionDialog("핏", options) { textFit.text = it }
        }

        textMaterial.setOnClickListener {
            val options = arrayOf("가죽", "비닐/PVC", "니트", "시퀸/글리터", "실크", "시폰", "네오프렌", "레이스", "퍼", "린넨", "우븐", "트위드", "스판덱스", "자카드", "무스탕", "플리스", "스웨이드", "울/캐시미어", "헤어 니트", "패딩", "데님", "저지", "벨벳", "메시", "코듀로이")
            showOptionDialog("소재", options) { textMaterial.text = it }
        }

        textSleeve.setOnClickListener {
            val options = arrayOf("없음", "민소매", "캡", "긴팔", "7부소매", "반팔")
            showOptionDialog("소매기장", options) { textSleeve.text = it }
        }

        textCollar.setOnClickListener {
            val options = arrayOf("차이나칼라", "밴드칼라", "테일러드칼라", "숄칼라", "셔츠칼라", "피터팬칼라", "보우칼라", "폴로칼라", "세일러칼라")
            showOptionDialog("옷깃", options) { textCollar.text = it }
        }
    }

    private fun showOptionDialog(title: String, options: Array<String>, onSelect: (String) -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(options) { _, which -> onSelect(options[which]) }
            .show()
    }

    private val categoryMap = mapOf(
        "상의" to listOf("후드티", "브라탑", "블라우스", "니트웨어", "탑", "티셔츠", "셔츠"),
        "하의" to listOf("조거팬츠", "스커트", "팬츠", "래깅스", "청바지"),
        "아우터" to listOf("짚업", "재킷", "코트", "가디건", "패딩", "베스트", "점퍼"),
        "원피스" to listOf("드레스", "점프수트")
    )

    private val lengthMap = mapOf(
        "상의" to arrayOf("크롭", "노멀", "롱"),
        "하의" to arrayOf("미니", "니렝스", "미디", "발목", "맥시"),
        "아우터" to arrayOf("크롭", "노멀", "하프", "롱", "맥시"),
        "원피스" to arrayOf("미니", "니렝스", "미디", "발목", "맥시")
    )
}
