package com.chatpataprani.reeldrop

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
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
                    .let { if (it.contains(".")) it else "$it.mp4" }

                val temp = File.createTempFile("reeldrop_", ".part", cacheDir)
                try {
                    downloadToFile(mediaUrl, temp)
                    if (!looksLikeVideo(temp)) error("Resolver returned an invalid video file")
                    saveToMediaStore(temp, filename)
                } finally {
                    temp.delete()
                }

                updateNotification("Download complete • Movies/Reeldrop", 100, false)
            } catch (t: Throwable) {
                updateNotification("Download failed • " + (t.message ?: "unknown error"), 0, false)
            } finally {
                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf(startId)
            }
        }
        return START_NOT_STICKY
    }

    private fun downloadToFile(downloadUrl: String, file: File) {
        val connection = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 30000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Reeldrop/0.3 Android")
            setRequestProperty("Accept", "video/*,application/octet-stream,*/*")
        }

        connection.connect()
        if (connection.responseCode !in 200..299) {
            error("Media server returned " + connection.responseCode)
        }

        val contentType = connection.contentType?.lowercase() ?: ""
        if (contentType.contains("text/html") || contentType.contains("application/json")) {
            error("Resolver returned a webpage instead of a video")
        }

        val total = connection.contentLengthLong
        var done = 0L
        var lastShown = -1

        FileOutputStream(file).use { out ->
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
                            updateNotification("Downloading... $progress%", progress, true)
                            lastShown = progress
                        }
                    }
                }
            }
        }
        connection.disconnect()

        if (file.length() < 1024) error("Downloaded file is empty or too small")
    }

    private fun looksLikeVideo(file: File): Boolean {
        FileInputStreamCompat(file).use { input ->
            val header = ByteArray(12)
            val read = input.read(header)
            if (read >= 8 &&
                header[4] == 'f'.code.toByte() &&
                header[5] == 't'.code.toByte() &&
                header[6] == 'y'.code.toByte() &&
                header[7] == 'p'.code.toByte()) {
                return true
            }
        }
        return false
    }

    private fun saveToMediaStore(file: File, filename: String) {
        if (Build.VERSION.SDK_INT < 29) {
            val destination = getExternalFilesDir(null)?.resolve(filename)
                ?: error("Storage unavailable")
            file.copyTo(destination, overwrite = true)
            return
        }

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, filename)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Reeldrop")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        val uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Could not create media entry")

        try {
            contentResolver.openOutputStream(uri)?.use { output ->
                file.inputStream().use { input -> input.copyTo(output) }
            } ?: error("Could not open media output")

            contentResolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) },
                null,
                null
            )
        } catch (t: Throwable) {
            contentResolver.delete(uri, null, null)
            throw t
        }
    }

    private fun safeFilename(name: String): String =
        name.replace(Regex("[\\/:*?\"<>|]"), "_").take(120).ifBlank {
            "reeldrop_" + System.currentTimeMillis()
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

private class FileInputStreamCompat(file: File) : java.io.FileInputStream(file)
