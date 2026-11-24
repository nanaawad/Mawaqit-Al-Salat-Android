package com.example.ui.components

import androidx.compose.ui.graphics.Color
import java.io.File

object TimeFormatters {

    fun formatMinuteOfDay(minuteOfDay: Int, is24Hour: Boolean): String {
        val m = ((minuteOfDay % 1440) + 1440) % 1440
        val h24 = m / 60
        val min = m % 60
        return if (is24Hour) {
            "%02d:%02d".format(h24, min)
        } else {
            val h12 = when {
                h24 == 0 -> 12
                h24 > 12 -> h24 - 12
                else -> h24
            }
            val period = if (h24 < 12) "ص" else "م"
            "%02d:%02d %s".format(h12, min, period)
        }
    }

    fun formatClockMain(hour24: Int, minute: Int, is24Hour: Boolean): String {
        return if (is24Hour) {
            "%02d:%02d".format(hour24, minute)
        } else {
            val h12 = when {
                hour24 == 0 -> 12
                hour24 > 12 -> hour24 - 12
                else -> hour24
            }
            "%02d:%02d".format(h12, minute)
        }
    }

    fun formatClockPeriod(hour24: Int, is24Hour: Boolean): String {
        if (is24Hour) return "24H"
        return if (hour24 < 12) "صباحاً" else "مساءً"
    }

    fun formatDurationHms(totalSeconds: Long): String {
        val s = totalSeconds.coerceAtLeast(0L)
        val hours = s / 3600
        val mins = (s % 3600) / 60
        val secs = s % 60
        return "%02d:%02d:%02d".format(hours, mins, secs)
    }

    fun formatDurationMs(totalSeconds: Int): String {
        val s = totalSeconds.coerceAtLeast(0)
        val mins = s / 60
        val secs = s % 60
        return "%02d:%02d".format(mins, secs)
    }

    fun formatOffsetBadge(offsetMinutes: Int): String = when {
        offsetMinutes > 0 -> "+$offsetMinutes د"
        offsetMinutes < 0 -> "$offsetMinutes د"
        else -> "أصلي"
    }

    fun validFileOrNull(path: String?): File? {
        if (path.isNullOrBlank()) return null
        val f = File(path)
        return if (f.exists() && f.length() > 0L) f else null
    }

    fun colorFromLong(argb: Long): Color = Color(argb.toInt())

    fun formatSyncTimestamp(millis: Long?): String {
        if (millis == null) return "حساب فلكي محلي"
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
        val h24 = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val m = cal.get(java.util.Calendar.MINUTE)
        return "آخر تحديث ${formatMinuteOfDay(h24 * 60 + m, false)}"
    }
}
