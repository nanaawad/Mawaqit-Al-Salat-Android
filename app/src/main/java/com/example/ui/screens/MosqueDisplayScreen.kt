package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CityConfig
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.MosquePhase
import com.example.data.model.PrayerName
import com.example.ui.components.AdhanOverlay
import com.example.ui.components.IqamaCountdownOverlay
import com.example.ui.components.MosqueBackgroundLayer
import com.example.ui.components.PostPrayerAdhkarOverlay
import com.example.ui.components.TimeFormatters
import com.example.ui.components.TurnOffPhoneOverlay
import com.example.ui.theme.MosqueGoldAccent
import com.example.ui.theme.SettingsCardDark
import com.example.ui.theme.ensureReadableColor
import com.example.ui.viewmodel.MosqueUiState

private val subtleTextShadow = Shadow(
    color = Color.Black.copy(alpha = 0.72f),
    offset = Offset(0f, 2f),
    blurRadius = 6f
)

@Composable
fun MosqueDisplayScreen(
    state: MosqueUiState,
    onOpenSettings: () -> Unit,
    onOpenCitySelector: () -> Unit,
    onSyncPrayerTimesNow: () -> Unit,
    onToggleAdhkar: () -> Unit,
    onTriggerTestCycle: () -> Unit,
    onStopAudio: () -> Unit,
    onDismissPhase: () -> Unit,
    onToggleAdhkarPause: () -> Unit,
    onNextAdhkar: () -> Unit,
    onAdjustPrayerOffset: (PrayerName, Int) -> Unit,
    onResetPrayerOffset: (PrayerName) -> Unit,
    onSetIqamaMinutes: (PrayerName, Int) -> Unit
) {
    val settings = state.settings
    val city = state.currentCity
    val schedule = state.todaySchedule
    var quickEditPrayer by remember { mutableStateOf<PrayerName?>(null) }

    val screenBg = TimeFormatters.colorFromLong(settings.screenBgColor)
    val boxBorder = TimeFormatters.colorFromLong(settings.boxBorderColor)
    val clockBg = TimeFormatters.colorFromLong(settings.clockBoxBgColor)
    val clockText = ensureReadableColor(TimeFormatters.colorFromLong(settings.clockBoxTextColor), clockBg, screenBg)
    val clockAccent = ensureReadableColor(TimeFormatters.colorFromLong(settings.clockAccentColor), clockBg, screenBg)
    val dateText = ensureReadableColor(TimeFormatters.colorFromLong(settings.dateTextColor), clockBg, screenBg)

    val sunriseBg = TimeFormatters.colorFromLong(settings.sunriseBoxBgColor)
    val sunriseText = ensureReadableColor(TimeFormatters.colorFromLong(settings.sunriseBoxTextColor), sunriseBg, screenBg)
    val jumuahBg = TimeFormatters.colorFromLong(settings.jumuahBoxBgColor)
    val jumuahText = ensureReadableColor(TimeFormatters.colorFromLong(settings.jumuahBoxTextColor), jumuahBg, screenBg)

    val nextBg = TimeFormatters.colorFromLong(settings.nextPrayerBoxBgColor)
    val nextText = ensureReadableColor(TimeFormatters.colorFromLong(settings.nextPrayerBoxTextColor), nextBg, screenBg)

    val prayerBg = TimeFormatters.colorFromLong(settings.prayerBoxBgColor)
    val prayerText = ensureReadableColor(TimeFormatters.colorFromLong(settings.prayerBoxTextColor), prayerBg, screenBg)
    val activeAccent = TimeFormatters.colorFromLong(settings.activePrayerAccentColor)

    val headerBg = TimeFormatters.colorFromLong(settings.headerBgColor)
    val headerText = ensureReadableColor(TimeFormatters.colorFromLong(settings.headerTextColor), headerBg, screenBg)
    val uiBtnBg = TimeFormatters.colorFromLong(settings.uiButtonBgColor)
    val uiBtnText = ensureReadableColor(TimeFormatters.colorFromLong(settings.uiButtonTextColor), uiBtnBg, screenBg)

    val effectiveMosqueName = city?.mosqueName?.takeIf { it.isNotBlank() }
        ?: settings.mosqueName.ifBlank { "مسجد النور" }
    val addressLine = listOfNotNull(
        city?.district?.takeIf { it.isNotBlank() },
        city?.street?.takeIf { it.isNotBlank() }
    ).joinToString(" — ")

    val isDefaultScreenBg = settings.screenBgColor == 0xFF07120EL || settings.screenBgColor == 0xFF090B12L
    val isLightBackdrop = screenBg.red * 0.299f + screenBg.green * 0.587f + screenBg.blue * 0.114f > 0.5f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .testTag("mosque_main_screen")
    ) {
        MosqueBackgroundLayer(
            customFilePath = settings.mainBgImagePath,
            defaultDrawableRes = R.drawable.img_bg_mosque,
            fallbackColor = screenBg,
            scrimAlpha = settings.bgBrightnessAlpha.coerceIn(0.05f, 0.78f)
        )

        if (!isDefaultScreenBg && settings.mainBgImagePath == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(screenBg.copy(alpha = if (isLightBackdrop) 0.78f else 0.45f))
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            val isWideLayout = maxWidth >= 560.dp || maxWidth > maxHeight
            val compactPhone = maxWidth < 520.dp
            val useFillSpaceBetween = isWideLayout || maxHeight >= 540.dp
            val scrollState = rememberScrollState()

            val clockMainStr = TimeFormatters.formatClockMain(
                state.currentHour24,
                state.currentMinute,
                settings.is24HourFormat
            )
            val secStr = "%02d".format(state.currentSecond)
            val compactPeriodStr = if (settings.is24HourFormat) {
                "24H"
            } else if (state.currentHour24 < 12) {
                "ص"
            } else {
                "م"
            }

            val sunriseTimeStr = schedule?.entries?.get(PrayerName.SUNRISE)?.let {
                TimeFormatters.formatMinuteOfDay(it.adjustedMinuteOfDay, settings.is24HourFormat)
            } ?: "--:--"

            val jumuahTimeStr = schedule?.entries?.get(PrayerName.JUMUAH)?.let {
                TimeFormatters.formatMinuteOfDay(it.adjustedMinuteOfDay, settings.is24HourFormat)
            } ?: "--:--"

            val nextPrayer = (state.phase as? MosquePhase.NormalCountdown)?.nextPrayer
            val cityDisplayLine = city?.let { "${it.countryAr} — ${it.cityAr}" } ?: ""
            val syncInfoLine = TimeFormatters.formatSyncTimestamp(schedule?.lastSyncedAtMillis)

            Column(
                modifier = if (useFillSpaceBetween) {
                    Modifier
                        .fillMaxSize()
                        .widthIn(max = 1240.dp)
                        .align(Alignment.Center)
                } else {
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                },
                verticalArrangement = if (useFillSpaceBetween) Arrangement.SpaceBetween else Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ==================== 1. AL-MOAZIN TOP HEADER ====================
                AlMoazinTopHeader(
                    isWideLayout = isWideLayout,
                    mosqueName = effectiveMosqueName,
                    addressLine = addressLine,
                    cityLabel = city?.cityAr ?: "المدينة",
                    syncInfoLine = syncInfoLine,
                    isOnline = state.isOnline,
                    isSyncing = state.isSyncingPrayerTimes,
                    showWeather = settings.showWeather,
                    tempCelsius = settings.cachedTempCelsius,
                    textScale = settings.textScale,
                    headerBg = headerBg,
                    headerText = headerText,
                    uiBtnBg = uiBtnBg,
                    uiBtnText = uiBtnText,
                    onOpenCitySelector = onOpenCitySelector,
                    onSyncNow = onSyncPrayerTimesNow,
                    onToggleAdhkar = onToggleAdhkar,
                    onTriggerTestCycle = onTriggerTestCycle,
                    onOpenSettings = onOpenSettings
                )

                if (isWideLayout) {
                    // ==================== 2A. LANDSCAPE / TV HERO: Sunrise | Joined Clock+Next Card | Jumuah ====================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (settings.showSunriseBox) {
                            AlMoazinSideCard(
                                title = "الشروق",
                                timeText = sunriseTimeStr,
                                bgColor = sunriseBg,
                                textColor = sunriseText,
                                borderColor = boxBorder,
                                textScale = settings.textScale,
                                onClick = { quickEditPrayer = PrayerName.SUNRISE },
                                modifier = Modifier
                                    .weight(0.21f)
                                    .testTag("sunrise_box")
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(0.18f))
                        }

                        JoinedClockAndNextCard(
                            clockMainStr = clockMainStr,
                            secondsStr = secStr,
                            periodStr = compactPeriodStr,
                            weekdayAr = state.weekdayAr,
                            hijriDateAr = state.hijriDate.formattedAr,
                            gregorianDateAr = state.gregorianDateAr,
                            cityDisplayLine = cityDisplayLine,
                            showHijri = settings.showHijriDate,
                            showGregorian = settings.showGregorianDate,
                            is24Hour = settings.is24HourFormat,
                            clockScale = settings.clockScale,
                            textScale = settings.textScale,
                            countdownScale = settings.countdownScale,
                            clockBg = clockBg,
                            clockText = clockText,
                            clockAccent = clockAccent,
                            dateText = dateText,
                            borderColor = boxBorder,
                            clockBgImagePath = settings.clockBoxBgImagePath,
                            phase = if (settings.showNextPrayerBox) state.phase as? MosquePhase.NormalCountdown else null,
                            schedule = schedule,
                            nextBg = nextBg,
                            nextText = nextText,
                            isWideLayout = true,
                            modifier = Modifier.weight(0.58f)
                        )

                        if (settings.showJumuahBox) {
                            AlMoazinSideCard(
                                title = "الجمعة",
                                timeText = jumuahTimeStr,
                                bgColor = jumuahBg,
                                textColor = jumuahText,
                                borderColor = boxBorder,
                                textScale = settings.textScale,
                                onClick = { quickEditPrayer = PrayerName.JUMUAH },
                                modifier = Modifier
                                    .weight(0.21f)
                                    .testTag("jumuah_box")
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(0.18f))
                        }
                    }
                } else {
                    // ==================== 2B. PHONE PORTRAIT HERO (BALANCED CENTER + ANCHORED BOTTOM) ====================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        JoinedClockAndNextCard(
                            clockMainStr = clockMainStr,
                            secondsStr = secStr,
                            periodStr = compactPeriodStr,
                            weekdayAr = state.weekdayAr,
                            hijriDateAr = state.hijriDate.formattedAr,
                            gregorianDateAr = state.gregorianDateAr,
                            cityDisplayLine = cityDisplayLine,
                            showHijri = settings.showHijriDate,
                            showGregorian = settings.showGregorianDate,
                            is24Hour = settings.is24HourFormat,
                            clockScale = settings.clockScale,
                            textScale = settings.textScale,
                            countdownScale = settings.countdownScale,
                            clockBg = clockBg,
                            clockText = clockText,
                            clockAccent = clockAccent,
                            dateText = dateText,
                            borderColor = boxBorder,
                            clockBgImagePath = settings.clockBoxBgImagePath,
                            phase = if (settings.showNextPrayerBox) state.phase as? MosquePhase.NormalCountdown else null,
                            schedule = schedule,
                            nextBg = nextBg,
                            nextText = nextText,
                            isWideLayout = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (settings.showSunriseBox || settings.showJumuahBox) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (settings.showSunriseBox) {
                                    AlMoazinSideCard(
                                        title = "الشروق",
                                        timeText = sunriseTimeStr,
                                        bgColor = sunriseBg,
                                        textColor = sunriseText,
                                        borderColor = boxBorder,
                                        textScale = settings.textScale * 0.88f,
                                        onClick = { quickEditPrayer = PrayerName.SUNRISE },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("sunrise_box")
                                    )
                                }
                                if (settings.showJumuahBox) {
                                    AlMoazinSideCard(
                                        title = "الجمعة",
                                        timeText = jumuahTimeStr,
                                        bgColor = jumuahBg,
                                        textColor = jumuahText,
                                        borderColor = boxBorder,
                                        textScale = settings.textScale * 0.88f,
                                        onClick = { quickEditPrayer = PrayerName.JUMUAH },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("jumuah_box")
                                    )
                                }
                            }
                        }
                    }
                }

                // ==================== 3. UNIFIED HORIZONTAL 5 PRAYER CARDS ACROSS ALL DEVICES ====================
                HorizontalPrayerCardsRow(
                    schedule = schedule,
                    nextPrayer = nextPrayer,
                    is24Hour = settings.is24HourFormat,
                    textScale = settings.textScale,
                    boxBg = prayerBg,
                    boxText = prayerText,
                    activeAccent = activeAccent,
                    borderColor = boxBorder,
                    screenBg = screenBg,
                    compactMode = compactPhone,
                    onClickPrayer = { quickEditPrayer = it }
                )
            }
        }

        if (settings.dimModeEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = settings.dimAlpha.coerceIn(0.1f, 0.75f)))
            )
        }

        val editingPrayer = quickEditPrayer
        if (editingPrayer != null && city != null && schedule != null) {
            QuickPrayerEditDialog(
                prayer = editingPrayer,
                city = city,
                schedule = schedule,
                is24Hour = settings.is24HourFormat,
                onAdjustOffset = { delta -> onAdjustPrayerOffset(editingPrayer, delta) },
                onResetOffset = { onResetPrayerOffset(editingPrayer) },
                onSetIqama = { mins -> onSetIqamaMinutes(editingPrayer, mins) },
                onOpenFullSettings = {
                    quickEditPrayer = null
                    onOpenSettings()
                },
                onDismiss = { quickEditPrayer = null }
            )
        }

        val clockFull = "${TimeFormatters.formatClockMain(state.currentHour24, state.currentMinute, settings.is24HourFormat)} ${TimeFormatters.formatClockPeriod(state.currentHour24, settings.is24HourFormat)}"
        when (val phase = state.phase) {
            is MosquePhase.AdhanActive -> AdhanOverlay(
                phase = phase,
                settings = settings.copy(mosqueName = effectiveMosqueName),
                clockText = clockFull,
                isAudioPlaying = state.isAudioPlaying,
                onStopAudio = onStopAudio,
                onDismiss = onDismissPhase
            )
            is MosquePhase.IqamaCountdown -> IqamaCountdownOverlay(
                phase = phase,
                settings = settings,
                onDismiss = onDismissPhase
            )
            is MosquePhase.TurnOffPhoneScreen -> TurnOffPhoneOverlay(
                phase = phase,
                settings = settings,
                onDismiss = onDismissPhase
            )
            is MosquePhase.PostPrayerAdhkar -> PostPrayerAdhkarOverlay(
                phase = phase,
                settings = settings,
                onTogglePause = onToggleAdhkarPause,
                onNext = onNextAdhkar,
                onDismiss = onDismissPhase
            )
            is MosquePhase.NormalCountdown -> Unit
        }
    }
}

