package com.skh.storyteller

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import edu.fju.medicineapp.utility.NullUtility
import edu.fju.medicineapp.utility.SOUT

/**
 * Description:
 *
 * Author: Shi_Kai_Lin
 *
 * Date: 2024/10/1
 */

object Storyteller
{
    var Error_OnInitFail        = -1
    var Error_NotInitialized    = -2
    var Error_StoryEmpty        = -3
    var Error_SpeakFail         = -4

    interface OnInfoListener
    {
        fun onStop(interrupted: Boolean)
        fun onError(error: Int)
    }

    fun installTTS(context: Context)
    {
        val installIntent = Intent()
        installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
        context.startActivity(installIntent)
    }

    private val TAG = Storyteller::class.simpleName.toString()
    private val StorytellerId = "StorytellerId"

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var story: String = ""

    private var textToSpeechOnInitListener = object: TextToSpeech.OnInitListener
    {
        override fun onInit(status: Int)
        {
            NullUtility.tryCatch()
            {
                if (status != TextToSpeech.SUCCESS)
                {
                    SOUT.Loge(TAG, "Storyteller 初始化失敗")
                    this@Storyteller.infoListener?.onError(Error_OnInitFail)
                    return
                }

                printInfo()

                textToSpeech?.setOnUtteranceProgressListener(utteranceProgressListener) // 設定語音進度監聽器
                isInitialized = true // 確認初始化完成
                speak(story)
            }
        }
    }

    private var utteranceProgressListener = object: UtteranceProgressListener()
    {
        override fun onStart(utteranceId: String?)
        {
            SOUT.Loge(TAG, "語音開始播放")
        }

        override fun onDone(utteranceId: String?)
        {
            SOUT.Loge(TAG, "語音播放完成")
            this@Storyteller.infoListener?.onStop(false)
            shutdown() // onDone
        }

        override fun onError(utteranceId: String?)
        {
            SOUT.Loge(TAG, "語音播放錯誤")
            this@Storyteller.infoListener?.onError(Error_SpeakFail)
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean)
        {
            SOUT.Loge(TAG, "語音被停止，是否為中斷: $interrupted")
            this@Storyteller.infoListener?.onStop(interrupted)
        }
    }

    private var infoListener: OnInfoListener? = null

    fun speak(context: Context, story: String, infoListener: OnInfoListener)
    {
        shutdown() // speak

        this.infoListener = infoListener
        this.story = story

        findGoogleTextToSpeechEngineName(context)?.let()
        {
            this.textToSpeech = TextToSpeech(context, textToSpeechOnInitListener, it)
            return
        }
        this.textToSpeech = TextToSpeech(context, textToSpeechOnInitListener)
    }

    // 語音輸出方法
    private fun speak(story: String)
    {
        if (!isInitialized)
        {
            this.infoListener?.onError(Error_NotInitialized)
            return
        }

        if (story == "")
        {
            this.infoListener?.onError(Error_StoryEmpty)
            return
        }

        textToSpeech?.setPitch(1.0f)        // 設定音調，1.0 是正常音調，>1.0 為高音調
        textToSpeech?.setSpeechRate(1.0f)   // 設定語速，1.0 是正常速度，<1.0 是慢速

        // 語音合成
        val result = textToSpeech?.speak(story,                         // 要朗讀的文本
                                         TextToSpeech.QUEUE_FLUSH,      // 播放模式，QUEUE_FLUSH 清除現有的語音，QUEUE_ADD 追加語音
                                         null,
                                         StorytellerId                  // 唯一標識這段語音
        )

        // 檢查語音合成是否成功
        if (result == TextToSpeech.SUCCESS)
        {
            SOUT.Loge(TAG, "語音開始成功")
        }
        else
        {
            SOUT.Loge(TAG, "語音合成失敗")
            this.infoListener?.onError(Error_SpeakFail)
        }

        return
    }

    // 釋放資源
    fun shutdown()
    {
        NullUtility.tryCatch()
        {
            if (!isInitialized)
                return

            textToSpeech?.stop()
            textToSpeech?.shutdown()
            isInitialized = false
            story = ""
        }
    }

    private fun findGoogleTextToSpeechEngineName(context: Context): String?
    {
        val tts = TextToSpeech(context) { }
        for (engine in tts.engines)
        {
            if (engine.name.contains("google", ignoreCase = true))
            {
                SOUT.Loge(TAG, "findGoogleTextToSpeechEngineName : ${engine.name}")
                return engine.name // 返回 Google TTS 引擎的包名
            }
        }

        tts.shutdown()
        return null
    }

    fun printInfo()
    {
        textToSpeech?.availableLanguages?.forEach()
        { locale ->
            SOUT.Loge(TAG, "支持的語言: ${locale.displayLanguage} (${locale.displayCountry})")
        }
    }
}
