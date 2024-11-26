package edu.fju.medicineapp

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.bumptech.glide.Glide
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import edu.fju.medicineapp.download.DownloadInfo
import edu.fju.medicineapp.download.DownloadInterface
import edu.fju.medicineapp.download.DownloadUtility
import edu.fju.medicineapp.pdf.PDFDetector
import edu.fju.medicineapp.pdf.PDFUtility
import edu.fju.medicineapp.utility.EncryptUtility
import edu.fju.medicineapp.utility.SOUT
import org.bouncycastle.oer.its.etsi102941.Url


class MedicineDetailActivity : AppCompatActivity()
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
    private lateinit var packageInsertPathButton: Button
    private lateinit var imageAPathImageView: ImageView
    private lateinit var imageBPathImageView: ImageView
    private var extractedText: String? = null // 儲存從 PDF 提取的文字內容


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine_detail)

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
        packageInsertPathButton = findViewById(R.id.packageInsertPathButton)
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

                        // 設定仿單按鈕點擊事件
                        packageInsertPathButton.setOnClickListener {
                            registerReceiver()
                            startDownload(medicine.packageInsertPath)

                        }
                    } else {
                        Toast.makeText(this, "無法獲取藥品詳細資訊", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        } else {
            Toast.makeText(this, "藥品代碼無效", Toast.LENGTH_SHORT).show()
        }

    }

       var pdfFromUri: Uri? =null   // 將 PDF 的 URL 轉換為 Uri 格式，方便後續處理

        // 自定義一個函數，用來根據 PDF 的 URL 生成一個唯一的檔案名稱，並加上 ".pdf" 作為檔案後綴
        fun getCustomFileName(pdfUrl: String): String
        {
            return EncryptUtility.encodeMd5(pdfUrl) + PDFDetector.File_Extension_PDF
        }

        // 啟動 PDF 下載的功能
        fun startDownload(pdf: String)
        {
            pdfFromUri = Uri.parse(pdf)
            pdfFromUri?.let()
            { pdfFromUri ->

                var pdfUrl      = pdfFromUri.toString() // 用 pdfFromUri.path 會分開網域跟路徑
                var pdfFileName = getCustomFileName(pdfUrl)
                var uri         = DownloadUtility.getInstance().findDownloadFile(this, DownloadInfo.Dir_Name_PDF, pdfFileName)  // 檢查是否已經下載過該檔案，若有則返回檔案的本地 Uri

                SOUT.Loge(TAG, "pdfFileName: $pdfFileName")
                SOUT.Loge(TAG, "pdfFromUri: $pdfUrl")
                SOUT.Loge(TAG, "uri: $uri")

                if (uri != null)   // 如果檔案已存在，則觸發下載完成的監聽事件
                    DownloadUtility.getInstance().getDownloadlistener()?.onDownLoadFinish(uri)
                else               // 如果檔案不存在，開始下載，並提供相關參數設定
                    DownloadUtility.getInstance().startDownload(this, pdfUrl, DownloadInfo.Dir_Name_PDF, pdfFileName, false, "", getString(R.string.pdf_downloading))
            }
        }

        // 註冊下載完成的廣播接收器，用於監聽下載過程的事件
        fun registerReceiver() {
            SOUT.Loge(TAG, "registerReceiver")
            DownloadUtility.getInstance().registerReceiver(this, object: DownloadInterface {
                override fun onStart() {
                    SOUT.Loge(TAG, "1. onStart")
                }

                // 當下載完成時的回調
                override fun onDownLoadFinish(uri: Uri) {
                    SOUT.Loge(TAG, "2. onDownLoadFinish 1: " + pdfFromUri?.toString())
                    SOUT.Loge(TAG, "2. onDownLoadFinish 2: " + uri.toString())

                    var pdfFile = DownloadInfo.getFileFromUri(this@MedicineDetailActivity, uri)   // 將下載完成的 Uri 轉換為本地檔案
                    var text = PDFUtility.getText(this@MedicineDetailActivity, pdfFile)

                    // 儲存提取的文字到全域變數
                    extractedText = text// 使用 PDFUtility 從檔案中提取文字內容

                    val intent = Intent(this@MedicineDetailActivity, OpenAIActivity::class.java)
                    intent.putExtra(OpenAIActivity.key_extractedText, extractedText)  // 傳遞仿單資料
                    startActivity(intent)

                }

                override fun onError(errorCode: Int)
                {
                    SOUT.Loge(TAG, "3. onError")
                }
            })


        }


}


