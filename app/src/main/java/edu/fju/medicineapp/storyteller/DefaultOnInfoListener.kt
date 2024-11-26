package com.skh.storyteller

import android.content.Context
import android.widget.Toast
import edu.fju.medicineapp.R
import edu.fju.medicineapp.utility.SOUT

/**
 * Description:
 *
 * Author: Shi_Kai_Lin
 *
 * Date: 2024/10/1
 */
class DefaultOnInfoListener(val context: Context): Storyteller.OnInfoListener
{
    private val TAG = DefaultOnInfoListener::class.java.simpleName.toString()

    override fun onStop(interrupted: Boolean)
    {
    }

    override fun onError(error: Int)
    {
        SOUT.Loge(TAG, "onError: $error")
        if (error == Storyteller.Error_OnInitFail)
            Toast.makeText(context, context.getString(R.string.storyteller_fail), Toast.LENGTH_SHORT).show()
    }
}