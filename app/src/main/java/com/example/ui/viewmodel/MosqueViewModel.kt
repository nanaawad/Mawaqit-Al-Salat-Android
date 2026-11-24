package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AdhkarEntity
import com.example.data.local.CachedPrayerScheduleEntity
import com.example.data.local.GlobalSettingsEntity
import com.example.data.local.MediaSlot
import com.example.data.model.AsrMadhab
import com.example.data.model.CalculationMethod
import com.example.data.model.CityConfig
import com.example.data.model.CityPreset
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.DstMode
import com.example.data.model.HighLatitudeRule
import com.example.data.model.MosquePhase
import com.example.data.model.PrayerName
import com.example.data.remote.AlAdhanClient
import com.example.data.repository.MosqueRepository
import com.example.domain.astronomy.HijriDateCalculator
import com.example.domain.astronomy.HijriDateResult
import com.example.domain.astronomy.PrayerTimeCalculator
import com.example.domain.phase.MosquePhaseEngine
import com.example.media.AdhanAudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

data class SimulationState(
    val active: Boolean = false,
    val prayer: PrayerName = PrayerName.ASR,
    val elapsedSeconds: Int = 0
)

data class MosqueUiState(
    val isReady: Boolean = false,
    val settings: GlobalSettingsEntity = GlobalSettingsEntity(),
    val currentCity: CityConfig? = null,
    val allCities: List<CityConfig> = emptyList(),
    val adhkarList: List<AdhkarEntity> = emptyList(),
    val todaySchedule: DailyPrayerSchedule? = null,
    val tomorrowSchedule: DailyPrayerSchedule? = null,
    val currentYear: Int = 2026,
    val currentMonth: Int = 1,
    val currentDay: Int = 1,
    val currentHour24: Int = 12,
    val currentMinute: Int = 0,
    val currentSecond: Int = 0,
    val weekdayAr: String = "",
    val gregorianDateAr: String = "",
    val hijriDate: HijriDateResult = HijriDateResult(1, 1, 1448, "محرم", ""),
    val phase: MosquePhase = MosquePhase.NormalCountdown(PrayerName.FAJR, 0L),
    val isOnline: Boolean = false,
    val isSyncingPrayerTimes: Boolean = false,
    val lastPrayerSyncMillis: Long? = null,
    val isAudioPlaying: Boolean = false,
    val isSimulationActive: Boolean = false,
    val saveBannerMessage: String? = null,
    val saveBannerIsError: Boolean = false
)

class MosqueViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MosqueRepository.create(application)
    private val audioPlayer = AdhanAudioPlayer(application)

    private val _uiState = MutableStateFlow(MosqueUiState())
    val uiState: StateFlow<MosqueUiState> = _uiState.asStateFlow()

    private val _simulation = MutableStateFlow(SimulationState())
    private val _dismissedPhaseKey = MutableStateFlow<String?>(null)
    private val _adhkarPaused = MutableStateFlow(false)
    private val _manualAdhkarElapsed = MutableStateFlow<Int?>(null)
    private val _isOnline = MutableStateFlow(checkCurrentlyOnline(application))
    private val _cachedSchedules = MutableStateFlow<Map<String, CachedPrayerScheduleEntity>>(emptyMap())

    private var cacheObserveJob: Job? = null
    private var observedCacheSignature: String? = null
    private var lastTriggeredAdhanKey: String? = null
    private var lastWeatherCityId: String? = null
    private var lastWeatherAttemptMillis: Long = 0L
    private var lastPrayerSyncKey: String? = null
    private var lastPrayerSyncAttemptMillis: Long = 0L

    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOnline.value = true
            _uiState.value.currentCity?.let { city ->
                refreshWeather(city, force = false)
                triggerAutoPrayerSyncIfNeeded(city, force = false)
            }
        }

        override fun onLost(network: Network) {
            _isOnline.value = checkCurrentlyOnline(getApplication())
        }
    }

    init {
        registerNetworkObserver()

        viewModelScope.launch {
            repository.ensureSeeded()

            combine(
                repository.globalSettingsFlow,
                repository.allCitiesFlow,
                repository.adhkarFlow,
                _isOnline
            ) { settings, cities, adhkar, online ->
                QuadData(settings, cities, adhkar, online)
            }.collect { data ->
                val city = data.cities.find { it.cityId == data.settings.selectedCityId }
                    ?: repository.getCityConfig(data.settings.selectedCityId)

                _uiState.update { state ->
                    state.copy(
                        isReady = true,
                        settings = data.settings,
                        currentCity = city,
                        allCities = data.cities,
                        adhkarList = data.adhkar,
                        isOnline = data.online
                    )
                }
                ensureCacheObserverForCity(city)
                recomputeClockAndSchedule()
                if (data.online) {
                    refreshWeather(city, force = false)
                    triggerAutoPrayerSyncIfNeeded(city, force = false)
                }
            }
        }

        viewModelScope.launch {
            while (true) {
                if (_simulation.value.active) {
                    _simulation.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                }
                if (_manualAdhkarElapsed.value != null && !_adhkarPaused.value) {
                    val totalDur = _uiState.value.settings.adhkarTotalDurationSeconds.coerceAtLeast(15)
                    val next = (_manualAdhkarElapsed.value ?: 0) + 1
                    if (next >= totalDur) {
                        _manualAdhkarElapsed.value = null
                    } else {
                        _manualAdhkarElapsed.value = next
                    }
                }
                recomputeClockAndSchedule()
                delay(1000L)
            }
        }
    }

    private data class QuadData(
        val settings: GlobalSettingsEntity,
        val cities: List<CityConfig>,
        val adhkar: List<AdhkarEntity>,
        val online: Boolean
    )

    private fun ensureCacheObserverForCity(city: CityConfig) {
        val tz = TimeZone.getTimeZone(city.timeZoneId)
        val cal = GregorianCalendar(tz)
        val todayIso = PrayerTimeCalculator.formatDateIso(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        val tomorrowCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        val tomorrowIso = PrayerTimeCalculator.formatDateIso(
            tomorrowCal.get(Calendar.YEAR),
            tomorrowCal.get(Calendar.MONTH) + 1,
            tomorrowCal.get(Calendar.DAY_OF_MONTH)
        )
        val obsKey = "${city.cityId}|$todayIso|$tomorrowIso"
        if (observedCacheSignature == obsKey) return
        observedCacheSignature = obsKey

        cacheObserveJob?.cancel()
        cacheObserveJob = viewModelScope.launch {
            repository.observeCachedSchedules(city.cityId, listOf(todayIso, tomorrowIso)).collect { map ->
                _cachedSchedules.value = map
                recomputeClockAndSchedule()
            }
        }
    }

    private fun recomputeClockAndSchedule() {
        val state = _uiState.value
        val city = state.currentCity ?: return
        val settings = state.settings

        val tz = TimeZone.getTimeZone(city.timeZoneId)
        val cal = GregorianCalendar(tz)
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)
        val weekday = cal.get(Calendar.DAY_OF_WEEK)

        val tomorrowCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        val tYear = tomorrowCal.get(Calendar.YEAR)
        val tMonth = tomorrowCal.get(Calendar.MONTH) + 1
        val tDay = tomorrowCal.get(Calendar.DAY_OF_MONTH)

        val todayIso = PrayerTimeCalculator.formatDateIso(year, month, day)
        val tomorrowIso = PrayerTimeCalculator.formatDateIso(tYear, tMonth, tDay)

        ensureCacheObserverForCity(city)
        val sig = AlAdhanClient.buildParamsSignature(city)
        val cachedMap = _cachedSchedules.value
        val cachedToday = cachedMap[todayIso]?.takeIf { it.paramsSignature == sig }
        val cachedTomorrow = cachedMap[tomorrowIso]?.takeIf { it.paramsSignature == sig }

        val todaySchedule = PrayerTimeCalculator.calculateDay(
            city = city,
            year = year,
            month = month,
            day = day,
            syncedBaseMinutes = cachedToday?.toBaseMinutesMap(),
            syncedSourceLabel = cachedToday?.sourceName,
            syncedAtMillis = cachedToday?.syncedAtMillis
        )

        val tomorrowSchedule = PrayerTimeCalculator.calculateDay(
            city = city,
            year = tYear,
            month = tMonth,
            day = tDay,
            syncedBaseMinutes = cachedTomorrow?.toBaseMinutesMap(),
            syncedSourceLabel = cachedTomorrow?.sourceName,
            syncedAtMillis = cachedTomorrow?.syncedAtMillis
        )

        val hijri = HijriDateCalculator.toHijri(year, month, day, settings.hijriOffsetDays)
        val gregAr = HijriDateCalculator.formatGregorianAr(year, month, day)
        val weekdayAr = HijriDateCalculator.weekdayNameAr(weekday)

        val sim = _simulation.value
        val enabledAdhkar = state.adhkarList.filter { it.isEnabled }

        val phase = if (sim.active) {
            evaluateSimulatedPhase(sim, settings, enabledAdhkar)
        } else {
            val nowSec = hour24 * 3600 + minute * 60 + second
            MosquePhaseEngine.evaluatePhase(
                nowSecondOfDay = nowSec,
                todaySchedule = todaySchedule,
                tomorrowSchedule = tomorrowSchedule,
                settings = settings,
                enabledAdhkar = enabledAdhkar,
                dismissedPhaseKey = _dismissedPhaseKey.value,
                adhkarPaused = _adhkarPaused.value,
                manualAdhkarElapsedSeconds = _manualAdhkarElapsed.value
            )
        }

        if (phase is MosquePhase.AdhanActive && settings.adhanAudioEnabled) {
            val triggerKey = if (sim.active) {
                "SIM_${sim.prayer.name}"
            } else {
                "${todaySchedule.dateIso}_${phase.prayer.name}"
            }
            if (lastTriggeredAdhanKey != triggerKey) {
                lastTriggeredAdhanKey = triggerKey
                audioPlayer.playAdhan(
                    prayer = phase.prayer,
                    fajrCustomPath = settings.fajrAdhanAudioPath,
                    defaultCustomPath = settings.defaultAdhanAudioPath,
                    fajrToneKey = settings.fajrAdhanToneKey,
                    defaultToneKey = settings.defaultAdhanToneKey
                )
            }
        }

        _uiState.update {
            it.copy(
                todaySchedule = todaySchedule,
                tomorrowSchedule = tomorrowSchedule,
                currentYear = year,
                currentMonth = month,
                currentDay = day,
                currentHour24 = hour24,
                currentMinute = minute,
                currentSecond = second,
                weekdayAr = weekdayAr,
                gregorianDateAr = gregAr,
                hijriDate = hijri,
                phase = phase,
                lastPrayerSyncMillis = cachedToday?.syncedAtMillis ?: it.lastPrayerSyncMillis,
                isAudioPlaying = audioPlayer.isPlaying(),
                isSimulationActive = sim.active
            )
        }
    }

    private fun evaluateSimulatedPhase(
        sim: SimulationState,
        settings: GlobalSettingsEntity,
        enabledAdhkar: List<AdhkarEntity>
    ): MosquePhase {
        val adhanSec = 10
        val iqamaSec = 10
        val phoneOffSec = 8
        val adhkarItemSec = 5
        val adhkarTotalSec = (enabledAdhkar.size.coerceAtLeast(1) * adhkarItemSec).coerceAtMost(20)
        val t = sim.elapsedSeconds

        return when {
            t < adhanSec -> MosquePhase.AdhanActive(
                prayer = sim.prayer,
                message = "حان الآن موعد أذان ${sim.prayer.arabicName} (تجربة)",
                elapsedSeconds = t,
                totalSeconds = adhanSec
            )
            t < adhanSec + iqamaSec -> MosquePhase.IqamaCountdown(
                prayer = sim.prayer,
                remainingSeconds = (adhanSec + iqamaSec) - t,
                totalSeconds = iqamaSec
            )
            t < adhanSec + iqamaSec + phoneOffSec -> MosquePhase.TurnOffPhoneScreen(
                prayer = sim.prayer,
                remainingSeconds = (adhanSec + iqamaSec + phoneOffSec) - t,
                totalSeconds = phoneOffSec
            )
            enabledAdhkar.isNotEmpty() && t < adhanSec + iqamaSec + phoneOffSec + adhkarTotalSec -> {
                val e = t - (adhanSec + iqamaSec + phoneOffSec)
                val idx = (e / adhkarItemSec) % enabledAdhkar.size
                val item = enabledAdhkar[idx]
                MosquePhase.PostPrayerAdhkar(
                    prayer = sim.prayer,
                    dhikrText = item.textAr,
                    dhikrSource = item.sourceAr,
                    repeatLabel = item.repeatLabel,
                    currentIndex = idx,
                    totalItems = enabledAdhkar.size,
                    remainingSeconds = adhkarTotalSec - e,
                    isPaused = false
                )
            }
            else -> {
                _simulation.value = SimulationState(active = false)
                lastTriggeredAdhanKey = null
                audioPlayer.stop()
                MosquePhase.NormalCountdown(PrayerName.DHUHR, 0L)
            }
        }
    }

    // ==================== City & Astronomical Actions ====================

    fun selectCity(cityId: String) {
        viewModelScope.launch {
            _dismissedPhaseKey.value = null
            lastTriggeredAdhanKey = null
            repository.selectCity(cityId)
                .onSuccess { showBanner("✓ تم حفظ اختيار مدينة ${worldCitiesCatalogLookup(cityId)}") }
                .onFailure { showError("فشل تغيير المدينة: ${it.localizedMessage}") }
        }
    }

    private fun worldCitiesCatalogLookup(cityId: String): String =
        _uiState.value.allCities.find { it.cityId == cityId }?.cityAr ?: cityId

    fun updateCityConfig(
        successMessage: String? = null,
        transform: (CityConfig) -> CityConfig
    ) {
        val current = _uiState.value.currentCity ?: return
        viewModelScope.launch {
            val updated = transform(current)
            repository.saveCityConfig(updated)
                .onSuccess {
                    if (successMessage != null) {
                        showBanner(successMessage)
                    } else {
                        showBanner("✓ تم حفظ إعدادات ${updated.cityAr}")
                    }
                }
                .onFailure { showError("فشل حفظ إعدادات المدينة: ${it.localizedMessage}") }
        }
    }

    fun updateCityMosqueMetadata(mosqueName: String, district: String, street: String) {
        updateCityConfig(successMessage = "✓ تم حفظ بيانات المسجد للمدينة") { city ->
            city.copy(
                mosqueName = mosqueName,
                district = district,
                street = street
            )
        }
    }

    fun adjustPrayerOffset(prayer: PrayerName, deltaMinutes: Int) {
        val current = _uiState.value.currentCity ?: return
        val todayIso = _uiState.value.todaySchedule?.dateIso ?: ""
        val currentOffset = current.manualOffsetsMinutes[prayer] ?: 0
        val newOffset = (currentOffset + deltaMinutes).coerceIn(-120, 120)
        updateCityConfig { city ->
            val map = city.manualOffsetsMinutes.toMutableMap().apply { put(prayer, newOffset) }
            city.copy(
                manualOffsetsMinutes = map,
                temporaryOffsetDateIso = if (city.temporaryOffsetsOnly) todayIso else city.temporaryOffsetDateIso
            )
        }
    }

    fun setOffsetTemporaryMode(temporaryOnly: Boolean) {
        val todayIso = _uiState.value.todaySchedule?.dateIso ?: ""
        updateCityConfig { city ->
            city.copy(
                temporaryOffsetsOnly = temporaryOnly,
                temporaryOffsetDateIso = if (temporaryOnly) todayIso else null
            )
        }
    }

    fun resetSinglePrayerOffset(prayer: PrayerName) {
        val cityId = _uiState.value.currentCity?.cityId ?: return
        viewModelScope.launch {
            repository.resetSinglePrayerOffset(cityId, prayer)
                .onSuccess { showBanner("تمت إعادة وقت ${prayer.arabicName} للحساب الأصلي") }
                .onFailure { showError("تعذر إعادة التعيين") }
        }
    }

    fun resetAllCityOffsets() {
        val cityId = _uiState.value.currentCity?.cityId ?: return
        viewModelScope.launch {
            repository.resetAllPrayerOffsets(cityId)
                .onSuccess { showBanner("تمت إعادة جميع المواقيت للحساب الفلكي الأصلي") }
                .onFailure { showError("تعذر إعادة التعيين") }
        }
    }

    fun setIqamaMinutes(prayer: PrayerName, minutes: Int) {
        val clamped = minutes.coerceIn(0, 90)
        updateCityConfig { city ->
            val map = city.iqamaMinutes.toMutableMap().apply { put(prayer, clamped) }
            city.copy(iqamaMinutes = map)
        }
    }

    fun setCityCalculationParams(
        method: CalculationMethod? = null,
        madhab: AsrMadhab? = null,
        highLat: HighLatitudeRule? = null,
        dstMode: DstMode? = null,
        customFajrAngle: Double? = null,
        clearCustomFajrAngle: Boolean = false,
        customIshaAngle: Double? = null,
        clearCustomIshaAngle: Boolean = false,
        customIshaMinutes: Int? = null,
        clearCustomIshaMinutes: Boolean = false
    ) {
        updateCityConfig { city ->
            city.copy(
                calculationMethod = method ?: city.calculationMethod,
                asrMadhab = madhab ?: city.asrMadhab,
                highLatitudeRule = highLat ?: city.highLatitudeRule,
                dstMode = dstMode ?: city.dstMode,
                customFajrAngle = when {
                    clearCustomFajrAngle -> null
                    customFajrAngle != null -> customFajrAngle
                    method != null -> null
                    else -> city.customFajrAngle
                },
                customIshaAngle = when {
                    clearCustomIshaAngle -> null
                    customIshaAngle != null -> customIshaAngle
                    method != null -> null
                    else -> city.customIshaAngle
                },
                customIshaMinutes = when {
                    clearCustomIshaMinutes -> null
                    customIshaMinutes != null -> customIshaMinutes
                    method != null -> null
                    else -> city.customIshaMinutes
                }
            )
        }
    }

    fun addCustomCity(
        countryAr: String,
        countryEn: String,
        cityAr: String,
        cityEn: String,
        latitude: Double,
        longitude: Double,
        timeZoneId: String,
        method: CalculationMethod
    ) {
        viewModelScope.launch {
            val id = "custom_${System.currentTimeMillis()}"
            val preset = CityPreset(
                cityId = id,
                countryAr = countryAr.trim(),
                countryEn = countryEn.trim().ifBlank { countryAr.trim() },
                cityAr = cityAr.trim(),
                cityEn = cityEn.trim().ifBlank { cityAr.trim() },
                latitude = latitude,
                longitude = longitude,
                timeZoneId = timeZoneId.trim().ifBlank { TimeZone.getDefault().id },
                defaultMethod = method,
                isCustom = true
            )
            repository.addCustomCity(preset)
                .onSuccess {
                    repository.selectCity(it.cityId)
                    showBanner("✓ تمت إضافة ${it.cityAr} وحفظها بنجاح")
                }
                .onFailure { showError("فشل إضافة المدينة: ${it.localizedMessage}") }
        }
    }

    // ==================== Online Prayer Sync ====================

    fun syncPrayerTimesNow() {
        val city = _uiState.value.currentCity ?: return
        if (!_isOnline.value) {
            showError("لا يوجد اتصال بالإنترنت حالياً — يعمل التطبيق بالمحرك الفلكي المحلي والمخزن.")
            return
        }
        triggerAutoPrayerSyncIfNeeded(city, force = true)
    }

    private fun triggerAutoPrayerSyncIfNeeded(city: CityConfig, force: Boolean) {
        if (!_isOnline.value) return
        val tz = TimeZone.getTimeZone(city.timeZoneId)
        val cal = GregorianCalendar(tz)
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val syncKey = "${city.cityId}_${AlAdhanClient.buildParamsSignature(city)}_${PrayerTimeCalculator.formatDateIso(year, month, day)}"
        val now = System.currentTimeMillis()
        if (!force && lastPrayerSyncKey == syncKey && (now - lastPrayerSyncAttemptMillis) < 90_000L) {
            return
        }

        lastPrayerSyncKey = syncKey
        lastPrayerSyncAttemptMillis = now
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingPrayerTimes = true) }
            val resultToday = repository.syncPrayerScheduleIfOnline(city, year, month, day, force = force)
            val tCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
            repository.syncPrayerScheduleIfOnline(
                city,
                tCal.get(Calendar.YEAR),
                tCal.get(Calendar.MONTH) + 1,
                tCal.get(Calendar.DAY_OF_MONTH),
                force = force
            )
            _uiState.update { it.copy(isSyncingPrayerTimes = false) }
            if (force) {
                resultToday
                    .onSuccess { showBanner("✓ تمت مزامنة مواقيت الصلاة لمدينة ${city.cityAr} وحفظها محلياً") }
                    .onFailure { showError("تعذرت المزامنة حالياً — تم الإبقاء على الحساب الفلكي المحلي الموثوق") }
            }
        }
    }

    // ==================== Global Settings, Media, Backup & Safe Reset ====================

    fun updateSettings(
        successMessage: String? = "✓ تم حفظ الإعدادات بنجاح",
        transform: (GlobalSettingsEntity) -> GlobalSettingsEntity
    ) {
        viewModelScope.launch {
            repository.updateGlobalSettings(transform)
                .onSuccess { if (successMessage != null) showBanner(successMessage) }
                .onFailure { showError("فشل حفظ الإعدادات: ${it.localizedMessage}") }
        }
    }

    fun resolvePickedFileName(uri: Uri): String = repository.resolvePickedFileDisplayName(uri)

    fun savePickedMedia(slot: MediaSlot, uri: Uri) {
        viewModelScope.launch {
            repository.saveCustomMedia(slot, uri)
                .onSuccess { showBanner("✓ تم حفظ الملف في ذاكرة التطبيق الدائمة بنجاح") }
                .onFailure { showError("فشل حفظ الملف: ${it.localizedMessage}") }
        }
    }

    fun clearPickedMedia(slot: MediaSlot) {
        viewModelScope.launch {
            repository.clearCustomMedia(slot)
                .onSuccess { showBanner("تم حذف الملف المخصص واستعادة الافتراضي") }
                .onFailure { showError("فشل حذف الملف المخصص") }
        }
    }

    fun resetAllSettingsToDefaults() {
        viewModelScope.launch {
            repository.resetAllSettingsToDefaults(clearMediaFiles = true)
                .onSuccess { showBanner("✓ تمت استعادة الإعدادات الافتراضية بناءً على طلبك") }
                .onFailure { showError("تعذرت استعادة الإعدادات الافتراضية") }
        }
    }

    fun exportBackupToUri(uri: Uri) {
        viewModelScope.launch {
            val jsonRes = repository.exportBackupJson()
            jsonRes.onSuccess { json ->
                val writeRes = withContext(Dispatchers.IO) {
                    runCatching {
                        getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(json.toByteArray(Charsets.UTF_8))
                            out.flush()
                        } ?: error("تعذر فتح ملف التصدير")
                    }
                }
                writeRes
                    .onSuccess { showBanner("✓ تم تصدير النسخة الاحتياطية لجميع الإعدادات بنجاح") }
                    .onFailure { showError("تعذر كتابة ملف النسخة الاحتياطية") }
            }.onFailure {
                showError("فشل تجهيز النسخة الاحتياطية: ${it.localizedMessage}")
            }
        }
    }

    fun importBackupFromUri(uri: Uri) {
        viewModelScope.launch {
            val readRes = withContext(Dispatchers.IO) {
                runCatching {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                        input.bufferedReader(Charsets.UTF_8).readText()
                    } ?: error("تعذر قراءة الملف")
                }
            }
            readRes.onSuccess { rawJson ->
                repository.importBackupJson(rawJson)
                    .onSuccess { showBanner("✓ تم استيراد واستعادة النسخة الاحتياطية بنجاح") }
                    .onFailure { showError("ملف النسخة الاحتياطية غير صالح: ${it.localizedMessage}") }
            }.onFailure {
                showError("تعذر فتح الملف المحدد")
            }
        }
    }

    // ==================== Audio, Simulation & Adhkar Controls ====================

    fun previewAdhanAudio(prayer: PrayerName) {
        val s = _uiState.value.settings
        audioPlayer.playAdhan(
            prayer = prayer,
            fajrCustomPath = s.fajrAdhanAudioPath,
            defaultCustomPath = s.defaultAdhanAudioPath,
            fajrToneKey = s.fajrAdhanToneKey,
            defaultToneKey = s.defaultAdhanToneKey,
            onCompleted = { _uiState.update { it.copy(isAudioPlaying = false) } }
        )
        _uiState.update { it.copy(isAudioPlaying = true) }
    }

    fun previewPickedAudioUri(uri: Uri) {
        audioPlayer.playPreviewUri(uri) {
            _uiState.update { it.copy(isAudioPlaying = false) }
        }
        _uiState.update { it.copy(isAudioPlaying = true) }
    }

    fun previewBuiltinTone(toneKey: String) {
        audioPlayer.playBuiltinTone(toneKey) {
            _uiState.update { it.copy(isAudioPlaying = false) }
        }
        _uiState.update { it.copy(isAudioPlaying = true) }
    }

    fun stopAudio() {
        audioPlayer.stop()
        _uiState.update { it.copy(isAudioPlaying = false) }
    }

    fun startFullCycleSimulation(prayer: PrayerName = PrayerName.ASR) {
        lastTriggeredAdhanKey = null
        _dismissedPhaseKey.value = null
        _manualAdhkarElapsed.value = null
        _simulation.value = SimulationState(active = true, prayer = prayer, elapsedSeconds = 0)
    }

    fun stopSimulation() {
        _simulation.value = SimulationState(active = false)
        lastTriggeredAdhanKey = null
        audioPlayer.stop()
    }

    fun dismissCurrentPhase() {
        val state = _uiState.value
        val dateIso = state.todaySchedule?.dateIso ?: return
        when (val p = state.phase) {
            is MosquePhase.AdhanActive -> {
                audioPlayer.stop()
                if (state.isSimulationActive) {
                    _simulation.update { it.copy(elapsedSeconds = 10) }
                } else {
                    _dismissedPhaseKey.value = "${dateIso}_${p.prayer.name}_ADHAN"
                }
            }
            is MosquePhase.IqamaCountdown -> {
                if (state.isSimulationActive) {
                    _simulation.update { it.copy(elapsedSeconds = 20) }
                } else {
                    _dismissedPhaseKey.value = "${dateIso}_${p.prayer.name}_IQAMA"
                }
            }
            is MosquePhase.TurnOffPhoneScreen -> {
                if (state.isSimulationActive) {
                    _simulation.update { it.copy(elapsedSeconds = 28) }
                } else {
                    _dismissedPhaseKey.value = "${dateIso}_${p.prayer.name}_PHONEOFF"
                }
            }
            is MosquePhase.PostPrayerAdhkar -> {
                if (state.isSimulationActive) {
                    stopSimulation()
                } else {
                    _manualAdhkarElapsed.value = null
                    _dismissedPhaseKey.value = "${dateIso}_${p.prayer.name}_ADHKAR"
                }
            }
            is MosquePhase.NormalCountdown -> Unit
        }
        recomputeClockAndSchedule()
    }

    fun toggleManualAdhkarDisplay() {
        if (_manualAdhkarElapsed.value != null) {
            _manualAdhkarElapsed.value = null
            _adhkarPaused.value = false
        } else {
            _adhkarPaused.value = false
            _manualAdhkarElapsed.value = 0
        }
        recomputeClockAndSchedule()
    }

    fun toggleAdhkarPause() {
        _adhkarPaused.value = !_adhkarPaused.value
        recomputeClockAndSchedule()
    }

    fun nextAdhkarItem() {
        val perItem = _uiState.value.settings.adhkarItemDurationSeconds.coerceAtLeast(3)
        val cur = _manualAdhkarElapsed.value
        if (cur != null) {
            _manualAdhkarElapsed.value = cur + perItem
            recomputeClockAndSchedule()
        }
    }

    fun toggleAdhkarItemEnabled(item: AdhkarEntity) {
        viewModelScope.launch {
            repository.saveAdhkarItem(item.copy(isEnabled = !item.isEnabled))
                .onSuccess { showBanner("✓ تم تحديث حالة الذكر") }
        }
    }

    fun addCustomAdhkarItem(textAr: String, sourceAr: String, repeatLabel: String) {
        if (textAr.isBlank()) return
        viewModelScope.launch {
            val nextOrder = (_uiState.value.adhkarList.maxOfOrNull { it.sortOrder } ?: 0) + 1
            repository.saveAdhkarItem(
                AdhkarEntity(
                    textAr = textAr.trim(),
                    sourceAr = sourceAr.trim().ifBlank { "ذكر مضاف" },
                    repeatLabel = repeatLabel.trim().ifBlank { "مرة واحدة" },
                    sortOrder = nextOrder,
                    isEnabled = true
                )
            ).onSuccess { showBanner("✓ تمت إضافة الذكر بنجاح") }
        }
    }

    fun resetAdhkarToCatalog() {
        viewModelScope.launch {
            repository.resetAdhkarToCatalog()
                .onSuccess { showBanner("✓ تم تحميل أذكار ما بعد الصلاة الكاملة (10 أذكار)") }
                .onFailure { showError("تعذر تحديث قائمة الأذكار") }
        }
    }

    fun refreshWeatherNow() {
        _uiState.value.currentCity?.let { refreshWeather(it, force = true) }
    }

    private fun refreshWeather(city: CityConfig, force: Boolean) {
        if (!_isOnline.value) return
        val now = System.currentTimeMillis()
        val settings = _uiState.value.settings
        val sameCityFresh = settings.cachedWeatherCityId == city.cityId &&
            settings.cachedWeatherUpdatedAtMillis != null &&
            (now - settings.cachedWeatherUpdatedAtMillis) < 30 * 60 * 1000L

        if (!force && sameCityFresh) return
        if (!force && lastWeatherCityId == city.cityId && (now - lastWeatherAttemptMillis) < 60_000L) return

        lastWeatherCityId = city.cityId
        lastWeatherAttemptMillis = now
        viewModelScope.launch {
            repository.refreshWeatherIfOnline(city)
        }
    }

    fun clearBanner() {
        _uiState.update { it.copy(saveBannerMessage = null) }
    }

    private fun showBanner(message: String) {
        _uiState.update { it.copy(saveBannerMessage = message, saveBannerIsError = false) }
        viewModelScope.launch {
            delay(2800L)
            _uiState.update { if (it.saveBannerMessage == message) it.copy(saveBannerMessage = null) else it }
        }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(saveBannerMessage = message, saveBannerIsError = true) }
    }

    private fun registerNetworkObserver() {
        runCatching {
            val req = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(req, networkCallback)
        }
    }

    private fun checkCurrentlyOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
        runCatching { connectivityManager?.unregisterNetworkCallback(networkCallback) }
    }
}
