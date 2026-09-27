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

        private val PUBLIC_COBALT_APIS = listOf(
            "https://co.wuk.sh",
            "https://co.tskau.team",
            "https://cobalt-api.hyper.lol"
        )
    }

    fun resolve(sourceUrl: String): ResolveResult {
        val errors = mutableListOf<String>()

        if (primaryBaseUrl.isNotBlank()) {
            runCatching { resolveCobalt(primaryBaseUrl.trimEnd('/'), sourceUrl) }
                .onSuccess { if (it.url != null) return it else errors += (it.error ?: "Primary resolver failed") }
                .onFailure { errors += (it.message ?: "Primary resolver failed") }
        }

        for (api in PUBLIC_COBALT_APIS) {
            runCatching { resolveCobalt(api, sourceUrl) }
                .onSuccess { if (it.url != null) return it else errors += "$api: ${it.error ?: "no media URL"}" }
                .onFailure { errors += "$api: ${it.message ?: "request failed"}" }

            runCatching { resolveCobaltLegacy(api, sourceUrl) }
                .onSuccess { if (it.url != null) return it else errors += "$api/api/json: ${it.error ?: "no media URL"}" }
                .onFailure { errors += "$api/api/json: ${it.message ?: "request failed"}" }
        }

        runCatching { resolveBackup(sourceUrl) }
            .onSuccess { if (it.url != null) return it else errors += (it.error ?: "Backup resolver failed") }
            .onFailure { errors += (it.message ?: "Backup resolver failed") }

        return ResolveResult(null, null, errors.joinToString(" | "))
    }

    private fun resolveCobalt(baseUrl: String, sourceUrl: String): ResolveResult {
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

    private fun resolveCobaltLegacy(baseUrl: String, sourceUrl: String): ResolveResult {
        val connection = (URL(baseUrl.trimEnd('/') + "/api/json").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10000
            readTimeout = 25000
            doOutput = true
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("User-Agent", "Reeldrop/0.2 Android")
        }

        val request = JSONObject()
            .put("url", sourceUrl)
            .put("vQuality", "1080")
            .put("aFormat", "mp3")
            .put("filenamePattern", "basic")

        connection.outputStream.use { it.write(request.toString().toByteArray(Charsets.UTF_8)) }
        val response = readResponse(connection)
        if (response.isBlank()) return ResolveResult(null, null, "empty response (${connection.responseCode})")

        val json = runCatching { JSONObject(response) }
            .getOrElse { return ResolveResult(null, null, "invalid JSON") }

        if (json.optString("status") == "error") {
            return ResolveResult(null, null, json.optString("text").ifBlank { "legacy resolver error" })
        }

        val direct = json.optString("url").trim()
        if (direct.startsWith("http://") || direct.startsWith("https://")) {
            return ResolveResult(direct, json.optString("filename").ifBlank { null })
        }

        val picker = json.optJSONArray("picker")
        if (picker != null) {
            for (i in 0 until picker.length()) {
                val item = picker.optJSONObject(i) ?: continue
                val itemUrl = item.optString("url").trim()
                if (itemUrl.startsWith("http://") || itemUrl.startsWith("https://")) {
                    return ResolveResult(itemUrl, item.optString("filename").ifBlank { null })
                }
            }
        }

        return ResolveResult(null, null, "unsupported legacy response")
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
