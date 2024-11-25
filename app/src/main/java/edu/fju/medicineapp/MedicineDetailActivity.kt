//package edu.fju.medicineapp
//
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle;
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.TextView;
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity;
//import com.bumptech.glide.Glide
//
//
//class MedicineDetailActivity : AppCompatActivity() {
//    private val TAG = ApiUtility::class.java.toString()
//
//    private lateinit var responseTextView: TextView
//    private lateinit var medicineCodeEditText: EditText
//    private lateinit var fetchMedicineButton: Button
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_medicine_detail) // Set the layout
//
//        // Initialize views(一開始要使用者輸入藥品編號的畫面)
//        medicineCodeEditText = findViewById(R.id.medicineCodeEditText)
//        fetchMedicineButton = findViewById(R.id.fetchMedicineButton)
//        responseTextView = findViewById(R.id.responseTextView)
//
//        //藥品成功查詢後，隱藏搜尋欄?
//
//        // 藥品成功查詢後，輸入框下面顯示的藥品資訊、圖片
//        var genericNameTextView = findViewById<TextView>(R.id.genericNameTextView)
//        var chineseBrandNameTextView = findViewById<TextView>(R.id.chineseBrandNameTextView)
//        var englishBrandNameTextView = findViewById<TextView>(R.id.englishBrandNameTextView)
//        var appearanceTextView = findViewById<TextView>(R.id.appearanceTextView)
//        var dosageTextView = findViewById<TextView>(R.id.dosageTextView)
//        var purposeTextView = findViewById<TextView>(R.id.purposeTextView)
//        var storageMethodTextView = findViewById<TextView>(R.id.storageMethodTextView)
//        var sideEffectTextView = findViewById<TextView>(R.id.sideEffectTextView)
//        var noticeTextView = findViewById<TextView>(R.id.noticeTextView)
//        var pregnancyBreastFeedingChildNoticeTextView = findViewById<TextView>(R.id.pregnancyBreastFeedingChildNoticeTextView)
//        var imageAPathImageView = findViewById<ImageView>(R.id.imageAPathImageView)
//        var imageBPathImageView = findViewById<ImageView>(R.id.imageBPathImageView)
//
//        // 將藥品資訊 TextView 設為不可見
//        responseTextView.visibility = View.GONE
//        genericNameTextView.visibility = View.GONE
//        chineseBrandNameTextView.visibility = View.GONE
//        englishBrandNameTextView.visibility = View.GONE
//        appearanceTextView.visibility = View.GONE
//        dosageTextView.visibility = View.GONE
//        purposeTextView.visibility = View.GONE
//        storageMethodTextView.visibility = View.GONE
//        sideEffectTextView.visibility = View.GONE
//        noticeTextView.visibility = View.GONE
//        pregnancyBreastFeedingChildNoticeTextView.visibility = View.GONE
//
//
//        // 註冊 ActivityResultLauncher，處理從 ApiActivity 返回的結果
//        var apiActivityResultLauncher = registerForActivityResult(
//            ActivityResultContracts.StartActivityForResult()
//        ) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                // 處理從 ApiActivity 返回的結果
//                val resultData = result.data?.getStringExtra("RESULT_DATA")
//                // 在這裡處理 resultData，或者跳轉到 MainActivity
//                val mainIntent = Intent(this, MainActivity::class.java)
//                mainIntent.putExtra("RESULT_DATA", resultData)
//                startActivity(mainIntent)
//            }
//        }
//
//        // 按鈕點擊監聽器，用來查詢藥品
//        fetchMedicineButton.setOnClickListener {
//            val medicineCode = medicineCodeEditText.text.toString()
//            if (medicineCode.isNotEmpty()) {
//                // Create an Intent to start MainActivity and pass the medicine code
////原程式碼          val intent = Intent(this, ApiUtility::class.java)
////原程式碼          intent.putExtra("MEDICINE_CODE", medicineCode)
////原程式碼
////原程式碼          // 使用 ActivityResultLauncher 啟動 ApiActivity
////原程式碼          apiActivityResultLauncher.launch(intent)
//
//                // 使用 ApiUtility 進行藥品查詢
//                ApiUtility.fetchMedicineDetails(this, medicineCode) { medicine ->   // { medicine -> 是 Lambda 回呼函數的開頭
//                    runOnUiThread {  // 在主執行緒上更新 UI
//                        if (medicine != null) {
//                            // 1.顯示查詢成功的提示
//                            responseTextView.text = "請參考以下藥品資訊"
//
//                            // 2.查詢成功後，就可以藥品資訊的 TextView 顯示出來
//                            responseTextView.visibility = View.VISIBLE
//                            genericNameTextView.visibility = View.VISIBLE
//                            chineseBrandNameTextView.visibility = View.VISIBLE
//                            englishBrandNameTextView.visibility = View.VISIBLE
//                            appearanceTextView.visibility = View.VISIBLE
//                            dosageTextView.visibility = View.VISIBLE
//                            purposeTextView.visibility = View.VISIBLE
//                            storageMethodTextView.visibility = View.VISIBLE
//                            sideEffectTextView.visibility = View.VISIBLE
//                            noticeTextView.visibility = View.VISIBLE
//                            pregnancyBreastFeedingChildNoticeTextView.visibility = View.VISIBLE
//
//                            // 3.設定藥品資訊 TextView 內容
//                            genericNameTextView.text = "學名: ${medicine.genericName}\n"
//                            chineseBrandNameTextView.text = "中文藥名: ${medicine.chineseBrandName}\n"
//                            englishBrandNameTextView.text = "英文藥名: ${medicine.englishBrandName}\n"
//                            appearanceTextView.text = "外觀: ${medicine.appearance}\n"
//                            dosageTextView.text = "劑量: ${medicine.dosage}"
//                            purposeTextView.text = "主要功能: ${medicine.purpose}\n"
//                            storageMethodTextView.text = "保存方法: ${medicine.storageMethod}\n"
//                            sideEffectTextView.text = "常見副作用: ${medicine.sideEffect}\n"
//                            noticeTextView.text = "注意: ${medicine.notice}\n"
//                            pregnancyBreastFeedingChildNoticeTextView.text = "孕婦、母乳哺育孩子須知: ${medicine.pregnancyBreastFeedingChildNotice}\n"
//
//                            // 4.圖片顯示
//                            Glide.with(this)
//                                .load(medicine.imageAPath)
//                                .placeholder(R.drawable.ball)
//                                .error(R.drawable.pika)
//                                .into(imageAPathImageView)
//
//                            Glide.with(this)
//                                .load(medicine.imageBPath)
//                                .placeholder(R.drawable.ball)
//                                .error(R.drawable.pika)
//                                .into(imageBPathImageView)
//
//                        } else {
//                            responseTextView.text = "您提供的藥品資料無效."
//                        }
//                    }
//                }
//            } else {
//
//                responseTextView.text = "Please enter a valid medicine code."
//                Log.e(TAG, "valid medicine code")
//                Toast.makeText(this, "Please enter a valid medicine code.", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//}
//

