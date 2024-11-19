package edu.fju.medicineapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle;
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView;
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide


class MedicineDetailActivity : AppCompatActivity() {
    private val TAG = ApiUtility::class.java.toString()

    private lateinit var responseTextView: TextView
    private lateinit var medicineCodeEditText: EditText
    private lateinit var fetchMedicineButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine_detail) // Set the layout

        // Initialize views(一開始要使用者輸入藥品編號的畫面)
        medicineCodeEditText = findViewById(R.id.medicineCodeEditText)
        fetchMedicineButton = findViewById(R.id.fetchMedicineButton)
        responseTextView = findViewById(R.id.responseTextView)

        //藥品成功查詢後，隱藏搜尋欄?

        // 藥品成功查詢後，輸入框下面顯示的藥品資訊、圖片
        var genericNameTextView = findViewById<TextView>(R.id.genericNameTextView)
        var chineseBrandNameTextView = findViewById<TextView>(R.id.chineseBrandNameTextView)
        var englishBrandNameTextView = findViewById<TextView>(R.id.englishBrandNameTextView)
        var appearanceTextView = findViewById<TextView>(R.id.appearanceTextView)
        var dosageTextView = findViewById<TextView>(R.id.dosageTextView)
        var purposeTextView = findViewById<TextView>(R.id.purposeTextView)
        var storageMethodTextView = findViewById<TextView>(R.id.storageMethodTextView)
        var sideEffectTextView = findViewById<TextView>(R.id.sideEffectTextView)
        var noticeTextView = findViewById<TextView>(R.id.noticeTextView)
        var pregnancyBreastFeedingChildNoticeTextView = findViewById<TextView>(R.id.pregnancyBreastFeedingChildNoticeTextView)
        var imageAPathImageView = findViewById<ImageView>(R.id.imageAPathImageView)
        var imageBPathImageView = findViewById<ImageView>(R.id.imageBPathImageView)

        // 將藥品資訊 TextView 設為不可見
        responseTextView.visibility = View.GONE
        genericNameTextView.visibility = View.GONE
        chineseBrandNameTextView.visibility = View.GONE
        englishBrandNameTextView.visibility = View.GONE
        appearanceTextView.visibility = View.GONE
        dosageTextView.visibility = View.GONE
        purposeTextView.visibility = View.GONE
        storageMethodTextView.visibility = View.GONE
        sideEffectTextView.visibility = View.GONE
        noticeTextView.visibility = View.GONE
        pregnancyBreastFeedingChildNoticeTextView.visibility = View.GONE


        // 註冊 ActivityResultLauncher，處理從 ApiActivity 返回的結果
        var apiActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 處理從 ApiActivity 返回的結果
                val resultData = result.data?.getStringExtra("RESULT_DATA")
                // 在這裡處理 resultData，或者跳轉到 MainActivity
                val mainIntent = Intent(this, MainActivity::class.java)
                mainIntent.putExtra("RESULT_DATA", resultData)
                startActivity(mainIntent)
            }
        }

        // 按鈕點擊監聽器，用來查詢藥品
        fetchMedicineButton.setOnClickListener {
            val medicineCode = medicineCodeEditText.text.toString()
            if (medicineCode.isNotEmpty()) {
                // Create an Intent to start MainActivity and pass the medicine code
//原程式碼          val intent = Intent(this, ApiUtility::class.java)
//原程式碼          intent.putExtra("MEDICINE_CODE", medicineCode)
//原程式碼
//原程式碼          // 使用 ActivityResultLauncher 啟動 ApiActivity
//原程式碼          apiActivityResultLauncher.launch(intent)

                // 使用 ApiUtility 進行藥品查詢
                ApiUtility.fetchMedicineDetails(this, medicineCode) { medicine ->   // { medicine -> 是 Lambda 回呼函數的開頭
                    runOnUiThread {  // 在主執行緒上更新 UI
                        if (medicine != null) {
                            // 1.顯示查詢成功的提示
                            responseTextView.text = "請參考以下藥品資訊"

                            // 2.查詢成功後，就可以藥品資訊的 TextView 顯示出來
                            responseTextView.visibility = View.VISIBLE
                            genericNameTextView.visibility = View.VISIBLE
                            chineseBrandNameTextView.visibility = View.VISIBLE
                            englishBrandNameTextView.visibility = View.VISIBLE
                            appearanceTextView.visibility = View.VISIBLE
                            dosageTextView.visibility = View.VISIBLE
                            purposeTextView.visibility = View.VISIBLE
                            storageMethodTextView.visibility = View.VISIBLE
                            sideEffectTextView.visibility = View.VISIBLE
                            noticeTextView.visibility = View.VISIBLE
                            pregnancyBreastFeedingChildNoticeTextView.visibility = View.VISIBLE

                            // 3.設定藥品資訊 TextView 內容
                            genericNameTextView.text = "學名: ${medicine.genericName}\n"
                            chineseBrandNameTextView.text = "中文藥名: ${medicine.chineseBrandName}\n"
                            englishBrandNameTextView.text = "英文藥名: ${medicine.englishBrandName}\n"
                            appearanceTextView.text = "外觀: ${medicine.appearance}\n"
                            dosageTextView.text = "劑量: ${medicine.dosage}"
                            purposeTextView.text = "主要功能: ${medicine.purpose}\n"
                            storageMethodTextView.text = "保存方法: ${medicine.storageMethod}\n"
                            sideEffectTextView.text = "常見副作用: ${medicine.sideEffect}\n"
                            noticeTextView.text = "注意: ${medicine.notice}\n"
                            pregnancyBreastFeedingChildNoticeTextView.text = "孕婦、母乳哺育孩子須知: ${medicine.pregnancyBreastFeedingChildNotice}\n"

                            // 4.圖片顯示
                            Glide.with(this)
                                .load(medicine.imageAPath)
                                .placeholder(R.drawable.ball)
                                .error(R.drawable.pika)
                                .into(imageAPathImageView)

                            Glide.with(this)
                                .load(medicine.imageBPath)
                                .placeholder(R.drawable.ball)
                                .error(R.drawable.pika)
                                .into(imageBPathImageView)

                        } else {
                            responseTextView.text = "您提供的藥品資料無效."
                        }
                    }
                }
            } else {

                responseTextView.text = "Please enter a valid medicine code."
                Log.e(TAG, "valid medicine code")
                Toast.makeText(this, "Please enter a valid medicine code.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}