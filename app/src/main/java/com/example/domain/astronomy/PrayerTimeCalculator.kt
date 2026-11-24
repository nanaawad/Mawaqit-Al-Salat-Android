package com.example.domain.astronomy

import com.example.data.model.AsrMadhab
import com.example.data.model.CityConfig
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.DstMode
import com.example.data.model.HighLatitudeRule
import com.example.data.model.PrayerName
import com.example.data.model.PrayerTimeEntry
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

/**
 * Pure Kotlin Astronomical Prayer Time Engine based on Jean Meeus solar position equations
 * and PrayTimes/Adhan-standard angle definitions.
 *
 * - Supports all global longitudes/latitudes and IANA time zones (with auto/manual DST control).
 * - Uses 2-pass iterative solar coordinate refinement for sub-minute accuracy.
 * - Seamlessly integrates validated online-synced base times when available while preserving
 *   per-city manual offsets and independent Iqama durations.
 */
object PrayerTimeCalculator {

    private const val DEG_TO_RAD = PI / 180.0
    private const val RAD_TO_DEG = 180.0 / PI

    private data class SolarCoordinates(
        val declinationDeg: Double,
        val equationOfTimeHours: Double
    )

    fun resolveTimeZoneOffsetHours(
        timeZoneId: String,
        year: Int,
        month: Int,
        day: Int,
        dstMode: DstMode
    ): Pair<Double, Boolean> {
        val tz = TimeZone.getTimeZone(timeZoneId)
        val noonCal = GregorianCalendar(tz).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val noonMillis = noonCal.timeInMillis
        val autoHours = tz.getOffset(noonMillis) / 3_600_000.0
        val autoDst = tz.inDaylightTime(noonCal.time)
        val standardHours = tz.rawOffset / 3_600_000.0

        return when (dstMode) {
            DstMode.AUTO -> autoHours to autoDst
            DstMode.FORCE_WINTER -> standardHours to false
            DstMode.FORCE_SUMMER -> (standardHours + 1.0) to true
        }
    }

