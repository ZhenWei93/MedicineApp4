package edu.fju.medicineapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import edu.fju.medicineapp.utility.SOUT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

// =================================================================================================
// Tool 定義
// =================================================================================================
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
    val name: String,                   //  工具的名稱。這是唯一標識工具的關鍵字，用來讓模型調用該工具。每個工具應該有一個唯一的名稱。
    val description: String,            //  工具的描述，解釋該工具的功能和用途。這有助於模型理解在何種情況下應該使用這個工具，並且能夠提供精確的結果。
    val parameters: ToolParameters,     //  定義工具所需的參數。
)

// =================================================================================================
// OpenAI API
// =================================================================================================
object AIModel
{
    // 模型設定
    val model = "gpt-3.5-turbo"

    /*
     1. 前往 OpenAI 官方網站 註冊一個帳號。
     2. 登入後，進入設定中的「API Keys」管理頁面，點擊「Create new secret key」以生成一組 API Key。
     3.目前採用資訊長提供的
    */
    val key = "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"

    // OpenAI API
    val urlString = "https://api.openai.com/v1/chat/completions"

    //  Tool 是用來描述應用程式可以執行的API，讓模型知道有哪些 API 可以調用 以及 調用這些 API 需要哪些參數。
    //  這些工具允許模型在回應用戶問題時，根據需求自動決定是否呼叫工具。
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
    val messages: List<Map<String, String>>,        //  role : content, role：用來說明該訊息是來自於誰, content ：每條訊息的實際內容
    val temperature: Double = 0.0,                  //  模型輸出的隨機性
    val functions: List<Tool>,                      //  Tools 是一個包含函數描述的陣列
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
        var key_extractedText = "以下是簡化後的藥品說明"
    }

    val client = OkHttpClient()
    var aici: AIConversationInterface? = null

    //  把整個對話歷程提供給模型，模型才能夠參考前後文
    //  role 為 system 指設定對話的背景或規則。
    // 我們在這邊已經先編織一個 時空背景 叫做  "我是新光醫院的助手，如果需要掛號服務，請跟我說！"
    // 這樣之後對話 OpenAI 就會以為自己是這樣一個角色
    val conversationHistory = mutableListOf( mapOf("role" to "system",
        "content" to "我是新光醫院的助手，如果需要掛號服務，請跟我說！") ,
        mapOf("role" to "system",
            "content" to     "我是理解藥品資訊的專家，我擅長簡化仿單資訊，用最精簡的文字回傳內容，"),
        mapOf("role" to "system",
            "content" to     "而且我擅長面對老人家，不用太複雜的文字或學術用語，我會講很簡單，而且我會講到啟智兒都聽得懂，"),
        mapOf("role" to "system",
            "content" to     "成份含量部分應要更簡潔，不要太臭長。"),
        mapOf("role" to "system",
            "content" to     "每當我簡化完藥品資訊 我會用 ${key_extractedText} 做開頭，然後換行，然後才輸出我簡化的東西。"),
        mapOf("role" to "system",
            "content" to     "項目排列應有適當間隔。"),
        mapOf("role" to "system",
            "content" to     "簡化後的文字最多300字。"),
        mapOf("role" to "assistant",
            "content" to "我是理解藥品資訊的專家，我擅長簡化仿單資訊，提供淺顯易懂的資訊"))

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


