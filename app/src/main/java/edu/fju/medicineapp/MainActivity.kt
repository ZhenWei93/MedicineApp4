package edu.fju.medicineapp

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener
{
    private val TAG = MainActivity::class.java.simpleName.toString()
    private lateinit var tts: TextToSpeech

    // 1. SingleTon 的好處跟限制是什麼?
    // 2. lambda 做什麼用? 好處是什麼?
    // 3. callback: (Medicine?) -> Unit 跟 callback: (String?) -> Unit 有什麼不一樣
    // 4. 為什麼我要 宣告一個 ResponseData 裡面只有一個成員 Medicine ?
    // 5. CoroutineScope(Dispatchers.IO).launch() 在背景執行，到時候回呼回來如果更新 UI 會不會有問題?
    //    那該怎麼辦?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化 TTS
        tts = TextToSpeech(this, this)

        ApiUtility.searchMedicineLists(this, "生理食鹽水") { medicines ->
            medicines?.let {
                for (medicine in it) {
                    Log.e(TAG, "medicine ${medicine.chineseBrandName}")
                    // 在成功獲取資料後讀出藥品名稱
                    speak(medicine.chineseBrandName)
                }
            } ?: run {
                Log.e(TAG, "未找到相關藥品信息")
            }
        }
    }

    // TextToSpeech 初始化回調
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // 設置語言
            val result = tts.setLanguage(Locale.TAIWAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "語音朗讀不支援該語言")
            }
        } else {
            Log.e(TAG, "TTS 初始化失敗")
        }
    }

    // 播放語音的方法
    private fun speak(text: String) {
        if (::tts.isInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onDestroy() {
        // 關閉 TTS 以釋放資源
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}