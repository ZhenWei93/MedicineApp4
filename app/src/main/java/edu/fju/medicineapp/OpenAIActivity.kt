package edu.fju.medicineapp


import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.skh.storyteller.DefaultOnInfoListener
import com.skh.storyteller.Storyteller
import edu.fju.medicineapp.data.PreferencesManager
import edu.fju.medicineapp.utility.SOUT
import edu.fju.medicineapp.utility.UIUtility.closeKeyboard
import android.Manifest
//import androidx.compose.ui.semantics.text


class OpenAIActivity: AppCompatActivity(), AIConversationInterface
{
    companion object
    {
        val TAG = OpenAIActivity::class.java.simpleName.toString()
        val key_extractedText = "key_extractedText"
    }
    var aiConversation = AIConversation()
    var lastPackageInserSummary = ""

    lateinit var userInfoTextView: TextView
    lateinit var inputTextField: EditText
    lateinit var outputLabel: TextView
    lateinit var responseButton: Button
    lateinit var summaryButton: Button
    lateinit var friendlyReadButton: Button

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_openai)

        userInfoTextView = findViewById<TextView>(R.id.UserInformation)
        inputTextField = findViewById<EditText>(R.id.inputTextField)
        outputLabel = findViewById<TextView>(R.id.outputLabel)
        responseButton = findViewById<Button>(R.id.responseButton)
        summaryButton = findViewById<Button>(R.id.summaryButton)
        friendlyReadButton  = findViewById<Button>(R.id.friendlyReadButton)

        var extractedText = intent.getStringExtra(key_extractedText) ?: "沒有仿單資料"
        if (extractedText.length > 2000)
            extractedText = extractedText.substring(0, 2000)

        SOUT.Loge(TAG, "extractedText:$extractedText")

        // 使用 PreferencesManager 獲取身份資料
        val id = PreferencesManager.getUserId(this)
        val username = PreferencesManager.getUsername(this)
        var identity = PreferencesManager.getIdentity(this)
//        identity = "child"
        SOUT.Loge(TAG, "id:$id, username:$username, identityDisplay:$identity")

        // 將英文代碼轉換成中文描述
        val identityDisplay = when (identity) {
            "teenager" -> "青少年（12-18歲）"
            "elderly" -> "年長者（65歲以上）"
            "pregnant" -> "孕婦"
            else -> "一般成人（19-64歲）"
        }

        // 設置文字內容、大小和顏色
        userInfoTextView.text = """
            【使用者】 : $username
            【身分】 : $identityDisplay
        """.trimIndent()
        userInfoTextView.textSize = 15f // 文字大小，單位為 sp
        userInfoTextView.setTextColor(Color.parseColor("#0C423F")) // 文字顏色：深青色
        userInfoTextView.setTypeface(null, Typeface.BOLD) // 可選：加粗文字
        userInfoTextView.setPadding(10, 8, 16, 8) // 可選：設置內邊距（單位：像素）

        aiConversation.addHistory(mapOf("role" to "system", "content" to aiConversation.getCharacterRule(identity)))

        aiConversation.addHistory(mapOf("role" to "system", "content" to aiConversation.getCharacterRule(identity)))

        summaryButton.setOnClickListener()
        {
            // 接下來的業務邏輯
            aiConversation.getCompletion("仿單資訊如下 : \n$extractedText", this)

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
            //確認是否開起麥克風
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
//            }
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
                "user" ->
                {
                    // "使用者說" 設置為粗體和綠色
                    val userText = SpannableString("使用者說：${message["content"]}\n\n")
                    userText.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // 加粗 "使用者說"
                    userText.setSpan(ForegroundColorSpan(Color.MAGENTA), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // 設置綠色
                    spannableStringBuilder.append(userText)
                }

                "assistantold" ->
                {
                    // "助手說" 設置為粗體和藍色
                    val assistantText = SpannableString("助手說：${message["content"]}\n\n")
                    assistantText.setSpan(StyleSpan(Typeface.BOLD), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // 加粗 "助手說"
                    assistantText.setSpan(ForegroundColorSpan(Color.BLUE), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // 設置藍色
                    spannableStringBuilder.append(assistantText)
                }
                "assistant" ->
                {
                    val assistantText = SpannableStringBuilder("助手說：\n")
                    // 設置 "助手說" 為加粗且藍色
                    assistantText.setSpan(StyleSpan(Typeface.BOLD), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    assistantText.setSpan(ForegroundColorSpan(Color.BLUE), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    // 正規表達式抓出 [colorFormatTagStart] 和 [colorFormatTagEnd] 包裹的文字
                    val regex = "\\[colorFormatTagStart](.+?)\\[colorFormatTagEnd]".toRegex()

                    var lastIndex = 0
                    val rawContent = message["content"].toString()
                    regex.findAll(rawContent).forEach()
                    { matchResult ->
                        // 加入中間非標記文字（如果有）
                        if (matchResult.range.first > lastIndex)
                        {
                            assistantText.append(rawContent.substring(lastIndex, matchResult.range.first))
                        }

                        // 被包住的文字內容
                        val content = matchResult.groupValues[1]
                        val coloredSpan = SpannableString(content)
                        val color = getColor(R.color.dark_gray)
                        coloredSpan.setSpan(ForegroundColorSpan(color), 0, content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        assistantText.append(coloredSpan)

                        lastIndex = matchResult.range.last + 1
                    }

                    // 加入剩餘未處理的文字
                    if (lastIndex < rawContent.length)
                    {
                        assistantText.append(rawContent.substring(lastIndex))
                    }
                    assistantText.append("\n\n")
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