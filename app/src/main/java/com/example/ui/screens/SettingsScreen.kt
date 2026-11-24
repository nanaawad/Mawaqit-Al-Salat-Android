package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.settings.AdhanAndPhoneOffContent
import com.example.ui.screens.settings.BackupAndExportContent
import com.example.ui.screens.settings.CityAndCalcTab
import com.example.ui.screens.settings.CollapsibleAccordionSection
import com.example.ui.screens.settings.ColorsAndPresetsContent
import com.example.ui.screens.settings.DisplayAndClockContent
import com.example.ui.screens.settings.IqamaAndJumuahContent
import com.example.ui.screens.settings.MediaUploadTab
import com.example.ui.screens.settings.PostPrayerAdhkarContent
import com.example.ui.screens.settings.PrayerManualOffsetsContent
import com.example.ui.theme.MosqueGoldAccent
import com.example.ui.theme.SettingsSurfaceDark
import com.example.ui.viewmodel.MosqueUiState
import com.example.ui.viewmodel.MosqueViewModel

@Composable
fun SettingsScreen(
    state: MosqueUiState,
    viewModel: MosqueViewModel,
    initialTab: Int = 0,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val defaultExpanded = remember(initialTab) {
        when (initialTab) {
            1 -> setOf("iqama_jumuah", "prayer_offsets")
            2 -> setOf("adhan_phone", "post_adhkar")
            3 -> setOf("media_audio")
            4 -> setOf("colors_theme")
            else -> setOf("countries_cities")
        }
    }
    var expandedSections by remember { mutableStateOf(defaultExpanded) }

    fun toggleSection(key: String) {
        expandedSections = if (key in expandedSections) expandedSections - key else expandedSections + key
    }

    val allSectionKeys = listOf(
        "countries_cities",
        "iqama_jumuah",
        "prayer_offsets",
        "adhan_phone",
        "post_adhkar",
        "media_audio",
        "colors_theme",
        "display_clock",
        "backup_export"
    )
    val allExpanded = allSectionKeys.all { it in expandedSections }

    val activeCity = state.currentCity
    val countryCount = remember(state.allCities) {
        state.allCities.map { it.countryAr }.distinct().size
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SettingsSurfaceDark)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 920.dp)
                .align(Alignment.TopCenter)
        ) {
            // Top Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "العودة للشاشة الرئيسية",
                            tint = MosqueGoldAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "الإعدادات",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                        activeCity?.let {
                            Text(
                                text = "${it.countryAr} — ${it.cityAr}",
                                color = Color(0xFFB2DFDB),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            expandedSections = if (allExpanded) emptySet() else allSectionKeys.toSet()
                        },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = if (allExpanded) "طي الكل" else "فتح الكل",
                            color = MosqueGoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldAccent),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("done_settings_button")
                    ) {
                        Text("عرض الشاشة", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Save Confirmation / Error Banner
            state.saveBannerMessage?.let { msg ->
                Surface(
                    color = if (state.saveBannerIsError) Color(0xFF5D1D1D) else Color(0xFF144D3D),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .testTag("save_status_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (state.saveBannerIsError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (state.saveBannerIsError) Color(0xFFFF8A80) else MosqueGoldAccent
                        )
                        Text(msg, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Vertical Collapsible Dropdown List of All Settings Sections
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Countries & Cities (with interactive Country & City dropdowns)
                CollapsibleAccordionSection(
                    title = "الدول والمدن والمزامنة",
                    subtitle = activeCity?.let { "${it.countryAr} • ${it.cityAr} ($countryCount دولة)" } ?: "$countryCount دولة",
                    icon = Icons.Default.Public,
                    expanded = "countries_cities" in expandedSections,
                    onToggle = { toggleSection("countries_cities") },
                    testTag = "section_countries_cities"
                ) {
                    CityAndCalcTab(state, viewModel)
                }

                // 2. Iqama & Jumuah
                CollapsibleAccordionSection(
                    title = "إقامة الصلاة وصلاة الجمعة",
                    subtitle = activeCity?.let { "الفجر +${it.iqamaMinutes[com.example.data.model.PrayerName.FAJR] ?: 20} د • الظهر +${it.iqamaMinutes[com.example.data.model.PrayerName.DHUHR] ?: 15} د" },
                    icon = Icons.Default.AccessTime,
                    expanded = "iqama_jumuah" in expandedSections,
                    onToggle = { toggleSection("iqama_jumuah") },
                    testTag = "section_iqama_jumuah"
                ) {
                    IqamaAndJumuahContent(state, viewModel)
                }

                // 3. Prayer Times & Manual Offsets
                CollapsibleAccordionSection(
                    title = "مواقيت الصلوات والتعديلات اليدوية",
                    subtitle = if (activeCity?.temporaryOffsetsOnly == true) "التعديل مؤقت لليوم فقط" else "التعديل دائم للأيام القادمة",
                    icon = Icons.Default.Schedule,
                    expanded = "prayer_offsets" in expandedSections,
                    onToggle = { toggleSection("prayer_offsets") },
                    testTag = "section_prayer_offsets"
                ) {
                    PrayerManualOffsetsContent(state, viewModel)
                }

                // 4. Adhan Audio & Turn Off Phone
                CollapsibleAccordionSection(
                    title = "الأذان وإغلاق الهاتف والمحاكاة",
                    subtitle = if (state.settings.adhanAudioEnabled) "صوت الأذان مفعّل • إغلاق الهاتف ${state.settings.phoneOffDurationSeconds} ث" else "صوت الأذان مكتوم",
                    icon = Icons.Default.NotificationsActive,
                    expanded = "adhan_phone" in expandedSections,
                    onToggle = { toggleSection("adhan_phone") },
                    testTag = "section_adhan_phone"
                ) {
                    AdhanAndPhoneOffContent(state, viewModel, onCloseSettingsForSimulation = onClose)
                }

                // 5. Post-Prayer Adhkar
                CollapsibleAccordionSection(
                    title = "أذكار ما بعد الصلاة",
                    subtitle = "${state.adhkarList.count { it.isEnabled }} ذكر مفعّل من أصل ${state.adhkarList.size}",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    expanded = "post_adhkar" in expandedSections,
                    onToggle = { toggleSection("post_adhkar") },
                    testTag = "section_post_adhkar"
                ) {
                    PostPrayerAdhkarContent(state, viewModel, onCloseSettingsForSimulation = onClose)
                }

                // 6. Images, Backgrounds & Custom Audio
                CollapsibleAccordionSection(
                    title = "الصور والخلفيات وأصوات الأذان",
                    subtitle = "رفع وتغيير صور الخلفيات والأذان والإقامة",
                    icon = Icons.Default.PermMedia,
                    expanded = "media_audio" in expandedSections,
                    onToggle = { toggleSection("media_audio") },
                    testTag = "section_media_audio"
                ) {
                    MediaUploadTab(state, viewModel)
                }

                // 7. Colors & Themes
                CollapsibleAccordionSection(
                    title = "الألوان والخطوط وخلفيات الصناديق",
                    subtitle = "توحيد ألوان الخطوط والصناديق أو تخصيص 19 عنصراً",
                    icon = Icons.Default.Palette,
                    expanded = "colors_theme" in expandedSections,
                    onToggle = { toggleSection("colors_theme") },
                    testTag = "section_colors_theme"
                ) {
                    ColorsAndPresetsContent(state, viewModel)
                }

                // 8. Clock, Display & Elements
                CollapsibleAccordionSection(
                    title = "الشاشة والساعة وإظهار العناصر والطقس",
                    subtitle = "${state.settings.mosqueName} • ${if (state.settings.is24HourFormat) "24 ساعة" else "12 ساعة"}",
                    icon = Icons.Default.DisplaySettings,
                    expanded = "display_clock" in expandedSections,
                    onToggle = { toggleSection("display_clock") },
                    testTag = "section_display_clock"
                ) {
                    DisplayAndClockContent(state, viewModel)
                }

                // 9. Backup, Import & Laptop Export (index.html)
                CollapsibleAccordionSection(
                    title = "النسخ الاحتياطي وتصدير الكمبيوتر (index.html)",
                    subtitle = "تصدير ملف index.html للابتوب وحفظ نسخة JSON",
                    icon = Icons.Default.SaveAlt,
                    expanded = "backup_export" in expandedSections,
                    onToggle = { toggleSection("backup_export") },
                    testTag = "section_backup_export"
                ) {
                    BackupAndExportContent(viewModel)
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}
