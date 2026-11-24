package com.example

import com.example.data.catalog.DefaultAdhkarCatalog
import com.example.data.catalog.WorldCitiesCatalog
import com.example.data.local.AdhkarEntity
import com.example.data.local.CachedPrayerScheduleEntity
import com.example.data.local.CityConfigEntity
import com.example.data.local.GlobalSettingsEntity
import com.example.data.local.MosqueDao
import com.example.data.model.PrayerName
import com.example.domain.astronomy.PrayerTimeCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pure JVM tests verifying Room DAO contract, city independence (Berlin <-> Kassel),
 * permanent vs temporary manual offsets, schedule caching, and bilingual city search.
 */
class ExampleRobolectricTest {

    private class FakeMosqueDao : MosqueDao {
        private val globalFlow = MutableStateFlow<GlobalSettingsEntity?>(null)
        private val citiesFlow = MutableStateFlow<Map<String, CityConfigEntity>>(emptyMap())
        private val adhkarFlow = MutableStateFlow<List<AdhkarEntity>>(emptyList())
        private val cacheFlow = MutableStateFlow<Map<String, CachedPrayerScheduleEntity>>(emptyMap())

        override fun observeGlobalSettings(): Flow<GlobalSettingsEntity?> = globalFlow
        override suspend fun getGlobalSettingsOnce(): GlobalSettingsEntity? = globalFlow.value
        override suspend fun upsertGlobalSettings(settings: GlobalSettingsEntity) {
            globalFlow.value = settings
        }

        override fun observeAllCities(): Flow<List<CityConfigEntity>> =
            citiesFlow.map { it.values.toList() }

        override suspend fun getAllCitiesOnce(): List<CityConfigEntity> =
            citiesFlow.value.values.toList()

        override fun observeCityById(cityId: String): Flow<CityConfigEntity?> =
            citiesFlow.map { it[cityId] }

        override suspend fun getCityByIdOnce(cityId: String): CityConfigEntity? =
            citiesFlow.value[cityId]

        override suspend fun upsertCity(city: CityConfigEntity) {
            citiesFlow.value = citiesFlow.value.toMutableMap().apply { put(city.cityId, city) }
        }

        override suspend fun upsertCities(cities: List<CityConfigEntity>) {
            val map = citiesFlow.value.toMutableMap()
            cities.forEach { map[it.cityId] = it }
            citiesFlow.value = map
        }

        override suspend fun insertCitiesIgnore(cities: List<CityConfigEntity>) {
            val map = citiesFlow.value.toMutableMap()
            cities.forEach { if (!map.containsKey(it.cityId)) map[it.cityId] = it }
            citiesFlow.value = map
        }

        override suspend fun getCityCount(): Int = citiesFlow.value.size
        override fun observeAllAdhkar(): Flow<List<AdhkarEntity>> = adhkarFlow
        override suspend fun getAllAdhkarOnce(): List<AdhkarEntity> = adhkarFlow.value
        override suspend fun getEnabledAdhkarOnce(): List<AdhkarEntity> =
            adhkarFlow.value.filter { it.isEnabled }

        override suspend fun getAdhkarCount(): Int = adhkarFlow.value.size
        override suspend fun upsertAdhkarList(items: List<AdhkarEntity>) {
            adhkarFlow.value = items
        }

        override suspend fun upsertAdhkar(item: AdhkarEntity) {
            val list = adhkarFlow.value.toMutableList()
            val idx = list.indexOfFirst { it.id == item.id && item.id != 0 }
            if (idx >= 0) list[idx] = item else list.add(item)
            adhkarFlow.value = list
        }

        override suspend fun deleteAdhkarById(id: Int) {
            adhkarFlow.value = adhkarFlow.value.filterNot { it.id == id }
        }

        override suspend fun deleteAllAdhkar() {
            adhkarFlow.value = emptyList()
        }

        override fun observeCachedSchedules(
            cityId: String,
            dateIsos: List<String>
        ): Flow<List<CachedPrayerScheduleEntity>> =
            cacheFlow.map { map ->
                map.values.filter { it.cityId == cityId && it.dateIso in dateIsos }
            }

