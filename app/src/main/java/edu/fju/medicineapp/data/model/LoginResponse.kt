package edu.fju.medicineapp.data.model

class LoginResponse (
    val message: String,
    val token: String,
    val username: String,
    val id: String,
    val identity: String,
    val error: String? = null
)


