package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CityConfigEntity::class,
        GlobalSettingsEntity::class,
        AdhkarEntity::class,
        CachedPrayerScheduleEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class MosqueDatabase : RoomDatabase() {
    abstract fun mosqueDao(): MosqueDao

    companion object {
        @Volatile
        private var INSTANCE: MosqueDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Preserve all user-saved cities while adding per-city mosque & calculation fields
                db.execSQL("ALTER TABLE city_configs ADD COLUMN mosqueName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE city_configs ADD COLUMN district TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE city_configs ADD COLUMN street TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE city_configs ADD COLUMN dstModeName TEXT NOT NULL DEFAULT 'AUTO'")
                db.execSQL("ALTER TABLE city_configs ADD COLUMN customFajrAngle REAL")
                db.execSQL("ALTER TABLE city_configs ADD COLUMN customIshaAngle REAL")
                db.execSQL("ALTER TABLE city_configs ADD COLUMN customIshaMinutes INTEGER")

                // Preserve all user-saved global settings while adding v3 color/border/brightness/tone fields
                db.execSQL("ALTER TABLE global_settings ADD COLUMN clockAccentColor INTEGER NOT NULL DEFAULT 4292851523")
                db.execSQL("ALTER TABLE global_settings ADD COLUMN boxBorderColor INTEGER NOT NULL DEFAULT 4287525928")
                db.execSQL("ALTER TABLE global_settings ADD COLUMN dateTextColor INTEGER NOT NULL DEFAULT 4291141688")
                db.execSQL("ALTER TABLE global_settings ADD COLUMN uiButtonBgColor INTEGER NOT NULL DEFAULT 4279576878")
                db.execSQL("ALTER TABLE global_settings ADD COLUMN uiButtonTextColor INTEGER NOT NULL DEFAULT 4291141688")
                db.execSQL("ALTER TABLE global_settings ADD COLUMN bgBrightnessAlpha REAL NOT NULL DEFAULT 0.25")
                db.execSQL("ALTER TABLE global_settings ADD COLUMN clockBoxBgImagePath TEXT")
                db.execSQL("ALTER TABLE global_settings ADD COLUMN fajrAdhanToneKey TEXT NOT NULL DEFAULT 'builtin_fajr'")
                db.execSQL("ALTER TABLE global_settings ADD COLUMN defaultAdhanToneKey TEXT NOT NULL DEFAULT 'builtin_default'")

                // Offline/Online prayer time cache table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cached_prayer_schedules (
                        cacheKey TEXT NOT NULL PRIMARY KEY,
                        cityId TEXT NOT NULL,
                        dateIso TEXT NOT NULL,
                        paramsSignature TEXT NOT NULL,
                        fajrMinute INTEGER NOT NULL,
                        sunriseMinute INTEGER NOT NULL,
                        dhuhrMinute INTEGER NOT NULL,
                        asrMinute INTEGER NOT NULL,
                        maghribMinute INTEGER NOT NULL,
                        ishaMinute INTEGER NOT NULL,
                        syncedAtMillis INTEGER NOT NULL,
                        sourceName TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Only transition unmodified v3 default palette to the requested Mawaqit Frosted Purple palette.
                // If user customized any color, leave all saved user colors completely untouched!
                db.execSQL(
                    """
                    UPDATE global_settings SET
                        screenBgColor = ${0xFF090B12L},
                        clockBoxBgColor = ${0xD15B1B99L},
                        clockBoxTextColor = ${0xFFFFFFFFL},
                        prayerBoxBgColor = ${0xC212161FL},
                        prayerBoxTextColor = ${0xFFFFFFFFL},
                        activePrayerAccentColor = ${0xD66A1B9AL},
                        nextPrayerBoxBgColor = ${0xDB141720L},
                        nextPrayerBoxTextColor = ${0xFFFFFFFFL},
                        sunriseBoxBgColor = ${0x00000000L},
                        sunriseBoxTextColor = ${0xFFFFFFFFL},
                        jumuahBoxBgColor = ${0x00000000L},
                        jumuahBoxTextColor = ${0xFFFFFFFFL},
                        headerBgColor = ${0x00000000L},
                        headerTextColor = ${0xFFFFFFFFL},
                        clockAccentColor = ${0xFFEAE0FFL},
                        boxBorderColor = ${0x38FFFFFFL},
                        dateTextColor = ${0xFFFFFFFFL},
                        uiButtonBgColor = ${0x66000000L},
                        uiButtonTextColor = ${0xFFFFFFFFL},
                        bgBrightnessAlpha = 0.20
                    WHERE screenBgColor = ${0xFF07120EL} AND clockBoxTextColor = ${0xFFC5A038L}
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): MosqueDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MosqueDatabase::class.java,
                    "mosque_prayer_times.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
