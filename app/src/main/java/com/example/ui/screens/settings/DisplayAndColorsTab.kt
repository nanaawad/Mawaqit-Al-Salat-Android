package com.example.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FontFamilyOption
import com.example.data.model.OrientationMode
import com.example.ui.theme.MosqueGoldAccent
import com.example.ui.theme.SettingsCardDark
import com.example.ui.theme.SettingsInputBg
import com.example.ui.viewmodel.MosqueUiState
import com.example.ui.viewmodel.MosqueViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun DisplayAndColorsTab(
    state: MosqueUiState,
    viewModel: MosqueViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ColorsAndPresetsContent(state, viewModel)
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
        DisplayAndClockContent(state, viewModel)
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
        BackupAndExportContent(viewModel)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorsAndPresetsContent(
    state: MosqueUiState,
    viewModel: MosqueViewModel
) {
    val s = state.settings
    var showAdvancedColors by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Unified 1-Click Master Color Controls
        Surface(
            color = SettingsInputBg,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MosqueGoldAccent.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "التحكم الموحّد السريع (تغيير شامل بضغطة واحدة):",
                    color = MosqueGoldAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.testTag("unified_all_fonts_color")) {
                    ColorPickerRow(
                        title = "1. تغيير جميع الخطوط والنصوص بلون واحد",
                        selectedColorLong = s.clockBoxTextColor,
                        presets = goldAndLightPresets
                    ) { chosen ->
                        viewModel.updateSettings("✓ تم توحيد لون جميع الخطوط والنصوص") {
                            it.copy(
                                headerTextColor = chosen,
                                clockBoxTextColor = chosen,
                                clockAccentColor = chosen,
                                dateTextColor = chosen,
                                nextPrayerBoxTextColor = chosen,
                                prayerBoxTextColor = chosen,
                                sunriseBoxTextColor = chosen,
                                jumuahBoxTextColor = chosen,
                                uiButtonTextColor = chosen,
                                announcementTextColor = chosen
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.testTag("unified_all_boxes_bg_color")) {
                    ColorPickerRow(
                        title = "2. تغيير خلفيات جميع الصناديق بلون واحد",
                        selectedColorLong = s.prayerBoxBgColor,
                        presets = boxBgPresets
                    ) { chosen ->
                        viewModel.updateSettings("✓ تم توحيد لون خلفيات جميع الصناديق") {
                            it.copy(
                                clockBoxBgColor = chosen,
                                nextPrayerBoxBgColor = chosen,
                                prayerBoxBgColor = chosen,
                                sunriseBoxBgColor = chosen,
                                jumuahBoxBgColor = chosen,
                                uiButtonBgColor = if (chosen == 0x00000000L) 0x66000000 else chosen
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "أنماط ألوان جاهزة بضغطة واحدة:",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            mosqueThemePresets.forEach { preset ->
                val isCurrent = s.screenBgColor == preset.screenBgColor &&
                    s.clockBoxTextColor == preset.clockBoxTextColor &&
                    s.boxBorderColor == preset.boxBorderColor
                Surface(
                    onClick = {
                        viewModel.updateSettings("✓ تم تطبيق وحفظ سمة: ${preset.nameAr}") {
                            it.copy(
                                screenBgColor = preset.screenBgColor,
                                clockBoxBgColor = preset.clockBoxBgColor,
                                clockBoxTextColor = preset.clockBoxTextColor,
                                clockAccentColor = preset.clockAccentColor,
                                boxBorderColor = preset.boxBorderColor,
                                dateTextColor = preset.dateTextColor,
                                prayerBoxBgColor = preset.prayerBoxBgColor,
                                prayerBoxTextColor = preset.prayerBoxTextColor,
                                activePrayerAccentColor = preset.activePrayerAccentColor,
                                nextPrayerBoxBgColor = preset.nextPrayerBoxBgColor,
                                nextPrayerBoxTextColor = preset.nextPrayerBoxTextColor,
                                sunriseBoxBgColor = preset.sunriseBoxBgColor,
                                sunriseBoxTextColor = preset.sunriseBoxTextColor,
                                jumuahBoxBgColor = preset.jumuahBoxBgColor,
                                jumuahBoxTextColor = preset.jumuahBoxTextColor,
                                headerBgColor = preset.headerBgColor,
                                headerTextColor = preset.headerTextColor,
                                uiButtonBgColor = preset.uiButtonBgColor,
                                uiButtonTextColor = preset.uiButtonTextColor
                            )
                        }
                    },
                    color = Color(preset.clockBoxBgColor.toInt()).copy(alpha = 1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = if (isCurrent) 2.5.dp else 1.2.dp,
                        color = if (isCurrent) MosqueGoldAccent else Color(preset.boxBorderColor.toInt())
                    ),
                    modifier = Modifier
                        .heightIn(min = 50.dp)
                        .testTag("theme_preset_${preset.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(Color(preset.clockBoxTextColor.toInt()))
                        )
                        Text(
                            text = preset.nameAr,
                            color = Color(preset.clockBoxTextColor.toInt()),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isCurrent) {
                            Icon(Icons.Default.Check, contentDescription = "السمة الحالية", tint = MosqueGoldAccent, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = { showAdvancedColors = !showAdvancedColors },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("toggle_advanced_colors_button")
        ) {
            Icon(
                imageVector = if (showAdvancedColors) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MosqueGoldAccent
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (showAdvancedColors) "إخفاء الألوان التفصيلية (19 عنصراً)" else "تخصيص كل قسم على حدة (19 عنصراً)",
                color = MosqueGoldAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        AnimatedVisibility(visible = showAdvancedColors) {
            Column(modifier = Modifier.padding(top = 6.dp)) {
                ColorPickerRow("1. خلفية التطبيق الرئيسية", s.screenBgColor, bgPresets) { c ->
                    viewModel.updateSettings { it.copy(screenBgColor = c) }
                }
                ColorPickerRow("2. لون خط اسم المسجد", s.headerTextColor, goldAndLightPresets) { c ->
                    viewModel.updateSettings { it.copy(headerTextColor = c) }
                }
                ColorPickerRow("3. خلفية الشريط العلوي", s.headerBgColor, boxBgPresets) { c ->
                    viewModel.updateSettings { it.copy(headerBgColor = c) }
                }
                ColorPickerRow("4. لون إطارات الصناديق والحدود", s.boxBorderColor, goldAndAccentPresets) { c ->
                    viewModel.updateSettings { it.copy(boxBorderColor = c) }
                }
                ColorPickerRow("5. خلفية صندوق الساعة", s.clockBoxBgColor, boxBgPresets) { c ->
                    viewModel.updateSettings { it.copy(clockBoxBgColor = c) }
                }
                ColorPickerRow("6. لون أرقام الساعة (ساعات/دقائق)", s.clockBoxTextColor, goldAndLightPresets) { c ->
                    viewModel.updateSettings { it.copy(clockBoxTextColor = c) }
                }
                ColorPickerRow("7. لون الثواني ومؤشر ص/م", s.clockAccentColor, goldAndLightPresets) { c ->
                    viewModel.updateSettings { it.copy(clockAccentColor = c) }
                }
                ColorPickerRow("8. لون التاريخ الهجري والميلادي", s.dateTextColor, goldAndLightPresets) { c ->
                    viewModel.updateSettings { it.copy(dateTextColor = c) }
                }
                ColorPickerRow("9. خلفية صندوق الصلاة القادمة", s.nextPrayerBoxBgColor, boxBgPresets) { c ->
                    viewModel.updateSettings { it.copy(nextPrayerBoxBgColor = c) }
                }
                ColorPickerRow("10. لون نص العد التنازلي والصلاة القادمة", s.nextPrayerBoxTextColor, goldAndLightPresets) { c ->
                    viewModel.updateSettings { it.copy(nextPrayerBoxTextColor = c) }
                }
                ColorPickerRow("11. خلفيات عدادات الصلوات", s.prayerBoxBgColor, boxBgPresets) { c ->
                    viewModel.updateSettings { it.copy(prayerBoxBgColor = c) }
                }
                ColorPickerRow("12. لون أسماء وأرقام الصلوات", s.prayerBoxTextColor, goldAndLightPresets) { c ->
                    viewModel.updateSettings { it.copy(prayerBoxTextColor = c) }
                }
                ColorPickerRow("13. لون تمييز الصلاة القادمة النشطة", s.activePrayerAccentColor, goldAndAccentPresets) { c ->
                    viewModel.updateSettings { it.copy(activePrayerAccentColor = c) }
                }
                ColorPickerRow("14. خلفية صندوق الشروق", s.sunriseBoxBgColor, boxBgPresets) { c ->
                    viewModel.updateSettings { it.copy(sunriseBoxBgColor = c) }
                }
                ColorPickerRow("15. لون نص صندوق الشروق", s.sunriseBoxTextColor, goldAndLightPresets) { c ->
                    viewModel.updateSettings { it.copy(sunriseBoxTextColor = c) }
                }
                ColorPickerRow("16. خلفية صندوق الجمعة", s.jumuahBoxBgColor, boxBgPresets) { c ->
                    viewModel.updateSettings { it.copy(jumuahBoxBgColor = c) }
                }
                ColorPickerRow("17. لون نص صندوق الجمعة", s.jumuahBoxTextColor, goldAndLightPresets) { c ->
                    viewModel.updateSettings { it.copy(jumuahBoxTextColor = c) }
                }
                ColorPickerRow("18. خلفية القوائم والأزرار العلوية", s.uiButtonBgColor, boxBgPresets) { c ->
                    viewModel.updateSettings { it.copy(uiButtonBgColor = c) }
                }
                ColorPickerRow("19. لون نصوص وأيقونات القوائم والأزرار", s.uiButtonTextColor, goldAndLightPresets) { c ->
                    viewModel.updateSettings { it.copy(uiButtonTextColor = c) }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DisplayAndClockContent(
    state: MosqueUiState,
    viewModel: MosqueViewModel
) {
    val s = state.settings
    var mosqueNameDraft by remember(s.mosqueName) { mutableStateOf(s.mosqueName) }

    LaunchedEffect(mosqueNameDraft) {
        val clean = mosqueNameDraft.trim().ifBlank { "مسجد النور" }
        if (clean != s.mosqueName && mosqueNameDraft.isNotBlank()) {
            delay(450L)
            viewModel.updateSettings { it.copy(mosqueName = clean) }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = mosqueNameDraft,
            onValueChange = { mosqueNameDraft = it },
            label = { Text("اسم المسجد الافتراضي العام") },
            singleLine = true,
            colors = readableTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mosque_name_input")
        )
        Button(
            onClick = { viewModel.updateSettings { it.copy(mosqueName = mosqueNameDraft.trim().ifBlank { "مسجد النور" }) } },
            colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldAccent),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("save_mosque_name_button")
        ) {
            Text("حفظ اسم المسجد الآن", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

        ToggleSettingRow("نظام 24 ساعة (عند الإيقاف يعمل بنظام 12 ساعة)", s.is24HourFormat) { checked ->
            viewModel.updateSettings { it.copy(is24HourFormat = checked) }
        }
        ToggleSettingRow("إبقاء الشاشة مضاءة دائماً (Wake Lock)", s.keepScreenOn) { checked ->
            viewModel.updateSettings { it.copy(keepScreenOn = checked) }
        }
        ToggleSettingRow("وضع التعتيم الليلي", s.dimModeEnabled) { checked ->
            viewModel.updateSettings { it.copy(dimModeEnabled = checked) }
        }
        if (s.dimModeEnabled) {
            Text("درجة التعتيم: ${(s.dimAlpha * 100).toInt()}%", color = Color(0xFFB2DFDB), fontSize = 13.sp)
            Slider(
                value = s.dimAlpha,
                onValueChange = { v -> viewModel.updateSettings(successMessage = null) { it.copy(dimAlpha = v) } },
                valueRange = 0.1f..0.75f,
                colors = SliderDefaults.colors(thumbColor = MosqueGoldAccent, activeTrackColor = MosqueGoldAccent)
            )
        }
        ToggleSettingRow("تشغيل التطبيق تلقائياً عند إقلاع الجهاز (TV Box)", s.autoStartOnBoot) { checked ->
            viewModel.updateSettings { it.copy(autoStartOnBoot = checked) }
        }

        Text("اتجاه الشاشة:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OrientationMode.entries.forEach { mode ->
                FilterChip(
                    selected = s.orientationModeName == mode.name,
                    onClick = { viewModel.updateSettings { it.copy(orientationModeName = mode.name) } },
                    label = { Text(mode.arabicName) },
                    colors = chipColors()
                )
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

        ToggleSettingRow("إظهار التاريخ الهجري", s.showHijriDate) { v -> viewModel.updateSettings { it.copy(showHijriDate = v) } }
        ToggleSettingRow("إظهار التاريخ الميلادي", s.showGregorianDate) { v -> viewModel.updateSettings { it.copy(showGregorianDate = v) } }
        ToggleSettingRow("إظهار صندوق الشروق", s.showSunriseBox) { v -> viewModel.updateSettings { it.copy(showSunriseBox = v) } }
        ToggleSettingRow("إظهار صندوق الجمعة", s.showJumuahBox) { v -> viewModel.updateSettings { it.copy(showJumuahBox = v) } }
        ToggleSettingRow("إظهار عداد الصلاة القادمة", s.showNextPrayerBox) { v -> viewModel.updateSettings { it.copy(showNextPrayerBox = v) } }
        ToggleSettingRow("إظهار حالة الطقس للمدينة", s.showWeather) { v -> viewModel.updateSettings { it.copy(showWeather = v) } }
        OutlinedButton(
            onClick = { viewModel.refreshWeatherNow() },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = MosqueGoldAccent)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (state.isOnline) "تحديث الطقس الآن" else "لا يوجد إنترنت (يُعرض آخر طقس محفوظ)",
                color = MosqueGoldAccent
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

        Text("نوع الخط:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FontFamilyOption.entries.forEach { opt ->
                FilterChip(
                    selected = s.fontFamilyKey == opt.key,
                    onClick = { viewModel.updateSettings { it.copy(fontFamilyKey = opt.key) } },
                    label = { Text(opt.arabicName) },
                    colors = chipColors()
                )
            }
        }

        ScaleSliderRow("حجم الساعة الرئيسية", s.clockScale, 0.7f..1.45f) { v ->
            viewModel.updateSettings(successMessage = null) { it.copy(clockScale = v) }
        }
        ScaleSliderRow("حجم العد التنازلي", s.countdownScale, 0.7f..1.45f) { v ->
            viewModel.updateSettings(successMessage = null) { it.copy(countdownScale = v) }
        }
        ScaleSliderRow("حجم النصوص العامة", s.textScale, 0.8f..1.35f) { v ->
            viewModel.updateSettings(successMessage = null) { it.copy(textScale = v) }
        }
    }
}

@Composable
fun BackupAndExportContent(
    viewModel: MosqueViewModel
) {
    var showConfirmResetAllDialog by remember { mutableStateOf(false) }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) viewModel.exportBackupToUri(uri)
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) viewModel.importBackupFromUri(uri)
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var htmlExportStatus by remember { mutableStateOf<String?>(null) }
    val exportHtmlLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        context.assets.open("www/index.html").use { input ->
                            context.contentResolver.openOutputStream(uri)?.use { out ->
                                input.copyTo(out)
                            } ?: error("تعذر فتح ملف الحفظ")
                        }
                    }.isSuccess
                }
                htmlExportStatus = if (ok) {
                    "✓ تم حفظ ملف index.html بنجاح — يعمل مباشرة على الكمبيوتر دون إنترنت."
                } else {
                    "تعذر حفظ الملف، حاول اختيار مجلد آخر مثل التنزيلات (Downloads)."
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "تُحفظ جميع الإعدادات والألوان وأوقات الإقامة دائماً ولا تُعاد للوضع الافتراضي إلا بموافقتك الصريحة هنا.",
            color = Color(0xFFB0C4C7),
            fontSize = 12.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { exportBackupLauncher.launch("al_moazin_backup.json") },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = MosqueGoldAccent)
                Spacer(modifier = Modifier.width(4.dp))
                Text("تصدير JSON", color = MosqueGoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { importBackupLauncher.launch(arrayOf("application/json", "text/*")) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, tint = Color(0xFF80CBC4))
                Spacer(modifier = Modifier.width(4.dp))
                Text("استيراد JSON", color = Color(0xFF80CBC4), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = { exportHtmlLauncher.launch("index.html") },
            colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldAccent),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("export_index_html_button")
        ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text("حفظ ملف index.html لتشغيله على اللابتوب", color = Color.Black, fontWeight = FontWeight.Bold)
        }
        htmlExportStatus?.let { status ->
            Text(status, color = MosqueGoldAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
            onClick = { showConfirmResetAllDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("reset_all_colors_button")
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color(0xFFFFAB91))
            Spacer(modifier = Modifier.width(6.dp))
            Text("استعادة جميع الإعدادات والألوان الافتراضية", color = Color(0xFFFFAB91), fontWeight = FontWeight.Bold)
        }
    }

    if (showConfirmResetAllDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmResetAllDialog = false },
            containerColor = SettingsCardDark,
            title = { Text("تأكيد استعادة الإعدادات الافتراضية", color = MosqueGoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "هل تريد فعلاً استعادة الألوان والشاشة والإعدادات العامة إلى الوضع الافتراضي؟\nلن تُمس مدنك المحفوظة إلا إذا قمت بتعديلها.",
                    color = Color.White,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllSettingsToDefaults()
                        showConfirmResetAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                ) {
                    Text("نعم، استعادة الافتراضي", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmResetAllDialog = false }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun ToggleSettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f).padding(end = 8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = MosqueGoldAccent)
        )
    }
}

@Composable
private fun ScaleSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ${(value * 100).toInt()}%", color = Color(0xFFE0ECEA), fontSize = 13.sp)
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = MosqueGoldAccent, activeTrackColor = MosqueGoldAccent)
        )
    }
}

private data class MosqueThemePreset(
    val id: String,
    val nameAr: String,
    val screenBgColor: Long,
    val clockBoxBgColor: Long,
    val clockBoxTextColor: Long,
    val clockAccentColor: Long,
    val boxBorderColor: Long,
    val dateTextColor: Long,
    val prayerBoxBgColor: Long,
    val prayerBoxTextColor: Long,
    val activePrayerAccentColor: Long,
    val nextPrayerBoxBgColor: Long,
    val nextPrayerBoxTextColor: Long,
    val sunriseBoxBgColor: Long,
    val sunriseBoxTextColor: Long,
    val jumuahBoxBgColor: Long,
    val jumuahBoxTextColor: Long,
    val headerBgColor: Long,
    val headerTextColor: Long,
    val uiButtonBgColor: Long,
    val uiButtonTextColor: Long
)

private val mosqueThemePresets = listOf(
    MosqueThemePreset(
        id = "mawaqit_purple",
        nameAr = "البنفسجي العصري (مواقيت)",
        screenBgColor = 0xFF090B12,
        clockBoxBgColor = 0xD15B1B99,
        clockBoxTextColor = 0xFFFFFFFF,
        clockAccentColor = 0xFFEAE0FF,
        boxBorderColor = 0x38FFFFFF,
        dateTextColor = 0xFFFFFFFF,
        prayerBoxBgColor = 0xC212161F,
        prayerBoxTextColor = 0xFFFFFFFF,
        activePrayerAccentColor = 0xD66A1B9A,
        nextPrayerBoxBgColor = 0xDB141720,
        nextPrayerBoxTextColor = 0xFFFFFFFF,
        sunriseBoxBgColor = 0x00000000,
        sunriseBoxTextColor = 0xFFFFFFFF,
        jumuahBoxBgColor = 0x00000000,
        jumuahBoxTextColor = 0xFFFFFFFF,
        headerBgColor = 0x00000000,
        headerTextColor = 0xFFFFFFFF,
        uiButtonBgColor = 0x66000000,
        uiButtonTextColor = 0xFFFFFFFF
    ),
    MosqueThemePreset(
        id = "almoazin_gold",
        nameAr = "ذهبي ملكي (المؤذن)",
        screenBgColor = 0xFF07120E,
        clockBoxBgColor = 0xE60E150F,
        clockBoxTextColor = 0xFFC5A038,
        clockAccentColor = 0xFFDFB743,
        boxBorderColor = 0xFF8E7428,
        dateTextColor = 0xFFC5A038,
        prayerBoxBgColor = 0xE60E150F,
        prayerBoxTextColor = 0xFFC5A038,
        activePrayerAccentColor = 0xFFD4AF37,
        nextPrayerBoxBgColor = 0xB3121814,
        nextPrayerBoxTextColor = 0xFFC5A038,
        sunriseBoxBgColor = 0xE60E150F,
        sunriseBoxTextColor = 0xFFC5A038,
        jumuahBoxBgColor = 0xE60E150F,
        jumuahBoxTextColor = 0xFFC5A038,
        headerBgColor = 0x00000000,
        headerTextColor = 0xFFC5A038,
        uiButtonBgColor = 0xFF15292E,
        uiButtonTextColor = 0xFFC5A038
    ),
    MosqueThemePreset(
        id = "midnight_navy",
        nameAr = "أزرق نيلي",
        screenBgColor = 0xFF071324,
        clockBoxBgColor = 0xE60D1F3C,
        clockBoxTextColor = 0xFF90CAF9,
        clockAccentColor = 0xFFFFE082,
        boxBorderColor = 0xFF2563EB,
        dateTextColor = 0xFFE3F2FD,
        prayerBoxBgColor = 0xE60E213A,
        prayerBoxTextColor = 0xFFE3F2FD,
        activePrayerAccentColor = 0xFF60A5FA,
        nextPrayerBoxBgColor = 0xE6102A4C,
        nextPrayerBoxTextColor = 0xFFFFE082,
        sunriseBoxBgColor = 0xE60E213A,
        sunriseBoxTextColor = 0xFFFFD54F,
        jumuahBoxBgColor = 0xE60E213A,
        jumuahBoxTextColor = 0xFF90CAF9,
        headerBgColor = 0x00000000,
        headerTextColor = 0xFFE3F2FD,
        uiButtonBgColor = 0xFF0F2744,
        uiButtonTextColor = 0xFF90CAF9
    ),
    MosqueThemePreset(
        id = "royal_emerald",
        nameAr = "أخضر زمردي",
        screenBgColor = 0xFF061E1A,
        clockBoxBgColor = 0xE60A2E28,
        clockBoxTextColor = 0xFFE6B84C,
        clockAccentColor = 0xFFFFE082,
        boxBorderColor = 0xFF26A69A,
        dateTextColor = 0xFFB2DFDB,
        prayerBoxBgColor = 0xE60C352E,
        prayerBoxTextColor = 0xFFFFFFFF,
        activePrayerAccentColor = 0xFFE6B84C,
        nextPrayerBoxBgColor = 0xE612453C,
        nextPrayerBoxTextColor = 0xFFFFE082,
        sunriseBoxBgColor = 0xE60C352E,
        sunriseBoxTextColor = 0xFFFFD54F,
        jumuahBoxBgColor = 0xE60C352E,
        jumuahBoxTextColor = 0xFFB2DFDB,
        headerBgColor = 0x00000000,
        headerTextColor = 0xFFFFE082,
        uiButtonBgColor = 0xFF0E3831,
        uiButtonTextColor = 0xFFFFE082
    ),
    MosqueThemePreset(
        id = "crimson_mihrab",
        nameAr = "قرمزي مسجدي",
        screenBgColor = 0xFF42080A,
        clockBoxBgColor = 0xE6600E10,
        clockBoxTextColor = 0xFFFFE082,
        clockAccentColor = 0xFFFFFFFF,
        boxBorderColor = 0xFFD4A037,
        dateTextColor = 0xFFFFF8E1,
        prayerBoxBgColor = 0xE64D0A0C,
        prayerBoxTextColor = 0xFFFFF8E1,
        activePrayerAccentColor = 0xFFD4A037,
        nextPrayerBoxBgColor = 0xE6380608,
        nextPrayerBoxTextColor = 0xFFFFE082,
        sunriseBoxBgColor = 0xE64D0A0C,
        sunriseBoxTextColor = 0xFFFFE082,
        jumuahBoxBgColor = 0xE64D0A0C,
        jumuahBoxTextColor = 0xFFFFE082,
        headerBgColor = 0x00000000,
        headerTextColor = 0xFFFFE082,
        uiButtonBgColor = 0xFF5A1012,
        uiButtonTextColor = 0xFFFFE082
    ),
    MosqueThemePreset(
        id = "silver_platinum",
        nameAr = "فضي بلاتيني",
        screenBgColor = 0xFF111418,
        clockBoxBgColor = 0xE6181C22,
        clockBoxTextColor = 0xFFE5E7EB,
        clockAccentColor = 0xFF93C5FD,
        boxBorderColor = 0xFF6B7280,
        dateTextColor = 0xFFD1D5DB,
        prayerBoxBgColor = 0xE6181C22,
        prayerBoxTextColor = 0xFFF3F4F6,
        activePrayerAccentColor = 0xFF60A5FA,
        nextPrayerBoxBgColor = 0xE61F242D,
        nextPrayerBoxTextColor = 0xFFE5E7EB,
        sunriseBoxBgColor = 0xE6181C22,
        sunriseBoxTextColor = 0xFFD1D5DB,
        jumuahBoxBgColor = 0xE6181C22,
        jumuahBoxTextColor = 0xFFD1D5DB,
        headerBgColor = 0x00000000,
        headerTextColor = 0xFFF3F4F6,
        uiButtonBgColor = 0xFF1F242D,
        uiButtonTextColor = 0xFFE5E7EB
    )
)

private val bgPresets = listOf(0xFF090B12, 0xFF07120E, 0xFF071324, 0xFF061E1A, 0xFF42080A, 0xFF111418)
private val boxBgPresets = listOf(0x00000000, 0xD15B1B99, 0xC212161F, 0xE60E150F, 0xE60D1F3C, 0xE60C352E)
private val goldAndLightPresets = listOf(0xFFFFFFFF, 0xFFC5A038, 0xFFFFE082, 0xFFEAE0FF, 0xFFB2DFDB, 0xFF90CAF9)
private val goldAndAccentPresets = listOf(0xD66A1B9A, 0x38FFFFFF, 0xFFD4AF37, 0xFF2563EB, 0xFF26A69A, 0xFF9E2A2B)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPickerRow(
    title: String,
    selectedColorLong: Long,
    presets: List<Long>,
    onSelect: (Long) -> Unit
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = "#%08X".format(selectedColorLong.toInt()),
                color = Color(0xFF9FB6B9),
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { preset ->
                val isSelected = selectedColorLong == preset
                Surface(
                    shape = CircleShape,
                    border = BorderStroke(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) MosqueGoldAccent else Color.White.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { onSelect(preset) }
                ) {
                    Box(
                        modifier = Modifier.background(Color(preset.toInt())),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "مختار",
                                tint = if (Color(preset.toInt()).luminanceSafe() > 0.5f) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = SettingsInputBg,
                border = BorderStroke(1.dp, MosqueGoldAccent.copy(alpha = 0.7f)),
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable { showCustomDialog = true }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = "لون مخصص",
                        tint = MosqueGoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomColorPickerDialog(
            title = title,
            initialColorLong = selectedColorLong,
            onDismiss = { showCustomDialog = false },
            onConfirm = { picked ->
                onSelect(picked)
                showCustomDialog = false
            }
        )
    }
}

@Composable
private fun CustomColorPickerDialog(
    title: String,
    initialColorLong: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val initInt = initialColorLong.toInt()
    var alpha by remember { mutableFloatStateOf(((initInt ushr 24) and 0xFF) / 255f) }
    var red by remember { mutableFloatStateOf(((initInt ushr 16) and 0xFF) / 255f) }
    var green by remember { mutableFloatStateOf(((initInt ushr 8) and 0xFF) / 255f) }
    var blue by remember { mutableFloatStateOf((initInt and 0xFF) / 255f) }

    val currentColorLong = remember(alpha, red, green, blue) {
        val a = (alpha * 255f).roundToInt().coerceIn(0, 255)
        val r = (red * 255f).roundToInt().coerceIn(0, 255)
        val g = (green * 255f).roundToInt().coerceIn(0, 255)
        val b = (blue * 255f).roundToInt().coerceIn(0, 255)
        ((a.toLong() shl 24) or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong())
    }
    var hexInput by remember(currentColorLong) {
        mutableStateOf("%08X".format(currentColorLong.toInt()))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SettingsCardDark,
        title = { Text("اختيار لون مخصص: $title", color = MosqueGoldAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = Color(currentColorLong.toInt()),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "معاينة النص فوق اللون",
                            color = if (Color(currentColorLong.toInt()).luminanceSafe() > 0.5f) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { raw ->
                        val cleaned = raw.removePrefix("#").uppercase().take(8)
                        hexInput = cleaned
                        if (cleaned.length == 6 || cleaned.length == 8) {
                            val parsed = cleaned.toLongOrNull(16)
                            if (parsed != null) {
                                val full = if (cleaned.length == 6) (0xFF000000L or parsed) else parsed
                                val i = full.toInt()
                                alpha = ((i ushr 24) and 0xFF) / 255f
                                red = ((i ushr 16) and 0xFF) / 255f
                                green = ((i ushr 8) and 0xFF) / 255f
                                blue = (i and 0xFF) / 255f
                            }
                        }
                    },
                    label = { Text("كود اللون (AARRGGBB أو RRGGBB)") },
                    singleLine = true,
                    colors = readableTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("الأحمر (R): ${(red * 255).roundToInt()}", color = Color(0xFFFF8A80), fontSize = 12.sp)
                Slider(value = red, onValueChange = { red = it }, colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red))

                Text("الأخضر (G): ${(green * 255).roundToInt()}", color = Color(0xFFA5D6A7), fontSize = 12.sp)
                Slider(value = green, onValueChange = { green = it }, colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green))

                Text("الأزرق (B): ${(blue * 255).roundToInt()}", color = Color(0xFF90CAF9), fontSize = 12.sp)
                Slider(value = blue, onValueChange = { blue = it }, colors = SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Blue))

                Text("الشفافية (Alpha): ${(alpha * 100).roundToInt()}%", color = Color.White, fontSize = 12.sp)
                Slider(value = alpha, onValueChange = { alpha = it }, valueRange = 0.0f..1.0f)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(currentColorLong) },
                colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldAccent)
            ) {
                Text("اعتماد اللون", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = Color.White) }
        }
    )
}

private fun Color.luminanceSafe(): Float = 0.299f * red + 0.587f * green + 0.114f * blue
