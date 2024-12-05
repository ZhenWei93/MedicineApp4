package edu.fju.medicineapp.utility

import android.content.Context
import android.util.Log
import android.widget.Toast
import edu.fju.medicineapp.BuildConfig

object SOUT
{
    fun Loge(TAG: String, message: String)
    {
        if (!BuildConfig.DEBUG)
            return

        Log.e(TAG, message)
    }

    fun bigPrint(tag: String, str: String)
    {
        if (!BuildConfig.DEBUG)
            return

        try
        {
            val iBatchPrintMaxLength = 1024
            if (str.length > iBatchPrintMaxLength)
            {
                val iBatchPrintCount = str.length / iBatchPrintMaxLength
                val iRemnantPrint = str.length % iBatchPrintMaxLength
                Loge(tag, "iBatchPrintCount: $iBatchPrintCount")
                Loge(tag, "iRemnantPrint: $iRemnantPrint")

                var i = 0
                while (i < iBatchPrintCount)
                {
                    Loge(tag, str.substring(i * iBatchPrintMaxLength, (i + 1) * iBatchPrintMaxLength))
                    i++
                }
                if (iRemnantPrint != 0)
                    Loge(tag, str.substring(i * iBatchPrintMaxLength, str.length))
            }
            else
                Loge(tag, str)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun showToast(TAG: String, context: Context?, message: String)
    {
        if (!BuildConfig.DEBUG)
            return

        Loge(TAG, message)

        if (context == null)
            return

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}