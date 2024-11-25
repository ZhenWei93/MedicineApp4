package edu.fju.medicineapp.pdf

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import edu.fju.medicineapp.utility.NullUtility
import edu.fju.medicineapp.utility.SOUT
import java.io.File


/**
 * Description:
 *
 * Author: Shi_Kai_Lin
 *
 * Date: 2024/11/14
 */
object PDFUtility
{
    val TAG = PDFUtility::class.java.simpleName.toString()

    fun getText(context: Context, pdfFilePath: String): String
    {
        NullUtility.tryCatch()
        {
            return getText(context, File(pdfFilePath))
        }

        return ""
    }

    fun getText(context: Context, pdfFile: File?): String
    {
        var pdfText: String = ""
        NullUtility.tryCatch()
        {
            if (pdfFile==null || !pdfFile.exists() || !pdfFile.canRead())
                return pdfText

            PDFBoxResourceLoader.init(context) // 只是設定 ASSET_MANAGER

            // 加載 PDF 文件
            val document: PDDocument = PDDocument.load(pdfFile)

            // 提取文字
            val textStripper = PDFTextStripper()
            pdfText = textStripper.getText(document)

            // 關閉文件
            document.close()
        }

        // 顯示提取的文字
        SOUT.Loge(TAG, "getText: $pdfText")

        return pdfText
    }
}