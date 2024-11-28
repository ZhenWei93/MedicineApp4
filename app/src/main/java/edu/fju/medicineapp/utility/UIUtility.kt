package edu.fju.medicineapp.utility

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object UIUtility
{
    // 關閉鍵盤
    fun closeKeyboard(context: Context, v: View)
    {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }
}