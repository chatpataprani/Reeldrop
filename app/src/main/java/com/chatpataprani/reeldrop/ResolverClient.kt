package com.chatpataprani.reeldrop

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

data class ResolveResult(
    val url: String?,
    val filename: String?,
    val error: String? = null
)

class ResolverClient(private val primaryBaseUrl: String) {

    companion object {
        // User-provided fallback endpoint. The source URL is appended as ?url=<encoded-url>.
        private const val BACKUP_API = "https://hostmyhosting.site/api/all_dl.php?url="
    }

    fun resolve(sourceUrl: String): ResolveResult {
        val errors = mutableListOf<String>()

        if (primaryBaseUrl.isNotBlank()) {
            runCatching { resolveCobalt(sourceUrl) }
                .onSuccess { if (it.url != null) return it else errors += (it.error ?: "Primary resolver failed") }
                .onFailure { errors += (it.message ?: "Primary resolver failed") }
        }

        runCatching { resolveBackup(sourceUrl) }
            .onSuccess { if (it.url != null) return it else errors += (it.error ?: "Backup resolver failed") }
            .onFailure { errors += (it.message ?: "Backup resolver failed") }

        return ResolveResult(null, null, errors.joinToString(" | "))
    }

    private fun resolveCobalt(sourceUrl: String): ResolveResult {
        val connection = (URL(primaryBaseUrl.trimEnd('/') + "/").openConnection() as HttpURLConnection).apply {
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
        val response = readResponse(connection)
        val json = JSONObject(response)

        if (json.optString("status") == "error") {
            return ResolveResult(null, null, json.optJSONObject("error")?.optString("code") ?: "Primary resolver error")
        }

        return when (json.optString("status")) {
            "redirect", "tunnel" -> ResolveResult(
                json.optString("url").ifBlank { null },
                json.optString("filename").ifBlank { null }
            )
            else -> ResolveResult(null, null, "Unsupported primary response")
        }
    }

    private fun resolveBackup(sourceUrl: String): ResolveResult {
        val encoded = URLEncoder.encode(sourceUrl, "UTF-8")
        val endpoint = BACKUP_API + encoded
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 30000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/json, text/plain, */*")
            setRequestProperty("User-Agent", "Reeldrop/0.1 Android")
        }

        val response = readResponse(connection)
        return parseBackupResponse(response, connection.contentType)
    }

    private fun parseBackupResponse(response: String, contentType: String?): ResolveResult {
        val trimmed = response.trim()

        // Some simple PHP download APIs return the media URL directly.
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return ResolveResult(trimmed, null)
        }

        // Others return JSON. Accept common field names so the backup is not tied
        // to one exact response wrapper.
        return runCatching {
            val json = JSONObject(trimmed)
            val candidates = listOf(
                "url", "download", "download_url", "downloadUrl",
                "link", "video", "video_url", "videoUrl", "result"
            )

            var mediaUrl: String? = null
            for (key in candidates) {
                val value = json.optString(key).trim()
                if (value.startsWith("http://") || value.startsWith("https://")) {
                    mediaUrl = value
                    break
                }
            }

            if (mediaUrl == null) {
                val data = json.optJSONObject("data")
                if (data != null) {
                    for (key in candidates) {
                        val value = data.optString(key).trim()
                        if (value.startsWith("http://") || value.startsWith("https://")) {
                            mediaUrl = value
                            break
                        }
                    }
                }
            }

            ResolveResult(
                mediaUrl,
                json.optString("filename").ifBlank { json.optString("file_name").ifBlank { null } },
                if (mediaUrl == null) "Backup returned no media URL" else null
            )
        }.getOrElse {
            ResolveResult(null, null, "Backup returned an unsupported response")
        }
    }

    private fun readResponse(connection: HttpURLConnection): String {
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }
        return stream?.bufferedReader()?.use { it.readText() } ?: ""
    }
}
