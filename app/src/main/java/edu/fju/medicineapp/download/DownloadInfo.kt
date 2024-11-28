package edu.fju.medicineapp.download

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.shockwave.pdfium.BuildConfig
import edu.fju.medicineapp.utility.AppInfoUtility
import edu.fju.medicineapp.utility.FileUtility
import edu.fju.medicineapp.utility.NullUtility
import edu.fju.medicineapp.utility.SOUT
import java.io.File
import java.net.URLEncoder

class DownloadInfo
{
    companion object
    {
        private val TAG: String = DownloadInfo::class.simpleName.toString()

        val Dir_Name_Root = "skh"                       // 理論上我們的資夾 都應該在
                                                        // 1.[看的見的App私有目錄]\skh
                                                        // 2.[看不見的App私有目錄 Files]
                                                        // 3.[看不見的App私有目錄 cache]
                                                        // 4.盡量不要放在 公開 Environment.DIRECTORY_DOWNLOADS\skh 因為解除安裝再安裝存取會有問題

        val Dir_Name_Health_Check_Report = "hcr"
        val Dir_Name_PDF = "pdf"

        const val ErrorCode_Storeage            = -1000
        const val ErrorCode_Path_Null           = -1001
        const val ErrorCode_Network             = -1002
        const val ErrorCode_DownloadManager     = -1003

        fun print(context: Context, authority: String?)
        {
            if (!BuildConfig.DEBUG)
                return

            try
            {
                var openRootDirPath     = getOpenRootDirPath(context)
                var filesRootDirPath    = getFilesRootDirPath(context)
                var cacheRootDirPath    = getCacheRootDirPath(context)
                var downloadRootDirPath = getDownloadRootDirPath()

                SOUT.Loge(TAG, "=== DownloadInfo ===")
                SOUT.Loge(TAG, "1. openRootDirPath: $openRootDirPath")
                SOUT.Loge(TAG, "2. filesRootDirPath: $filesRootDirPath")
                SOUT.Loge(TAG, "3. cacheRootDirPath: $cacheRootDirPath")
                SOUT.Loge(TAG, "4. downloadRootDirPath: $downloadRootDirPath")

                SOUT.Loge(TAG, "=== Make Test Dir ===")
                var orf = makeOpenRootDirChildUri(context, "test", "test1.txt")
                var frf = makeFilesRootDirChildUri(context, "test", "test1.txt")
                var crf = makeCacheRootDirChildUri(context, "test", "test1.txt")
                SOUT.Loge(TAG, "1. makeOpenRootDirChildFile: $orf")
                SOUT.Loge(TAG, "2. makeFilesRootDirChildFile: $frf")
                SOUT.Loge(TAG, "3. makeCacheRootDirChildFile: $crf")

                SOUT.Loge(TAG, "=== write Test file ===")
                orf?.let { File(it.path).writeText("hello: ${it}") }
                frf?.let { File(it.path).writeText("hello: ${it}") }
                crf?.let { File(it.path).writeText("hello: ${it}") }

                SOUT.Loge(TAG, "=== Find Test Dir ===")
                orf = findOpenRootDirChildFile(context, "test", "test1.txt")
                frf = findFilesRootDirChildFile(context, "test", "test1.txt")
                crf = findCacheRootDirChildFile(context, "test", "test1.txt")
                SOUT.Loge(TAG, "1. findOpenRootDirChildFile: $orf")
                SOUT.Loge(TAG, "2. findFilesRootDirChildFile: $frf")
                SOUT.Loge(TAG, "3. findCacheRootDirChildFile: $crf")

                SOUT.Loge(TAG, "=== read Test file ===")
                orf?.let {var sl = File(it.path).readLines(); if (sl.isNotEmpty()) SOUT.Loge(TAG, "1. findOpenRootDirChildFile:  ${sl.get(0)}")}
                frf?.let {var sl = File(it.path).readLines(); if (sl.isNotEmpty()) SOUT.Loge(TAG, "2. findFilesRootDirChildFile: ${sl.get(0)}")}
                crf?.let {var sl = File(it.path).readLines(); if (sl.isNotEmpty()) SOUT.Loge(TAG, "3. findCacheRootDirChildFile: ${sl.get(0)}")}

                SOUT.Loge(TAG, "=== FileProvider support ===")
                NullUtility.let2Safe(authority, orf){ authority, uri -> SOUT.Loge(TAG, "1. FileProvider: ${FileProvider.getUriForFile(context, authority, File(uri.path))}")}
                NullUtility.let2Safe(authority, frf){ authority, uri -> SOUT.Loge(TAG, "2. FileProvider: ${FileProvider.getUriForFile(context, authority, File(uri.path))}")}
                NullUtility.let2Safe(authority, crf){ authority, uri -> SOUT.Loge(TAG, "3. FileProvider: ${FileProvider.getUriForFile(context, authority, File(uri.path))}")}
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }

//==================================================================================================
// [看的見的App私有目錄]
//==================================================================================================
        fun getOpenRootDirPath(context: Context): String?
        {
            // 1.採用私有目錄 (避免如果放在Download資料夾; 解除安裝再安裝 android 11 無權限再存取之前下載過的檔案)
            // 2.一般用電腦看硬碟的方式看的到 (Android 11(含)以後手機裡的檔案管理員無權開啟)
            // 電腦找的 位置是:        本機/LSKY S23/內部儲存空間/Android/data/com.app.skh/files/skh
            // Device Explorer 位置是:/storage/emulated/0/Android/data/com.app.skh/files/skh
            // 程式輸出 位置是:        /storage/emulated/0/Android/data/com.app.skh/files/skh
            val privateDir = context.getExternalFilesDir(null) ?: return null
            var openRootDirPath = privateDir.absolutePath + File.separator + Dir_Name_Root
            return openRootDirPath
        }

//==================================================================================================
// [看不見的App私有目錄 Files]
//==================================================================================================
        fun getFilesRootDirPath(context: Context): String?
        {
            // 1.這個情況確保系統永遠吐一個內部的 files dir
            // 2.非常私有 只能透過 Android Studio Device Explorer 看
            // Device Explorer 位置是: /data/data/com.app.skh/files
            // 程式輸出 位置是:         /data/user/0/com.app.skh/files
            return context.filesDir.path
        }

//==================================================================================================
// [看不見的App私有目錄 cache]
//==================================================================================================
        fun getCacheRootDirPath(context: Context): String?
        {
            // 1.這個情況確保系統永遠吐一個內部的 cache dir
            // 2.非常私有 只能透過 Android Studio Device Explorer 看
            // Device Explorer 位置是: /data/data/com.app.skh/cache
            // 程式輸出 位置是:         /data/user/0/com.app.skh/cache
            return context.cacheDir.path
        }

        fun getCacheDirFile(context: Context, subCacheDirName: String?): File?
        {
            try
            {
                val cachePath = getCacheRootDirPath(context)

                if (subCacheDirName == null)
                    return File(cachePath)

                cachePath?.let()
                {
                    var cacheDir = File(cachePath + File.separator + subCacheDirName)
                    if (!cacheDir.exists())
                        cacheDir.mkdir()

                    return cacheDir
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

            return null
        }

        fun deleteAllCache(context: Context, subCacheDirName: String)
        {
            try
            {
                if (subCacheDirName.isEmpty() || subCacheDirName.isBlank())
                    return

                var cacheDirFile = getCacheDirFile(context, subCacheDirName)
                cacheDirFile?.let()
                { cacheDirFile->

                    if (cacheDirFile.exists())
                    {
                        val cacheFolderSize: Long = FileUtility.getDirectorySize(cacheDirFile) / (1024 * 1024) //計算檔案大小
                        SOUT.Loge(TAG, "deleteAllCache:${cacheDirFile.path}")
                        SOUT.Loge(TAG, "deleteAllCache size(M):$cacheFolderSize")

                        if (cacheFolderSize > 100) // 大於 100M 才刪除
                            FileUtility.deleteNest(cacheDirFile)
                    }
                }
            }
            catch (e: java.lang.Exception)
            {
                e.printStackTrace()
            }
        }

//==================================================================================================
// 產生 [看的見的App私有目錄] 下的 檔案(其實只是路徑 檔案還未產生)
//==================================================================================================
        fun makeOpenRootDirChildUri(context: Context, subDownloadDirName: String?, fileName: String): Uri?
        {
            return makeChildUri(FileUtility.createDir(getOpenRootDirPath(context)), subDownloadDirName, fileName)
        }
//==================================================================================================
// 產生 [看不見的App私有目錄 Files] 下的 檔案(其實只是路徑 檔案還未產生)
//==================================================================================================
        fun makeFilesRootDirChildUri(context: Context, subDownloadDirName: String?, fileName: String): Uri?
        {
            return makeChildUri(FileUtility.createDir(getFilesRootDirPath(context)), subDownloadDirName, fileName)
        }
//==================================================================================================
// 產生 [看不見的App私有目錄 cache] 下的 檔案(其實只是路徑 檔案還未產生)
//==================================================================================================
        fun makeCacheRootDirChildUri(context: Context, subDownloadDirName: String?, fileName: String): Uri?
        {
            return makeChildUri(FileUtility.createDir(getCacheRootDirPath(context)), subDownloadDirName, fileName)
        }

        private fun makeChildUri(rootDir: File?, subDownloadDirName: String?, fileName: String): Uri?
        {
            try
            {
                if (rootDir == null)
                    return null

                var downloadsDir: File = rootDir
                if (subDownloadDirName != null)
                    downloadsDir = File(rootDir, File.separator + subDownloadDirName)

                if (!downloadsDir.exists())
                    downloadsDir.mkdirs()

                var destinationFile = File(downloadsDir, fileName)

                val destinationUri = Uri.fromFile(destinationFile)
                return destinationUri
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

            return null
        }

//==================================================================================================
// 詢問 [看的見的App私有目錄] 下的 檔案 是否存在
//==================================================================================================
        fun findOpenRootDirChildFile(context: Context, subDownloadDirName: String?, fileName: String): Uri?
        {
            return findFile(context, FileUtility.createDir(getOpenRootDirPath(context)), subDownloadDirName, fileName)
        }
//==================================================================================================
// 詢問 [看不見的App私有目錄 Files] 下的 檔案 是否存在
//==================================================================================================
        fun findFilesRootDirChildFile(context: Context, subDownloadDirName: String?, fileName: String): Uri?
        {
            return findFile(context, FileUtility.createDir(getFilesRootDirPath(context)), subDownloadDirName, fileName)
        }
//==================================================================================================
// 詢問 [看不見的App私有目錄 cache] 下的 檔案 是否存在
//==================================================================================================
        fun findCacheRootDirChildFile(context: Context, subDownloadDirName: String?, fileName: String): Uri?
        {
            return findFile(context, FileUtility.createDir(getCacheRootDirPath(context)), subDownloadDirName, fileName)
        }

        private fun findFile(context: Context, rootDir: File?, subDownloadDirName: String?, fileName: String): Uri?
        {
            try
            {
                if (rootDir == null)
                    return null

                var downloadsDir: File = rootDir
                if (subDownloadDirName != null)
                    downloadsDir = File(rootDir, File.separator + subDownloadDirName)

                if (!downloadsDir.exists())
                    return null

                var destinationFile = File(downloadsDir, fileName)
                if (destinationFile.exists() && destinationFile.canRead())
                {
                    val destinationUri = Uri.fromFile(destinationFile)
                    return destinationUri
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

            return null
        }

//==================================================================================================
// [看的見的App公開目錄 download]
//建議不要用了 因為 App解除安裝再安裝 會無法存取之前下載過的檔案 (是的 很奇怪!!!)
//==================================================================================================
        private fun getDownloadRootDirPath(): String
        {
            // 最公開的路徑
            // 電腦找的 位置是:    本機\LSKY S23\內部儲存空間\Download\skh
            // 程式輸出 位置是:    Download/skh
            return "${Environment.DIRECTORY_DOWNLOADS}/${DownloadInfo.Dir_Name_Root}"
        }

        // 取得外部可見的 下載資料夾檔案路徑
        private fun makePublicRootDirChildFile(context: Context, subDownloadDirName: String?, fileName: String, mimeType: String): Uri?
        {
            if (AppInfoUtility.has10Q())
            {
                val relativePath =  subDownloadDirName?.let { getDownloadRootDirPath() + File.separator + it } ?:
                                    getDownloadRootDirPath()

                val contentValues = ContentValues().apply()
                {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                }

                return context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            }

            val downloadsRootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            var downloadsDir: File = downloadsRootDir
            if (subDownloadDirName != null)
                downloadsDir = File(downloadsRootDir, File.separator + Dir_Name_Root + File.separator + subDownloadDirName)

            if (!downloadsDir.exists())
                downloadsDir.mkdirs()

            val file = File(downloadsDir, fileName)
            return Uri.fromFile(file)
        }

        // 詢問外部可見的 下載資料夾檔案 是否存在
        private fun findPublicRootDirChildFile(context: Context, subDownloadDirName: String?, fileName: String, mimeType: String): Uri?
        {
            if (AppInfoUtility.has10Q())
            {
                val relativePath =  subDownloadDirName?.let { getDownloadRootDirPath() + File.separator + it} ?:
                                    getDownloadRootDirPath()

                val selection = "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.MIME_TYPE}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"
                val selectionArgs = arrayOf(fileName, mimeType, relativePath)
                val projection = arrayOf(MediaStore.MediaColumns._ID)
                val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

                context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use()
                { cursor ->
                    if (cursor != null && cursor.moveToFirst())
                    {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                        val uri = Uri.withAppendedPath(uri, id.toString())
                        SOUT.Loge(TAG, "findPublicDestinationFile exist 1: $uri")
                        return uri
                    }
                }
            }

            val downloadsRootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            var downloadsDir: File = downloadsRootDir
            if (subDownloadDirName != null)
                downloadsDir = File(downloadsRootDir, File.separator + Dir_Name_Root + File.separator + subDownloadDirName)

           val file = File(downloadsDir, fileName)
            var canRead = file.canRead()

            if (file.exists() && !canRead)
            {
                file.delete()
                SOUT.Loge(TAG, "findPublicDestinationFile exist delete...${file.absolutePath}")
            }

            if (file.exists() && canRead)
            {
                val uri = Uri.fromFile(file)
                SOUT.Loge(TAG, "findPublicDestinationFile exist 2: ${canRead} ${uri}")
                return uri
            }

            return null
        }

//==================================================================================================
// 其他 常用方法
//==================================================================================================
        // 轉換 外部可見的下載資料夾檔案路徑URI 成為 file
        fun getFileFromUri(context: Context, uri: Uri): File?
        {
            if (uri.scheme == "file")
            {
                return File(uri.path)
            }
            else
            {
                val filePath: String?
                context.contentResolver.query(uri, null, null, null, null)?.use()
                { cursor ->
                    cursor.moveToFirst()
                    val columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                    filePath = cursor.getString(columnIndex)
                    return File(filePath)
                }
            }
            return null
        }

        // 從 DownloadManager 給的 Cursor 讀取 Uri, 例如: content://downloads/all_downloads/1652
        fun findUriFromDownloadManager(c: Cursor):String
        {
            try
            {
                var index = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

                if (index == -1)
                    return ""

                return c.getString(index) ?: ""
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

            return ""
        }

        fun isFileExist(uri: Uri?): Boolean
        {
            val file = File(uri?.path ?: "")
            return file.exists()
        }

        fun encodePathToUri(path: String): Uri?
        {
            try
            {
                val encodedPath = URLEncoder.encode(path, "UTF-8")
                return Uri.parse(encodedPath)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

            return null
        }

        fun encodeChineseCharacters(url: String): String
        {
            val parts = url.split("/")
            val encodedParts = parts.map()
            { part ->
                try
                {
                    if (containsChineseCharacters(part))
                        URLEncoder.encode(part, "UTF-8")
                    else
                        part
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                    part
                }
            }
            return encodedParts.joinToString("/")
        }

        private fun containsChineseCharacters(input: String): Boolean
        {
            return input.any()
            {
                it.code >= 0x4e00 && it.code <= 0x9fff
            }
        }
    }
}