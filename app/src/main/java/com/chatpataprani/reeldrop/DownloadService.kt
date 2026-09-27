package com.chatpataprani.reeldrop

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class DownloadService : Service() {
    companion object {
        const val EXTRA_URL = "url"
        private const val CHANNEL = "downloads"
        private const val NOTIFICATION_ID = 1001
    }

    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, notification("Preparing...", -1, true))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val source = intent?.getStringExtra(EXTRA_URL)
        if (source.isNullOrBlank()) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        executor.execute {
            try {
                val result = ResolverClient("").resolve(source)
                val mediaUrl = result.url ?: error(result.error ?: "Could not resolve media")
                val filename = safeFilename(result.filename ?: ("reeldrop_" + System.currentTimeMillis() + ".mp4"))
                val uri = createDestination(filename)

                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { streamDownload(mediaUrl, it) }
                        ?: error("Could not open output")
                    contentResolver.update(uri, ContentValues().apply {
                        put(MediaStore.Video.Media.IS_PENDING, 0)
                    }, null, null)
                } else {
                    val file = getExternalFilesDir(null)?.resolve(filename)
                        ?: error("Storage unavailable")
                    FileOutputStream(file).use { streamDownload(mediaUrl, it) }
                }

                updateNotification("Download complete", 100, false)
            } catch (t: Throwable) {
                updateNotification("Download failed: " + (t.message ?: "unknown error"), 0, false)
            } finally {
                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf(startId)
            }
        }
        return START_NOT_STICKY
    }

    private fun streamDownload(downloadUrl: String, out: java.io.OutputStream) {
        val connection = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 30000
            instanceFollowRedirects = true
        }
        connection.connect()
        if (connection.responseCode !in 200..299) error("Media server returned " + connection.responseCode)

        val total = connection.contentLengthLong
        var done = 0L
        var lastShown = -1

        BufferedInputStream(connection.inputStream).use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                out.write(buffer, 0, count)
                done += count

                if (total > 0) {
                    val progress = ((done * 100) / total).toInt().coerceIn(0, 99)
                    if (progress != lastShown) {
                        updateNotification("Downloading... " + progress + "%", progress, true)
                        lastShown = progress
                    }
                }
            }
        }
        connection.disconnect()
    }

    private fun createDestination(filename: String): Uri? {
        if (Build.VERSION.SDK_INT < 29) return null
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, filename)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Reeldrop")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        return contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
    }

    private fun safeFilename(name: String): String =
        name.replace(Regex("[\\/:*?\"<>|]"), "_").take(120).ifBlank {
            "reeldrop_" + System.currentTimeMillis() + ".mp4"
        }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL, "Downloads", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun notification(text: String, progress: Int, ongoing: Boolean) =
        NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Reeldrop")
            .setContentText(text)
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)
            .setProgress(100, progress.coerceAtLeast(0), progress < 0)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0, Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()

    private fun updateNotification(text: String, progress: Int, ongoing: Boolean) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification(text, progress, ongoing))
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
