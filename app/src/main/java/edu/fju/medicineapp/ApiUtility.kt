package edu.fju.medicineapp

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.gson.Gson

// 定義 ApiUtility 用於集中處理 API 請求
object ApiUtility
{
    private val TAG = ApiUtility::class.java.simpleName.toString()
    private const val BASE_URL = "https://prod-skmhappm.skh.org.tw:8080/services/outpatient/api"

    // 獲取藥物列表，並回傳 Medicine 物件
    fun searchMedicineLists(context: Context, medicineName: String, callback: (List<Medicines>?) -> Unit)
    {
        Log.e(TAG, "searchMedicineLists")
        try
        {
            val urlString = "$BASE_URL/getMedicineListByName/$medicineName"
            HttpUtility.doGet(context, urlString)
            { jsonString ->

                if (jsonString == null)
                {
                    callback(null)
                    return@doGet
                }

                val gson = Gson()
                val ResponseData = gson.fromJson(jsonString, ResponseData::class.java)
                Log.e(TAG, "searchMedicineLists: $jsonString")

                callback(ResponseData.result)
                // 回傳解析後的資料
            }
        }
        catch (e:Exception)
        {
            e.printStackTrace()
            callback(null)
        }
    }

    fun fetchMedicineDetails(context: Context, medicineCode: String, callback: (Medicine?) -> Unit)
    {
        Log.e(TAG, "fetchMedicineDetails")
        try
        {
            val urlString = "$BASE_URL/getMedicineDetailByCode/$medicineCode"
            HttpUtility.doGet(context, urlString)
            { jsonString ->

                if (jsonString == null)
                {
                    callback(null)
                    return@doGet
                }

                val gson = Gson()
                val responseData = gson.fromJson(jsonString, MedicineDetailsResponseData::class.java)

                callback(responseData.result) // 回傳解析後的資料
            }
        }
        catch (e:Exception)
        {
            e.printStackTrace()
            callback(null)
        }
    }
}






