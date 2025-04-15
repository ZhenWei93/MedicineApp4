package edu.fju.medicineapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class CoverActivity : AppCompatActivity() {

    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cover)

        startButton = findViewById(R.id.startButton)

        // 修改這裡的變數名稱，避免不一致
        startButton.setOnClickListener {
            val intent = Intent(this, KetchCameraActivity::class.java)
            startActivity(intent)
        }
    }
}
