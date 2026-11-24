package com.example.data.model

import androidx.compose.runtime.Immutable

enum class PrayerName(val arabicName: String, val englishName: String, val orderIndex: Int) {
    FAJR("الفجر", "Fajr", 0),
    SUNRISE("الشروق", "Sunrise", 1),
    DHUHR("الظهر", "Dhuhr", 2),
    ASR("العصر", "Asr", 3),
    MAGHRIB("المغرب", "Maghrib", 4),
    ISHA("العشاء", "Isha", 5),
    JUMUAH("الجمعة", "Jumuah", 6);

    companion object {
        val fiveDailyPrayers = listOf(FAJR, DHUHR, ASR, MAGHRIB, ISHA)
        val adjustablePrayers = listOf(FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA, JUMUAH)
    }
}

enum class CalculationMethod(
    val arabicName: String,
    val englishName: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutesAfterMaghrib: Int = 0,
    val dhuhrOffsetMinutes: Int = 1,
    val maghribOffsetMinutes: Int = 0
) {
    JORDAN_AWQAF(
        arabicName = "وزارة الأوقاف الأردنية",
        englishName = "Jordan Ministry of Awqaf",
        fajrAngle = 18.0,
        ishaAngle = 18.0,
        dhuhrOffsetMinutes = 1
    ),
    UMM_AL_QURA(
        arabicName = "أم القرى (السعودية)",
        englishName = "Umm Al-Qura (Makkah)",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        ishaMinutesAfterMaghrib = 90,
        dhuhrOffsetMinutes = 1
    ),
    MWL(
        arabicName = "رابطة العالم الإسلامي (ألمانيا/أوروبا)",
        englishName = "Muslim World League (MWL)",
        fajrAngle = 18.0,
        ishaAngle = 17.0,
        dhuhrOffsetMinutes = 1
    ),
    IGMG_GERMANY(
        arabicName = "الهيئة الإسلامية في ألمانيا (IGMG)",
        englishName = "IGMG / Central Europe",
        fajrAngle = 18.0,
        ishaAngle = 16.5,
        dhuhrOffsetMinutes = 1
    ),
    DIYANET(
        arabicName = "رئاسة الشؤون الدينية التركية (DITIB)",
        englishName = "Diyanet / DITIB",
        fajrAngle = 18.0,
        ishaAngle = 17.0,
        dhuhrOffsetMinutes = 1
    ),
    EGYPT(
        arabicName = "الهيئة المصرية العامة للمساحة",
        englishName = "Egyptian General Authority",
        fajrAngle = 19.5,
        ishaAngle = 17.5,
        dhuhrOffsetMinutes = 1
    ),
    ISNA(
        arabicName = "الجمعية الإسلامية لأمريكا الشمالية (ISNA)",
        englishName = "ISNA (North America)",
        fajrAngle = 15.0,
        ishaAngle = 15.0,
        dhuhrOffsetMinutes = 1
    ),
    KARACHI(
        arabicName = "جامعة العلوم الإسلامية كراتشي",
        englishName = "Univ. of Islamic Sciences, Karachi",
        fajrAngle = 18.0,
        ishaAngle = 18.0,
        dhuhrOffsetMinutes = 1
    ),
    UAE(
        arabicName = "الهيئة العامة للشؤون الإسلامية (الإمارات)",
        englishName = "UAE / Dubai",
        fajrAngle = 18.2,
        ishaAngle = 18.2,
        dhuhrOffsetMinutes = 1
    )
}

enum class AsrMadhab(val arabicName: String, val shadowFactor: Int) {
    SHAFI("الشافعي / المالكي / الحنبلي (الجمهور)", 1),
    HANAFI("الحنفي", 2)
}

enum class HighLatitudeRule(val arabicName: String) {
    TWILIGHT_ANGLE("تقدير الزاوية النسبية (موصى به لألمانيا وأوروبا)"),
    SEVENTH_OF_THE_NIGHT("سُبع الليل"),
    MIDDLE_OF_THE_NIGHT("منتصف الليل"),
    NONE("بدون تعديل (للمناطق المعتدلة)")
}

enum class DstMode(val arabicName: String) {
    AUTO("تلقائي حسب المنطقة الزمنية"),
    FORCE_SUMMER("توقيت صيفي (+1 ساعة)"),
    FORCE_WINTER("توقيت شتوي / قياسي")
}

enum class OrientationMode(val arabicName: String) {
    AUTO("تلقائي حسب الجهاز"),
    LANDSCAPE("أفقي (مناسب للتلفزيون والـ TV Box)"),
    PORTRAIT("عمودي (مناسب للهاتف)")
}