    fun calculateDay(
        city: CityConfig,
        year: Int,
        month: Int, // 1..12
        day: Int,
        syncedBaseMinutes: Map<PrayerName, Int>? = null,
        syncedSourceLabel: String? = null,
        syncedAtMillis: Long? = null
    ): DailyPrayerSchedule {
        val (autoHours, _) = resolveTimeZoneOffsetHours(city.timeZoneId, year, month, day, DstMode.AUTO)
        val (effectiveHours, isDst) = resolveTimeZoneOffsetHours(city.timeZoneId, year, month, day, city.dstMode)
        val dstOverrideDeltaMinutes = if (city.dstMode == DstMode.AUTO) {
            0
        } else {
            ((effectiveHours - autoHours) * 60.0).roundToInt()
        }

        val dateIso = formatDateIso(year, month, day)
        val activeOffsets = city.effectiveOffsetsForDate(dateIso)

        val entries = mutableMapOf<PrayerName, PrayerTimeEntry>()
        val iqamaMap = city.iqamaMinutes

        val requiredOnlineKeys = listOf(
            PrayerName.FAJR,
            PrayerName.SUNRISE,
            PrayerName.DHUHR,
            PrayerName.ASR,
            PrayerName.MAGHRIB,
            PrayerName.ISHA
        )
        val hasValidSynced = syncedBaseMinutes != null &&
            requiredOnlineKeys.all { syncedBaseMinutes.containsKey(it) }

        val baseMinutesMap: Map<PrayerName, Int> = if (hasValidSynced && syncedBaseMinutes != null) {
            requiredOnlineKeys.associateWith { p ->
                wrapMinutes((syncedBaseMinutes[p] ?: 0) + dstOverrideDeltaMinutes)
            }
        } else {
            val jd0 = julianDayAtZeroUt(year, month, day)
            val rawHours = computeRawPrayerHours(
                jd0 = jd0,
                lat = city.latitude,
                lon = city.longitude,
                tzHours = effectiveHours,
                fajrAngle = city.effectiveFajrAngle(),
                ishaAngle = city.effectiveIshaAngle(),
                ishaMinutesAfterMaghrib = city.effectiveIshaMinutes(),
                madhab = city.asrMadhab,
                highLatRule = city.highLatitudeRule
            )
            mapOf(
                PrayerName.FAJR to hoursToRoundedMinutes(rawHours.fajr),
                PrayerName.SUNRISE to hoursToRoundedMinutes(rawHours.sunrise),
                PrayerName.DHUHR to hoursToRoundedMinutes(rawHours.dhuhr) + city.calculationMethod.dhuhrOffsetMinutes,
                PrayerName.ASR to hoursToRoundedMinutes(rawHours.asr),
                PrayerName.MAGHRIB to hoursToRoundedMinutes(rawHours.maghrib) + city.calculationMethod.maghribOffsetMinutes,
                PrayerName.ISHA to hoursToRoundedMinutes(rawHours.isha)
            )
        }

        for ((prayer, origMin) in baseMinutesMap) {
            val offset = activeOffsets[prayer] ?: 0
            val adjusted = wrapMinutes(origMin + offset)
            val iqama = iqamaMap[prayer] ?: defaultIqamaFor(prayer)
            entries[prayer] = PrayerTimeEntry(
                prayer = prayer,
                originalMinuteOfDay = wrapMinutes(origMin),
                offsetMinutes = offset,
                adjustedMinuteOfDay = adjusted,
                iqamaMinutes = iqama
            )
        }

        val dhuhrOriginal = entries[PrayerName.DHUHR]?.originalMinuteOfDay ?: (12 * 60)
        val jumuahBase = if (city.jumuahFollowsDhuhr) {
            entries[PrayerName.DHUHR]?.adjustedMinuteOfDay ?: dhuhrOriginal
        } else {
            city.jumuahCustomHour * 60 + city.jumuahCustomMinute
        }
        val jumuahOffset = if (city.jumuahFollowsDhuhr) (activeOffsets[PrayerName.JUMUAH] ?: 0) else 0
        val jumuahAdjusted = wrapMinutes(jumuahBase + jumuahOffset)
        entries[PrayerName.JUMUAH] = PrayerTimeEntry(
            prayer = PrayerName.JUMUAH,
            originalMinuteOfDay = if (city.jumuahFollowsDhuhr) dhuhrOriginal else jumuahBase,
            offsetMinutes = jumuahOffset,
            adjustedMinuteOfDay = jumuahAdjusted,
            iqamaMinutes = iqamaMap[PrayerName.JUMUAH] ?: (iqamaMap[PrayerName.DHUHR] ?: 15)
        )

        return DailyPrayerSchedule(
            cityId = city.cityId,
            dateIso = dateIso,
            timeZoneId = city.timeZoneId,
            isDstActive = isDst,
            entries = entries,
            sourceLabel = if (hasValidSynced) {
                syncedSourceLabel ?: "مزامنة AlAdhan الموثوقة"
            } else {
                "حساب فلكي محلي"
            },
            lastSyncedAtMillis = if (hasValidSynced) syncedAtMillis else null
        )
    }

    private data class RawPrayerHours(
        val fajr: Double,
        val sunrise: Double,
        val dhuhr: Double,
        val asr: Double,
        val maghrib: Double,
        val isha: Double
    )

