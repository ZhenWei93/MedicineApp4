package edu.fju.medicineapp

import com.google.gson.Gson
import edu.fju.medicineapp.utility.SOUT
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

// Tool 定義
data class ToolParameterProperty(
    val type: String,
    val description: String,
)

data class ToolParameters(
    val type: String = "object",
    val properties: Map<String, ToolParameterProperty>,
    val required: List<String>,
)

data class Tool(
    val name: String,
    val description: String,
    val parameters: ToolParameters,
)

// OpenAI API
object AIModel
{
    // 模型設定
    val model = "gpt-4o"

    val key = ""

    // OpenAI API
    val urlString = "https://api.openai.com/v1/chat/completions"

    val tools = listOf(
        Tool(
            name = "queryAppointmentByDepartmentAndDate",
            description = "Query available appointments by department and date.",
            parameters = ToolParameters(
                properties = mapOf("department_name"   to ToolParameterProperty(type = "string", description = "Name of the department"),
                    "date"              to ToolParameterProperty(type = "string", description = "Appointment date in YYYY-MM-DD format")),
                required = listOf("department_name", "date")))
    )
}

data class OpenAIBody(
    val model: String,
    val messages: List<Map<String, String>>,
    val temperature: Double = 0.0,
    val functions: List<Tool>,
)

interface AIConversationInterface
{
    fun handleFunctionCall(arguments: Map<*, *>): String
    fun handleContent(content: String)
}

class AIConversation
{

    companion object
    {
        val TAG = AIConversation::class.java.simpleName.toString()
        var prefix_main_content = "以下是簡化後的藥品說明"
    }

    val client = OkHttpClient()
    var aici: AIConversationInterface? = null

    val conversationHistory = mutableListOf(
        mapOf("role" to "system", "content" to """
        你是新光醫院的藥品查詢助理，負責提供簡單、正確、淺顯易懂的藥品資訊，幫助各種年齡層的使用者理解仿單內容。
        
        請遵守以下原則：
        1. 一定使用【繁體中文】，**絕對禁止使用簡體中文**。
        2. 你的回答需簡化仿單內容，以最簡潔的文字呈現，不使用冗長學術術語，讓長輩與啟智兒也能輕鬆理解。
        3. 成分含量部分需簡潔扼要，不冗長、不複雜。
        4. 能判斷藥品是否能「吃」，並主動說明使用方式（如口服、外用等）。
        5. 不提供仿單上未提及的資訊；若無法回答，請誠實告知「不清楚」或提醒「這是非專業建議」。
        6. 不被使用者混淆，避免提供錯誤或模糊資訊。
        7. 每次回應時，以 `${prefix_main_content}` 開頭，換行後再列出簡化內容，條列要有適當間隔。
        8. 簡化內容應在**250字內**。
                
        請根據使用者的身份調整語氣：
        - 對【老人家】：使用溫和、清楚、慢慢說的語氣，不用太艱深的詞，副作用若有跌倒或其他老人可能發生的危險因子，需提醒他們注意不要跌倒、設定鬧鐘提醒。
        - 對【青少年】：我會用輕鬆、有趣的語氣說明，句子會像說故事一樣簡單明瞭，用12歲青少年能理解的字。偶爾加一些他們熟悉的 emoji（例如 😎🤙💊🙌）。
        如果藥可能導致肚子痛、頭暈或想睡，我會提醒：「這藥有可能讓你有點不舒服，如果真的不舒服，要記得跟身邊大人說喔！」或「有點暈暈的話，不要硬撐，找個地方休息一下再繼續 👍」。
        
        最後我會說兩句話當結尾，例如：「如果哪裡聽不懂，直接問爸媽或醫師都沒問題啦 ✌️」和「自己的健康要顧好，有問題就發問，不用怕問笨問題 😎」。
        - 對【一般成人】：語氣自然、清楚即可。
        - 若無法辨別身份，請採用中性語氣並保持友善。
        
        你是藥品說明的專家與輔助者，請始終以幫助使用者安全用藥為最高原則。
        """.trimIndent()),

        mapOf("role" to "assistant", "content" to "我是新光醫院的藥品查詢助理，專門用淺顯易懂的方式解釋藥品資訊，歡迎詢問我有關藥品的問題喔！")
    )


