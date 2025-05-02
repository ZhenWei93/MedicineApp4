package edu.fju.medicineapp.data.model

data class User(
    val id: String,
    val username: String,
    val age: Int,
    val password: String,
    val identity: String
)