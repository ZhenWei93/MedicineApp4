package edu.fju.medicineapp.utility

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import androidx.core.content.FileProvider
import java.io.File

/**
 * Description:
 *
 * Author: Shi_Kai_Lin
 *
 * Date: 2024/5/30
 */
class FileUtility
{
    companion object
    {

//==================================================================================================
// 判斷檔案是否存在
//==================================================================================================
        fun isExist(strPath: String?): Boolean
        {
            if (TextUtils.isEmpty(strPath))
                return false

            val file = File(strPath)
            return file.exists()
        }
//==================================================================================================
// 取出副檔名(包含點)：
// 例如　.mp3
//==================================================================================================
        fun getFileExt(strFileName: String?): String
        {
            var strExt = ""

            strFileName?.let()
            { strFileName->

                val iStart = strFileName.lastIndexOf(".")
                if (iStart != -1)
                    strExt = strFileName.substring(iStart)
            }

            return strExt
        }

//==================================================================================================
// 產生資料夾(已存在則不產生)
//==================================================================================================
        fun createDir(dirPath: String?): File?
        {
            try
            {
                if (dirPath == null)
                    return null

                val dirFile = File(dirPath)
                if (!dirFile.exists())
                    dirFile.mkdir()

                return dirFile
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

            return null
        }

//==================================================================================================
// 刪除檔案或資料夾（無巢狀功能）：
// 檔案： 直接刪除
// 資料夾： 要看資料夾下面有沒有檔案; 沒有的話刪除成功; 有的話無法刪除
//==================================================================================================
        fun deleteFile(file: File?): Boolean
        {
            try
            {
                if (file != null && file.exists())
                    return file.delete()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

            return false
        }

//==================================================================================================
// 刪除檔案或資料夾（巢狀功能）：
// 檔案或資料夾都可以傳入
//==================================================================================================
        fun deleteNest(directory: File?): Boolean
        {
            directory?.let()
            {
                try
                {
                    if (directory.exists())
                    {
                        val files = directory.listFiles()
                        if (files != null)
                        {
                            for (file in files)
                            {
                                if (file.isDirectory)
                                {
                                    deleteNest(file)
                                }
                                file.delete()
                            }
                        }
                    }

                    return directory.delete()
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }

            return true
        }

//==================================================================================================
// 計算資料夾下的所有檔案大小
//==================================================================================================
        fun getDirectorySize(directory: File): Long
        {
            if (!directory.isDirectory)
                return directory.length()

            var totalSize: Long = 0
            if (directory.exists())
            {
                val files = directory.listFiles()
                if (files != null)
                {
                    for (file in files)
                    {
                        totalSize += if (file.isDirectory)
                        {
                            getDirectorySize(file)
                        }
                        else
                        {
                            file.length()
                        }
                    }
                }
            }
            return totalSize
        }

//==================================================================================================
// 分享 File Provider 允許的檔案
//==================================================================================================
        fun shareInternalFile(context: Context?, authority:String, fileUri: Uri, mimeType: String)
        {
            try
            {
                if (context == null)
                    return

                val newFile = File(fileUri.path)

                if (newFile.exists() && newFile.canRead())
                {
                    // 使用FileProvider提供內容URI
                    val contentUri = FileProvider.getUriForFile(context, authority, newFile)

                    // 創建分享Intent
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = mimeType
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    // 啟動分享Intent
                    context.startActivity(Intent.createChooser(shareIntent, "Share file"))
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }

//==================================================================================================
// 開啟 Asset裡面的檔案
//==================================================================================================
        fun getStringFromAssets(context: Context?, strFileName: String?): String
        {
            var str = ""

            NullUtility.let2(context, strFileName)
            { context, strFileName->

                try
                {
                    val assetManager = context.assets
                    val assetFile = assetManager.open(strFileName)
                    val size = assetFile.available()
                    val buffer = ByteArray(size)
                    assetFile.read(buffer)
                    assetFile.close()
                    str = String(buffer, Charsets.UTF_8)
                }
                catch (e: java.lang.Exception)
                {
                    e.printStackTrace()
                }
            }

            return str
        }

    }
}