package edu.fju.medicineapp

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object HttpUtility
{
    private val TAG = HttpUtility::class.java.simpleName.toString()

    private const val API_KEY_NAME = "bowwow"
    private const val API_KEY_VALUE = "418ae1f8967ec371f7504e29268c5486"

    init
    {
        trustAllCertificates()
    }

    // 設置信任所有憑證，忽略 SSL 驗證，測試用
    private fun trustAllCertificates()
    {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager
        {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
        HttpsURLConnection.setDefaultHostnameVerifier(HostnameVerifier { _, _ -> true })
    }

    // 發送 GET 請求以獲取藥物詳細資料，並透過 callback 返回結果(回傳 Medicine 物件)
    fun doGet(context: Context, urlString: String, callback: (String?) -> Unit)
    {
        // 非同步請求藥物資料
        CoroutineScope(Dispatchers.IO).launch()
        {
            try
            {
                // 建立 URL 連接
                val url = URL(urlString)
                val urlConnection = url.openConnection() as HttpURLConnection

                // 設置請求方法和標頭
                urlConnection.requestMethod = "GET"
                urlConnection.setRequestProperty(API_KEY_NAME, API_KEY_VALUE)
                urlConnection.setRequestProperty("Content-Type", "application/json")

                // 獲取 HTTP 回應碼
                val responseCode = urlConnection.responseCode
                Log.e(TAG, "1.doGet urlString: $urlString")
                Log.e(TAG, "2.doGet responseCode: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 若回應成功，讀取輸入流並組成回應字串
                    val input = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    val response = StringBuilder()
                    input.useLines { lines -> lines.forEach { response.append(it) } }
                    Log.e(TAG, "3.doGet response: ${response}")

                    // 切換到主執行緒來回傳結果，以便在 callback 中更新 UI
                    withContext(Dispatchers.Main) {
                        callback(response.toString())  // 回傳解析後的資料
                    }
                } else {
                    // 其實這裡有一個問題就是發生錯誤跟資料為空會分不出來
                    // 這裡在實務上會分更仔細
                    Log.e(TAG, "4.doGet response: null")

                    // 再次切換到主執行緒來回傳結果，以便在 callback 中更新 UI
                    withContext(Dispatchers.Main) {
                        callback(null)      // 回傳 null 表示請求失敗
                    }
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                // 再次切換到主執行緒來回傳結果
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }
}