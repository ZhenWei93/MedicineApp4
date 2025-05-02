package edu.fju.medicineapp.data.model

data class RegisterResponse(
    val message: String,
    val user: User?,
    val id: String,
    val username: String,
    val age: Int,
    val identity: String,
    val error: String? = null
)