    private fun computeRawPrayerHours(
        jd0: Double,
        lat: Double,
        lon: Double,
        tzHours: Double,
        fajrAngle: Double,
        ishaAngle: Double,
        ishaMinutesAfterMaghrib: Int,
        madhab: AsrMadhab,
        highLatRule: HighLatitudeRule
    ): RawPrayerHours {
        var tFajr = 5.0 / 24.0
        var tSunrise = 6.0 / 24.0
        var tDhuhr = 12.0 / 24.0
        var tAsr = 15.0 / 24.0
        var tMaghrib = 18.0 / 24.0
        var tIsha = 19.5 / 24.0

        var fajr = 5.0
        var sunrise = 6.0
        var dhuhr = 12.0
        var asr = 15.0
        var maghrib = 18.0
        var isha = 19.5

        repeat(2) {
            dhuhr = computeMidDay(jd0 + tDhuhr - tzHours / 24.0, lon, tzHours)
            sunrise = computeSunAngleTime(
                jd = jd0 + tSunrise - tzHours / 24.0,
                lat = lat,
                lon = lon,
                tzHours = tzHours,
                angleDeg = 0.8333,
                afterNoon = false
            ) ?: 6.0
            maghrib = computeSunAngleTime(
                jd = jd0 + tMaghrib - tzHours / 24.0,
                lat = lat,
                lon = lon,
                tzHours = tzHours,
                angleDeg = 0.8333,
                afterNoon = true
            ) ?: 18.0

            val fajrRaw = computeSunAngleTime(
                jd = jd0 + tFajr - tzHours / 24.0,
                lat = lat,
                lon = lon,
                tzHours = tzHours,
                angleDeg = fajrAngle,
                afterNoon = false
            )
            val ishaRaw = if (ishaMinutesAfterMaghrib > 0) {
                maghrib + ishaMinutesAfterMaghrib / 60.0
            } else {
                computeSunAngleTime(
                    jd = jd0 + tIsha - tzHours / 24.0,
                    lat = lat,
                    lon = lon,
                    tzHours = tzHours,
                    angleDeg = ishaAngle,
                    afterNoon = true
                )
            }
            asr = computeAsrTime(
                jd = jd0 + tAsr - tzHours / 24.0,
                lat = lat,
                lon = lon,
                tzHours = tzHours,
                shadowFactor = madhab.shadowFactor
            )

            val nightDuration = positiveMod(sunrise - maghrib, 24.0)
            fajr = adjustHighLatFajr(fajrRaw, sunrise, nightDuration, fajrAngle, highLatRule)
            isha = if (ishaMinutesAfterMaghrib > 0) {
                maghrib + ishaMinutesAfterMaghrib / 60.0
            } else {
                adjustHighLatIsha(ishaRaw, maghrib, nightDuration, ishaAngle, highLatRule)
            }

            tFajr = fajr / 24.0
            tSunrise = sunrise / 24.0
            tDhuhr = dhuhr / 24.0
            tAsr = asr / 24.0
            tMaghrib = maghrib / 24.0
            tIsha = isha / 24.0
        }

        return RawPrayerHours(
            fajr = fajr,
            sunrise = sunrise,
            dhuhr = dhuhr,
            asr = asr,
            maghrib = maghrib,
            isha = isha
        )
    }

    private fun adjustHighLatFajr(
        rawFajr: Double?,
        sunrise: Double,
        nightDuration: Double,
        fajrAngle: Double,
        rule: HighLatitudeRule
    ): Double {
        if (rule == HighLatitudeRule.NONE) return rawFajr ?: (sunrise - 1.5)
        val portion = nightPortion(fajrAngle, nightDuration, rule)
        val limit = sunrise - portion
        if (rawFajr == null) return limit
        val diff = positiveMod(sunrise - rawFajr, 24.0)
        return if (diff > portion) limit else rawFajr
    }

    private fun adjustHighLatIsha(
        rawIsha: Double?,
        maghrib: Double,
        nightDuration: Double,
        ishaAngle: Double,
        rule: HighLatitudeRule
    ): Double {
        if (rule == HighLatitudeRule.NONE) return rawIsha ?: (maghrib + 1.5)
        val portion = nightPortion(ishaAngle, nightDuration, rule)
        val limit = maghrib + portion
        if (rawIsha == null) return limit
        val diff = positiveMod(rawIsha - maghrib, 24.0)
        return if (diff > portion) limit else rawIsha
    }

    private fun nightPortion(angle: Double, nightDuration: Double, rule: HighLatitudeRule): Double =
        when (rule) {
            HighLatitudeRule.MIDDLE_OF_THE_NIGHT -> nightDuration * 0.5
            HighLatitudeRule.SEVENTH_OF_THE_NIGHT -> nightDuration / 7.0
            HighLatitudeRule.TWILIGHT_ANGLE -> (angle / 60.0) * nightDuration
            HighLatitudeRule.NONE -> nightDuration * 0.5
        }

