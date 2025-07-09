package com.example.myot.model

import java.io.Serializable

data class RecommendItem(
    val id: Long,
    val imageUrl: String,
    var description: String,
    val clothingIds: List<Long>,
    val tpo: String,
    val created_at: String
): Serializable
