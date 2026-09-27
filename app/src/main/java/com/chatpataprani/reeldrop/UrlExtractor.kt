package com.chatpataprani.reeldrop

import java.util.regex.Pattern

object UrlExtractor {
    private val pattern = Pattern.compile("(https?://[^\\s]+)", Pattern.CASE_INSENSITIVE)

    fun extract(text: String): String? {
        val m = pattern.matcher(text)
        if (!m.find()) return null
        return m.group(1)?.trimEnd('.', ',', '!', '?', ')', ']', '}', '"')
    }
}
