package edu.fju.medicineapp.data

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import edu.fju.medicineapp.OpenAIActivity.Companion.TAG
import edu.fju.medicineapp.data.api.ApiClient
import edu.fju.medicineapp.data.model.User
import edu.fju.medicineapp.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val apiClient = ApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 配置 Spinner
        val identityOptions = listOf("幼兒:(1-3歲)","兒童(4-12歲)","青少年(13-18歲)","一般成人(19-64歲)", "孕婦","年長者(65-150歲)")
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, identityOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerIdentity.adapter = adapter
        binding.spinnerIdentity.setSelection(3) // 預設選擇「一般成人」

        // 提交按鈕（註冊）
        binding.buttonRegister.setOnClickListener {
            val id = binding.editTextIdNumber.text.toString().trim()
            val username = binding.editTextName.text.toString().trim()
            val ageText = binding.editTextAge.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val selectedIdentity = binding.spinnerIdentity.selectedItem.toString()

            if (id.isEmpty() || username.isEmpty() || ageText.isEmpty()) {
                binding.textViewResult.text = "請填寫所有欄位"
                return@setOnClickListener
            }
            if (!id.matches(Regex("[A-Z][0-9]{9}"))) {
                binding.textViewResult.text = "身份證字號格式錯誤"
                return@setOnClickListener
            }
            val age = ageText.toIntOrNull()
            if (age == null || age < 0 || age > 120) {
                binding.textViewResult.text = "年齡無效"
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.textViewResult.text = "密碼需至少6位"
                return@setOnClickListener
            }

            // 判斷身分：年長者優先，否則依據 Spinner
            val identity = when {
                age >= 65 -> "elderly"
                selectedIdentity == "孕婦" -> "pregnant"
                selectedIdentity == "幼兒" -> "baby"
                selectedIdentity == "孩童" -> "child"
                selectedIdentity == "青少年" -> "teenager"
                else -> "general"
            }
            Log.d(TAG, "Determined identity: $identity")

            val user = User(id = id, username = username, age = age, password = password, identity = identity)
            registerUser(user)
        }

        }
    private fun registerUser(user: User) {
        lifecycleScope.launch {
            binding.buttonRegister.isEnabled = false
            try {
                val result = apiClient.registerUser(user)
                result.onSuccess { response ->
                    binding.textViewResult.text = response.message
                    Toast.makeText(this@RegisterActivity, "註冊成功", Toast.LENGTH_SHORT).show()

                    // 使用 PreferencesManager 儲存資料
                    PreferencesManager.saveUserData(
                        context = this@RegisterActivity,
                        id = user.id,
                        username = user.username,
                        age = user.age,
                        identity = user.identity
                    )
                    // 跳轉到登入頁面
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                }.onFailure { exception ->
                    binding.textViewResult.text = "註冊失敗：${exception.message}"
                    Toast.makeText(this@RegisterActivity, "註冊失敗：${exception.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.textViewResult.text = "錯誤：${e.message}"
                Toast.makeText(this@RegisterActivity, "錯誤：${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.buttonRegister.isEnabled = true
            }
        }
    }
}