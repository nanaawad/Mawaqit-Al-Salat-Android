package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.catalog.DefaultAdhkarCatalog
import com.example.data.catalog.WorldCitiesCatalog
import com.example.data.local.AdhkarEntity
import com.example.data.local.CachedPrayerScheduleEntity
import com.example.data.local.CityConfigEntity
import com.example.data.local.GlobalSettingsEntity
import com.example.data.local.MediaSlot
import com.example.data.local.MediaStorageManager
import com.example.data.local.MosqueDao
import com.example.data.model.CityConfig
import com.example.data.model.CityPreset
import com.example.data.model.PrayerName
import com.example.data.remote.AlAdhanClient
import com.example.data.remote.WeatherClient
import com.example.domain.astronomy.PrayerTimeCalculator
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@JsonClass(generateAdapter = true)
data class MosqueBackupPayload(
    val version: Int = 3,
    val exportedAtMillis: Long = System.currentTimeMillis(),
    val globalSettings: GlobalSettingsEntity,
    val cities: List<CityConfigEntity>,
    val adhkar: List<AdhkarEntity>
)

class MosqueRepository(
    private val dao: MosqueDao,
    private val mediaStorage: MediaStorageManager
) {
    private val initMutex = Mutex()
    private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val backupAdapter = moshi.adapter(MosqueBackupPayload::class.java)

    val globalSettingsFlow: Flow<GlobalSettingsEntity> =
        dao.observeGlobalSettings().map { it ?: GlobalSettingsEntity() }

    val allCitiesFlow: Flow<List<CityConfig>> =
        dao.observeAllCities().map { list -> list.map { it.toDomain() } }

    val adhkarFlow: Flow<List<AdhkarEntity>> =
        dao.observeAllAdhkar()

    fun observeCachedSchedules(cityId: String, dateIsos: List<String>): Flow<Map<String, CachedPrayerScheduleEntity>> =
        dao.observeCachedSchedules(cityId, dateIsos).map { list -> list.associateBy { it.dateIso } }

    /**
     * Ensures built-in cities, default global settings (ONLY if missing), and default Adhkar
     * are populated without ever overwriting user-saved settings or custom offsets.
     */
    suspend fun ensureSeeded() = initMutex.withLock {
        dao.insertCitiesIgnore(WorldCitiesCatalog.cities.map { CityConfigEntity.fromPreset(it) })
        if (dao.getGlobalSettingsOnce() == null) {
            dao.upsertGlobalSettings(GlobalSettingsEntity())
        }
        val existingAdhkar = dao.getAllAdhkarOnce()
        val isLegacySeed = (existingAdhkar.size == 7 && existingAdhkar.none { it.textAr.contains("عِلْمًا نَافِعًا") }) ||
            (existingAdhkar.size == 10 && existingAdhkar.none { it.textAr.contains("۝١") })
        if (existingAdhkar.isEmpty() || isLegacySeed) {
            dao.deleteAllAdhkar()
            dao.upsertAdhkarList(DefaultAdhkarCatalog.defaultItems)
        }
    }

    suspend fun getCityConfig(cityId: String): CityConfig {
        val existing = dao.getCityByIdOnce(cityId)
        if (existing != null) return existing.toDomain()
        val preset = WorldCitiesCatalog.findById(cityId) ?: WorldCitiesCatalog.defaultCity
        val entity = CityConfigEntity.fromPreset(preset)
        dao.upsertCity(entity)
        return entity.toDomain()
    }

    suspend fun saveCityConfig(config: CityConfig): Result<Unit> = runCatching {
        dao.upsertCity(CityConfigEntity.fromDomain(config))
    }

    suspend fun addCustomCity(preset: CityPreset): Result<CityConfig> = runCatching {
        val entity = CityConfigEntity.fromPreset(preset.copy(isCustom = true))
        dao.upsertCity(entity)
        entity.toDomain()
    }

    suspend fun selectCity(cityId: String): Result<Unit> = runCatching {
        val current = dao.getGlobalSettingsOnce() ?: GlobalSettingsEntity()
        dao.upsertGlobalSettings(current.copy(selectedCityId = cityId))
    }

    suspend fun updateGlobalSettings(transform: (GlobalSettingsEntity) -> GlobalSettingsEntity): Result<GlobalSettingsEntity> =
        runCatching {
            val current = dao.getGlobalSettingsOnce() ?: GlobalSettingsEntity()
            val updated = transform(current)
            dao.upsertGlobalSettings(updated)
            updated
        }

    suspend fun resetSinglePrayerOffset(cityId: String, prayer: PrayerName): Result<CityConfig> =
        runCatching {
            val city = getCityConfig(cityId)
            val updatedOffsets = city.manualOffsetsMinutes.toMutableMap().apply {
                this[prayer] = 0
            }
            val updated = city.copy(manualOffsetsMinutes = updatedOffsets)
            dao.upsertCity(CityConfigEntity.fromDomain(updated))
            updated
        }

    suspend fun resetAllPrayerOffsets(cityId: String): Result<CityConfig> = runCatching {
        val city = getCityConfig(cityId)
        val updated = city.copy(
            manualOffsetsMinutes = PrayerName.adjustablePrayers.associateWith { 0 },
            temporaryOffsetsOnly = false,
            temporaryOffsetDateIso = null
        )
        dao.upsertCity(CityConfigEntity.fromDomain(updated))
        updated
    }

    suspend fun resetAllSettingsToDefaults(clearMediaFiles: Boolean = true): Result<GlobalSettingsEntity> = runCatching {
        val current = dao.getGlobalSettingsOnce() ?: GlobalSettingsEntity()
        if (clearMediaFiles) {
            listOf(
                current.mainBgImagePath,
                current.clockBoxBgImagePath,
                current.adhanImagePath,
                current.iqamaBgImagePath,
                current.phoneOffImagePath,
                current.adhkarBgImagePath,
                current.fajrAdhanAudioPath,
                current.defaultAdhanAudioPath
            ).forEach { mediaStorage.clearFile(it) }
        }
        val fresh = GlobalSettingsEntity()
        dao.upsertGlobalSettings(fresh)
        fresh
    }

    fun resolvePickedFileDisplayName(uri: Uri): String =
        mediaStorage.getPickedFileDisplayName(uri)

    suspend fun saveCustomMedia(slot: MediaSlot, uri: Uri): Result<String> {
        val current = dao.getGlobalSettingsOnce() ?: GlobalSettingsEntity()
        val oldPath = when (slot) {
            MediaSlot.MAIN_BG -> current.mainBgImagePath
            MediaSlot.CLOCK_BG -> current.clockBoxBgImagePath
            MediaSlot.ADHAN_IMG -> current.adhanImagePath
            MediaSlot.IQAMA_BG -> current.iqamaBgImagePath
            MediaSlot.PHONE_OFF_IMG -> current.phoneOffImagePath
            MediaSlot.ADHKAR_BG -> current.adhkarBgImagePath
            MediaSlot.FAJR_AUDIO -> current.fajrAdhanAudioPath
            MediaSlot.DEFAULT_AUDIO -> current.defaultAdhanAudioPath
        }
        return mediaStorage.savePickedFile(slot, uri, oldPath).onSuccess { newPath ->
            val updated = when (slot) {
                MediaSlot.MAIN_BG -> current.copy(mainBgImagePath = newPath)
                MediaSlot.CLOCK_BG -> current.copy(clockBoxBgImagePath = newPath)
                MediaSlot.ADHAN_IMG -> current.copy(adhanImagePath = newPath)
                MediaSlot.IQAMA_BG -> current.copy(iqamaBgImagePath = newPath)
                MediaSlot.PHONE_OFF_IMG -> current.copy(phoneOffImagePath = newPath)
                MediaSlot.ADHKAR_BG -> current.copy(adhkarBgImagePath = newPath)
                MediaSlot.FAJR_AUDIO -> current.copy(fajrAdhanAudioPath = newPath)
                MediaSlot.DEFAULT_AUDIO -> current.copy(defaultAdhanAudioPath = newPath)
            }
            dao.upsertGlobalSettings(updated)
        }
    }

    suspend fun clearCustomMedia(slot: MediaSlot): Result<Unit> = runCatching {
        val current = dao.getGlobalSettingsOnce() ?: GlobalSettingsEntity()
        val oldPath = when (slot) {
            MediaSlot.MAIN_BG -> current.mainBgImagePath
            MediaSlot.CLOCK_BG -> current.clockBoxBgImagePath
            MediaSlot.ADHAN_IMG -> current.adhanImagePath
            MediaSlot.IQAMA_BG -> current.iqamaBgImagePath
            MediaSlot.PHONE_OFF_IMG -> current.phoneOffImagePath
            MediaSlot.ADHKAR_BG -> current.adhkarBgImagePath
            MediaSlot.FAJR_AUDIO -> current.fajrAdhanAudioPath
            MediaSlot.DEFAULT_AUDIO -> current.defaultAdhanAudioPath
        }
        mediaStorage.clearFile(oldPath)
        val updated = when (slot) {
            MediaSlot.MAIN_BG -> current.copy(mainBgImagePath = null)
            MediaSlot.CLOCK_BG -> current.copy(clockBoxBgImagePath = null)
            MediaSlot.ADHAN_IMG -> current.copy(adhanImagePath = null)
            MediaSlot.IQAMA_BG -> current.copy(iqamaBgImagePath = null)
            MediaSlot.PHONE_OFF_IMG -> current.copy(phoneOffImagePath = null)
            MediaSlot.ADHKAR_BG -> current.copy(adhkarBgImagePath = null)
            MediaSlot.FAJR_AUDIO -> current.copy(fajrAdhanAudioPath = null)
            MediaSlot.DEFAULT_AUDIO -> current.copy(defaultAdhanAudioPath = null)
        }
        dao.upsertGlobalSettings(updated)
    }

    suspend fun saveAdhkarItem(item: AdhkarEntity): Result<Unit> = runCatching {
        dao.upsertAdhkar(item)
    }

    suspend fun deleteAdhkarItem(id: Int): Result<Unit> = runCatching {
        dao.deleteAdhkarById(id)
    }

    suspend fun resetAdhkarToCatalog(): Result<Unit> = runCatching {
        dao.deleteAllAdhkar()
        dao.upsertAdhkarList(DefaultAdhkarCatalog.defaultItems)
    }

    suspend fun syncPrayerScheduleIfOnline(
        city: CityConfig,
        year: Int,
        month: Int,
        day: Int,
        force: Boolean = false
    ): Result<CachedPrayerScheduleEntity> = runCatching {
        val dateIso = PrayerTimeCalculator.formatDateIso(year, month, day)
        val cacheKey = "${city.cityId}_$dateIso"
        val sig = AlAdhanClient.buildParamsSignature(city)
        val existing = dao.getCachedSchedule(cacheKey)
        val now = System.currentTimeMillis()

        if (!force && existing != null && existing.paramsSignature == sig && (now - existing.syncedAtMillis) < 12 * 3600_000L) {
            return@runCatching existing
        }

        val (methodId, methodSettings) = AlAdhanClient.resolveMethodAndSettings(city)
        val response = AlAdhanClient.api.getTimings(
            dateDdMmYyyy = AlAdhanClient.formatAlAdhanDate(year, month, day),
            latitude = city.latitude,
            longitude = city.longitude,
            method = methodId,
            school = AlAdhanClient.mapSchool(city.asrMadhab),
            latitudeAdjustmentMethod = AlAdhanClient.mapHighLatRule(city.highLatitudeRule),
            timeZoneString = city.timeZoneId,
            methodSettings = methodSettings
        )

        val validated = AlAdhanClient.parseAndValidateTimings(response.data?.timings)
            ?: error("بيانات المواقيت الواردة غير مكتملة أو غير منطقية")

        val entity = CachedPrayerScheduleEntity(
            cacheKey = cacheKey,
            cityId = city.cityId,
            dateIso = dateIso,
            paramsSignature = sig,
            fajrMinute = validated.fajrMinute,
            sunriseMinute = validated.sunriseMinute,
            dhuhrMinute = validated.dhuhrMinute,
            asrMinute = validated.asrMinute,
            maghribMinute = validated.maghribMinute,
            ishaMinute = validated.ishaMinute,
            syncedAtMillis = now,
            sourceName = "مزامنة AlAdhan"
        )
        dao.upsertCachedSchedule(entity)
        entity
    }

    suspend fun refreshWeatherIfOnline(city: CityConfig): Result<GlobalSettingsEntity> = runCatching {
        val response = WeatherClient.api.getCurrentWeather(city.latitude, city.longitude)
        val cw = response.currentWeather ?: error("لا تتوفر بيانات طقس")
        val current = dao.getGlobalSettingsOnce() ?: GlobalSettingsEntity()
        val updated = current.copy(
            cachedTempCelsius = cw.temperature,
            cachedWeatherCode = cw.weatherCode,
            cachedWeatherUpdatedAtMillis = System.currentTimeMillis(),
            cachedWeatherCityId = city.cityId
        )
        dao.upsertGlobalSettings(updated)
        updated
    }

    suspend fun exportBackupJson(): Result<String> = runCatching {
        val settings = dao.getGlobalSettingsOnce() ?: GlobalSettingsEntity()
        val cities = dao.getAllCitiesOnce()
        val adhkar = dao.getAllAdhkarOnce()
        val payload = MosqueBackupPayload(
            globalSettings = settings,
            cities = cities,
            adhkar = adhkar
        )
        backupAdapter.toJson(payload)
    }

    suspend fun importBackupJson(rawJson: String): Result<Unit> = runCatching {
        val payload = backupAdapter.fromJson(rawJson) ?: error("صيغة ملف النسخة الاحتياطية غير صالحة")
        dao.upsertGlobalSettings(payload.globalSettings.copy(id = 1))
        if (payload.cities.isNotEmpty()) {
            dao.upsertCities(payload.cities)
        }
        if (payload.adhkar.isNotEmpty()) {
            dao.upsertAdhkarList(payload.adhkar)
        }
        dao.insertCitiesIgnore(WorldCitiesCatalog.cities.map { CityConfigEntity.fromPreset(it) })
    }

    companion object {
        fun create(context: Context): MosqueRepository {
            val db = com.example.data.local.MosqueDatabase.getInstance(context)
            return MosqueRepository(db.mosqueDao(), MediaStorageManager(context.applicationContext))
        }
    }
}
