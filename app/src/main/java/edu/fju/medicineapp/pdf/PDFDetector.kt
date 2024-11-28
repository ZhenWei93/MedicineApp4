package edu.fju.medicineapp.pdf

import android.content.Context
import edu.fju.medicineapp.utility.SOUT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class PDFDetector
{
    interface PDFDetectionListener
    {
        fun onDetectionResult(isPDF: Boolean)
    }

    companion object
    {
        const val File_Extension_PDF = ".pdf"

        private val TAG: String = PDFDetector::class.java.simpleName.toString()
        private val Is_PDF_1: String = File_Extension_PDF
        //private val Is_PDF_2: String = "https://drive.google.com/file"
        private var isPDFArray = arrayOf(Is_PDF_1)

        fun isPDF(context: Context, url: String, listener: PDFDetectionListener)
        {
            SOUT.Loge(TAG, "isPDF: " + url)

            listener.onDetectionResult(isPDF(url))

            // detectPDF(url, listener)
        }

        fun isPDF(url: String): Boolean
        {
            var urlLowerCase = url.lowercase()
            for (isPDFTag in isPDFArray)
            {
                if(urlLowerCase.contains(isPDFTag))
                    return true
            }

            return false
        }

        private fun detectPDF(url: String, listener: PDFDetectionListener?)
        {
            GlobalScope.launch(Dispatchers.IO)
            {
                val isPDF = detectPDFInternal(url)
                withContext(Dispatchers.Main)
                {
                    listener?.onDetectionResult(isPDF)
                }
            }
        }

        private fun detectPDFInternal(url: String): Boolean
        {
            // 只能判斷 Local 檔案
            // 網路上的檔案可能會有
            // 1.下載太慢
            // 2.網頁轉址最後才是PDF 等問題
            var connection: HttpURLConnection? = null
            return try
            {
                connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.connect()
                connection.inputStream.use()
                { inputStream ->
                    val buffer = ByteArray(8)
                    val bytesRead = inputStream.read(buffer)

                   var boRet: Boolean =  bytesRead >= 4 &&
                              buffer[0] == '%'.toByte() &&
                              buffer[1] == 'P'.toByte() &&
                              buffer[2] == 'D'.toByte() &&
                              buffer[3] == 'F'.toByte()

                    SOUT.Loge(TAG, "detectPDFInternal: " + String(buffer, Charsets.US_ASCII))
                    SOUT.Loge(TAG, "detectPDFInternal: " + buffer[0])
                    SOUT.Loge(TAG, "detectPDFInternal: " + buffer[1])
                    SOUT.Loge(TAG, "detectPDFInternal: " + buffer[2])
                    SOUT.Loge(TAG, "detectPDFInternal: " + buffer[3])

                    return@use boRet
                }
            }
            catch (e: IOException)
            {
                e.printStackTrace()
                false
            }
            finally
            {
                connection?.disconnect()
            }
        }
    }
}