enum class FontFamilyOption(val key: String, val arabicName: String) {
    CAIRO("cairo", "القاهرة (Cairo)"),
    AMIRI("amiri", "أميري الكلاسيكي (Amiri)"),
    TAJAWAL("tajawal", "تجوال العصري (Tajawal)"),
    JETBRAINS_MONO("jetbrains_mono", "رقمي واضح (JetBrains Mono)")
}

@Immutable
data class CityPreset(
    val cityId: String,
    val countryAr: String,
    val countryEn: String,
    val cityAr: String,
    val cityEn: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val defaultMethod: CalculationMethod,
    val defaultMadhab: AsrMadhab = AsrMadhab.SHAFI,
    val defaultHighLatRule: HighLatitudeRule = HighLatitudeRule.TWILIGHT_ANGLE,
    val isCustom: Boolean = false
)

@Immutable
data class CityConfig(
    val cityId: String,
    val countryAr: String,
    val countryEn: String,
    val cityAr: String,
    val cityEn: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val calculationMethod: CalculationMethod,
    val asrMadhab: AsrMadhab,
    val highLatitudeRule: HighLatitudeRule,
    val manualOffsetsMinutes: Map<PrayerName, Int>,
    val temporaryOffsetsOnly: Boolean,
    val temporaryOffsetDateIso: String?,
    val iqamaMinutes: Map<PrayerName, Int>,
    val jumuahFollowsDhuhr: Boolean,
    val jumuahCustomHour: Int,
    val jumuahCustomMinute: Int,
    val isCustomCity: Boolean,
    val mosqueName: String = "",
    val district: String = "",
    val street: String = "",
    val dstMode: DstMode = DstMode.AUTO,
    val customFajrAngle: Double? = null,
    val customIshaAngle: Double? = null,
    val customIshaMinutes: Int? = null
) {
    fun effectiveOffsetsForDate(dateIso: String): Map<PrayerName, Int> {
        if (!temporaryOffsetsOnly) return manualOffsetsMinutes
        return if (temporaryOffsetDateIso == dateIso) {
            manualOffsetsMinutes
        } else {
            emptyMap()
        }
    }

    fun effectiveFajrAngle(): Double = customFajrAngle ?: calculationMethod.fajrAngle
    fun effectiveIshaAngle(): Double = customIshaAngle ?: calculationMethod.ishaAngle
    fun effectiveIshaMinutes(): Int = customIshaMinutes ?: calculationMethod.ishaMinutesAfterMaghrib
}

@Immutable
data class PrayerTimeEntry(
    val prayer: PrayerName,
    val originalMinuteOfDay: Int,
    val offsetMinutes: Int,
    val adjustedMinuteOfDay: Int,
    val iqamaMinutes: Int
) {
    val iqamaMinuteOfDay: Int
        get() = (adjustedMinuteOfDay + iqamaMinutes).coerceIn(0, 23 * 60 + 59)
}

@Immutable
data class DailyPrayerSchedule(
    val cityId: String,
    val dateIso: String,
    val timeZoneId: String,
    val isDstActive: Boolean,
    val entries: Map<PrayerName, PrayerTimeEntry>,
    val sourceLabel: String = "حساب فلكي محلي",
    val lastSyncedAtMillis: Long? = null
) {
    fun getAdjustedMinutes(prayer: PrayerName): Int =
        entries[prayer]?.adjustedMinuteOfDay ?: 0
}

sealed interface MosquePhase {
    data class NormalCountdown(
        val nextPrayer: PrayerName,
        val remainingSeconds: Long
    ) : MosquePhase

    data class AdhanActive(
        val prayer: PrayerName,
        val message: String,
        val elapsedSeconds: Int,
        val totalSeconds: Int
    ) : MosquePhase

    data class IqamaCountdown(
        val prayer: PrayerName,
        val remainingSeconds: Int,
        val totalSeconds: Int
    ) : MosquePhase

    data class TurnOffPhoneScreen(
        val prayer: PrayerName,
        val remainingSeconds: Int,
        val totalSeconds: Int
    ) : MosquePhase

    data class PostPrayerAdhkar(
        val prayer: PrayerName,
        val dhikrText: String,
        val dhikrSource: String,
        val repeatLabel: String,
        val currentIndex: Int,
        val totalItems: Int,
        val remainingSeconds: Int,
        val isPaused: Boolean
    ) : MosquePhase
}
