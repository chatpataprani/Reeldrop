package com.chatpataprani.reeldrop.util

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object InstagramProfileAnalyzer {
    sealed class Result {
        data class Profile(val data: InstagramProfile) : Result()
        data object PrivateAccount : Result()
        data object NotFound : Result()
    }

    data class InstagramProfile(
        val username: String,
        val displayName: String?,
        val bio: String?,
        val followers: Long?,
        val following: Long?,
        val posts: Long?,
        val verified: Boolean,
        val isBusiness: Boolean,
        val profileImageUrl: String?,
    )

    private val client = OkHttpClient()

    suspend fun analyze(usernameOrUrl: String): Result = withContext(Dispatchers.IO) {
        val username = normalizeUsername(usernameOrUrl) ?: return@withContext Result.NotFound
        val url = "https://www.instagram.com/$username/"
        val userAgent =
            "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL}) AppleWebKit/537.36 Chrome/140.0 Mobile Safari/537.36"

        runCatching {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Accept", "text/html,application/xhtml+xml")
                .header("Referer", "https://www.instagram.com/")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.code == 404) return@use Result.NotFound
                val body = response.body.string()

                if (
                    Regex("\"is_private\"\\s*:\\s*true", RegexOption.IGNORE_CASE).containsMatchIn(body) ||
                    body.contains("This Account is Private", ignoreCase = true)
                ) return@use Result.PrivateAccount

                if (response.code in 400..499) return@use Result.NotFound

                val profile = InstagramProfile(
                    username = username,
                    displayName = extractMeta(body, "og:title")
                        ?.substringBefore(" (@")
                        ?.takeIf { it.isNotBlank() },
                    bio = extractMeta(body, "description"),
                    followers = extractCount(body, "edge_followed_by") ?: extractCount(body, "followers"),
                    following = extractCount(body, "edge_follow"),
                    posts = extractCount(body, "edge_owner_to_timeline_media") ?: extractCount(body, "posts"),
                    verified = Regex("\"is_verified\"\\s*:\\s*true", RegexOption.IGNORE_CASE).containsMatchIn(body),
                    isBusiness = Regex("\"is_business_account\"\\s*:\\s*true", RegexOption.IGNORE_CASE).containsMatchIn(body),
                    profileImageUrl = extractMeta(body, "og:image"),
                )

                if (
                    profile.displayName == null &&
                    profile.followers == null &&
                    profile.posts == null &&
                    !body.contains(username, ignoreCase = true)
                ) Result.NotFound else Result.Profile(profile)
            }
        }.getOrElse { Result.NotFound }
    }

    private fun normalizeUsername(input: String): String? {
        val value = input.trim().removeSuffix("/")
        val username = when {
            value.startsWith("@") -> value.drop(1)
            value.contains("instagram.com/", ignoreCase = true) ->
                value.substringAfter("instagram.com/", "").substringBefore("/")
            else -> value
        }.trim()
        return username.takeIf { Regex("[A-Za-z0-9._]{1,30}").matches(it) }
    }

    private fun extractMeta(html: String, property: String): String? =
        Regex(
            """<meta[^>]+(?:property|name)=["']$property["'][^>]+content=["']([^"']*)["']""",
            RegexOption.IGNORE_CASE,
        ).find(html)?.groupValues?.getOrNull(1)?.htmlDecode()

    private fun extractCount(html: String, key: String): Long? {
        val primary = Regex(
            """"$key"\s*:\s*\{\s*"count"\s*:\s*(\d+)""",
            RegexOption.IGNORE_CASE,
        ).find(html)?.groupValues?.getOrNull(1)?.toLongOrNull()
        return primary ?: Regex(""""$key"\s*:\s*(\d+)""")
            .find(html)?.groupValues?.getOrNull(1)?.toLongOrNull()
    }

    private fun String.htmlDecode(): String =
        replace("&quot;", "\"")
            .replace("&#x27;", "'")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
}
