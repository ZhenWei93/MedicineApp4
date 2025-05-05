package edu.fju.medicineapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import edu.fju.medicineapp.data.LoginActivity
import edu.fju.medicineapp.data.PreferencesManager

class ProfilePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)

        // 從 PreferencesManager 獲取使用者資訊
        val username = PreferencesManager.getUsername(this) ?: "未知使用者"
        val age = PreferencesManager.getAge(this) ?: 0
        val identity = PreferencesManager.getIdentity(this) ?: "未知身分"

        // 檢查是否登入
        if (username == "N/A" && identity == "general" && age == 0) {
            Toast.makeText(this, "請先登入", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 將 identity 轉換為中文顯示
        val displayIdentity = when (identity) {
            "elderly" -> "年長者"
            "pregnant" -> "孕婦"
            "baby" -> "幼兒"
            "child" -> "孩童"
            "teenager" -> "青少年"
            else -> "一般成人"
        }

        // 設置使用者資訊到 TextView
        findViewById<TextView>(R.id.tv_username).text = "使用者名稱：$username"
        findViewById<TextView>(R.id.tv_age).text = "年齡：$age"
        findViewById<TextView>(R.id.tv_identity).text = "身分：$displayIdentity"

        // 設置登出按鈕
        findViewById<Button>(R.id.btn_logout).setOnClickListener {
            PreferencesManager.clearUserData(this)
            Toast.makeText(this, "已登出", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        // 防止登出按鈕誤觸
        findViewById<Button>(R.id.btn_logout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("確認登出")
                .setMessage("確定要登出嗎？")
                .setPositiveButton("確定") { _, _ ->
                    PreferencesManager.clearUserData(this)
                    Toast.makeText(this, "已登出", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }
}