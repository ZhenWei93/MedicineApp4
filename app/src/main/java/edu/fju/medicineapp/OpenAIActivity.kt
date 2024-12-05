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