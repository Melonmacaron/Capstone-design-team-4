package com.example.myot.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClothingItem(
    val id: Long,
    val imageResId: Int = -1,
    val imageUri: String? = null,
    val mainCategory: String,
    val category: String,
    val length: String?,               // ✅ 수정
    val color: String,
    val fit: String,
    val material: String,
    val sleeveLength: String? = null,
    val collar: String? = null
) : Parcelable
