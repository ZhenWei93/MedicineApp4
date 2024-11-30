//package edu.fju.medicineapp
//
//import android.content.Context
//import android.os.Bundle
//import android.view.inputmethod.InputMethodManager
//import android.widget.Button
//import android.widget.EditText
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import android.content.Intent
//import kotlinx.coroutines.*
//
//
//
//
//class OpenAIActivity: AppCompatActivity(), AIConversationInterface
//{
//
//    lateinit var inputTextField: EditText
//    lateinit var outputLabel: TextView
//    lateinit var responseButton: Button
//    lateinit var summaryButton: Button
//
//    override fun onCreate(savedInstanceState: Bundle?)
//    {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_openai)
//
//        inputTextField = findViewById<EditText>(R.id.inputTextField)
//        outputLabel = findViewById<TextView>(R.id.outputLabel)
//        responseButton = findViewById<Button>(R.id.responseButton)
//        summaryButton = findViewById<Button>(R.id.summaryButton)
//
//        val extractedText = intent.getStringExtra("仿單資料") ?: "nono仿單資料"
//
//        // 當點擊摘要按鈕時，將仿單資料傳遞給 AIConversation 並顯示摘要
////        summaryButton.setOnClickListener {
////            // 接下來的業務邏輯
////            AIConversation.getCompletion(prompt = extractedText, this, customPrompt = true) { summary ->
////                // 在回调中更新 UI
////                runOnUiThread {
////                    outputLabel.text = "摘要結果：\n$summary"
////                }
////            }
////        }
//        summaryButton.setOnClickListener {
//            // 使用協程來處理耗時的任務
//            CoroutineScope(Dispatchers.IO).launch {
//                // 異步操作，避免阻塞主執行緒
//                try{
//                    val summary = withContext(Dispatchers.IO) {
//                        AIConversation.getCompletion(
//                            prompt = extractedText,
//                            this@OpenAIActivity,
//                            customPrompt = true,
//                            callback = { response ->
//                                // 在回調中更新 UI
//
//                                    outputLabel.text = "摘要結果：\n$response"
//
//                            }
//
//                        )
//                    }
//                }catch (e: Exception) {
//                    outputLabel.text = "發生錯誤: ${e.message}"
//
//                }
//            }
//        }
//
//
//
//
////        我現在頭暈反胃！
////        我應該看哪一科的醫生？
////        我想知道 2024-11-12 消化科有沒有醫生可以預約？
//
//        responseButton.setOnClickListener()
//        {
//            val prompt = inputTextField.text.toString()
//            if (prompt.isNotEmpty())
//            {
//
//                AIConversation.getCompletion(prompt = prompt, this@OpenAIActivity, customPrompt = false)
//                { response ->
//                    runOnUiThread {
//                        outputLabel.text = response
//                    }
//                }
//            }
//
//            inputTextField.setText("")
//            closeKeyboard(this, inputTextField)
//        }
//
//        updateConversationLabel(outputLabel)
//    }
//
////    private fun queryMedicineInfo(chineseBrandName: String, genericName: String): String
////    { // 模擬查詢結果
////        return "$genericName 的中文藥名是 $chineseBrandName 。"
////    }
////
////    override fun handleFunctionCall(arguments: Map<*, *>): String
////    {
////        var result = ""
////
////        // 從參數中提取中文藥名和通用名
////        val chineseBrandName = arguments["chinese_brand_name"] as? String
////        val genericName = arguments["generic_name"] as? String
////
////        // 如果藥品名稱存在，執行查詢
////        if (chineseBrandName != null && genericName != null) {
////            result = queryMedicineInfo(chineseBrandName, genericName)
////        } else {
////            result = "請提供有效的藥品中文名稱和通用名。"
////        }
////
////        return result
////    }
//
//
//    private fun queryAppointmentByDepartmentAndDate(departmentName: String, date: String): String
//    { // 模擬查詢結果
//        return "$departmentName 的醫生在 $date 有空位。"
//    }
//
//    override fun handleFunctionCall(arguments: Map<*, *>): String
//    {
//        var result = ""
//
//        val departmentName = arguments["department_name"] as? String
//        val date = arguments["date"] as? String
//
//        if (departmentName != null && date != null)
//        {
//            result = queryAppointmentByDepartmentAndDate(departmentName, date)
//        }
//
//        return result
//    }
//
//    override fun handleContent(content: String)
//    {
//        runOnUiThread()
//        {
//            updateConversationLabel(outputLabel)
//        }
//    }
//
//    // 將格式化後的文字顯示在 UILabel 中
//    private fun updateConversationLabel(outputLabel: TextView)
//    {
//        val displayText = AIConversation.conversationHistory.joinToString("\n\n")
//        { message ->
//            when (message["role"])
//            {
//                "system" -> "系統說：${message["content"]}"
//                "user" -> "使用者說：${message["content"]}"
//                "assistant" -> "助手說：${message["content"]}"
//                else -> ""
//            }
//        }
//        outputLabel.text = displayText
//    }
//
//    // 關閉鍵盤
//    fun closeKeyboard(context: Context, editText: EditText)
//    {
//        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(editText.windowToken, 0)
//    }
//
//}
package edu.fju.medicineapp


