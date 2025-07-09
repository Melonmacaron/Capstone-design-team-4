package com.example.myot.data.model
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClothesUpdateRequest(
    val id: Long,
    val imageResId: Int = -1,
    val imageUri: String? = null,
    val maincategory: String,
    val category: String,
    val length: String?,               // ✅ 수정
    val color: String,
    val fit: String,
    val material: String,
    val sleeveLength: String? = null,
    val collar: String? = null
) : Parcelable
