package com.example.data.remote

import com.example.data.model.AsrMadhab
import com.example.data.model.CalculationMethod
import com.example.data.model.CityConfig
import com.example.data.model.HighLatitudeRule
import com.example.data.model.PrayerName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class AlAdhanResponseDto(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "data") val data: AlAdhanDataDto? = null
)

@JsonClass(generateAdapter = true)
data class AlAdhanDataDto(
    @Json(name = "timings") val timings: AlAdhanTimingsDto? = null
)

@JsonClass(generateAdapter = true)
data class AlAdhanTimingsDto(
    @Json(name = "Fajr") val fajr: String? = null,
    @Json(name = "Sunrise") val sunrise: String? = null,
    @Json(name = "Dhuhr") val dhuhr: String? = null,
    @Json(name = "Asr") val asr: String? = null,
    @Json(name = "Maghrib") val maghrib: String? = null,
    @Json(name = "Isha") val isha: String? = null
)

interface AlAdhanApi {
    @GET("v1/timings/{date}")
    suspend fun getTimings(
        @Path("date") dateDdMmYyyy: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int,
        @Query("school") school: Int,
        @Query("latitudeAdjustmentMethod") latitudeAdjustmentMethod: Int?,
        @Query("timezonestring") timeZoneString: String,
        @Query("methodSettings") methodSettings: String? = null
    ): AlAdhanResponseDto
}

data class ValidatedPrayerTimings(
    val fajrMinute: Int,
    val sunriseMinute: Int,
    val dhuhrMinute: Int,
    val asrMinute: Int,
    val maghribMinute: Int,
    val ishaMinute: Int
) {
    fun toMap(): Map<PrayerName, Int> = mapOf(
        PrayerName.FAJR to fajrMinute,
        PrayerName.SUNRISE to sunriseMinute,
        PrayerName.DHUHR to dhuhrMinute,
        PrayerName.ASR to asrMinute,
        PrayerName.MAGHRIB to maghribMinute,
        PrayerName.ISHA to ishaMinute
    )
}

object AlAdhanClient {
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val api: AlAdhanApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AlAdhanApi::class.java)
    }

    fun formatAlAdhanDate(year: Int, month: Int, day: Int): String =
        "%02d-%02d-%04d".format(day, month, year)

    fun resolveMethodAndSettings(city: CityConfig): Pair<Int, String?> {
        val hasCustomAngles = city.customFajrAngle != null ||
            city.customIshaAngle != null ||
            city.customIshaMinutes != null

        if (hasCustomAngles || city.calculationMethod == CalculationMethod.IGMG_GERMANY) {
            val fajr = city.effectiveFajrAngle()
            val ishaSpec = if (city.effectiveIshaMinutes() > 0) {
                "${city.effectiveIshaMinutes()} min"
            } else {
                "${city.effectiveIshaAngle()}"
            }
            return 99 to "$fajr,null,$ishaSpec"
        }

        val methodId = when (city.calculationMethod) {
            CalculationMethod.KARACHI -> 1
            CalculationMethod.ISNA -> 2
            CalculationMethod.MWL -> 3
            CalculationMethod.UMM_AL_QURA -> 4
            CalculationMethod.EGYPT -> 5
            CalculationMethod.DIYANET -> 13
            CalculationMethod.UAE -> 16
            CalculationMethod.JORDAN_AWQAF -> 23
            CalculationMethod.IGMG_GERMANY -> 99
        }
        return methodId to null
    }

    fun mapSchool(madhab: AsrMadhab): Int = when (madhab) {
        AsrMadhab.SHAFI -> 0
        AsrMadhab.HANAFI -> 1
    }

    fun mapHighLatRule(rule: HighLatitudeRule): Int? = when (rule) {
        HighLatitudeRule.MIDDLE_OF_THE_NIGHT -> 1
        HighLatitudeRule.SEVENTH_OF_THE_NIGHT -> 2
        HighLatitudeRule.TWILIGHT_ANGLE -> 3
        HighLatitudeRule.NONE -> null
    }

    fun buildParamsSignature(city: CityConfig): String = buildString {
        append(city.calculationMethod.name)
        append('|').append(city.asrMadhab.name)
        append('|').append(city.highLatitudeRule.name)
        append('|').append(city.customFajrAngle ?: "d")
        append('|').append(city.customIshaAngle ?: "d")
        append('|').append(city.customIshaMinutes ?: "d")
        append('|').append("%.4f,%.4f".format(city.latitude, city.longitude))
        append('|').append(city.timeZoneId)
    }

    fun parseMinuteOfDay(rawTime: String?): Int? {
        if (rawTime.isNullOrBlank()) return null
        val match = Regex("(\\d{1,2}):(\\d{2})").find(rawTime.trim()) ?: return null
        val hour = match.groupValues[1].toIntOrNull() ?: return null
        val minute = match.groupValues[2].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour * 60 + minute
    }

    fun parseAndValidateTimings(dto: AlAdhanTimingsDto?): ValidatedPrayerTimings? {
        if (dto == null) return null
        val fajr = parseMinuteOfDay(dto.fajr) ?: return null
        val sunrise = parseMinuteOfDay(dto.sunrise) ?: return null
        val dhuhr = parseMinuteOfDay(dto.dhuhr) ?: return null
        val asr = parseMinuteOfDay(dto.asr) ?: return null
        val maghrib = parseMinuteOfDay(dto.maghrib) ?: return null
        val isha = parseMinuteOfDay(dto.isha) ?: return null

        val strictlyAscending = fajr < sunrise &&
            sunrise < dhuhr &&
            dhuhr < asr &&
            asr < maghrib &&
            maghrib < isha

        if (!strictlyAscending) return null

        return ValidatedPrayerTimings(
            fajrMinute = fajr,
            sunriseMinute = sunrise,
            dhuhrMinute = dhuhr,
            asrMinute = asr,
            maghribMinute = maghrib,
            ishaMinute = isha
        )
    }
}