import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.skh.storyteller.DefaultOnInfoListener
import com.skh.storyteller.Storyteller
import edu.fju.medicineapp.utility.SOUT
import edu.fju.medicineapp.utility.UIUtility.closeKeyboard


class OpenAIActivity: AppCompatActivity(), AIConversationInterface
{
    companion object
    {
        val TAG = OpenAIActivity::class.java.simpleName.toString()
        val key_extractedText = "key_extractedText"
    }
    var aiConversation = AIConversation()
    var lastPackageInserSummary = ""

    lateinit var inputTextField: EditText
    lateinit var outputLabel: TextView
    lateinit var responseButton: Button
    lateinit var summaryButton: Button
    lateinit var friendlyReadButton: Button

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_openai)

        inputTextField = findViewById<EditText>(R.id.inputTextField)
        outputLabel = findViewById<TextView>(R.id.outputLabel)
        responseButton = findViewById<Button>(R.id.responseButton)
        summaryButton = findViewById<Button>(R.id.summaryButton)
        friendlyReadButton  = findViewById<Button>(R.id.friendlyReadButton)

        var extractedText = intent.getStringExtra(key_extractedText) ?: "沒有仿單資料"
        if (extractedText.length > 2000)
            extractedText = extractedText.substring(0, 2000)

        SOUT.Loge(TAG, "extractedText:$extractedText")

        summaryButton.setOnClickListener()
        {
            // 接下來的業務邏輯
            aiConversation.getCompletion(extractedText, this)

            inputTextField.setText("")
            closeKeyboard(this, inputTextField)
        }

        responseButton.setOnClickListener()
        {
            val prompt = inputTextField.text.toString()
            if (prompt.isNotEmpty())
            {
                aiConversation.getCompletion(prompt, this)
            }

            inputTextField.setText("")
            closeKeyboard(this, inputTextField)
        }

        friendlyReadButton.setOnClickListener()
        {
            SOUT.Loge(TAG, "lastPackageInserSummary: $lastPackageInserSummary")
            if (lastPackageInserSummary.isNotEmpty())
                Storyteller.speak(this, lastPackageInserSummary, DefaultOnInfoListener(this))
        }

        updateConversationLabel(outputLabel)
    }

    override fun onPause()
    {
        super.onPause()
        Storyteller.shutdown()
    }

    private fun queryAppointmentByDepartmentAndDate(departmentName: String, date: String): String
    { // 模擬查詢結果
        return "$departmentName 的醫生在 $date 有空位。"
    }

    override fun handleFunctionCall(arguments: Map<*, *>): String
    {
        var result = ""

        val departmentName = arguments["department_name"] as? String
        val date = arguments["date"] as? String

        if (departmentName != null && date != null)
        {
            result = queryAppointmentByDepartmentAndDate(departmentName, date)
        }

        return result
    }

    override fun handleContent(content: String)
    {
        runOnUiThread()
        {
            updateConversationLabel(outputLabel)
            checkPackageInserSummary(content)
        }
    }

    // 將格式化後的文字顯示在 UILabel 中
    private fun updateConversationLabel(outputLabel: TextView)
    {
        val spannableStringBuilder = SpannableStringBuilder()

        aiConversation.conversationHistory.forEach()
        { message ->
            when (message["role"])
            {
                //"system" -> "系統說：${message["content"]}"     //嘗試隱藏
                "user" -> {
                    // "使用者說" 设置为粗体和绿色
                    val userText = SpannableString("使用者說：${message["content"]}\n\n")
                    userText.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // 加粗 "使用者說"
                    userText.setSpan(ForegroundColorSpan(Color.MAGENTA), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // 设置绿色
                    spannableStringBuilder.append(userText)
                }
                "assistant" -> {
                    // "助手說" 设置为粗体和蓝色
                    val assistantText = SpannableString("助手說：${message["content"]}\n\n")
                    assistantText.setSpan(StyleSpan(Typeface.BOLD), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // 加粗 "助手說"
                    assistantText.setSpan(ForegroundColorSpan(Color.BLUE), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // 设置蓝色
                    spannableStringBuilder.append(assistantText)
                }
                else -> {} // 如果是其他角色，則不顯示
            }
        }

        outputLabel.text = spannableStringBuilder
    }

    private fun checkPackageInserSummary(content: String)
    {
        if (content.contains(AIConversation.prefix_main_content))
            lastPackageInserSummary = content.replace("助手說：", "")
    }
}