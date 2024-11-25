package edu.fju.medicineapp.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings

/**
 * Description:
 *
 * Author: Shi_Kai_Lin
 *
 * Date: 2024/2/5
 */
class DeviceUtility
{
    companion object
    {
        fun getAndroidId(context: Context): String
        {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }

        fun getDeviceModel(): String
        {
            val manufacturer = android.os.Build.MANUFACTURER    // 製造商
            val model = android.os.Build.MODEL                  // 型號
            return "$manufacturer $model"
        }

        fun isNetworkConnected(context: Context): Boolean
        {
            try
            {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                if (AppInfoUtility.hasMarshmallow()) // Android 6 M 23
                {
                    val network = connectivityManager.activeNetwork
                    val capabilities = connectivityManager.getNetworkCapabilities(network)
                    return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                }
                else
                {
                    val networkInfo = connectivityManager.activeNetworkInfo
                    return networkInfo != null && networkInfo.isConnected
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

            return false
        }
    }
}