@Composable
private fun AlMoazinTopHeader(
    isWideLayout: Boolean,
    mosqueName: String,
    addressLine: String,
    cityLabel: String,
    syncInfoLine: String,
    isOnline: Boolean,
    isSyncing: Boolean,
    showWeather: Boolean,
    tempCelsius: Double?,
    textScale: Float,
    headerBg: Color,
    headerText: Color,
    uiBtnBg: Color,
    uiBtnText: Color,
    onOpenCitySelector: () -> Unit,
    onSyncNow: () -> Unit,
    onToggleAdhkar: () -> Unit,
    onTriggerTestCycle: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val hasOpaqueHeader = headerBg.alpha >= 0.08f
    val statusDotColor = if (isOnline) Color(0xFF2ED573) else Color(0xFFFFA502)

    Surface(
        color = if (hasOpaqueHeader) headerBg else Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (hasOpaqueHeader) 10.dp else 2.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // RTL Start: Airy borderless Menu + Settings + status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(uiBtnBg.copy(alpha = 0.42f))
                            .testTag("menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "القائمة",
                            tint = uiBtnText,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = SettingsCardDark
                    ) {
                        DropdownMenuItem(
                            text = { Text("الإعدادات الكاملة", color = Color.White, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = MosqueGoldAccent) },
                            onClick = {
                                menuExpanded = false
                                onOpenSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("تحديث ومزامنة المواقيت الآن", color = Color(0xFF80CBC4), fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null, tint = Color(0xFF80CBC4)) },
                            onClick = {
                                menuExpanded = false
                                onSyncNow()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("اختيار الدولة والمدينة", color = Color.White, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MosqueGoldAccent) },
                            onClick = {
                                menuExpanded = false
                                onOpenCitySelector()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("أذكار ما بعد الصلاة", color = Color.White, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = Color.White) },
                            modifier = Modifier.testTag("quick_adhkar_button"),
                            onClick = {
                                menuExpanded = false
                                onToggleAdhkar()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("تجربة الأذان والإقامة", color = MosqueGoldAccent, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = { Icon(Icons.Default.PlayCircle, contentDescription = null, tint = MosqueGoldAccent) },
                            modifier = Modifier.testTag("test_cycle_button"),
                            onClick = {
                                menuExpanded = false
                                onTriggerTestCycle()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(uiBtnBg.copy(alpha = 0.42f))
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "الإعدادات",
                        tint = uiBtnText,
                        modifier = Modifier.size(21.dp)
                    )
                }

                if (isWideLayout) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(statusDotColor)
                        )
                        Text(
                            text = if (isSyncing) "مزامنة..." else if (isOnline) "متصل" else "أوفلاين",
                            color = headerText.copy(alpha = 0.85f),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            style = TextStyle(shadow = if (hasOpaqueHeader) null else subtleTextShadow)
                        )
                    }
                }
            }

            // Center: Mosque Name (+ optional District & Street)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mosqueName,
                    color = headerText,
                    style = TextStyle(
                        fontSize = ((if (isWideLayout) 30 else 23) * textScale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        shadow = if (hasOpaqueHeader) null else subtleTextShadow
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("mosque_name_title")
                )
                if (addressLine.isNotBlank()) {
                    Text(
                        text = addressLine,
                        color = headerText.copy(alpha = 0.82f),
                        fontSize = (12 * textScale).sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(shadow = if (hasOpaqueHeader) null else subtleTextShadow)
                    )
                }
            }

            // RTL End: Airy City & Weather Pill
            Surface(
                onClick = onOpenCitySelector,
                color = uiBtnBg.copy(alpha = 0.45f),
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("city_chip_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (!isWideLayout) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(statusDotColor)
                        )
                    }
                    if (showWeather && tempCelsius != null) {
                        Text(
                            text = "${tempCelsius.toInt()}°",
                            color = uiBtnText,
                            fontSize = (13f * textScale).sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "الطقس",
                            tint = uiBtnText,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "تغيير المدينة",
                            tint = uiBtnText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = cityLabel,
                        color = uiBtnText,
                        fontSize = (12.5f * textScale).sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isWideLayout && syncInfoLine.isNotBlank()) {
                        Text(
                            text = "• $syncInfoLine",
                            color = uiBtnText.copy(alpha = 0.72f),
                            fontSize = 10.5.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JoinedClockAndNextCard(
    clockMainStr: String,
    secondsStr: String,
    periodStr: String,
    weekdayAr: String,
    hijriDateAr: String,
    gregorianDateAr: String,
    cityDisplayLine: String,
    showHijri: Boolean,
    showGregorian: Boolean,
    is24Hour: Boolean,
    clockScale: Float,
    textScale: Float,
    countdownScale: Float,
    clockBg: Color,
    clockText: Color,
    clockAccent: Color,
    dateText: Color,
    borderColor: Color,
    clockBgImagePath: String?,
    phase: MosquePhase.NormalCountdown?,
    schedule: DailyPrayerSchedule?,
    nextBg: Color,
    nextText: Color,
    isWideLayout: Boolean,
    modifier: Modifier = Modifier
) {
    val customClockBg = TimeFormatters.validFileOrNull(clockBgImagePath)
    val nextTimeStr = phase?.let { p ->
        schedule?.entries?.get(p.nextPrayer)?.let {
            TimeFormatters.formatMinuteOfDay(it.adjustedMinuteOfDay, is24Hour)
        }
    }

    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.2.dp, borderColor),
        shadowElevation = 10.dp,
        modifier = modifier.clip(RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Frosted Clock Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(clockBg)
                    .testTag("main_clock_box")
            ) {
                if (customClockBg != null) {
                    AsyncImage(
                        model = customClockBg,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(clockBg.copy(alpha = clockBg.alpha.coerceAtLeast(0.55f)))
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = if (isWideLayout) 28.dp else 14.dp,
                            vertical = if (isWideLayout) 16.dp else 12.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = clockMainStr,
                                color = clockText,
                                style = TextStyle(
                                    fontSize = ((if (isWideLayout) 74 else 56) * clockScale).sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFeatureSettings = "tnum",
                                    lineHeight = ((if (isWideLayout) 76 else 58) * clockScale).sp
                                ),
                                modifier = Modifier.testTag("main_clock_digits")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = ":$secondsStr",
                                    color = clockAccent,
                                    style = TextStyle(
                                        fontSize = ((if (isWideLayout) 26 else 20) * clockScale).sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"
                                    )
                                )
                                Text(
                                    text = if (is24Hour) "24H" else periodStr,
                                    color = clockAccent.copy(alpha = 0.92f),
                                    fontSize = ((if (isWideLayout) 15 else 13) * textScale).sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    if (showGregorian) {
                        Text(
                            text = "$weekdayAr، $gregorianDateAr",
                            color = dateText,
                            fontSize = ((if (isWideLayout) 16 else 14) * textScale).sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("gregorian_date_text")
                        )
                    } else {
                        Text(
                            text = weekdayAr,
                            color = dateText,
                            fontSize = ((if (isWideLayout) 15 else 13) * textScale).sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("gregorian_date_text")
                        )
                    }

                    if (showHijri) {
                        Text(
                            text = hijriDateAr,
                            color = dateText.copy(alpha = 0.90f),
                            fontSize = ((if (isWideLayout) 15 else 13) * textScale).sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("hijri_date_text")
                        )
                    }

                    if (cityDisplayLine.isNotBlank()) {
                        Text(
                            text = cityDisplayLine,
                            color = dateText.copy(alpha = 0.78f),
                            fontSize = ((if (isWideLayout) 12.5f else 11.5f) * textScale).sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Seamlessly Attached Two-Tone Next Prayer Footer
            if (phase != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(nextBg)
                        .padding(
                            horizontal = if (isWideLayout) 18.dp else 12.dp,
                            vertical = if (isWideLayout) 10.dp else 8.dp
                        )
                        .testTag("next_prayer_box"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = null,
                            tint = nextText.copy(alpha = 0.90f),
                            modifier = Modifier.size(if (isWideLayout) 20.dp else 17.dp)
                        )
                        Text(
                            text = "${phase.nextPrayer.arabicName} بعد ${TimeFormatters.formatDurationHms(phase.remainingSeconds)}",
                            color = nextText,
                            style = TextStyle(
                                fontSize = ((if (isWideLayout) 21 else 17) * countdownScale).sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFeatureSettings = "tnum"
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (nextTimeStr != null) {
                            Text(
                                text = "($nextTimeStr)",
                                color = nextText.copy(alpha = 0.78f),
                                style = TextStyle(
                                    fontSize = ((if (isWideLayout) 14f else 12.5f) * countdownScale).sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFeatureSettings = "tnum"
                                ),
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = null,
                            tint = nextText.copy(alpha = 0.90f),
                            modifier = Modifier.size(if (isWideLayout) 20.dp else 17.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlMoazinSideCard(
    title: String,
    timeText: String,
    bgColor: Color,
    textColor: Color,
    borderColor: Color,
    textScale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasFill = bgColor.alpha >= 0.08f
    Surface(
        onClick = onClick,
        color = if (hasFill) bgColor else Color.Transparent,
        shape = RoundedCornerShape(18.dp),
        border = if (hasFill) BorderStroke(1.2.dp, borderColor.copy(alpha = 0.95f)) else null,
        shadowElevation = if (hasFill) 6.dp else 0.dp,
        modifier = modifier.heightIn(min = 72.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = textColor.copy(alpha = 0.95f),
                style = TextStyle(
                    fontSize = (18 * textScale).sp,
                    fontWeight = FontWeight.Bold,
                    shadow = if (hasFill) null else subtleTextShadow
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeText,
                color = textColor,
                style = TextStyle(
                    fontSize = (25 * textScale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFeatureSettings = "tnum",
                    shadow = if (hasFill) null else subtleTextShadow
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HorizontalPrayerCardsRow(
    schedule: DailyPrayerSchedule?,
    nextPrayer: PrayerName?,
    is24Hour: Boolean,
    textScale: Float,
    boxBg: Color,
    boxText: Color,
    activeAccent: Color,
    borderColor: Color,
    screenBg: Color,
    compactMode: Boolean,
    onClickPrayer: (PrayerName) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .testTag("prayer_boxes_row"),
        horizontalArrangement = Arrangement.spacedBy(if (compactMode) 5.dp else 10.dp)
    ) {
        PrayerName.fiveDailyPrayers.forEach { prayer ->
            AlMoazinPrayerCard(
                prayer = prayer,
                schedule = schedule,
                isNext = nextPrayer == prayer,
                is24Hour = is24Hour,
                textScale = textScale,
                boxBg = boxBg,
                boxText = boxText,
                activeAccent = activeAccent,
                borderColor = borderColor,
                screenBg = screenBg,
                compactMode = compactMode,
                onClick = { onClickPrayer(prayer) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("prayer_card_${prayer.name.lowercase()}")
            )
        }
    }
}

@Composable
private fun AlMoazinPrayerCard(
    prayer: PrayerName,
    schedule: DailyPrayerSchedule?,
    isNext: Boolean,
    is24Hour: Boolean,
    textScale: Float,
    boxBg: Color,
    boxText: Color,
    activeAccent: Color,
    borderColor: Color,
    screenBg: Color,
    compactMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entry = schedule?.entries?.get(prayer)
    val timeStr = entry?.let { TimeFormatters.formatMinuteOfDay(it.adjustedMinuteOfDay, is24Hour) } ?: "--:--"
    val iqamaLabel = entry?.let {
        if (compactMode) "+${it.iqamaMinutes}" else "إقامة ${it.iqamaMinutes} د"
    } ?: ""
    val offsetBadge = entry?.offsetMinutes?.takeIf { it != 0 }?.let { TimeFormatters.formatOffsetBadge(it) }

    val cardBg = if (isNext) {
        if (activeAccent.alpha >= 0.08f) {
            activeAccent
        } else {
            boxBg.copy(alpha = (boxBg.alpha + 0.14f).coerceAtMost(0.98f))
        }
    } else {
        boxBg
    }
    val cardText = if (isNext) ensureReadableColor(boxText, cardBg, screenBg) else boxText
    val nameColor = cardText.copy(alpha = 0.96f)
    val timeColor = cardText
    val iqamaColor = cardText.copy(alpha = 0.86f)
    val cardBorder = if (isNext) cardText.copy(alpha = 0.45f) else borderColor

    Surface(
        onClick = onClick,
        color = cardBg,
        shape = RoundedCornerShape(if (compactMode) 14.dp else 18.dp),
        border = BorderStroke(
            width = if (isNext) 1.8.dp else 1.1.dp,
            color = cardBorder
        ),
        shadowElevation = if (isNext) 8.dp else 4.dp,
        modifier = modifier.heightIn(min = if (compactMode) 82.dp else 98.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (compactMode) 3.dp else 8.dp,
                    vertical = if (compactMode) 9.dp else 11.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = prayer.arabicName,
                color = nameColor,
                fontSize = ((if (compactMode) 12.5f else 17f) * textScale).sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(if (compactMode) 3.dp else 4.dp))
            Text(
                text = timeStr,
                color = timeColor,
                style = TextStyle(
                    fontSize = ((if (compactMode) 13.5f else 22f) * textScale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFeatureSettings = "tnum"
                ),
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(if (compactMode) 3.dp else 4.dp))
            Text(
                text = if (offsetBadge != null) "$iqamaLabel • $offsetBadge" else iqamaLabel,
                color = iqamaColor,
                fontSize = ((if (compactMode) 10.5f else 12.5f) * textScale).sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickPrayerEditDialog(
    prayer: PrayerName,
    city: CityConfig,
    schedule: DailyPrayerSchedule,
    is24Hour: Boolean,
    onAdjustOffset: (Int) -> Unit,
    onResetOffset: () -> Unit,
    onSetIqama: (Int) -> Unit,
    onOpenFullSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val entry = schedule.entries[prayer]
    val orig = entry?.let { TimeFormatters.formatMinuteOfDay(it.originalMinuteOfDay, is24Hour) } ?: "--:--"
    val adj = entry?.let { TimeFormatters.formatMinuteOfDay(it.adjustedMinuteOfDay, is24Hour) } ?: "--:--"
    val off = entry?.offsetMinutes ?: 0
    val iqama = city.iqamaMinutes[prayer] ?: 15

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SettingsCardDark,
        title = {
            Text(
                text = "ضبط سريع: ${prayer.arabicName} (${city.cityAr})",
                color = MosqueGoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("الوقت الأصلي: $orig  ←  المعدل: $adj", color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "التعديل الحالي: ${TimeFormatters.formatOffsetBadge(off)}",
                    color = MosqueGoldAccent,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(-5 to "-5", -1 to "-1", 1 to "+1", 5 to "+5").forEach { (delta, lbl) ->
                        Button(
                            onClick = { onAdjustOffset(delta) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (delta > 0) Color(0xFF1A4D43) else Color(0xFF422626)
                            ),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) { Text(lbl, color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
                if (prayer != PrayerName.SUNRISE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الإقامة: $iqama د", color = Color.White, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onSetIqama(iqama - 1) },
                                modifier = Modifier.size(48.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("-", color = Color.White, fontSize = 18.sp) }
                            OutlinedButton(
                                onClick = { onSetIqama(iqama + 1) },
                                modifier = Modifier.size(48.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("+", color = Color.White, fontSize = 18.sp) }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (off != 0) {
                        TextButton(onClick = onResetOffset) {
                            Text("إلغاء التعديل", color = Color(0xFFFFAB91))
                        }
                    }
                    TextButton(onClick = onOpenFullSettings) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = MosqueGoldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("كل الإعدادات", color = MosqueGoldAccent)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldAccent)
            ) { Text("تم", color = Color.Black, fontWeight = FontWeight.Bold) }
        }
    )
}
