package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerName
import com.example.ui.components.TimeFormatters
import com.example.ui.theme.MosqueGoldAccent
import com.example.ui.theme.SettingsInputBg
import com.example.ui.viewmodel.MosqueUiState
import com.example.ui.viewmodel.MosqueViewModel

@Composable
fun PrayerOffsetsTab(
    state: MosqueUiState,
    viewModel: MosqueViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        IqamaAndJumuahContent(state, viewModel)
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
        PrayerManualOffsetsContent(state, viewModel)
    }
}

@Composable
fun IqamaAndJumuahContent(
    state: MosqueUiState,
    viewModel: MosqueViewModel
) {
    val city = state.currentCity ?: return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "مدة الإقامة لكل صلاة لمدينة ${city.cityAr} (بالدقائق):",
            color = MosqueGoldAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp
        )
        (PrayerName.fiveDailyPrayers + PrayerName.JUMUAH).forEach { prayer ->
            val mins = city.iqamaMinutes[prayer] ?: 15
            Surface(
                color = SettingsInputBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BoxPadding(horizontal = 12, vertical = 6) {
                    StepperSettingRow(
                        label = "إقامة صلاة ${prayer.arabicName}",
                        valueText = "$mins دقيقة",
                        onDecrease = { viewModel.setIqamaMinutes(prayer, mins - 1) },
                        onIncrease = { viewModel.setIqamaMinutes(prayer, mins + 1) }
                    )
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

        Text(
            text = "إعدادات صلاة الجمعة:",
            color = MosqueGoldAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("اتّباع وقت صلاة الظهر تلقائياً", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("عند الإيقاف يمكنك تحديد وقت ثابت للجمعة", color = Color(0xFF9FB6B9), fontSize = 12.sp)
            }
            Switch(
                checked = city.jumuahFollowsDhuhr,
                onCheckedChange = { follow ->
                    viewModel.updateCityConfig { it.copy(jumuahFollowsDhuhr = follow) }
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = MosqueGoldAccent)
            )
        }
        if (!city.jumuahFollowsDhuhr) {
            StepperSettingRow(
                label = "ساعة صلاة الجمعة المخصصة",
                valueText = "%02d:%02d".format(city.jumuahCustomHour, city.jumuahCustomMinute),
                onDecrease = {
                    viewModel.updateCityConfig {
                        val total = (it.jumuahCustomHour * 60 + it.jumuahCustomMinute - 5).coerceAtLeast(10 * 60)
                        it.copy(jumuahCustomHour = total / 60, jumuahCustomMinute = total % 60)
                    }
                },
                onIncrease = {
                    viewModel.updateCityConfig {
                        val total = (it.jumuahCustomHour * 60 + it.jumuahCustomMinute + 5).coerceAtMost(15 * 60)
                        it.copy(jumuahCustomHour = total / 60, jumuahCustomMinute = total % 60)
                    }
                }
            )
        }
    }
}

@Composable
fun PrayerManualOffsetsContent(
    state: MosqueUiState,
    viewModel: MosqueViewModel
) {
    val city = state.currentCity ?: return
    val schedule = state.todaySchedule
    val is24 = state.settings.is24HourFormat

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            color = SettingsInputBg,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (city.temporaryOffsetsOnly) "تعديل مؤقت لليوم فقط" else "تعديل دائم (يُطبق تلقائياً على الأيام الجديدة)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "يحفظ فرق الدقائق فوق الحساب الفلكي اليومي.",
                            color = Color(0xFFB0C4C7),
                            fontSize = 11.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = !city.temporaryOffsetsOnly,
                        onCheckedChange = { permanent -> viewModel.setOffsetTemporaryMode(!permanent) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = MosqueGoldAccent),
                        modifier = Modifier.testTag("permanent_offset_switch")
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.resetAllCityOffsets() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_all_offsets_button")
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color(0xFFFFAB91))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إعادة جميع مواقيت ${city.cityAr} إلى الحساب الأصلي", color = Color(0xFFFFAB91), fontSize = 12.5.sp)
                }
            }
        }

        PrayerName.adjustablePrayers.forEach { prayer ->
            val entry = schedule?.entries?.get(prayer)
            val origStr = entry?.let { TimeFormatters.formatMinuteOfDay(it.originalMinuteOfDay, is24) } ?: "--:--"
            val adjStr = entry?.let { TimeFormatters.formatMinuteOfDay(it.adjustedMinuteOfDay, is24) } ?: "--:--"
            val offset = entry?.offsetMinutes ?: (city.manualOffsetsMinutes[prayer] ?: 0)

            Surface(
                color = SettingsInputBg,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (offset != 0) MosqueGoldAccent.copy(alpha = 0.5f) else Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(prayer.arabicName, color = Color.White, fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "الأصلي: $origStr  ←  المعدل: $adjStr",
                                color = Color(0xFFB2DFDB),
                                fontSize = 12.5.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = TimeFormatters.formatOffsetBadge(offset),
                                color = if (offset == 0) Color(0xFF9FB6B9) else MosqueGoldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                            if (offset != 0) {
                                TextButton(
                                    onClick = { viewModel.resetSinglePrayerOffset(prayer) },
                                    modifier = Modifier.testTag("reset_offset_${prayer.name.lowercase()}")
                                ) {
                                    Text("إلغاء", color = Color(0xFFFFAB91), fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(-5 to "-5", -1 to "-1", 1 to "+1", 5 to "+5").forEach { (delta, label) ->
                            Button(
                                onClick = { viewModel.adjustPrayerOffset(prayer, delta) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (delta > 0) Color(0xFF1A4D43) else Color(0xFF422626),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("offset_${prayer.name.lowercase()}_$delta")
                            ) {
                                Text(label, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxPadding(
    horizontal: Int,
    vertical: Int,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = horizontal.dp, vertical = vertical.dp)) {
        content()
    }
}
