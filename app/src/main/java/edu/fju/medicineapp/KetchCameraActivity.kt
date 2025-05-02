package edu.fju.medicineapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLHandshakeException

class KetchCameraActivity : AppCompatActivity() {
    private var capturedBitmap: Bitmap? = null // 儲存拍攝的 Bitmap


    private lateinit var imageView: ImageView
    private lateinit var cameraButton: Button
    private lateinit var photoUri: Uri
    private lateinit var photoFile: File
    private lateinit var reTakeButton: Button
    private lateinit var searchButton: Button
    private lateinit var resultText: TextView // 顯示"先拍照再查詢"

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageView.setImageURI(photoUri)
                photoUri?.let { uri ->
                    try {
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            capturedBitmap = BitmapFactory.decodeStream(inputStream)
                            imageView.setImageBitmap(capturedBitmap)
                            resultText.text = "請點擊搜尋按鈕進行查詢"
                        }
                    } catch (e: IOException) {
                        Toast.makeText(this, "圖片處理失敗：${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "拍照失敗", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        imageView = findViewById(R.id.imageView)
        cameraButton = findViewById(R.id.cameraButton)
        reTakeButton = findViewById(R.id.reTakeButton)
        searchButton = findViewById(R.id.searchButton)
        resultText = findViewById(R.id.resultText)

        cameraButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    100
                )
            } else {
                takePhoto()
            }
        }

        reTakeButton.setOnClickListener {
            takePhoto()
        }

        //將bitmap傳至雲端API
        searchButton.setOnClickListener {
//            testUploadImage()
            capturedBitmap?.let { bitmap ->
                uploadImage(bitmap)
            } ?: Toast.makeText(this, "請先拍攝照片", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takePhoto() {
        photoFile = File.createTempFile("photo_", ".jpg", cacheDir)
        photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(photoUri)
    }

//    fun testUploadImage() {
//        // 模擬一張圖片 (例如從資源載入)
//        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.farma) // 替換為你的測試圖片
//        if (bitmap == null) {
//            Log.e("Test", "無法載入測試圖片")
//            return
//        }

//        // 呼叫 uploadImage 函數
//        uploadImage(bitmap)
//    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    //上傳圖片位址至模型並查詢結果回傳至詳細頁面
    private fun uploadImage(bitmap: Bitmap) {
        if (!isNetworkAvailable()) {
            runOnUiThread {
                Toast.makeText(this, "無網路連線，請檢查網路設置", Toast.LENGTH_SHORT).show()
            }
            return
        }

            Log.d(
                "UploadImage",
                "receive Bitmap, turn to JPEG, size: ${bitmap.width}x${bitmap.height}"
            )
            // 將 Bitmap 轉為字節陣列
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val byteArray = stream.toByteArray()
            // 創建 Multipart 請求
            val requestFile = byteArray.toRequestBody("image/jpeg".toMediaType())

            // 初始化 OkHttpClient
            val client = OkHttpClient().newBuilder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .build()


            // 構建請求
            val request = Request.Builder()
                .url("https://web-production-ba67.up.railway.app/api/query_image") // 替換為你的 API 地址，例如 http://10.0.2.2:5000
                .post(
                    MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", "image.jpg", requestFile)
                        .build()
                )
                .build()


            // 異步執行請求
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val errorMessage = when {
                        e is SocketTimeoutException -> "連線超時，請檢查伺服器狀態或網路"
                        e is UnknownHostException -> "無法解析伺服器地址，請檢查網路或域名"
                        e is SSLHandshakeException -> "SSL 連線失敗，請檢查伺服器證書或設備時間"
                        else -> "無法連接到伺服器：${e.message}"
                    }
                    Log.e(
                        "UploadImage",
                        "Request failed: $errorMessage, URL: ${call.request().url}",
                        e
                    )
                    runOnUiThread {
                        resultText.text = "錯誤：$errorMessage"
                        Toast.makeText(
                            this@KetchCameraActivity,
                            "上傳失敗：${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response)
                {
                    var result = response.body?.string()
                    Log.e("UploadImage", "Server error ${response.code}: ${result}")
                    result?.let { responseBody ->
                        try {
                            if (responseBody.isEmpty()) {
                                runOnUiThread {
                                    resultText.text = "伺服器回應為空"
                                    Toast.makeText(this@KetchCameraActivity, "無有效回應", Toast.LENGTH_SHORT).show()
                                }
                                return
                            }
                            val jsonObject = JSONObject(responseBody)
                            // 後續邏輯
                        } catch (e: JSONException) {
                            runOnUiThread {
                                resultText.text = "JSON 解析錯誤"
                                Toast.makeText(this@KetchCameraActivity, "無效的回應格式", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    if (response.isSuccessful) {
                        result?.let { responseBody ->
                            try {
                                // 解析 JSON
                                val jsonObject = JSONObject(responseBody)
                                // 直接獲取 medicationCode 和 chineseBrandName
                                val medicineCode = jsonObject.optString("medicationCode", "未知")
                                val chineseBrandName =
                                    jsonObject.optString("chineseBrandName", "未知")
//                                val similarity = med.optDouble("distance", 0.0)

                                // 確認是否有有效資料
                                if (medicineCode != "未知" || chineseBrandName != "未知") {
                                    // 跳轉到 MedicineDetailActivity
                                    val intent = Intent(
                                        this@KetchCameraActivity,
                                        MedicineDetailActivity::class.java
                                    ).apply {
                                        putExtra("chineseBrandName", chineseBrandName)
                                        putExtra("MEDICINE_CODE", medicineCode)
//                                        putExtra("similarity", similarity)
                                    }
                                    startActivity(intent)
                                } else {
                                    runOnUiThread {
                                        resultText.text = "無查詢結果"
                                        Toast.makeText(
                                            this@KetchCameraActivity,
                                            "無匹配的藥品",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                runOnUiThread {
                                    resultText.text = "解析錯誤：${e.message}"
                                    Toast.makeText(
                                        this@KetchCameraActivity,
                                        "解析失敗：${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            resultText.text = "查詢失敗：伺服器回應錯誤"
                            Toast.makeText(
                                this@KetchCameraActivity,
                                "伺服器錯誤：${response.code}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
        }
        }



