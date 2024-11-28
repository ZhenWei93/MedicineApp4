package edu.fju.medicineapp.download

import android.net.Uri

interface DownloadInterface
{
    fun onStart()
    fun onDownLoadFinish(uri: Uri)
    fun onError(errorCode: Int)
    fun onProgressUpdate(progress: Int){}
}