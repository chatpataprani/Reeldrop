package com.chatpataprani.reeldrop.database.backup

import com.chatpataprani.reeldrop.database.objects.CommandTemplate
import com.chatpataprani.reeldrop.database.objects.DownloadedVideoInfo
import com.chatpataprani.reeldrop.database.objects.OptionShortcut
import kotlinx.serialization.Serializable

@Serializable
data class Backup(
    val templates: List<CommandTemplate>? = null,
    val shortcuts: List<OptionShortcut>? = null,
    val downloadHistory: List<DownloadedVideoInfo>? = null,
)
