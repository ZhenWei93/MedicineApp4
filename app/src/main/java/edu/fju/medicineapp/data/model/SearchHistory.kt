package edu.fju.medicineapp.data.model

data class SearchHistory(
    val id: Int,
    val user_id: String,
    val query_text: String,
    val query_time: String,
    val medicationCode: String? // 添加 medicationCode 字段
)