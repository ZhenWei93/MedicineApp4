package edu.fju.medicineapp.download

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import edu.fju.medicineapp.R
import edu.fju.medicineapp.utility.AppInfoUtility
import edu.fju.medicineapp.utility.DeviceUtility
import edu.fju.medicineapp.utility.SOUT
import java.io.File

/**
 * Description:
 *
 * Author: Shi_Kai_Lin
 *
 * Date: 2024/2/1
 */
class DownloadUtility
{
//==================================================================================================
// 單例
//==================================================================================================
    companion object
    {
        private val TAG: String = DownloadUtility::class.simpleName.toString()

        private var instance: DownloadUtility? = null         // 單例實例
        fun getInstance(): DownloadUtility
        {
            if (instance == null)
                instance = DownloadUtility()

            return instance as DownloadUtility
        }
    }

    private val NotificationChannelId = "download_channel"
    private val NotificationChannelName = "Download Channel"
    private var downloadId: Long = -1
    private var boShowNotification: Boolean = false

//==================================================================================================
// 廣播
//==================================================================================================
    private var downloadlistener: DownloadInterface? = null

    fun getDownloadlistener(): DownloadInterface?
    {
        return this.downloadlistener
    }

    fun registerReceiver(context: Context, downloadlistener: DownloadInterface)
    {
        try
        {
            ContextCompat.registerReceiver(context, downloadCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), ContextCompat.RECEIVER_EXPORTED)

            this.downloadlistener = downloadlistener
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private val downloadCompleteReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context?, intent: Intent?)
        {
            SOUT.Loge(TAG, "onReceive: " + intent?.action)

            if (context == null)
                return

            try
            {
                // Cancel 也會發生 DownloadManager.ACTION_DOWNLOAD_COMPLETE
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent?.action)
                {
                    if (boShowNotification)
                        showDownloadCompleteNotification(context) // 下載完成，發送通知

                    var uri = findDownloadFile(context, intent)
                    if (uri != null)
                    {
                        downloadlistener?.onDownLoadFinish(uri)
                        return
                    }

                    downloadlistener?.onError(DownloadInfo.ErrorCode_Path_Null)
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    // 記得在不使用時解除註冊廣播接收器
    fun unregisterReceiver(context: Context)
    {
        try
        {
            if (downloadlistener == null)
                return
            downloadlistener = null

            context.unregisterReceiver(downloadCompleteReceiver)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

//==================================================================================================
// 下載
//==================================================================================================
    private fun getDownloadManager(context: Context): DownloadManager?
    {
        return context.getSystemService(DOWNLOAD_SERVICE) as? DownloadManager
    }

    // 檔案 我們是設定 OpenRootDir 下
    fun startDownload(context: Context, url: String, subDownloadDirName: String?, fileName: String, boShowNotification: Boolean, notificationTitle: String, notificationDescription: String)
    {
        try
        {
            if (!DeviceUtility.isNetworkConnected(context))
            {
                downloadlistener?.onError(DownloadInfo.ErrorCode_Network)
                return
            }

            val downloadManager = getDownloadManager(context)
            if (downloadManager == null)
            {
                downloadlistener?.onError(DownloadInfo.ErrorCode_DownloadManager)
                return
            }

            cancelLastDownLoad(context)

            this.boShowNotification = boShowNotification

            var downloadUri = DownloadInfo.encodeChineseCharacters(url)
            val request = DownloadManager.Request(Uri.parse(downloadUri))

            // 下載標題和描述
            request.setTitle(notificationTitle)
            request.setDescription(notificationDescription)
            if (boShowNotification)
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)                              // 下載的可見性，VISIBILITY_VISIBLE_NOTIFY_COMPLETED 表示下載完成後顯示通知

            val destinationUri = DownloadInfo.makeOpenRootDirChildUri(context, subDownloadDirName, fileName)
            if (destinationUri == null)
            {
                downloadlistener?.onError(DownloadInfo.ErrorCode_Storeage)
                return
            }

            SOUT.Loge(TAG, "downloadUri: $downloadUri")
            SOUT.Loge(TAG, "destinationUri: $destinationUri")
            //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS , "skh\\$fileName")  // 下載目錄和文件名
            request.setDestinationUri(destinationUri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)                  // 允許蜂窩數據下載

            downloadId = downloadManager.enqueue(request)  // 將請求提交到 DownloadManager，取得下載的 ID
            SOUT.Loge(TAG, "downloadId: $downloadId")
            downloadlistener?.onStart()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun cancelLastDownLoad(context: Context)
    {
        try
        {
            if (downloadId == -1L)
                return

            SOUT.Loge(TAG, "cancelLastDownLoad: " + downloadId)

            getDownloadManager(context)?.let()
            {
                if (findDownloadFile(context, downloadId) != null) // cancelLastDownLoad
                {
                    SOUT.Loge(TAG, "cancelLastDownLoad only cancel: " + downloadId)
                    return@let
                }

                SOUT.Loge(TAG, "cancelLastDownLoad remove: " + downloadId)
                it.remove(downloadId)
            }

            downloadId = -1L
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    // 藉由 DownloadManager 廣播後，取得 Intent 透過 downloadId 至 DB查詢下載狀況 (檔案 我們是設定 OpenRootDir 下)
    fun findDownloadFile(context: Context, intent: Intent?): Uri?
    {
        val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
        if (downloadId == -1L)
            return null

        return findDownloadFile(context, downloadId)
    }

    // 透過 downloadId 至 DB查詢下載狀況 (檔案 我們是設定 OpenRootDir 下)
    private fun findDownloadFile(context: Context, downloadId: Long): Uri?
    {
        val downloadManager = getDownloadManager(context) ?: return null
        var cursor: Cursor? = null
        var downloadedUri: Uri? = null

        try
        {
            if (downloadId == -1L)
                return null

            val query = DownloadManager.Query().setFilterById(downloadId)
            cursor = downloadManager.query(query)
            if (cursor!=null && cursor.moveToFirst())
            {
                val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex))
                {
                    val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                    downloadedUri = Uri.parse(cursor.getString(uriIndex))
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        finally
        {
            cursor?.takeIf { !it.isClosed }?.close()
        }

        return downloadedUri
    }

    // 透過 subDownloadDirName 與 fileName 至 DB查詢下載狀況 (檔案 我們是設定 OpenRootDir 下)
    fun findDownloadFile(context: Context, subDownloadDirName: String?, fileName: String): Uri?
    {
        val downloadManager = getDownloadManager(context) ?: return null
        var cursor: Cursor? = null

        // 先从 DB 找
        try
        {
            val query = DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)
            cursor = downloadManager.query(query)
            while (cursor.moveToNext())
            {
                val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                val downloadedUri = Uri.parse(cursor.getString(uriIndex))

                val file = File(downloadedUri.path)
                if (file.exists() && file.name == fileName)
                {
                    if (subDownloadDirName != null)
                    {
                        val parentDirectory = file.parentFile
                        if (parentDirectory?.name == subDownloadDirName)
                        {
                            return downloadedUri
                        }
                    }
                    else
                    {
                        return downloadedUri
                    }
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        finally
        {
            cursor?.takeIf { !it.isClosed }?.close()
        }

        return null
    }

//==================================================================================================
// 通知
//==================================================================================================
    private fun showDownloadCompleteNotification(context: Context)
    {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 創建通知（Android 8.0以上需要）
        if (AppInfoUtility.hasOreo())
        {
            val channel = NotificationChannel(NotificationChannelId, NotificationChannelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // 創建通知
        val notification = NotificationCompat.Builder(context, NotificationChannelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.getString(R.string.download_complete))
            .setContentText(context.getString(R.string.your_file_has_been_downloaded))
            .setAutoCancel(true)
            .build()

        // 顯示通知
        notificationManager.notify(1, notification)
    }
}


