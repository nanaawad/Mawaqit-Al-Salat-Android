package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MosqueDao {

    @Query("SELECT * FROM global_settings WHERE id = 1")
    fun observeGlobalSettings(): Flow<GlobalSettingsEntity?>

    @Query("SELECT * FROM global_settings WHERE id = 1")
    suspend fun getGlobalSettingsOnce(): GlobalSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGlobalSettings(settings: GlobalSettingsEntity)

    @Query("SELECT * FROM city_configs")
    fun observeAllCities(): Flow<List<CityConfigEntity>>

    @Query("SELECT * FROM city_configs")
    suspend fun getAllCitiesOnce(): List<CityConfigEntity>

    @Query("SELECT * FROM city_configs WHERE cityId = :cityId")
    fun observeCityById(cityId: String): Flow<CityConfigEntity?>

    @Query("SELECT * FROM city_configs WHERE cityId = :cityId")
    suspend fun getCityByIdOnce(cityId: String): CityConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCity(city: CityConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCities(cities: List<CityConfigEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCitiesIgnore(cities: List<CityConfigEntity>)

    @Query("SELECT COUNT(*) FROM city_configs")
    suspend fun getCityCount(): Int

    @Query("SELECT * FROM adhkar_items ORDER BY sortOrder ASC, id ASC")
    fun observeAllAdhkar(): Flow<List<AdhkarEntity>>

    @Query("SELECT * FROM adhkar_items ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllAdhkarOnce(): List<AdhkarEntity>

    @Query("SELECT * FROM adhkar_items WHERE isEnabled = 1 ORDER BY sortOrder ASC, id ASC")
    suspend fun getEnabledAdhkarOnce(): List<AdhkarEntity>

    @Query("SELECT COUNT(*) FROM adhkar_items")
    suspend fun getAdhkarCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAdhkarList(items: List<AdhkarEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAdhkar(item: AdhkarEntity)

    @Query("DELETE FROM adhkar_items WHERE id = :id")
    suspend fun deleteAdhkarById(id: Int)

    @Query("DELETE FROM adhkar_items")
    suspend fun deleteAllAdhkar()

    @Query("SELECT * FROM cached_prayer_schedules WHERE cityId = :cityId AND dateIso IN (:dateIsos)")
    fun observeCachedSchedules(cityId: String, dateIsos: List<String>): Flow<List<CachedPrayerScheduleEntity>>

    @Query("SELECT * FROM cached_prayer_schedules WHERE cacheKey = :cacheKey")
    suspend fun getCachedSchedule(cacheKey: String): CachedPrayerScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCachedSchedule(schedule: CachedPrayerScheduleEntity)

    @Query("DELETE FROM cached_prayer_schedules WHERE dateIso < :oldestKeepIso")
    suspend fun deleteExpiredCaches(oldestKeepIso: String)
}
