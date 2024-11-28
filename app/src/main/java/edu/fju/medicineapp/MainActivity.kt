package edu.fju.medicineapp

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity()
{
    private val TAG = MainActivity::class.java.simpleName.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        ApiUtility.searchMedicineLists(this, "生理食鹽水") { medicines ->
            medicines?.let {
                for (medicine in it) {
                    Log.e(TAG, "medicine ${medicine.chineseBrandName}")
                }
            } ?: run {
                Log.e(TAG, "未找到相關藥品信息")
            }
        }
    }


}