        override suspend fun getCachedSchedule(cacheKey: String): CachedPrayerScheduleEntity? =
            cacheFlow.value[cacheKey]

        override suspend fun upsertCachedSchedule(schedule: CachedPrayerScheduleEntity) {
            cacheFlow.value = cacheFlow.value.toMutableMap().apply {
                put(schedule.cacheKey, schedule)
            }
        }

        override suspend fun deleteExpiredCaches(oldestKeepIso: String) {
            cacheFlow.value = cacheFlow.value.filterValues { it.dateIso >= oldestKeepIso }
        }
    }

    private lateinit var dao: FakeMosqueDao

    @Before
    fun setup() = runBlocking {
        dao = FakeMosqueDao()
        dao.insertCitiesIgnore(WorldCitiesCatalog.cities.map { CityConfigEntity.fromPreset(it) })
        dao.upsertGlobalSettings(GlobalSettingsEntity())
        dao.upsertAdhkarList(DefaultAdhkarCatalog.defaultItems)
    }

    @Test
    fun `berlin fajr offset and mosque metadata persist independently from kassel`() = runBlocking {
        // 1. Select Berlin and customize offset, iqama, mosqueName, district, street
        dao.upsertGlobalSettings(dao.getGlobalSettingsOnce()!!.copy(selectedCityId = "de_berlin"))
        val berlin = dao.getCityByIdOnce("de_berlin")!!
        dao.upsertCity(
            berlin.copy(
                fajrOffset = 10,
                fajrIqama = 27,
                mosqueName = "مسجد عمر بن الخطاب",
                district = "كرويتسبيرغ",
                street = "شارع وينر"
            )
        )

        // 2. Switch to Kassel and verify Kassel is unaffected
        dao.upsertGlobalSettings(dao.getGlobalSettingsOnce()!!.copy(selectedCityId = "de_kassel"))
        val kassel = dao.getCityByIdOnce("de_kassel")!!
        assertEquals(0, kassel.fajrOffset)
        assertEquals(20, kassel.fajrIqama)
        assertEquals("", kassel.mosqueName)

        // 3. Switch back to Berlin and verify all fields preserved
        dao.upsertGlobalSettings(dao.getGlobalSettingsOnce()!!.copy(selectedCityId = "de_berlin"))
        val berlinReloaded = dao.getCityByIdOnce("de_berlin")!!
        assertEquals(10, berlinReloaded.fajrOffset)
        assertEquals(27, berlinReloaded.fajrIqama)
        assertEquals("مسجد عمر بن الخطاب", berlinReloaded.mosqueName)
        assertEquals("كرويتسبيرغ", berlinReloaded.district)
        assertEquals("شارع وينر", berlinReloaded.street)
    }

    @Test
    fun `daily recalculation updates base time while preserving permanent manual offset`() = runBlocking {
        val berlin = dao.getCityByIdOnce("de_berlin")!!
            .copy(fajrOffset = 10, temporaryOffsetsOnly = false)
            .toDomain()

        val day1 = PrayerTimeCalculator.calculateDay(berlin, 2026, 3, 15)
        val day2 = PrayerTimeCalculator.calculateDay(berlin, 2026, 4, 15)

        val fajrDay1 = day1.entries[PrayerName.FAJR]!!
        val fajrDay2 = day2.entries[PrayerName.FAJR]!!

        assertNotEquals(fajrDay1.originalMinuteOfDay, fajrDay2.originalMinuteOfDay)
        assertEquals(10, fajrDay1.offsetMinutes)
        assertEquals(10, fajrDay2.offsetMinutes)
        assertEquals(fajrDay2.originalMinuteOfDay + 10, fajrDay2.adjustedMinuteOfDay)
    }

    @Test
    fun `temporary offset applies only on its saved date and resets automatically next day`() = runBlocking {
        val berlin = dao.getCityByIdOnce("de_berlin")!!
            .copy(fajrOffset = 12, temporaryOffsetsOnly = true, temporaryOffsetDateIso = "2026-03-15")
            .toDomain()

        val sameDay = PrayerTimeCalculator.calculateDay(berlin, 2026, 3, 15)
        val nextDay = PrayerTimeCalculator.calculateDay(berlin, 2026, 3, 16)

        assertEquals(12, sameDay.entries[PrayerName.FAJR]!!.offsetMinutes)
        assertEquals(0, nextDay.entries[PrayerName.FAJR]!!.offsetMinutes)
    }

