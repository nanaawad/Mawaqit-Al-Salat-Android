package com.example.domain.astronomy

import java.util.Calendar
import kotlin.math.floor

data class HijriDateResult(
    val day: Int,
    val month: Int, // 1..12
    val year: Int,
    val monthNameAr: String,
    val formattedAr: String
)

/**
 * Pure Kotlin Kuwaiti/Civil Islamic Hijri date converter with configurable +/- day offset.
 * Works identically on Android API 24+ and JVM Robolectric tests.
 */
object HijriDateCalculator {

    private val hijriMonthsAr = listOf(
        "محرم",
        "صفر",
        "ربيع الأول",
        "ربيع الآخر",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب",
        "شعبان",
        "رمضان",
        "شوال",
        "ذو القعدة",
        "ذو الحجة"
    )

    private val gregorianMonthsAr = listOf(
        "يناير",
        "فبراير",
        "مارس",
        "أبريل",
        "مايو",
        "يونيو",
        "يوليو",
        "أغسطس",
        "سبتمبر",
        "أكتوبر",
        "نوفمبر",
        "ديسمبر"
    )

    private val weekdaysAr = mapOf(
        Calendar.SUNDAY to "الأحد",
        Calendar.MONDAY to "الإثنين",
        Calendar.TUESDAY to "الثلاثاء",
        Calendar.WEDNESDAY to "الأربعاء",
        Calendar.THURSDAY to "الخميس",
        Calendar.FRIDAY to "الجمعة",
        Calendar.SATURDAY to "السبت"
    )

    fun weekdayNameAr(dayOfWeek: Int): String = weekdaysAr[dayOfWeek] ?: ""

    fun formatGregorianAr(year: Int, month: Int, day: Int): String {
        val monthName = gregorianMonthsAr.getOrElse(month - 1) { "" }
        return "$day $monthName $year م"
    }

    fun toHijri(
        year: Int,
        month: Int, // 1..12
        day: Int,
        offsetDays: Int = 0
    ): HijriDateResult {
        val julianDay = gregorianToJulianDay(year, month, day) + offsetDays
        val l0 = julianDay - 1948440 + 10632
        val n = floor((l0 - 1) / 10631.0).toInt()
        var l = l0 - 10631 * n + 354
        val j = (floor((10985 - l) / 5316.0).toInt()) * (floor((50 * l) / 17719.0).toInt()) +
            (floor(l / 5670.0).toInt()) * (floor((43 * l) / 15238.0).toInt())
        l = l - (floor((30 - j) / 15.0).toInt()) * (floor((17719 * j) / 50.0).toInt()) -
            (floor(j / 16.0).toInt()) * (floor((15238 * j) / 43.0).toInt()) + 29
        val hMonth = floor((24 * l) / 709.0).toInt().coerceIn(1, 12)
        val hDay = (l - floor((709 * hMonth) / 24.0).toInt()).coerceIn(1, 30)
        val hYear = 30 * n + j - 30

        val monthName = hijriMonthsAr.getOrElse(hMonth - 1) { "" }
        return HijriDateResult(
            day = hDay,
            month = hMonth,
            year = hYear,
            monthNameAr = monthName,
            formattedAr = "$hDay $monthName $hYear هـ"
        )
    }

    private fun gregorianToJulianDay(year: Int, month: Int, day: Int): Int {
        var y = year
        var m = month
        if (m < 3) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + (a / 4)
        return (floor(365.25 * (y + 4716)).toInt() +
            floor(30.6001 * (m + 1)).toInt() +
            day + b - 1524)
    }
}
