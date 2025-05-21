
package edu.fju.medicineapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.fju.medicineapp.data.LoginActivity
import edu.fju.medicineapp.data.PreferencesManager
import edu.fju.medicineapp.data.api.ApiClient
import edu.fju.medicineapp.data.model.SearchHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class ProfilePageActivity : AppCompatActivity() {

    private val apiClient = ApiClient()
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val historyList = mutableListOf<SearchHistory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)

        // 初始化 RecyclerView
        recyclerView = findViewById(R.id.recycler_view_history)
        recyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = HistoryAdapter(historyList)
        recyclerView.adapter = historyAdapter

        // 從 PreferencesManager 獲取使用者資訊
        val userId = PreferencesManager.getUserId(this) ?: ""
        val username = PreferencesManager.getUsername(this) ?: "未知使用者"
        val identity = PreferencesManager.getIdentity(this) ?: "未知身分"

        // 檢查是否登入
        if (userId == "N/A" || (username == "N/A" && identity == "general")) {
            Toast.makeText(this, "請先登入", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 將 identity 轉換為中文顯示
        val displayIdentity = when (identity) {
            "elderly" -> "年長者"
            "pregnant" -> "孕婦"
            "teenager" -> "青少年"
            else -> "一般成人"
        }

        // 設置使用者資訊到 TextView
        findViewById<TextView>(R.id.tv_username).text = "使用者名稱：$username"
        findViewById<TextView>(R.id.tv_identity).text = "身分：$displayIdentity"

        // 加載搜尋歷史
        loadSearchHistory(userId)

        // 設置登出按鈕
        findViewById<Button>(R.id.btn_logout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("確認登出")
                .setMessage("確定要登出嗎？")
                .setPositiveButton("確定") { _, _ ->
                    PreferencesManager.clearUserData(this)
                    Toast.makeText(this, "已登出", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    private fun loadSearchHistory(userId: String) {
        lifecycleScope.launch {
            try {
                val result = apiClient.getSearchHistory(userId)
                result.onSuccess { history ->
                    historyList.clear()
                    historyList.addAll(history)
                    historyAdapter.notifyDataSetChanged()
                }.onFailure { exception ->
                    Log.e("ProfilePageActivity", "Failed to load history: ${exception.message}", exception)
                    Toast.makeText(this@ProfilePageActivity, "加載歷史紀錄失敗：${exception.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ProfilePageActivity", "Error loading history: ${e.message}", e)
                Toast.makeText(this@ProfilePageActivity, "錯誤：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // RecyclerView Adapter
    inner class HistoryAdapter(private val historyList: List<SearchHistory>) :
        RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()) // 匹配後端格式
        private val displayFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault()) // 目標顯示格式

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val history = historyList[position]
            holder.text1.text = "藥品：${history.query_text}"
            // 格式化 query_time
            val formattedTime = try {
                val parsedDate = dateFormat.parse(history.query_time)
                if (parsedDate != null) {
                    displayFormat.format(parsedDate)
                } else {
                    history.query_time
                }
            } catch (e: Exception) {
                Log.e("HistoryAdapter", "Failed to parse time ${history.query_time}: ${e.message}")
                history.query_time // 如果解析失敗，使用原始格式
            }
            holder.text2.text = "時間：$formattedTime"

            // 點擊歷史紀錄，根據 query_text 查詢藥品並跳轉
            holder.itemView.setOnClickListener {
                val queryText = history.query_text
                ApiUtility.searchMedicineLists(this@ProfilePageActivity, queryText) { medicines ->
                    runOnUiThread {
                        if (medicines != null && medicines.isNotEmpty()) {
                            val medicine = medicines.firstOrNull { it.chineseBrandName == queryText }
                            if (medicine != null) {
                                val intent = Intent(holder.itemView.context, MedicineDetailActivity::class.java)
                                intent.putExtra("MEDICINE_CODE", medicine.medicationCode)
                                holder.itemView.context.startActivity(intent)
                            } else {
                                Toast.makeText(holder.itemView.context, "找不到該藥品的詳細資訊", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(holder.itemView.context, "查詢藥品失敗", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        override fun getItemCount(): Int = historyList.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val text1: TextView = itemView.findViewById(android.R.id.text1)
            val text2: TextView = itemView.findViewById(android.R.id.text2)
        }
    }
}