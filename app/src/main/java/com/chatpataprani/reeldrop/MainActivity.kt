package com.chatpataprani.reeldrop

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
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
        if (pendingUrl != null) requestNotificationAndDownload() else showDownloader()
    }

    private fun showDownloader() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }
        root.addView(TextView(this).apply {
            text = "Reeldrop"
            textSize = 30f
        })
        root.addView(TextView(this).apply {
            text = "Paste an Instagram Reel link and download it."
            textSize = 16f
            setPadding(0, 12, 0, 24)
        })
        val input = EditText(this).apply {
            hint = "https://www.instagram.com/reel/..."
            setSingleLine(true)
        }
        root.addView(input)
        root.addView(Button(this).apply {
            text = "Download Reel"
            setOnClickListener {
                val url = UrlExtractor.extract(input.text.toString().trim())
                if (url == null || !url.contains("instagram.com", true)) {
                    input.error = "Enter a valid Instagram URL"
                    return@setOnClickListener
                }
                pendingUrl = url
                requestNotificationAndDownload()
            }
        })
        setContentView(root)
    }

    private fun requestNotificationAndDownload() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startPendingDownload()
        }
    }

    private fun startPendingDownload() {
        val url = pendingUrl ?: return
        ContextCompat.startForegroundService(
            this,
            Intent(this, DownloadService::class.java).putExtra(DownloadService.EXTRA_URL, url)
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
