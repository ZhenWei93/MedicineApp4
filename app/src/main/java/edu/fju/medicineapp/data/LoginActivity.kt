package edu.fju.medicineapp.data

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import edu.fju.medicineapp.data.api.ApiClient
import edu.fju.medicineapp.databinding.ActivityLoginBinding
import edu.fju.medicineapp.CoverActivity
import edu.fju.medicineapp.data.model.LoginRequest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val apiClient = ApiClient()
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 登入按鈕
        binding.buttonLogin.setOnClickListener {
            val id = binding.editTextIdNumber.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            // 記錄用戶輸入（隱藏密碼）
            Log.d(TAG, "User input: id=$id, password=[HIDDEN]")

            // 驗證輸入
            if (id.isEmpty()) {
                val errorMsg = "請輸入身份證字號"
                Log.w(TAG, errorMsg)
                binding.textViewResult.text = errorMsg
                return@setOnClickListener
            }
            if (!id.matches(Regex("[A-Z][12][0-9]{8}"))) {
                val errorMsg = "身份證字號格式錯誤"
                Log.w(TAG, errorMsg)
                binding.textViewResult.text = errorMsg
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                val errorMsg = "請輸入密碼"
                Log.w(TAG, errorMsg)
                binding.textViewResult.text = errorMsg
                return@setOnClickListener
            }
            if (password.length < 6) {
                val errorMsg = "密碼需至少6位"
                Log.w(TAG, errorMsg)
                binding.textViewResult.text = errorMsg
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(id = id, password = password)
            loginUser(loginRequest)
        }
    }

    private fun loginUser(loginRequest: LoginRequest) {
        lifecycleScope.launch {
            binding.buttonLogin.isEnabled = false
            Log.i(TAG, "Initiating API request for login: id=${loginRequest.id}")
            try {
                val result = apiClient.loginUser(loginRequest)
                result.onSuccess { loginResponse ->
                    Log.i(TAG, "Login successful: message=${loginResponse.message}, id=${loginResponse.id}, username=${loginResponse.username}, identity=${loginResponse.identity}")
                    Toast.makeText(this@LoginActivity, "登入成功", Toast.LENGTH_SHORT).show()

                    // 使用 PreferencesManager 儲存資料
                    PreferencesManager.saveUserData(
                        context = this@LoginActivity,
                        id = loginResponse.id,
                        username = loginResponse.username,
                        age = loginResponse.age,
                        identity = loginResponse.identity,
                        token = loginResponse.token
                    )
                    // 跳轉到 CoverActivity
                    val intent = Intent(this@LoginActivity, CoverActivity::class.java).apply {
                        putExtra("id", loginResponse.id)
                        putExtra("username", loginResponse.username)
                        putExtra("age", loginResponse.age)
                        putExtra("identity", loginResponse.identity)
                        // 不傳遞 password，提升安全性
                    }
                    startActivity(intent)
                    finish()
                }.onFailure { exception ->
                    val errorMessage = when {
                        exception.message?.contains("Invalid id or password") == true -> "身份證字號或密碼錯誤"
                        exception.message?.contains("Network error") == true -> "網路連線失敗"
                        exception.message?.contains("伺服器回應格式錯誤") == true -> "伺服器回應異常，請聯繫管理員"
                        else -> "登入失敗：${exception.message}"
                    }
                    Log.e(TAG, "Login failed: $errorMessage", exception)
                    binding.textViewResult.text = errorMessage
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during login: ${e.message}", e)
                binding.textViewResult.text = "登入失敗：${e.message}"
                Toast.makeText(this@LoginActivity, "登入失敗：${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.buttonLogin.isEnabled = true
            }
        }
    }
}