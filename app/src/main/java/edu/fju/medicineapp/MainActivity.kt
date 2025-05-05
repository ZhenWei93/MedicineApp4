package edu.fju.medicineapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import edu.fju.medicineapp.data.LoginActivity
import edu.fju.medicineapp.data.PreferencesManager
import edu.fju.medicineapp.data.RegisterActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 檢查是否已登入
        val userId = PreferencesManager.getUserId(this)
        if (userId != "N/A") {
            // 已登入，直接跳轉至 CoverActivity
            startActivity(Intent(this, CoverActivity::class.java))
            finish()
            return
        }

        // 設置按鈕點擊事件
        findViewById<Button>(R.id.btn_login).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<Button>(R.id.RegisterButton).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // 點擊直接使用按鈕，直接跳轉至查詢頁面
        findViewById<Button>(R.id.DirectButton).setOnClickListener {
            startActivity(Intent(this, CoverActivity::class.java))
        }
    }
}