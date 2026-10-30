package com.zonic.ai

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object Gemini {
    private val models = listOf("gemini-3.5-flash", "gemini-3-flash-preview", "gemini-2.5-flash")

    fun ask(key: String, prompt: String, mime: String? = null, b64: String? = null): String {
        var last = "ERR: Koi jawab nahi aaya"
        for (m in models) {
            try {
                val c = URL("https://generativelanguage.googleapis.com/v1beta/models/$m:generateContent").openConnection() as HttpURLConnection
                c.requestMethod = "POST"
                c.setRequestProperty("Content-Type", "application/json")
                c.setRequestProperty("x-goog-api-key", key)
                c.connectTimeout = 20000
                c.readTimeout = 90000
                c.doOutput = true
                val parts = JSONArray()
                if (mime != null && b64 != null) {
                    parts.put(JSONObject().put("inline_data", JSONObject().put("mime_type", mime).put("data", b64)))
                }
                parts.put(JSONObject().put("text", prompt))
                val body = JSONObject().put("contents", JSONArray().put(JSONObject().put("parts", parts)))
                c.outputStream.use { it.write(body.toString().toByteArray()) }
                val code = c.responseCode
                val stream = if (code in 200..299) c.inputStream else c.errorStream
                val txt = stream.bufferedReader().readText()
                if (code in 200..299) {
                    return JSONObject(txt).getJSONArray("candidates").getJSONObject(0)
                        .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                }
                last = "ERR: $code " + txt.take(160)
                if (code == 429 || code == 401 || code == 403) return last
            } catch (e: Exception) {
                last = "ERR: " + (e.message ?: "Network masla")
            }
        }
        return last
    }
}
