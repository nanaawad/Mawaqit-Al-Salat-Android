package com.example.domain.phase

import com.example.data.local.AdhkarEntity
import com.example.data.local.GlobalSettingsEntity
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.MosquePhase
import com.example.data.model.PrayerName

object MosquePhaseEngine {

    /**
     * Evaluates the exact real-time mosque display phase for a given second-of-day
     * against today's prayer schedule, iqama durations, turn-off-phone window, and post-prayer adhkar.
     */
    fun evaluatePhase(
        nowSecondOfDay: Int,
        todaySchedule: DailyPrayerSchedule,
        tomorrowSchedule: DailyPrayerSchedule,
        settings: GlobalSettingsEntity,
        enabledAdhkar: List<AdhkarEntity>,
        dismissedPhaseKey: String? = null,
        adhkarPaused: Boolean = false,
        manualAdhkarElapsedSeconds: Int? = null
    ): MosquePhase {
        // Manual Adhkar preview/override if triggered from UI
        if (manualAdhkarElapsedSeconds != null && enabledAdhkar.isNotEmpty()) {
            return buildAdhkarPhase(
                prayer = PrayerName.DHUHR,
                elapsedInAdhkar = manualAdhkarElapsedSeconds,
                settings = settings,
                enabledAdhkar = enabledAdhkar,
                isPaused = adhkarPaused
            )
        }

        // Check real-time prayer event windows for the 5 obligatory prayers
        for (prayer in PrayerName.fiveDailyPrayers) {
            val entry = todaySchedule.entries[prayer] ?: continue
            val adhanSec = entry.adjustedMinuteOfDay * 60
            val iqamaDurationSec = (entry.iqamaMinutes * 60).coerceAtLeast(0)
            val effectiveAdhanSec = if (iqamaDurationSec > 0) {
                settings.adhanDisplaySeconds.coerceIn(10, (iqamaDurationSec - 1).coerceAtLeast(10))
            } else {
                settings.adhanDisplaySeconds.coerceAtLeast(10)
            }
            val iqamaTargetSec = if (iqamaDurationSec > 0) {
                adhanSec + iqamaDurationSec.coerceAtLeast(effectiveAdhanSec)
            } else {
                adhanSec + effectiveAdhanSec
            }
            val phoneOffEndSec = iqamaTargetSec + settings.phoneOffDurationSeconds.coerceAtLeast(0)
            val adhkarStartSec = phoneOffEndSec + settings.adhkarStartDelaySeconds.coerceAtLeast(0)
            val adhkarEndSec = adhkarStartSec + settings.adhkarTotalDurationSeconds.coerceAtLeast(0)

            val phaseKeyPrefix = "${todaySchedule.dateIso}_${prayer.name}"

            // 1. Adhan window
            if (nowSecondOfDay in adhanSec until (adhanSec + effectiveAdhanSec)) {
                val key = "${phaseKeyPrefix}_ADHAN"
                if (dismissedPhaseKey != key) {
                    return MosquePhase.AdhanActive(
                        prayer = prayer,
                        message = "حان الآن موعد أذان ${prayer.arabicName}",
                        elapsedSeconds = nowSecondOfDay - adhanSec,
                        totalSeconds = effectiveAdhanSec
                    )
                }
            }

            // 2. Iqama countdown window
            if (iqamaDurationSec > 0 && nowSecondOfDay in (adhanSec + effectiveAdhanSec) until iqamaTargetSec) {
                val key = "${phaseKeyPrefix}_IQAMA"
                if (dismissedPhaseKey != key) {
                    return MosquePhase.IqamaCountdown(
                        prayer = prayer,
                        remainingSeconds = iqamaTargetSec - nowSecondOfDay,
                        totalSeconds = iqamaDurationSec
                    )
                }
            }

            // 3. Turn-off-phone window
            if (settings.phoneOffDurationSeconds > 0 && nowSecondOfDay in iqamaTargetSec until phoneOffEndSec) {
                val key = "${phaseKeyPrefix}_PHONEOFF"
                if (dismissedPhaseKey != key) {
                    return MosquePhase.TurnOffPhoneScreen(
                        prayer = prayer,
                        remainingSeconds = phoneOffEndSec - nowSecondOfDay,
                        totalSeconds = settings.phoneOffDurationSeconds
                    )
                }
            }

            // 4. Post-prayer Adhkar window
            if (settings.adhkarEnabled && enabledAdhkar.isNotEmpty() &&
                settings.adhkarTotalDurationSeconds > 0 &&
                nowSecondOfDay in adhkarStartSec until adhkarEndSec
            ) {
                val key = "${phaseKeyPrefix}_ADHKAR"
                if (dismissedPhaseKey != key) {
                    return buildAdhkarPhase(
                        prayer = prayer,
                        elapsedInAdhkar = nowSecondOfDay - adhkarStartSec,
                        settings = settings,
                        enabledAdhkar = enabledAdhkar,
                        isPaused = adhkarPaused
                    )
                }
            }
        }

        // Default: Normal countdown to next prayer (including Sunrise or 5 prayers -> next day Fajr)
        val nextPrayerCandidates = listOf(
            PrayerName.FAJR,
            PrayerName.SUNRISE,
            PrayerName.DHUHR,
            PrayerName.ASR,
            PrayerName.MAGHRIB,
            PrayerName.ISHA
        )

        for (p in nextPrayerCandidates) {
            val targetSec = (todaySchedule.entries[p]?.adjustedMinuteOfDay ?: continue) * 60
            if (targetSec > nowSecondOfDay) {
                return MosquePhase.NormalCountdown(
                    nextPrayer = p,
                    remainingSeconds = (targetSec - nowSecondOfDay).toLong()
                )
            }
        }

        val tomorrowFajrSec = (tomorrowSchedule.entries[PrayerName.FAJR]?.adjustedMinuteOfDay ?: (5 * 60)) * 60
        val remaining = (86400 - nowSecondOfDay) + tomorrowFajrSec
        return MosquePhase.NormalCountdown(
            nextPrayer = PrayerName.FAJR,
            remainingSeconds = remaining.toLong()
        )
    }

    private fun buildAdhkarPhase(
        prayer: PrayerName,
        elapsedInAdhkar: Int,
        settings: GlobalSettingsEntity,
        enabledAdhkar: List<AdhkarEntity>,
        isPaused: Boolean
    ): MosquePhase.PostPrayerAdhkar {
        val perItem = settings.adhkarItemDurationSeconds.coerceAtLeast(3)
        val totalDur = settings.adhkarTotalDurationSeconds.coerceAtLeast(perItem)
        val index = (elapsedInAdhkar / perItem) % enabledAdhkar.size
        val item = enabledAdhkar[index]
        return MosquePhase.PostPrayerAdhkar(
            prayer = prayer,
            dhikrText = item.textAr,
            dhikrSource = item.sourceAr,
            repeatLabel = item.repeatLabel,
            currentIndex = index,
            totalItems = enabledAdhkar.size,
            remainingSeconds = (totalDur - elapsedInAdhkar).coerceAtLeast(0),
            isPaused = isPaused
        )
    }
}
