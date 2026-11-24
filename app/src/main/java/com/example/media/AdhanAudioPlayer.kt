package com.example.media

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.R
import com.example.data.model.PrayerName
import java.io.File

enum class BuiltinAdhanTone(val key: String, val arabicName: String) {
    BUILTIN_FAJR("builtin_fajr", "نغمة أذان الفجر المدمجة"),
    BUILTIN_DEFAULT("builtin_default", "نغمة الأذان المدمجة (القياسية)")
}

class AdhanAudioPlayer(private val context: Context) {

    private var player: MediaPlayer? = null

    fun playAdhan(
        prayer: PrayerName,
        fajrCustomPath: String?,
        defaultCustomPath: String?,
        fajrToneKey: String = BuiltinAdhanTone.BUILTIN_FAJR.key,
        defaultToneKey: String = BuiltinAdhanTone.BUILTIN_DEFAULT.key,
        onCompleted: () -> Unit = {}
    ) {
        stop()
        val customPath = if (prayer == PrayerName.FAJR) fajrCustomPath else defaultCustomPath
        val customFile = customPath?.let { File(it) }?.takeIf { it.exists() && it.canRead() && it.length() > 0L }
        val toneKey = if (prayer == PrayerName.FAJR) fajrToneKey else defaultToneKey

        try {
            val mp = if (customFile != null) {
                MediaPlayer().apply {
                    setAudioAttributes(alarmAttributes())
                    setDataSource(customFile.absolutePath)
                    prepare()
                }
            } else {
                val rawRes = when {
                    toneKey == BuiltinAdhanTone.BUILTIN_FAJR.key -> R.raw.adhan_fajr_default
                    toneKey == BuiltinAdhanTone.BUILTIN_DEFAULT.key -> R.raw.adhan_default
                    prayer == PrayerName.FAJR -> R.raw.adhan_fajr_default
                    else -> R.raw.adhan_default
                }
                MediaPlayer.create(context, rawRes)?.apply {
                    setAudioAttributes(alarmAttributes())
                }
            }

            attachAndStart(mp, onCompleted)
        } catch (e: Exception) {
            Log.e("AdhanAudioPlayer", "Failed to play adhan audio", e)
            stop()
            onCompleted()
        }
    }

    /**
     * Allows user to preview/listen to a picked Uri BEFORE committing it to permanent storage.
     */
    fun playPreviewUri(uri: Uri, onCompleted: () -> Unit = {}) {
        stop()
        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(alarmAttributes())
                setDataSource(context, uri)
                prepare()
            }
            attachAndStart(mp, onCompleted)
        } catch (e: Exception) {
            Log.e("AdhanAudioPlayer", "Failed to preview picked audio uri", e)
            stop()
            onCompleted()
        }
    }

    fun playBuiltinTone(toneKey: String, onCompleted: () -> Unit = {}) {
        stop()
        val rawRes = if (toneKey == BuiltinAdhanTone.BUILTIN_FAJR.key) {
            R.raw.adhan_fajr_default
        } else {
            R.raw.adhan_default
        }
        try {
            val mp = MediaPlayer.create(context, rawRes)?.apply {
                setAudioAttributes(alarmAttributes())
            }
            attachAndStart(mp, onCompleted)
        } catch (e: Exception) {
            Log.e("AdhanAudioPlayer", "Failed to play builtin tone", e)
            stop()
            onCompleted()
        }
    }

    private fun attachAndStart(mp: MediaPlayer?, onCompleted: () -> Unit) {
        if (mp == null) {
            onCompleted()
            return
        }
        mp.setOnCompletionListener {
            stop()
            onCompleted()
        }
        mp.setOnErrorListener { _, _, _ ->
            stop()
            onCompleted()
            true
        }
        mp.start()
        player = mp
    }

    private fun alarmAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

    fun isPlaying(): Boolean = runCatching { player?.isPlaying == true }.getOrDefault(false)

    fun stop() {
        runCatching {
            player?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        }
        player = null
    }
}
