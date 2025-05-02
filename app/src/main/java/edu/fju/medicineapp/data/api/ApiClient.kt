package edu.fju.medicineapp.data.api

import android.util.Log
import edu.fju.medicineapp.data.model.LoginRequest
import edu.fju.medicineapp.data.model.RegisterResponse
import edu.fju.medicineapp.data.model.User
import com.google.gson.Gson
import edu.fju.medicineapp.data.model.LoginResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiClient {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val baseUrl = "https://authapi-production-70c2.up.railway.app/" // 替換為你的 Railway API URL
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val TAG = "ApiClient"

    suspend fun registerUser(user: User): Result<RegisterResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // 記錄請求資料
                Log.d(TAG, "Sending register request: ${gson.toJson(user)}")

                val json = gson.toJson(user)
                val requestBody = json.toRequestBody(jsonMediaType)
                val httpRequest = Request.Builder()
                    .url("${baseUrl}api/register")
                    .post(requestBody)
                    .build()

                client.newCall(httpRequest).execute().use { response ->
                    val responseBody = response.body?.string()
                    // 記錄回應狀態和內容
                    Log.d(TAG, "Received response: code=${response.code}, body=$responseBody")

                    val registerResponse = gson.fromJson(responseBody, RegisterResponse::class.java)
                    if (response.isSuccessful) {
                        Log.i(
                            TAG,
                            "Registration successful: message=${registerResponse.message}, id=${registerResponse.id}"
                        )
                        Result.success(registerResponse)
                    } else {
                        val errorMsg =
                            registerResponse.error ?: "Error: ${response.code} ${response.message}"
                        Log.e(TAG, "Registration failed: $errorMsg")
                        Result.failure(Exception(errorMsg))
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error during registration: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during registration: ${e.message}", e)
                Result.failure(Exception("Unexpected error: ${e.message}"))
            }
        }
    }

    suspend fun loginUser(request: LoginRequest): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // 記錄請求（隱藏密碼）
                val requestLog = request.copy(password = "[HIDDEN]")
                Log.d(TAG, "Sending login request: ${gson.toJson(requestLog)}")

                val json = gson.toJson(request)
                val requestBody = json.toRequestBody(jsonMediaType)
                val httpRequest = Request.Builder()
                    .url("${baseUrl}api/login")
                    .post(requestBody)
                    .build()

                client.newCall(httpRequest).execute().use { response ->
                    val responseBody = response.body?.string()
                    Log.d(TAG, "Received login response: code=${response.code}, body=$responseBody")

                    val loginResponse = gson.fromJson(responseBody, LoginResponse::class.java)
                    if (response.isSuccessful) {
                        Log.i(
                            TAG,
                            "Login successful: message=${loginResponse.message}, username=${loginResponse.username}"
                        )
                        Result.success(loginResponse)
                    } else {
                        val errorMsg =
                            loginResponse.error ?: "Error: ${response.code} ${response.message}"
                        Log.e(TAG, "Login failed: $errorMsg")
                        Result.failure(Exception(errorMsg))
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error during login: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            } catch (e: java.net.UnknownHostException) {
                Log.e(TAG, "Cannot resolve host: ${baseUrl}api/login", e)
                Result.failure(Exception("無法連接到伺服器，請檢查網路"))
            } catch (e: com.google.gson.JsonSyntaxException) {
                Log.e(TAG, "Invalid JSON response: ${e.message}", e)
                Result.failure(Exception("伺服器回應格式錯誤，請聯繫管理員"))
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during login: ${e.message}", e)
                Result.failure(Exception("Unexpected error: ${e.message}"))
            }
        }
    }
}