package edu.fju.medicineapp.data

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {
    private const val PREF_NAME = "auth"
    private const val KEY_ID = "id"
    private const val KEY_USERNAME = "username"
    private const val KEY_AGE = "age"
    private const val KEY_IDENTITY = "identity"
    private const val KEY_TOKEN = "token"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // 儲存用戶資料
    fun saveUserData(
        context: Context,
        id: String,
        username: String,
        age: Int,
        identity: String,
        token: String? = null
    ) {
        getPreferences(context).edit()
            .putString(KEY_ID, id)
            .putString(KEY_USERNAME, username)
            .putInt(KEY_AGE, age)
            .putString(KEY_IDENTITY, identity)
            .apply {
                if (token != null) putString(KEY_TOKEN, token)
            }
            .apply()
    }

    // 獲取用戶資料
    fun getUserId(context: Context): String = getPreferences(context).getString(KEY_ID, "N/A") ?: "N/A"
    fun getUsername(context: Context): String = getPreferences(context).getString(KEY_USERNAME, "N/A") ?: "N/A"
    fun getAge(context: Context): Int = getPreferences(context).getInt(KEY_AGE, 0)
    fun getIdentity(context: Context): String = getPreferences(context).getString(KEY_IDENTITY, "general") ?: "general"
    fun getToken(context: Context): String? = getPreferences(context).getString(KEY_TOKEN, null)

    // 清除所有資料（例如登出）
    fun clearUserData(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
}