package com.example.myot.model

data class GenerateImageRequest(
    val user_image_id: String,
    val top_id: Int,
    val bottom_id: Int
)
