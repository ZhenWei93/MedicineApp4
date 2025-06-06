package edu.fju.medicineapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.fju.medicineapp.data.PreferencesManager
import edu.fju.medicineapp.data.api.ApiClient
import edu.fju.medicineapp.utility.UIUtility
import kotlinx.coroutines.launch

class SearchMedicineActivity : AppCompatActivity() {
    private lateinit var medicineListEditText: EditText
    private lateinit var searchMedicineButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicineAdapter
    private val apiClient = ApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        medicineListEditText = findViewById(R.id.medicineListEditText)
        searchMedicineButton = findViewById(R.id.searchMedicineButton)

        // 設置 RecyclerView 和適配器
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MedicineAdapter(listOf())
        recyclerView.adapter = adapter

        // 設置搜索按鈕的點擊事件
        searchMedicineButton.setOnClickListener {
            val medicineName = medicineListEditText.text.toString()
            if (medicineName.isNotEmpty()) {
                // 調用 ApiUtility獲取藥品列表
                ApiUtility.searchMedicineLists(this, medicineName)
                {
                        medicines ->
                    UIUtility.closeKeyboard(this@SearchMedicineActivity, searchMedicineButton)
                    runOnUiThread {
                        if (medicines != null && medicines.isNotEmpty()) {
                            // 更新 RecyclerView 顯示藥品列表
                            adapter.updateData(medicines)
                        } else {
                            Toast.makeText(this, "未找到相關藥品訊息", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "請輸入藥品名稱", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 内部類 MedicineAdapter，用於 RecyclerView 顯示藥品列表
    inner class MedicineAdapter(private var medicineList: List<Medicines>) : RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false)
            return MedicineViewHolder(view)
        }

        override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
            val medicine = medicineList[position]
            holder.genericNameTextView.text = "${medicine.genericName}"
            holder.chineseBrandNameTextView.text = "${medicine.chineseBrandName}"
            holder.englishBrandNameTextView.text = "${medicine.englishBrandName.take(50)}"

            // 設置點擊事件，允許 GUEST 用戶跳轉並嘗試儲存歷史
            holder.itemView.setOnClickListener {
                val userId = PreferencesManager.getUserId(holder.itemView.context)
                val intent = Intent(holder.itemView.context, MedicineDetailActivity::class.java)
                intent.putExtra("MEDICINE_CODE", medicine.medicationCode)

                // 檢查是否需要儲存歷史紀錄
                if (userId != "N/A" && userId != "GUEST") {
                    lifecycleScope.launch {
                        val result = apiClient.addSearchHistory(userId, medicine.chineseBrandName)
                        result.onSuccess {
                            Toast.makeText(holder.itemView.context, "已儲存搜尋歷史", Toast.LENGTH_SHORT).show()
                        }.onFailure { exception ->
                            Toast.makeText(holder.itemView.context, "儲存歷史失敗：${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                // 總是允許跳轉到 MedicineDetailActivity
                holder.itemView.context.startActivity(intent)
            }
        }

        override fun getItemCount(): Int = medicineList.size

        // 更新數據並刷新列表
        fun updateData(newData: List<Medicines>) {
            medicineList = newData
            notifyDataSetChanged()
        }

        // ViewHolder 定義
        inner class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val genericNameTextView: TextView = itemView.findViewById(R.id.genericNameTextView)
            val chineseBrandNameTextView: TextView = itemView.findViewById(R.id.chineseBrandNameTextView)
            val englishBrandNameTextView: TextView = itemView.findViewById(R.id.englishBrandNameTextView)
        }
    }
}