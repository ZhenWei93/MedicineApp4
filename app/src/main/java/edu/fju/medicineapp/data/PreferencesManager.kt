package edu.fju.medicineapp.data

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {
    private const val PREF_NAME = "auth"
    private const val KEY_ID = "id"
    private const val KEY_USERNAME = "username"
    private const val KEY_IDENTITY = "identity"
    private const val KEY_TOKEN = "token"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }


    // 獲取用戶資料
    fun getUserId(context: Context): String = getPreferences(context).getString(KEY_ID, "N/A") ?: "N/A"
    fun getUsername(context: Context): String = getPreferences(context).getString(KEY_USERNAME, "訪客") ?: "訪客"
    fun getIdentity(context: Context): String = getPreferences(context).getString(KEY_IDENTITY, "general") ?: "general"
    fun getToken(context: Context): String? = getPreferences(context).getString(KEY_TOKEN, null)

    // 儲存用戶資料
    fun saveUserData(context: Context, id: String, username: String, identity: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString(KEY_ID, id)
            putString(KEY_USERNAME, username)
            putString(KEY_IDENTITY, identity)
            apply()
        }
    }
//    fun saveUserData(
//        context: Context,
//        id: String,
//        username: String,
//        age: Int,
//        identity: String,
//        token: String? = null
//    ) {
//        getPreferences(context).edit()
//            .putString(KEY_ID, id)
//            .putString(KEY_USERNAME, username)
//            .putInt(KEY_AGE, age)
//            .putString(KEY_IDENTITY, identity)
//            .apply {
//                if (token != null) putString(KEY_TOKEN, token)
//            }
//            .apply()
//    }


    // 清除所有資料（例如登出）
    fun clearUserData(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            clear()
            apply()
        }
    }
}