package com.chatpataprani.reeldrop

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var pendingUrl: String? = null

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            startPendingDownload()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pendingUrl = extractSharedUrl(intent)
        if (pendingUrl == null) {
            Toast.makeText(this, "Share an Instagram URL to Reeldrop", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startPendingDownload()
        }
    }

    private fun startPendingDownload() {
        val url = pendingUrl ?: return
        ContextCompat.startForegroundService(
            this,
            Intent(this, DownloadService::class.java)
                .putExtra(DownloadService.EXTRA_URL, url)
        )
        pendingUrl = null
        finish()
    }

    private fun extractSharedUrl(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null

        val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
            ?: intent.clipData?.getItemAt(0)?.text?.toString()?.trim()

        return text?.let(UrlExtractor::extract)
    }
}
