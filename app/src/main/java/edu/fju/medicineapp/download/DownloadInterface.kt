package edu.fju.medicineapp.download

import android.net.Uri

/**
 * Description:
 *
 * Author: Shi_Kai_Lin
 *
 * Date: 2024/2/15
 */
interface DownloadInterface
{
    fun onStart()
    fun onDownLoadFinish(uri: Uri)
    fun onError(errorCode: Int)
    fun onProgressUpdate(progress: Int){}
}