package edu.fju.medicineapp

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.speech.tts.TextToSpeech
import android.widget.Button
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class MedicineDetailActivity : AppCompatActivity(), TextToSpeech.OnInitListener
{
    val TAG = MedicineDetailActivity::class.java.simpleName.toString()
    private lateinit var genericNameTextView: TextView
    private lateinit var chineseBrandNameTextView: TextView
    private lateinit var englishBrandNameTextView: TextView
    private lateinit var appearanceTextView: TextView
    private lateinit var dosageTextView: TextView
    private lateinit var purposeTextView: TextView
    private lateinit var storageMethodTextView: TextView
    private lateinit var sideEffectTextView: TextView
    private lateinit var noticeTextView: TextView
    private lateinit var pregnancyBreastFeedingChildNoticeTextView: TextView
    private lateinit var imageAPathImageView: ImageView
    private lateinit var imageBPathImageView: ImageView
    private lateinit var tts: TextToSpeech
    private lateinit var readButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine_detail)

        // 初始化 Text-to-Speech
        tts = TextToSpeech(this, this)

        // 初始化按鈕
        readButton = findViewById(R.id.readButton)
        readButton.setOnClickListener {
            readMedicineDetails()
        }

        // 初始化各個 TextView 和 ImageView
        genericNameTextView = findViewById(R.id.genericNameTextView)
        chineseBrandNameTextView = findViewById(R.id.chineseBrandNameTextView)
        englishBrandNameTextView = findViewById(R.id.englishBrandNameTextView)
        appearanceTextView = findViewById(R.id.appearanceTextView)
        dosageTextView = findViewById(R.id.dosageTextView)
        purposeTextView = findViewById(R.id.purposeTextView)
        storageMethodTextView = findViewById(R.id.storageMethodTextView)
        sideEffectTextView = findViewById(R.id.sideEffectTextView)
        noticeTextView = findViewById(R.id.noticeTextView)
        pregnancyBreastFeedingChildNoticeTextView = findViewById(R.id.pregnancyBreastFeedingChildNoticeTextView)
        imageAPathImageView = findViewById(R.id.imageAPathImageView)
        imageBPathImageView = findViewById(R.id.imageBPathImageView)


        // 接收傳遞過來的 medicineCode
        val medicineCode = intent.getStringExtra("MEDICINE_CODE")
        Log.e(TAG, "medicineCode: $medicineCode")
        if (medicineCode != null) {
            // 使用 medicineCode 獲取藥品詳細資料
            ApiUtility.fetchMedicineDetails(this, medicineCode) { medicine ->
                runOnUiThread {
                    if (medicine != null) {
                        // 顯示藥品詳細資訊
                        genericNameTextView.text = "學名: ${medicine.genericName}"
                        chineseBrandNameTextView.text = "中文藥名: ${medicine.chineseBrandName}"
                        englishBrandNameTextView.text = "英文藥名: ${medicine.englishBrandName}"
                        appearanceTextView.text = "外觀: ${medicine.appearance}"
                        dosageTextView.text = "用法: ${medicine.dosage}"
                        purposeTextView.text = "用途: ${medicine.purpose}"
                        storageMethodTextView.text = "儲存方式: ${medicine.storageMethod}"
                        sideEffectTextView.text = "副作用: ${medicine.sideEffect}"
                        noticeTextView.text = "注意事項: ${medicine.notice}"
                        pregnancyBreastFeedingChildNoticeTextView.text = "孕婦/哺乳期/兒童注意: ${medicine.pregnancyBreastFeedingChildNotice}"

                        // 顯示圖片
                        Glide.with(this)
                            .load(medicine.imageAPath)
                            .into(imageAPathImageView)

                        Glide.with(this)
                            .load(medicine.imageBPath)
                            .into(imageBPathImageView)
                    } else {
                        Toast.makeText(this, "無法獲取藥品詳細資訊", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "藥品代碼無效", Toast.LENGTH_SHORT).show()
        }

    }
    // Text-to-Speech 初始化完成後回調
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.TAIWAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "不支援的語言", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Text-to-Speech 初始化失敗", Toast.LENGTH_SHORT).show()
        }
    }

    // 清理資源
    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
    private fun readMedicineDetails() {
        val details = """
            學名: ${genericNameTextView.text}
            中文藥名: ${chineseBrandNameTextView.text}
            英文藥名: ${englishBrandNameTextView.text}
            外觀: ${appearanceTextView.text}
            用法: ${dosageTextView.text}
            用途: ${purposeTextView.text}
            儲存方式: ${storageMethodTextView.text}
            副作用: ${sideEffectTextView.text}
            注意事項: ${noticeTextView.text}
            孕婦、哺乳期與兒童注意事項: ${pregnancyBreastFeedingChildNoticeTextView.text}
        """.trimIndent()

        if (tts.isSpeaking) {
            tts.stop() // 如果已經在朗讀，先停止
        }
        tts.speak(details, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}