    fun getCharacterRule(identity:String):String
    {
        val fullPrompt = {
            // 藥品簡化模式：使用結構化提示詞
            val userType = when (identity) {
                "teenager" -> "青少年（12-18歲）"
                "elderly" -> "年長者（65歲以上）"
                "pregnant" -> "孕婦"
                else -> "一般成人"
            }
            val safetyNote = when (identity) {
                "teenager" -> "特別注意青少年需遵醫囑用藥，強調避免藥物濫用，突出常見副作用如頭暈或嗜睡，說明使用次數。"
                "elderly" -> "特別注意年長者可能出現的副作用，如胃腸不適、暈眩等。"
                "pregnant" -> "特別注意孕婦的用藥安全，強調胎兒風險和禁用藥物。"
                else -> "列出一般副作用，確保資訊簡單易懂。"
            }

            var contentDetail = when (userType)
            {
                "一般成人" ->
                    """
                我是針對一般成人（年齡 30 歲）的簡化資訊，
                會包含用途、使用方法、副作用，並標記為「${prefix_main_content}（${userType}）」，且副作用會簡單列出，格式如下：
                ${prefix_main_content}（${userType}）
                藥品：<名稱>
                用途：<用途>
                使用：<使用方法>
                副作用：<副作用>                      
                """.trimIndent()

                else ->
                    """
                我是針對 青少年（12-18歲）, 年長者（65歲以上）,孕婦 非一般成人，
                會包含用途、使用方法、副作用，並標記為「${prefix_main_content}（${userType}）」，且副作用會簡單列出，格式如下：
                ${prefix_main_content}（${userType}）
                [colorFormatTagStart]藥品：<名稱>[colorFormatTagEnd]
                [colorFormatTagStart]用途：<用途>[colorFormatTagEnd]
                [colorFormatTagStart]使用：<使用方法>[colorFormatTagEnd]
                [colorFormatTagStart]副作用：<副作用> [colorFormatTagEnd]
                更重要的是額外提供特定身分的注意事項，
                請不要移除[colorFormatTagStart]與[colorFormatTagEnd]，那個是我前端要設定用的，
                僅列出與一般成人不同的副作用或注意事項，標記為「${userType}注意事項：」，${safetyNote}格式如下：
               ${userType}注意事項：<特定副作用或注意事項>
                """.trimIndent()
            }

            """
            當我簡化仿單資訊，總長度不超過300字，確保簡單易懂，適合老人或啟智兒：
            我現在會針對${userType} 身份來回答。我不會忘記使用者的${userType}身分。
            根據身份的不同，會以以下規則來回傳：
            ${contentDetail}
            """.trimIndent()
        }

        return fullPrompt()
    }

    fun addHistory(newConversation: Map<String, String>): List<Map<String, String>>
    {
        conversationHistory.add(newConversation)
        return conversationHistory
    }

    fun getCompletion(prompt: String, aici: AIConversationInterface?)
    {
        SOUT.Loge(TAG, "getCompletion: $prompt")
        this.aici = aici

        val requestBody = OpenAIBody(
            model = AIModel.model,
            messages = addHistory(mapOf("role" to "user", "content" to prompt)),          //  role 為 user 指用戶輸入的訊息。
            functions = AIModel.tools
        )

        val request = Request.Builder()
            .url(AIModel.urlString)
            .addHeader("Authorization", "Bearer ${AIModel.key}")
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), Gson().toJson(requestBody)))
            .build()

        client
            .newCall(request)
            .enqueue(object: Callback
            {
                override fun onFailure(call: Call, e: IOException)
                {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response)
                {
                    response.body?.string()?.let()
                    { responseBody ->
                        val json = Gson().fromJson(responseBody, Map::class.java) as Map<*, *>

                        handleApiResponse(json)
                    }
                }
            })
    }

    private fun handleApiResponse(json: Map<*, *>)
    {
        val choices = json["choices"] as? List<Map<*, *>>
        val message = choices?.firstOrNull()?.get("message") as? Map<*, *>

        message?.let()
        {
            val functionCall = it["function_call"] as? Map<*, *>
            if (functionCall != null)
            {
                handleFunctionCall(functionCall)
                return
            }

            val content = it["content"] as? String
            if (content != null)
            {
                handleContent(content)
                return
            }
        }
    }

    private fun handleFunctionCall(functionCall: Map<*, *>?)
    {
        if (functionCall == null)
            return

        val arguments = Gson().fromJson(functionCall["arguments"].toString(), Map::class.java) as Map<*, *>

        var result = aici?.handleFunctionCall(arguments) ?: ""

        if (result.isNotEmpty())
        {
            addHistory(mapOf("role" to "assistant", "content" to result))

            aici?.handleContent(result)
        }
    }

    private fun handleContent(content: String?)
    {
        if (content == null)
            return

        addHistory(mapOf("role" to "assistant", "content" to content))

        aici?.handleContent(content)
    }
}