package com.example.data.local

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class MediaSlot(val prefix: String, val defaultExt: String) {
    MAIN_BG("main_bg", "jpg"),
    CLOCK_BG("clock_bg", "jpg"),
    ADHAN_IMG("adhan_img", "jpg"),
    IQAMA_BG("iqama_bg", "jpg"),
    PHONE_OFF_IMG("phone_off", "jpg"),
    ADHKAR_BG("adhkar_bg", "jpg"),
    FAJR_AUDIO("fajr_adhan", "mp3"),
    DEFAULT_AUDIO("default_adhan", "mp3")
}

/**
 * Copies user-picked images and audio files into internal app storage (filesDir/mosque_media)
 * with explicit fsync(). Guarantees permanent access across restarts, reboots, and offline operation.
 */
class MediaStorageManager(private val context: Context) {

    private val mediaDir: File
        get() = File(context.filesDir, "mosque_media").apply {
            if (!exists()) mkdirs()
        }

    fun getPickedFileDisplayName(uri: Uri): String {
        runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) {
                            val name = cursor.getString(idx)
                            if (!name.isNullOrBlank()) return name
                        }
                    }
                }
        }
        return uri.lastPathSegment ?: "ملف صوتي مخصص"
    }

    suspend fun savePickedFile(slot: MediaSlot, sourceUri: Uri, oldPath: String?): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val resolver = context.contentResolver
                runCatching {
                    resolver.takePersistableUriPermission(sourceUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val mime = resolver.getType(sourceUri)
                val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
                    ?.takeIf { it.isNotBlank() }
                    ?: slot.defaultExt

                val destFile = File(mediaDir, "${slot.prefix}_${System.currentTimeMillis()}.$ext")
                resolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                        output.flush()
                        runCatching { output.fd.sync() }
                    }
                } ?: error("تعذر قراءة الملف المحدد")

                if (!destFile.exists() || destFile.length() == 0L) {
                    destFile.delete()
                    error("الملف المنسوخ فارغ")
                }

                if (!oldPath.isNullOrBlank() && oldPath != destFile.absolutePath) {
                    runCatching { File(oldPath).delete() }
                }

                destFile.absolutePath
            }
        }

    suspend fun clearFile(path: String?) = withContext(Dispatchers.IO) {
        if (!path.isNullOrBlank()) {
            runCatching { File(path).delete() }
        }
    }
}
