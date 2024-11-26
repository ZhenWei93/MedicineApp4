package edu.fju.medicineapp

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.skh.storyteller.DefaultOnInfoListener
import com.skh.storyteller.Storyteller
import edu.fju.medicineapp.utility.SOUT


class OpenAIActivity: AppCompatActivity(), AIConversationInterface
{
    companion object
    {
        val TAG = OpenAIActivity::class.java.simpleName.toString()
        val key_extractedText = "key_extractedText"
    }
    var aiConversation = AIConversation()
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

        var extractedText = intent.getStringExtra(key_extractedText) ?: "nono仿單資料"
        if (extractedText.length > 2000)
            extractedText = extractedText.substring(0, 2000)

        SOUT.Loge(TAG, "extractedText:$extractedText")

        summaryButton.setOnClickListener()
        {
            // 接下來的業務邏輯
            aiConversation.getCompletion(extractedText, this)
        }

//        我現在頭暈反胃！
//        我應該看哪一科的醫生？
//        我想知道 2024-11-12 消化科有沒有醫生可以預約？

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

        updateConversationLabel(outputLabel)

        friendlyReadButton.setOnClickListener()
        {
            SOUT.Loge(TAG, "lastPackageInserSummary: $key_extractedText")
            if (key_extractedText.isNotEmpty())
                Storyteller.speak(this, key_extractedText, DefaultOnInfoListener(this))
        }

        updateConversationLabel(outputLabel)
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
        }
    }

    // 將格式化後的文字顯示在 UILabel 中
    private fun updateConversationLabel(outputLabel: TextView)
    {
        val displayText = aiConversation.conversationHistory.joinToString("\n\n")
        { message ->
            when (message["role"])
            {
                "system" -> "系統說：${message["content"]}"
                "user" -> "使用者說：${message["content"]}"
                "assistant" -> "助手說：${message["content"]}"
                else -> ""
            }
        }
        outputLabel.text = displayText
    }

    // 關閉鍵盤
    fun closeKeyboard(context: Context, editText: EditText)
    {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

}