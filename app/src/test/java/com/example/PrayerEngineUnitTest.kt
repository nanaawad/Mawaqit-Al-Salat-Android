package com.example

import com.example.data.catalog.DefaultAdhkarCatalog
import com.example.data.catalog.WorldCitiesCatalog
import com.example.data.local.CityConfigEntity
import com.example.data.local.GlobalSettingsEntity
import com.example.data.model.AsrMadhab
import com.example.data.model.DstMode
import com.example.data.model.MosquePhase
import com.example.data.model.PrayerName
import com.example.data.remote.AlAdhanClient
import com.example.data.remote.AlAdhanTimingsDto
import com.example.domain.astronomy.HijriDateCalculator
import com.example.domain.astronomy.PrayerTimeCalculator
import com.example.domain.phase.MosquePhaseEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerEngineUnitTest {

    private fun presetToConfig(id: String) =
        CityConfigEntity.fromPreset(WorldCitiesCatalog.findById(id)!!).toDomain()

    @Test
    fun `amman berlin and makkah produce distinct schedules and dst is detected`() {
        val amman = presetToConfig("jo_amman")
        val berlin = presetToConfig("de_berlin")
        val makkah = presetToConfig("sa_makkah")

        // Summer date (July 15): Berlin is in CEST (DST active)
        val berlinSummer = PrayerTimeCalculator.calculateDay(berlin, 2026, 7, 15)
        val berlinWinter = PrayerTimeCalculator.calculateDay(berlin, 2026, 1, 15)
        assertTrue(berlinSummer.isDstActive)
        assertFalse(berlinWinter.isDstActive)

        val ammanSummer = PrayerTimeCalculator.calculateDay(amman, 2026, 7, 15)
        val makkahSummer = PrayerTimeCalculator.calculateDay(makkah, 2026, 7, 15)

        assertNotEquals(
            ammanSummer.getAdjustedMinutes(PrayerName.DHUHR),
            berlinSummer.getAdjustedMinutes(PrayerName.DHUHR)
        )
        assertNotEquals(
            makkahSummer.getAdjustedMinutes(PrayerName.MAGHRIB),
            ammanSummer.getAdjustedMinutes(PrayerName.MAGHRIB)
        )

        // Sanity bounds for Amman in July: Fajr ~ 03:45-04:30, Dhuhr ~ 12:30-13:00, Maghrib ~ 19:30-20:00
        val ammanFajr = ammanSummer.getAdjustedMinutes(PrayerName.FAJR)
        val ammanDhuhr = ammanSummer.getAdjustedMinutes(PrayerName.DHUHR)
        val ammanMaghrib = ammanSummer.getAdjustedMinutes(PrayerName.MAGHRIB)
        assertTrue("Amman Fajr=$ammanFajr", ammanFajr in 210..290)
        assertTrue("Amman Dhuhr=$ammanDhuhr", ammanDhuhr in 740..790)
        assertTrue("Amman Maghrib=$ammanMaghrib", ammanMaghrib in 1150..1210)
    }

    @Test
    fun `hanafi asr is later than shafi asr`() {
        val shafi = presetToConfig("jo_amman").copy(asrMadhab = AsrMadhab.SHAFI)
        val hanafi = shafi.copy(asrMadhab = AsrMadhab.HANAFI)

        val sDay = PrayerTimeCalculator.calculateDay(shafi, 2026, 5, 10)
        val hDay = PrayerTimeCalculator.calculateDay(hanafi, 2026, 5, 10)

        assertTrue(hDay.getAdjustedMinutes(PrayerName.ASR) > sDay.getAdjustedMinutes(PrayerName.ASR))
    }

    @Test
    fun `jumuah follows dhuhr unless custom time is enabled`() {
        val amman = presetToConfig("jo_amman")
        val dayFollow = PrayerTimeCalculator.calculateDay(amman, 2026, 11, 20)
        assertEquals(
            dayFollow.getAdjustedMinutes(PrayerName.DHUHR),
            dayFollow.getAdjustedMinutes(PrayerName.JUMUAH)
        )

        val customJumuah = amman.copy(jumuahFollowsDhuhr = false, jumuahCustomHour = 13, jumuahCustomMinute = 15)
        val dayCustom = PrayerTimeCalculator.calculateDay(customJumuah, 2026, 11, 20)
        assertEquals(13 * 60 + 15, dayCustom.getAdjustedMinutes(PrayerName.JUMUAH))
    }

    @Test
    fun `phase engine transitions through adhan iqama phone off and adhkar in real time`() {
        val amman = presetToConfig("jo_amman")
        val today = PrayerTimeCalculator.calculateDay(amman, 2026, 11, 23)
        val tomorrow = PrayerTimeCalculator.calculateDay(amman, 2026, 11, 24)
        val settings = GlobalSettingsEntity(
            adhanDisplaySeconds = 120,
            phoneOffDurationSeconds = 20,
            adhkarEnabled = true,
            adhkarStartDelaySeconds = 10,
            adhkarTotalDurationSeconds = 120,
            adhkarItemDurationSeconds = 15
        )
        val adhkar = DefaultAdhkarCatalog.defaultItems

        val dhuhrSec = today.getAdjustedMinutes(PrayerName.DHUHR) * 60
        val iqamaSec = today.entries[PrayerName.DHUHR]!!.iqamaMinutes * 60

        // 1. Before Dhuhr -> NormalCountdown to Dhuhr
        val pBefore = MosquePhaseEngine.evaluatePhase(dhuhrSec - 30, today, tomorrow, settings, adhkar)
        assertTrue(pBefore is MosquePhase.NormalCountdown)
        assertEquals(PrayerName.DHUHR, (pBefore as MosquePhase.NormalCountdown).nextPrayer)
        assertEquals(30L, pBefore.remainingSeconds)

        // 2. At Dhuhr -> AdhanActive
        val pAdhan = MosquePhaseEngine.evaluatePhase(dhuhrSec + 5, today, tomorrow, settings, adhkar)
        assertTrue(pAdhan is MosquePhase.AdhanActive)
        assertEquals(PrayerName.DHUHR, (pAdhan as MosquePhase.AdhanActive).prayer)

        // 3. After Adhan -> IqamaCountdown
        val pIqama = MosquePhaseEngine.evaluatePhase(dhuhrSec + 130, today, tomorrow, settings, adhkar)
        assertTrue(pIqama is MosquePhase.IqamaCountdown)
        assertEquals(iqamaSec - 130, (pIqama as MosquePhase.IqamaCountdown).remainingSeconds)

        // 4. At Iqama == 0 -> TurnOffPhoneScreen
        val pPhoneOff = MosquePhaseEngine.evaluatePhase(dhuhrSec + iqamaSec + 5, today, tomorrow, settings, adhkar)
        assertTrue(pPhoneOff is MosquePhase.TurnOffPhoneScreen)

        // 5. After phoneOff + startDelay -> PostPrayerAdhkar
        val pAdhkar = MosquePhaseEngine.evaluatePhase(dhuhrSec + iqamaSec + 20 + 15, today, tomorrow, settings, adhkar)
        assertTrue(pAdhkar is MosquePhase.PostPrayerAdhkar)
    }

    @Test
    fun `hijri converter produces valid arabic date and supports day offset`() {
        val h0 = HijriDateCalculator.toHijri(2026, 11, 23, 0)
        val h1 = HijriDateCalculator.toHijri(2026, 11, 23, 1)
        assertTrue(h0.year >= 1447)
        assertTrue(h0.formattedAr.endsWith("هـ"))
        assertNotEquals(h0.formattedAr, h1.formattedAr)
    }

    @Test
    fun `aladhan parser validates strictly ascending timings and strips timezone tags`() {
        val valid = AlAdhanClient.parseAndValidateTimings(
            AlAdhanTimingsDto(
                fajr = "04:15 (CEST)",
                sunrise = "05:45 (CEST)",
                dhuhr = "13:10",
                asr = "16:50",
                maghrib = "20:20",
                isha = "21:55"
            )
        )
        assertNotNull(valid)
        assertEquals(4 * 60 + 15, valid!!.fajrMinute)
        assertEquals(21 * 60 + 55, valid.ishaMinute)

        val invalidOrder = AlAdhanClient.parseAndValidateTimings(
            AlAdhanTimingsDto(
                fajr = "06:15",
                sunrise = "05:45",
                dhuhr = "13:10",
                asr = "16:50",
                maghrib = "20:20",
                isha = "21:55"
            )
        )
        assertNull(invalidOrder)

        val malformed = AlAdhanClient.parseAndValidateTimings(
            AlAdhanTimingsDto("99:99", "05:45", "13:10", "16:50", "20:20", "21:55")
        )
        assertNull(malformed)
    }

    @Test
    fun `dst mode override shifts times by 60 minutes and custom angles take effect`() {
        val berlinWinter = presetToConfig("de_berlin").copy(dstMode = DstMode.FORCE_WINTER)
        val berlinSummer = berlinWinter.copy(dstMode = DstMode.FORCE_SUMMER)

        val wDay = PrayerTimeCalculator.calculateDay(berlinWinter, 2026, 1, 15)
        val sDay = PrayerTimeCalculator.calculateDay(berlinSummer, 2026, 1, 15)

        assertFalse(wDay.isDstActive)
        assertTrue(sDay.isDstActive)
        assertEquals(60, sDay.getAdjustedMinutes(PrayerName.DHUHR) - wDay.getAdjustedMinutes(PrayerName.DHUHR))

        val normalAngle = presetToConfig("jo_amman").copy(customFajrAngle = 15.0)
        val wideAngle = presetToConfig("jo_amman").copy(customFajrAngle = 19.5)
        val nDay = PrayerTimeCalculator.calculateDay(normalAngle, 2026, 4, 10)
        val lDay = PrayerTimeCalculator.calculateDay(wideAngle, 2026, 4, 10)
        assertTrue(lDay.getAdjustedMinutes(PrayerName.FAJR) < nDay.getAdjustedMinutes(PrayerName.FAJR))
    }

    @Test
    fun `synced base schedule overrides astronomical base while keeping manual offsets`() {
        val amman = presetToConfig("jo_amman").copy(
            manualOffsetsMinutes = mapOf(PrayerName.FAJR to 5)
        )
        val syncedBase = mapOf(
            PrayerName.FAJR to 310,
            PrayerName.SUNRISE to 390,
            PrayerName.DHUHR to 745,
            PrayerName.ASR to 920,
            PrayerName.MAGHRIB to 1065,
            PrayerName.ISHA to 1150
        )
        val sched = PrayerTimeCalculator.calculateDay(
            city = amman,
            year = 2026,
            month = 11,
            day = 23,
            syncedBaseMinutes = syncedBase,
            syncedAtMillis = 123456L
        )
        assertEquals("مزامنة AlAdhan الموثوقة", sched.sourceLabel)
        assertEquals(310, sched.entries[PrayerName.FAJR]!!.originalMinuteOfDay)
        assertEquals(315, sched.entries[PrayerName.FAJR]!!.adjustedMinuteOfDay)
    }
}
