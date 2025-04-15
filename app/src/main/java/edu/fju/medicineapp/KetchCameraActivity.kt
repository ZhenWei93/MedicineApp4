package edu.fju.medicineapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

class KetchCameraActivity : AppCompatActivity() {
    private val CAMERA_REQUEST_CODE = 100
    private val IMAGE_CAPTURE_CODE = 101
    private var imageUri: Uri? = null

    private lateinit var imageView: ImageView
    private lateinit var cameraButton: Button
    private lateinit var photoUri: Uri
    private lateinit var photoFile: File
    private lateinit var searchButton: Button
//    private lateinit var resultText: TextView // 顯示"先拍照再查詢"

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageView.setImageURI(photoUri)
            } else {
                Toast.makeText(this, "拍照失敗", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        imageView = findViewById(R.id.imageView)
        cameraButton = findViewById(R.id.cameraButton)
        searchButton = findViewById(R.id.searchButton)
//        resultText = findViewById(R.id.resultText)

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
    }

    private fun takePhoto() {
        photoFile = File.createTempFile("photo_", ".jpg", cacheDir)
        photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(photoUri)
//        // 創建 Multipart 請求
//        val requestFile = byteArray.toRequestBody("image/jpeg".toMediaType())
//        val body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile)
//
//        // 初始化 OkHttpClient
//        val client = OkHttpClient()
//
//        // 構建請求
//        val request = Request.Builder()
//            .url("http://YOUR_SERVER_IP:5000/query_image") // 替換為你的 API 地址，例如 http://10.0.2.2:5000
//            .post(MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addPart(body)
//                .build())
//            .build()
//
//        // 異步執行請求
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                runOnUiThread {
//                    resultText.text = "錯誤：無法連接到伺服器 - ${e.message}"
//                    Toast.makeText(this@CameraCaptureActivity, "上傳失敗：${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                if (response.isSuccessful) {
//                    response.body?.string()?.let { responseBody ->
//                        try {
//                            // 解析 JSON
//                            val jsonObject = JSONObject(responseBody)
//                            val medicine_info = jsonObject.getJSONArray("medicine_info")
//                            if (medicine_info.length() > 0) {
//                                // 只取第一個結果
//                                val med = medicine_info.getJSONObject(0)
//                                val medicineCode = med.optString("medicineCode")
//                                val chineseBrandName = med.optString("chineseBrandName","未知")
////                              val similarity = med.optDouble("distance", 0.0)
//
//                                // 跳轉到 MedicineDetailActivity
//                                val intent = Intent(this@CameraCaptureActivity, MedicineDetailActivity::class.java).apply {
//                                    putExtra("chineseBrandName", chineseBrandName)
//                                    putExtra("MEDICINE_CODE",medicineCode)
////                                  putExtra("similarity", similarity)
//                                }
//                                startActivity(intent)
//                            } else {
//                                runOnUiThread {
//                                    resultText.text = "無查詢結果"
//                                    Toast.makeText(this@CameraCaptureActivity, "無匹配的藥品", Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                        } catch (e: Exception) {
//                            runOnUiThread {
//                                resultText.text = "解析錯誤：${e.message}"
//                                Toast.makeText(this@CameraCaptureActivity, "解析失敗：${e.message}", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//                } else {
//                    runOnUiThread {
//                        resultText.text = "查詢失敗：伺服器回應錯誤"
//                        Toast.makeText(this@CameraCaptureActivity, "伺服器錯誤：${response.code}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        })
    }
}

