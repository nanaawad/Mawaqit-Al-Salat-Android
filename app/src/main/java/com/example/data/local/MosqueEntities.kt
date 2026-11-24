package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.catalog.WorldCitiesCatalog
import com.example.data.model.AsrMadhab
import com.example.data.model.CalculationMethod
import com.example.data.model.CityConfig
import com.example.data.model.CityPreset
import com.example.data.model.DstMode
import com.example.data.model.FontFamilyOption
import com.example.data.model.HighLatitudeRule
import com.example.data.model.OrientationMode
import com.example.data.model.PrayerName

@Entity(tableName = "city_configs")
data class CityConfigEntity(
    @PrimaryKey val cityId: String,
    val countryAr: String,
    val countryEn: String,
    val cityAr: String,
    val cityEn: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val calculationMethodName: String,
    val asrMadhabName: String,
    val highLatitudeRuleName: String,
    val fajrOffset: Int = 0,
    val sunriseOffset: Int = 0,
    val dhuhrOffset: Int = 0,
    val asrOffset: Int = 0,
    val maghribOffset: Int = 0,
    val ishaOffset: Int = 0,
    val jumuahOffset: Int = 0,
    val temporaryOffsetsOnly: Boolean = false,
    val temporaryOffsetDateIso: String? = null,
    val fajrIqama: Int = 20,
    val dhuhrIqama: Int = 15,
    val asrIqama: Int = 15,
    val maghribIqama: Int = 10,
    val ishaIqama: Int = 15,
    val jumuahIqama: Int = 15,
    val jumuahFollowsDhuhr: Boolean = true,
    val jumuahCustomHour: Int = 13,
    val jumuahCustomMinute: Int = 0,
    val isCustomCity: Boolean = false,
    // v3 per-city mosque details, DST override, and custom calculation angles
    val mosqueName: String = "",
    val district: String = "",
    val street: String = "",
    val dstModeName: String = DstMode.AUTO.name,
    val customFajrAngle: Double? = null,
    val customIshaAngle: Double? = null,
    val customIshaMinutes: Int? = null
) {
    fun toDomain(): CityConfig {
        val method = runCatching { CalculationMethod.valueOf(calculationMethodName) }
            .getOrDefault(CalculationMethod.MWL)
        val madhab = runCatching { AsrMadhab.valueOf(asrMadhabName) }
            .getOrDefault(AsrMadhab.SHAFI)
        val highLat = runCatching { HighLatitudeRule.valueOf(highLatitudeRuleName) }
            .getOrDefault(HighLatitudeRule.TWILIGHT_ANGLE)
        val dstMode = runCatching { DstMode.valueOf(dstModeName) }
            .getOrDefault(DstMode.AUTO)

        return CityConfig(
            cityId = cityId,
            countryAr = countryAr,
            countryEn = countryEn,
            cityAr = cityAr,
            cityEn = cityEn,
            latitude = latitude,
            longitude = longitude,
            timeZoneId = timeZoneId,
            calculationMethod = method,
            asrMadhab = madhab,
            highLatitudeRule = highLat,
            manualOffsetsMinutes = mapOf(
                PrayerName.FAJR to fajrOffset,
                PrayerName.SUNRISE to sunriseOffset,
                PrayerName.DHUHR to dhuhrOffset,
                PrayerName.ASR to asrOffset,
                PrayerName.MAGHRIB to maghribOffset,
                PrayerName.ISHA to ishaOffset,
                PrayerName.JUMUAH to jumuahOffset
            ),
            temporaryOffsetsOnly = temporaryOffsetsOnly,
            temporaryOffsetDateIso = temporaryOffsetDateIso,
            iqamaMinutes = mapOf(
                PrayerName.FAJR to fajrIqama,
                PrayerName.SUNRISE to 0,
                PrayerName.DHUHR to dhuhrIqama,
                PrayerName.ASR to asrIqama,
                PrayerName.MAGHRIB to maghribIqama,
                PrayerName.ISHA to ishaIqama,
                PrayerName.JUMUAH to jumuahIqama
            ),
            jumuahFollowsDhuhr = jumuahFollowsDhuhr,
            jumuahCustomHour = jumuahCustomHour,
            jumuahCustomMinute = jumuahCustomMinute,
            isCustomCity = isCustomCity,
            mosqueName = mosqueName,
            district = district,
            street = street,
            dstMode = dstMode,
            customFajrAngle = customFajrAngle,
            customIshaAngle = customIshaAngle,
            customIshaMinutes = customIshaMinutes
        )
    }

    companion object {
        fun fromPreset(preset: CityPreset): CityConfigEntity = CityConfigEntity(
            cityId = preset.cityId,
            countryAr = preset.countryAr,
            countryEn = preset.countryEn,
            cityAr = preset.cityAr,
            cityEn = preset.cityEn,
            latitude = preset.latitude,
            longitude = preset.longitude,
            timeZoneId = preset.timeZoneId,
            calculationMethodName = preset.defaultMethod.name,
            asrMadhabName = preset.defaultMadhab.name,
            highLatitudeRuleName = preset.defaultHighLatRule.name,
            isCustomCity = preset.isCustom
        )

        fun fromDomain(config: CityConfig): CityConfigEntity = CityConfigEntity(
            cityId = config.cityId,
            countryAr = config.countryAr,
            countryEn = config.countryEn,
            cityAr = config.cityAr,
            cityEn = config.cityEn,
            latitude = config.latitude,
            longitude = config.longitude,
            timeZoneId = config.timeZoneId,
            calculationMethodName = config.calculationMethod.name,
            asrMadhabName = config.asrMadhab.name,
            highLatitudeRuleName = config.highLatitudeRule.name,
            fajrOffset = config.manualOffsetsMinutes[PrayerName.FAJR] ?: 0,
            sunriseOffset = config.manualOffsetsMinutes[PrayerName.SUNRISE] ?: 0,
            dhuhrOffset = config.manualOffsetsMinutes[PrayerName.DHUHR] ?: 0,
            asrOffset = config.manualOffsetsMinutes[PrayerName.ASR] ?: 0,
            maghribOffset = config.manualOffsetsMinutes[PrayerName.MAGHRIB] ?: 0,
            ishaOffset = config.manualOffsetsMinutes[PrayerName.ISHA] ?: 0,
            jumuahOffset = config.manualOffsetsMinutes[PrayerName.JUMUAH] ?: 0,
            temporaryOffsetsOnly = config.temporaryOffsetsOnly,
            temporaryOffsetDateIso = config.temporaryOffsetDateIso,
            fajrIqama = config.iqamaMinutes[PrayerName.FAJR] ?: 20,
            dhuhrIqama = config.iqamaMinutes[PrayerName.DHUHR] ?: 15,
            asrIqama = config.iqamaMinutes[PrayerName.ASR] ?: 15,
            maghribIqama = config.iqamaMinutes[PrayerName.MAGHRIB] ?: 10,
            ishaIqama = config.iqamaMinutes[PrayerName.ISHA] ?: 15,
            jumuahIqama = config.iqamaMinutes[PrayerName.JUMUAH] ?: 15,
            jumuahFollowsDhuhr = config.jumuahFollowsDhuhr,
            jumuahCustomHour = config.jumuahCustomHour,
            jumuahCustomMinute = config.jumuahCustomMinute,
            isCustomCity = config.isCustomCity,
            mosqueName = config.mosqueName,
            district = config.district,
            street = config.street,
            dstModeName = config.dstMode.name,
            customFajrAngle = config.customFajrAngle,
            customIshaAngle = config.customIshaAngle,
            customIshaMinutes = config.customIshaMinutes
        )
    }
}

