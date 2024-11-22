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
//                            val pdfUrl = medicine.packageInsertPath
//                            if (!pdfUrl.isNullOrEmpty()) {
//                                Log.i(TAG, "成功取得藥品仿單: $pdfUrl")
//                                // 創建 Intent 啟動 PdfViewerActivity，並傳遞 PDF URL
//                                val intent = Intent(this, PdfViewerActivity::class.java)
//                                intent.putExtra("PDF_URL", pdfUrl)
//                                startActivity(intent) // 啟動 PdfViewerActivity
//                            } else {
//                                Log.e(TAG, "藥品仿單 URL 無效")
//                                Toast.makeText(this, "藥品仿單無法顯示", Toast.LENGTH_SHORT).show()
//                            }
                            registerReceiver()
                            startDownload()
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

        var pdf = "https://www.skh.org.tw/Pharmacy_img/202403120951121MLD01.pdf"
        var pdfFromUri = Uri.parse(pdf)
        fun getCustomFileName(pdfUrl: String): String
        {
            return EncryptUtility.encodeMd5(pdfUrl) + PDFDetector.File_Extension_PDF
        }
        fun startDownload()
        {
            pdfFromUri?.let()
            { pdfFromUri ->

                var pdfUrl      = pdfFromUri.toString() // 用 pdfFromUri.path 會分開網域跟路徑
                var pdfFileName = getCustomFileName(pdfUrl)
                var uri         = DownloadUtility.getInstance().findDownloadFile(this, DownloadInfo.Dir_Name_PDF, pdfFileName)

                SOUT.Loge(TAG, "pdfFileName: $pdfFileName")
                SOUT.Loge(TAG, "pdfFromUri: $pdfUrl")
                SOUT.Loge(TAG, "uri: $uri")

                if (uri != null)
                    DownloadUtility.getInstance().getDownloadlistener()?.onDownLoadFinish(uri)
                else
                    DownloadUtility.getInstance().startDownload(this, pdfUrl, DownloadInfo.Dir_Name_PDF, pdfFileName, false, "", getString(R.string.pdf_downloading))
            }
        }

        fun registerReceiver()
        {
            SOUT.Loge(TAG, "registerReceiver")
            DownloadUtility.getInstance().registerReceiver(this, object: DownloadInterface
            {
                override fun onStart()
                {
                    SOUT.Loge(TAG, "1. onStart")
                }

                override fun onDownLoadFinish(uri: Uri)
                {
                    SOUT.Loge(TAG, "2. onDownLoadFinish 1: " + pdfFromUri?.toString())
                    SOUT.Loge(TAG, "2. onDownLoadFinish 2: " + uri.toString())

                    var pdfFile = DownloadInfo.getFileFromUri(this@MedicineDetailActivity, uri)
                    var text = PDFUtility.getText(this@MedicineDetailActivity, pdfFile)

                }

                override fun onError(errorCode: Int)
                {
                    SOUT.Loge(TAG, "3. onError")
                }
            })
        }

}

//// PdfViewerActivity 用於顯示 PDF 仿單
//class PdfViewerActivity : AppCompatActivity() {
//    val TAG = PdfViewerActivity::class.java.simpleName.toString()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_pdf_viewer)
//
//        // WebView 元件用於顯示網頁內容
//        val webView: WebView = findViewById(R.id.webView)
//        val pdfUrl = intent.getStringExtra("PDF_URL")
//
//        if (pdfUrl != null) {
//            Log.i(TAG, "Opening PDF: $pdfUrl")
//            webView.settings.javaScriptEnabled = true
//            webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK   // 設定 WebView 的快取模式為 LOAD_CACHE_ELSE_NETWORK，如果網路不可用，則嘗試從快取加載內容
//            webView.webViewClient = WebViewClient()   // 指定 WebView 的客戶端，以避免跳轉到其他瀏覽器
//            // 使用 Google Docs 的線上顯示功能來顯示 PDF
//            webView.loadUrl("https://docs.google.com/viewer?url=$pdfUrl")
//        } else {
//            Log.e(TAG, "PDF URL is null")
//        }
//    }
//}
