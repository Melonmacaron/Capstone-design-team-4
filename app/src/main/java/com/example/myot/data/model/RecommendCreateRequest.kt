package com.example.myot.model

data class RecommendCreateRequest(
    val tpo: String,
    val selected_item_id: Int  // ✅ 이름과 타입 모두 서버와 일치
)