@Entity(tableName = "global_settings")
data class GlobalSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val selectedCityId: String = WorldCitiesCatalog.defaultCity.cityId,
    val mosqueName: String = "مسجد النور",
    val is24HourFormat: Boolean = false,
    val hijriOffsetDays: Int = 0,
    val orientationModeName: String = OrientationMode.AUTO.name,
    val keepScreenOn: Boolean = true,
    val dimModeEnabled: Boolean = false,
    val dimAlpha: Float = 0.35f,
    val announcementText: String = "",
    val showAnnouncement: Boolean = false,
    val showWeather: Boolean = true,
    val showHijriDate: Boolean = true,
    val showGregorianDate: Boolean = true,
    val showSunriseBox: Boolean = true,
    val showJumuahBox: Boolean = true,
    val showNextPrayerBox: Boolean = true,
    val autoStartOnBoot: Boolean = false,

    // Adhan, Iqama, Phone-Off & Adhkar Phase Durations
    val adhanAudioEnabled: Boolean = true,
    val adhanDisplaySeconds: Int = 120,
    val phoneOffDurationSeconds: Int = 20,
    val adhkarEnabled: Boolean = true,
    val adhkarStartDelaySeconds: Int = 5,
    val adhkarTotalDurationSeconds: Int = 180,
    val adhkarItemDurationSeconds: Int = 15,

    // Appearance & Typography
    val fontFamilyKey: String = FontFamilyOption.CAIRO.key,
    val clockScale: Float = 1.0f,
    val countdownScale: Float = 1.0f,
    val textScale: Float = 1.0f,

    // Colors (Mawaqit Frosted Purple & Charcoal Classic matching user screenshots; stored as ARGB Long)
    val screenBgColor: Long = 0xFF090B12,
    val clockBoxBgColor: Long = 0xD15B1B99,
    val clockBoxTextColor: Long = 0xFFFFFFFF,
    val prayerBoxBgColor: Long = 0xC212161F,
    val prayerBoxTextColor: Long = 0xFFFFFFFF,
    val activePrayerAccentColor: Long = 0xD66A1B9A,
    val nextPrayerBoxBgColor: Long = 0xDB141720,
    val nextPrayerBoxTextColor: Long = 0xFFFFFFFF,
    val sunriseBoxBgColor: Long = 0x00000000,
    val sunriseBoxTextColor: Long = 0xFFFFFFFF,
    val jumuahBoxBgColor: Long = 0x00000000,
    val jumuahBoxTextColor: Long = 0xFFFFFFFF,
    val headerBgColor: Long = 0x00000000,
    val headerTextColor: Long = 0xFFFFFFFF,
    val announcementBgColor: Long = 0x00000000,
    val announcementTextColor: Long = 0xFFFFFFFF,

    // v3+ expanded color, border, brightness & tone controls
    val clockAccentColor: Long = 0xFFEAE0FF,
    val boxBorderColor: Long = 0x38FFFFFF,
    val dateTextColor: Long = 0xFFFFFFFF,
    val uiButtonBgColor: Long = 0x66000000,
    val uiButtonTextColor: Long = 0xFFFFFFFF,
    val bgBrightnessAlpha: Float = 0.20f,
    val clockBoxBgImagePath: String? = null,
    val fajrAdhanToneKey: String = "builtin_fajr",
    val defaultAdhanToneKey: String = "builtin_default",

    // Custom local media file paths (copied into app filesDir for reboot-safe persistence)
    val mainBgImagePath: String? = null,
    val adhanImagePath: String? = null,
    val iqamaBgImagePath: String? = null,
    val phoneOffImagePath: String? = null,
    val adhkarBgImagePath: String? = null,
    val fajrAdhanAudioPath: String? = null,
    val defaultAdhanAudioPath: String? = null,

    // Cached Weather (used offline automatically)
    val cachedTempCelsius: Double? = null,
    val cachedWeatherCode: Int? = null,
    val cachedWeatherUpdatedAtMillis: Long? = null,
    val cachedWeatherCityId: String? = null
)

@Entity(tableName = "adhkar_items")
data class AdhkarEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val textAr: String,
    val sourceAr: String,
    val repeatLabel: String,
    val sortOrder: Int,
    val isEnabled: Boolean = true
)

@Entity(tableName = "cached_prayer_schedules")
data class CachedPrayerScheduleEntity(
    @PrimaryKey val cacheKey: String, // "${cityId}_${dateIso}"
    val cityId: String,
    val dateIso: String,
    val paramsSignature: String,
    val fajrMinute: Int,
    val sunriseMinute: Int,
    val dhuhrMinute: Int,
    val asrMinute: Int,
    val maghribMinute: Int,
    val ishaMinute: Int,
    val syncedAtMillis: Long,
    val sourceName: String = "AlAdhan"
) {
    fun toBaseMinutesMap(): Map<PrayerName, Int> = mapOf(
        PrayerName.FAJR to fajrMinute,
        PrayerName.SUNRISE to sunriseMinute,
        PrayerName.DHUHR to dhuhrMinute,
        PrayerName.ASR to asrMinute,
        PrayerName.MAGHRIB to maghribMinute,
        PrayerName.ISHA to ishaMinute
    )
}