    private fun computeMidDay(jd: Double, lon: Double, tzHours: Double): Double {
        val solar = solarPosition(jd)
        return 12.0 + tzHours - (lon / 15.0) - solar.equationOfTimeHours
    }

    private fun computeSunAngleTime(
        jd: Double,
        lat: Double,
        lon: Double,
        tzHours: Double,
        angleDeg: Double,
        afterNoon: Boolean
    ): Double? {
        val solar = solarPosition(jd)
        val midDay = 12.0 + tzHours - (lon / 15.0) - solar.equationOfTimeHours
        val latRad = lat * DEG_TO_RAD
        val declRad = solar.declinationDeg * DEG_TO_RAD
        val cosH = (-sin(angleDeg * DEG_TO_RAD) - sin(latRad) * sin(declRad)) /
            (cos(latRad) * cos(declRad))
        if (cosH < -1.0 || cosH > 1.0) return null
        val tHours = acos(cosH) * RAD_TO_DEG / 15.0
        return if (afterNoon) midDay + tHours else midDay - tHours
    }

    private fun computeAsrTime(
        jd: Double,
        lat: Double,
        lon: Double,
        tzHours: Double,
        shadowFactor: Int
    ): Double {
        val solar = solarPosition(jd)
        val midDay = 12.0 + tzHours - (lon / 15.0) - solar.equationOfTimeHours
        val latRad = lat * DEG_TO_RAD
        val declRad = solar.declinationDeg * DEG_TO_RAD
        val altitudeRad = atan(1.0 / (shadowFactor + tan(abs(latRad - declRad))))
        val cosH = (sin(altitudeRad) - sin(latRad) * sin(declRad)) /
            (cos(latRad) * cos(declRad))
        val clamped = cosH.coerceIn(-1.0, 1.0)
        val tHours = acos(clamped) * RAD_TO_DEG / 15.0
        return midDay + tHours
    }

    private fun solarPosition(jd: Double): SolarCoordinates {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(g * DEG_TO_RAD) + 0.020 * sin(2.0 * g * DEG_TO_RAD))
        val e = 23.439 - 0.00000036 * d

        val raRad = atan2(cos(e * DEG_TO_RAD) * sin(l * DEG_TO_RAD), cos(l * DEG_TO_RAD))
        val raHours = fixHour(raRad * RAD_TO_DEG / 15.0)
        val declDeg = asin(sin(e * DEG_TO_RAD) * sin(l * DEG_TO_RAD)) * RAD_TO_DEG
        val eotHours = q / 15.0 - raHours
        val wrappedEot = when {
            eotHours > 12.0 -> eotHours - 24.0
            eotHours < -12.0 -> eotHours + 24.0
            else -> eotHours
        }
        return SolarCoordinates(declinationDeg = declDeg, equationOfTimeHours = wrappedEot)
    }

    private fun julianDayAtZeroUt(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(a: Double): Double = positiveMod(a, 360.0)
    private fun fixHour(h: Double): Double = positiveMod(h, 24.0)
    private fun positiveMod(value: Double, mod: Double): Double {
        val res = value % mod
        return if (res < 0.0) res + mod else res
    }

    private fun hoursToRoundedMinutes(hours: Double): Int {
        val normalized = fixHour(hours)
        return (normalized * 60.0).roundToInt()
    }

    private fun wrapMinutes(minutes: Int): Int {
        val m = minutes % 1440
        return if (m < 0) m + 1440 else m
    }

    fun formatDateIso(year: Int, month: Int, day: Int): String =
        "%04d-%02d-%02d".format(year, month, day)

    fun defaultIqamaFor(prayer: PrayerName): Int = when (prayer) {
        PrayerName.FAJR -> 20
        PrayerName.SUNRISE -> 0
        PrayerName.DHUHR -> 15
        PrayerName.ASR -> 15
        PrayerName.MAGHRIB -> 10
        PrayerName.ISHA -> 15
        PrayerName.JUMUAH -> 15
    }
}
