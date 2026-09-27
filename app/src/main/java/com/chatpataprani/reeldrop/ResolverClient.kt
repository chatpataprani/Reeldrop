package com.chatpataprani.reeldrop

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ResolveResult(val url: String?, val filename: String?, val error: String? = null)

class ResolverClient(private val baseUrl: String) {
    fun resolve(sourceUrl: String): ResolveResult {
        require(baseUrl.isNotBlank()) {
            "Resolver is not configured. Set RESOLVER_BASE_URL in app/build.gradle.kts."
        }

        val connection = (URL(baseUrl.trimEnd('/') + "/").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 30000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
        }

        val request = JSONObject()
            .put("url", sourceUrl)
            .put("videoQuality", "1080")
            .put("downloadMode", "auto")
            .put("filenameStyle", "basic")

        connection.outputStream.use { it.write(request.toString().toByteArray()) }

        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        val response = stream?.bufferedReader()?.use { it.readText() } ?: ""
        val json = JSONObject(response)

        if (json.optString("status") == "error") {
            return ResolveResult(null, null, json.optJSONObject("error")?.optString("code") ?: "Resolver error")
        }

        return when (json.optString("status")) {
            "redirect", "tunnel" -> ResolveResult(
                json.optString("url").ifBlank { null },
                json.optString("filename").ifBlank { null }
            )
            else -> ResolveResult(null, null, "Unsupported resolver response")
        }
    }
}
