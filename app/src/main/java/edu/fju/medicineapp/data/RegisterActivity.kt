package edu.fju.medicineapp.data

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
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
import kotlinx.coroutines.delay
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
        val identityOptions = listOf( "青少年(12-18歲)", "一般成人(19-64歲)", "孕婦", "年長者(65-150歲)")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, identityOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#0C423F")) // 選中項字體顏色
                view.setBackgroundColor(Color.parseColor("#F1F1F1")) // 選中項背景顏色
                view.textSize = 18f // 文字大小 (單位：sp)
                view.setPadding(10, 10, 10, 10) // 內邊距 (單位：dp)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#0C423F")) // 下拉選項字體顏色
                view.setBackgroundColor(Color.parseColor("#E0F7FA")) // 下拉選項背景顏色
                view.textSize = 18f // 文字大小 (單位：sp)
                view.setPadding(10, 20, 10, 20) // 內邊距 (單位：dp)
                return view
            }
        }
        binding.spinnerIdentity.adapter = adapter
        binding.spinnerIdentity.setSelection(1) // 預設選擇「一般成人」

        // 提交按鈕（註冊）
        binding.buttonRegister.setOnClickListener {
            val id = binding.editTextIdNumber.text.toString().trim()
            val username = binding.editTextName.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val selectedIdentity = binding.spinnerIdentity.selectedItem.toString()

            // 驗證必填欄位
            if (id.isEmpty() || username.isEmpty() || password.isEmpty()) {
                binding.textViewResult.text = "請填寫所有欄位"
                return@setOnClickListener
            }

            // 驗證身份證字號格式
            if (!id.matches(Regex("[A-Z][0-9]{9}"))) {
                binding.textViewResult.text = "身份證字號格式錯誤"
                return@setOnClickListener
            }

            // 驗證密碼長度
            if (password.length < 6) {
                binding.textViewResult.text = "密碼需至少6位"
                return@setOnClickListener
            }

            // 根據 Spinner 選擇映射身分
            val identity = when (selectedIdentity) {
                "孕婦" -> "pregnant"
                "青少年(12-18歲)" -> "teenager"
                "年長者(65-150歲)" -> "elderly"
                else -> "general"
            }
            Log.d(TAG, "Determined identity: $identity")

            val user = User(id = id, username = username, password = password, identity = identity)
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
                        identity = user.identity
                    )
                    // 延遲 1 秒後跳轉
                    delay(1500L)

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