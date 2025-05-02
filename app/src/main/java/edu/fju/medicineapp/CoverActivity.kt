package edu.fju.medicineapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import edu.fju.medicineapp.data.RegisterActivity

class CoverActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var EnterCameraButton: Button
    private lateinit var RegisterButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cover)

        startButton = findViewById(R.id.startButton)
        EnterCameraButton = findViewById(R.id.EnterCameraButton)
        RegisterButton = findViewById(R.id.RegisterButton)

        // 修改這裡的變數名稱，避免不一致
        startButton.setOnClickListener {
            val intent = Intent(this, SearchMedicineActivity::class.java)
            startActivity(intent)
        }

        EnterCameraButton.setOnClickListener {
            val intent = Intent(this, KetchCameraActivity::class.java)
            startActivity(intent)
        }

        RegisterButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