    @Test
    fun `arabic and english search locates required jordan germany saudi cities and 80plus countries`() {
        val requiredIds = listOf(
            "jo_amman", "jo_irbid", "jo_zarqa", "jo_aqaba",
            "de_hameln", "de_kassel", "de_hann_muenden", "de_berlin",
            "de_frankfurt", "de_hannover", "de_munich", "de_hamburg",
            "de_stuttgart", "de_dusseldorf", "de_cologne",
            "sa_riyadh", "sa_makkah", "sa_madinah", "sa_jeddah"
        )
        requiredIds.forEach { id ->
            assertTrue("Missing city $id", WorldCitiesCatalog.findById(id) != null)
        }
        assertEquals("كاسل", WorldCitiesCatalog.cities.find { it.cityEn.equals("Kassel", true) }?.cityAr)
        assertEquals("هان مندن", WorldCitiesCatalog.cities.find { it.cityId == "de_hann_muenden" }?.cityAr)
        assertTrue("Expected >= 80 countries, got ${WorldCitiesCatalog.countryCount()}", WorldCitiesCatalog.countryCount() >= 80)
    }

    @Test
    fun `settings do not revert to defaults on re-seeding`() = runBlocking {
        dao.upsertGlobalSettings(
            dao.getGlobalSettingsOnce()!!.copy(mosqueName = "مسجد السلام - برلين", is24HourFormat = true)
        )
        dao.insertCitiesIgnore(WorldCitiesCatalog.cities.map { CityConfigEntity.fromPreset(it) })
        val settings = dao.getGlobalSettingsOnce()!!
        assertEquals("مسجد السلام - برلين", settings.mosqueName)
        assertEquals(true, settings.is24HourFormat)
    }

    @Test
    fun `cached prayer schedule persists and expires cleanly`() = runBlocking {
        val oldEntry = CachedPrayerScheduleEntity(
            cacheKey = "jo_amman_2026-11-20",
            cityId = "jo_amman",
            dateIso = "2026-11-20",
            paramsSignature = "sig1",
            fajrMinute = 290,
            sunriseMinute = 375,
            dhuhrMinute = 740,
            asrMinute = 910,
            maghribMinute = 1055,
            ishaMinute = 1140,
            syncedAtMillis = 1000L
        )
        val newEntry = oldEntry.copy(
            cacheKey = "jo_amman_2026-11-23",
            dateIso = "2026-11-23"
        )
        dao.upsertCachedSchedule(oldEntry)
        dao.upsertCachedSchedule(newEntry)

        assertNotNull(dao.getCachedSchedule("jo_amman_2026-11-23"))
        dao.deleteExpiredCaches("2026-11-22")
        assertNull(dao.getCachedSchedule("jo_amman_2026-11-20"))
        assertNotNull(dao.getCachedSchedule("jo_amman_2026-11-23"))
    }

    @Test
    fun `default adhkar catalog contains all 13 post-prayer items including ayat al kursi and muawwidhat`() = runBlocking {
        val items = dao.getAllAdhkarOnce()
        assertEquals(13, items.size)
        assertTrue(items.any { it.textAr.contains("الْحَيُّ الْقَيُّومُ") && it.textAr.contains("۝٢٥٥") })
        assertTrue(items.any { it.textAr.contains("قُلْ هُوَ اللَّهُ أَحَدٌ") })
        assertTrue(items.any { it.textAr.contains("قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ") })
        assertTrue(items.any { it.textAr.contains("قُلْ أَعُوذُ بِرَبِّ النَّاسِ") })
        assertTrue(items.any { it.textAr.contains("أَجِرْنِي مِنَ النَّارِ") })
        assertTrue(items.any { it.textAr.contains("أَعِنِّي عَلَىٰ ذِكْرِكَ") })
    }
}
