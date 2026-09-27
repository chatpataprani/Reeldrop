package com.chatpataprani.reeldrop.ui.page.settings.network

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.material.icons.outlined.SettingsEthernet
import androidx.compose.material.icons.outlined.SignalCellular4Bar
import androidx.compose.material.icons.outlined.SignalCellularConnectedNoInternet4Bar
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.chatpataprani.reeldrop.R
import com.chatpataprani.reeldrop.ui.common.booleanState
import com.chatpataprani.reeldrop.ui.component.BackButton
import com.chatpataprani.reeldrop.ui.component.PreferenceInfo
import com.chatpataprani.reeldrop.ui.component.PreferenceItem
import com.chatpataprani.reeldrop.ui.component.PreferenceSubtitle
import com.chatpataprani.reeldrop.ui.component.PreferenceSwitch
import com.chatpataprani.reeldrop.ui.component.PreferenceSwitchWithDivider
import com.chatpataprani.reeldrop.util.ARIA2C
import com.chatpataprani.reeldrop.util.CELLULAR_DOWNLOAD
import com.chatpataprani.reeldrop.util.COOKIES
import com.chatpataprani.reeldrop.util.CUSTOM_COMMAND
import com.chatpataprani.reeldrop.util.FORCE_IPV4
import com.chatpataprani.reeldrop.util.PROXY
import com.chatpataprani.reeldrop.util.PreferenceUtil.getBoolean
import com.chatpataprani.reeldrop.util.PreferenceUtil.updateBoolean
import com.chatpataprani.reeldrop.util.PreferenceUtil.updateValue
import com.chatpataprani.reeldrop.util.RATE_LIMIT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkPreferences(navigateToCookieProfilePage: () -> Unit = {}, onNavigateBack: () -> Unit) {
    val context = LocalContext.current

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true },
        )

    var showConcurrentDownloadDialog by remember { mutableStateOf(false) }
    var showRateLimitDialog by remember { mutableStateOf(false) }
    var showProxyDialog by remember { mutableStateOf(false) }
    var aria2c by remember { mutableStateOf(ARIA2C.getBoolean()) }
    var proxy by PROXY.booleanState
    var isCookiesEnabled by COOKIES.booleanState
    var forceIpv4 by FORCE_IPV4.booleanState

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(modifier = Modifier, text = stringResource(id = R.string.network)) },
                navigationIcon = { BackButton { onNavigateBack() } },
                scrollBehavior = scrollBehavior,
            )
        },
        content = {
            val isCustomCommandEnabled by CUSTOM_COMMAND.booleanState

            LazyColumn(contentPadding = it) {
                if (isCustomCommandEnabled)
                    item {
                        PreferenceInfo(
                            text = stringResource(id = R.string.custom_command_enabled_hint)
                        )
                    }
                item { PreferenceSubtitle(text = stringResource(R.string.general_settings)) }
                item {
                    val batteryOptimized =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val powerManager = context.getSystemService(PowerManager::class.java)
                            !powerManager.isIgnoringBatteryOptimizations(context.packageName)
                        } else false

                    PreferenceItem(
                        title = stringResource(R.string.battery_configuration),
                        description =
                            if (batteryOptimized) {
                                stringResource(R.string.battery_configuration_desc)
                            } else {
                                stringResource(R.string.service_title)
                            },
                        icon = Icons.Outlined.BatterySaver,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent =
                                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = Uri.parse("package:" + context.packageName)
                                    }
                                runCatching { context.startActivity(intent) }
                            }
                        },
                    )
                }
                item {
                    var isRateLimitEnabled by remember { mutableStateOf(RATE_LIMIT.getBoolean()) }

                    PreferenceSwitchWithDivider(
                        title = stringResource(R.string.rate_limit),
                        description = stringResource(R.string.rate_limit_desc),
                        icon = Icons.Outlined.Speed,
                        enabled = !isCustomCommandEnabled,
                        isChecked = isRateLimitEnabled,
                        onChecked = {
                            isRateLimitEnabled = !isRateLimitEnabled
                            updateValue(RATE_LIMIT, isRateLimitEnabled)
                        },
                        onClick = { showRateLimitDialog = true },
                    )
                }
                item {
                    var isDownloadWithCellularEnabled by remember {
                        mutableStateOf(CELLULAR_DOWNLOAD.getBoolean())
                    }
                    PreferenceSwitch(
                        title = stringResource(R.string.download_with_cellular),
                        description = stringResource(R.string.download_with_cellular_desc),
                        icon =
                            if (isDownloadWithCellularEnabled) Icons.Outlined.SignalCellular4Bar
                            else Icons.Outlined.SignalCellularConnectedNoInternet4Bar,
                        isChecked = isDownloadWithCellularEnabled,
                        onClick = {
                            isDownloadWithCellularEnabled = !isDownloadWithCellularEnabled
                            updateValue(CELLULAR_DOWNLOAD, isDownloadWithCellularEnabled)
                        },
                    )
                }

                item { PreferenceSubtitle(text = stringResource(id = R.string.advanced_settings)) }

                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.aria2),
                        icon = Icons.Outlined.Bolt,
                        description = stringResource(R.string.aria2_desc),
                        isChecked = aria2c,
                        onClick = {
                            aria2c = !aria2c
                            updateValue(ARIA2C, aria2c)
                        },
                    )
                }
                item {
                    PreferenceSwitchWithDivider(
                        title = stringResource(id = R.string.proxy),
                        description = stringResource(id = R.string.proxy_desc),
                        icon = Icons.Outlined.VpnKey,
                        isChecked = proxy,
                        onChecked = {
                            proxy = !proxy
                            PROXY.updateBoolean(proxy)
                        },
                        onClick = { showProxyDialog = true },
                        enabled = !isCustomCommandEnabled,
                    )
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.concurrent_download),
                        description = stringResource(R.string.concurrent_download_desc),
                        icon = Icons.Outlined.OfflineBolt,
                        enabled = !aria2c && !isCustomCommandEnabled,
                    ) {
                        showConcurrentDownloadDialog = true
                    }
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.force_ipv4),
                        description = stringResource(id = R.string.force_ipv4_desc),
                        icon = Icons.Outlined.SettingsEthernet,
                        enabled = !isCustomCommandEnabled,
                        isChecked = forceIpv4,
                    ) {
                        forceIpv4 = !forceIpv4
                        FORCE_IPV4.updateBoolean(forceIpv4)
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.cookies),
                        description = stringResource(R.string.cookies_desc),
                        icon = Icons.Outlined.Cookie,
                        onClick = { navigateToCookieProfilePage() },
                    )
                }
            }
        },
    )

    if (showConcurrentDownloadDialog) {
        ConcurrentDownloadDialog { showConcurrentDownloadDialog = false }
    }

    if (showRateLimitDialog) {
        RateLimitDialog { showRateLimitDialog = false }
    }
    if (showProxyDialog) {
        ProxyConfigurationDialog { showProxyDialog = false }
    }
}
