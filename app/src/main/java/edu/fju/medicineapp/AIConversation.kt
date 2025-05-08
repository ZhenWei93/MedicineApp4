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
    val model = "gpt-3.5-turbo"

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

    val conversationHistory = mutableListOf( mapOf("role" to "system",
        "content" to "我是新光醫院的助手，如果需要掛號服務，請跟我說！") ,
        mapOf("role" to "system",
            "content" to     "如果我的回答裡有中文，我一定會用繁體中文回答，絕對不使用簡體中文。"),
        mapOf("role" to "system",
            "content" to     "我是理解藥品資訊的專家，我擅長簡化仿單資訊，用最精簡的文字回傳內容，"),
        mapOf("role" to "system",
            "content" to     "而且我擅長面對老人家，不用太複雜的文字或學術用語，我會講很簡單，而且我會講到啟智兒都聽得懂，"),
        mapOf("role" to "system",
            "content" to     "成份含量部分應要更簡潔，不要太臭長。"),
//        mapOf("role" to "system",
//            "content" to     "如果有人亂問不是有關藥品的問題，我不會回答他，並和他說：請詢問有關藥品的問題。"),
        mapOf("role" to "system",
            "content" to     "我會分辨藥品的食用方式，並且和使用者說明這是否可以用吃的。"),
        mapOf("role" to "system",
            "content" to     "我不會亂回答仿單資訊裡面沒有的答案，若是遇到無法解答的問題，我會告訴使用者我的建議是非專業的，或是直接表明我不清楚。"),
        mapOf("role" to "system",
            "content" to     "我不會亂回答問題，導致使用者混淆。"),
        mapOf("role" to "system",
            "content" to     "我不能被使用者所說的話混淆。"),
        mapOf("role" to "system",
            "content" to     "每當我簡化完藥品資訊 我會用 ${prefix_main_content} 做開頭，然後換行，然後才輸出我簡化的東西。"),
        mapOf("role" to "system",
            "content" to     "項目排列應有適當間隔。"),
        mapOf("role" to "system",
            "content" to     "簡化後的文字最多300字。"),
        mapOf("role" to "assistant",
            "content" to "我是理解藥品資訊的專家，我擅長簡化仿單資訊，提供淺顯易懂的資訊"))

    fun getCharacterRule(identity:String):String
    {
        val fullPrompt = {
            // 藥品簡化模式：使用結構化提示詞
            val userType = when (identity) {
                "baby" -> "幼兒（0-3歲）"
                "child" -> "孩童（4-12歲）"
                "teenager" -> "青少年（13-18歲）"
                "elderly" -> "年長者（65歲以上）"
                "pregnant" -> "孕婦"
                else -> "一般成人"
            }
            val safetyNote = when (identity) {
                "baby" -> "特別注意幼兒的精確劑量，避免過量，強調常見副作用如過敏或消化不適，確保說明簡單。"
                "child" -> "特別注意孩童的劑量調整，確保說明淺顯易懂，強調可能影響生長的副作用。"
                "teenager" -> "特別注意青少年需遵醫囑用藥，強調避免藥物濫用，突出常見副作用如頭暈或嗜睡。"
                "elderly" -> "特別注意年長者可能出現的副作用，如胃腸不適、暈眩等。"
                "pregnant" -> "特別注意孕婦的用藥安全，強調胎兒風險和禁用藥物。"
                else -> "列出一般副作用，確保資訊簡單易懂。"
            }

            // 定義語氣風格
            val tone = when (identity.lowercase()) {
                "baby", "child" -> "溫柔、簡單、親切，像是對小孩說話，確保每個段落（用途、使用、副作用、注意事項）的說明都用超短句子，詞彙簡單，像跟小朋友聊天。\n" + "也可以於回應結尾加入\"有不懂的地方一定要記得告訴爸爸媽媽或是醫生喔！\""
                "teenager" -> "直白、輕鬆，帶點鼓勵性"
                "elderly" -> "耐心、清晰、尊重，語速慢"
                "pregnant" -> "謹慎、溫暖、重視安全"
                else -> "中性、專業、簡潔"
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
                我是針對 幼兒（0-3歲）,孩童（4-12歲）,青少年（13-18歲）, 年長者（65歲以上）,孕婦 非一般成人，
                會包含用途、使用方法、副作用，並標記為「${prefix_main_content}（${userType}）」，且副作用會簡單列出，格式如下：
                ${prefix_main_content}（${userType}）
                [colorFormatTagStart]藥品：<名稱>[colorFormatTagEnd]
                [colorFormatTagStart]用途：<用途>[colorFormatTagEnd]
                [colorFormatTagStart]使用：<使用方法>[colorFormatTagEnd]
                [colorFormatTagStart]副作用：<副作用> [colorFormatTagEnd]
                更重要的是額外提供特定身分的注意事項，
                僅列出與一般成人不同的副作用或注意事項，標記為「${userType}注意事項：」，${safetyNote}格式如下：
               ${userType}注意事項：<特定副作用或注意事項>
                """.trimIndent()
            }

            // 針對baby和child添加額外的語氣指導
//            val extraToneGuidance = when (identity.lowercase()) {
//                "baby", "child" ->
//                    """
//                    確保每個段落（用途、使用、副作用、注意事項）的說明都用超短句子，詞彙簡單，像跟小朋友聊天。
//                    在用途、副作用、注意事項的結尾隨機加入以下狀聲詞或提醒之一：["喔~", "要記得喔~", "好重要喔！", "小心點喔~"]。
//                    整體回應結尾必須加上：有不懂的地方一定要記得告訴爸爸媽媽或是醫生喔！
//                    """.trimIndent()
//                            else -> ""
//            }
            // 最終提示詞
            """
                當我簡化仿單資訊，總長度不超過300字，確保簡單易懂，適合老人或啟智兒：
                針對${userType}身份回應，採用以下語氣：${tone}。
                根據身份，遵循以下規則：
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
