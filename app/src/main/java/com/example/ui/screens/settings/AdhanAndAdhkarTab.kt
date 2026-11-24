package com.example.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.PrayerName
import com.example.ui.theme.MosqueGoldAccent
import com.example.ui.theme.SettingsCardDark
import com.example.ui.theme.SettingsInputBg
import com.example.ui.viewmodel.MosqueUiState
import com.example.ui.viewmodel.MosqueViewModel

@Composable
fun AdhanAndAdhkarTab(
    state: MosqueUiState,
    viewModel: MosqueViewModel,
    onCloseSettingsForSimulation: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        AdhanAndPhoneOffContent(state, viewModel, onCloseSettingsForSimulation)
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
        PostPrayerAdhkarContent(state, viewModel, onCloseSettingsForSimulation)
    }
}

@Composable
fun AdhanAndPhoneOffContent(
    state: MosqueUiState,
    viewModel: MosqueViewModel,
    onCloseSettingsForSimulation: () -> Unit
) {
    val s = state.settings
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("تشغيل صوت الأذان تلقائياً عند دخول الوقت", color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Switch(
                checked = s.adhanAudioEnabled,
                onCheckedChange = { enabled -> viewModel.updateSettings { it.copy(adhanAudioEnabled = enabled) } },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = MosqueGoldAccent)
            )
        }

        StepperSettingRow(
            label = "مدة عرض صورة الأذان",
            valueText = "${s.adhanDisplaySeconds} ثانية",
            onDecrease = { viewModel.updateSettings { it.copy(adhanDisplaySeconds = (it.adhanDisplaySeconds - 10).coerceAtLeast(15)) } },
            onIncrease = { viewModel.updateSettings { it.copy(adhanDisplaySeconds = (it.adhanDisplaySeconds + 10).coerceAtMost(300)) } }
        )

        StepperSettingRow(
            label = "مدة عرض صورة إغلاق الهاتف عند الإقامة",
            valueText = "${s.phoneOffDurationSeconds} ثانية",
            onDecrease = { viewModel.updateSettings { it.copy(phoneOffDurationSeconds = (it.phoneOffDurationSeconds - 5).coerceAtLeast(5)) } },
            onIncrease = { viewModel.updateSettings { it.copy(phoneOffDurationSeconds = (it.phoneOffDurationSeconds + 5).coerceAtMost(120)) } }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { viewModel.previewAdhanAudio(PrayerName.FAJR) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MosqueGoldAccent)
                Text("تجربة أذان الفجر", color = MosqueGoldAccent, fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { viewModel.previewAdhanAudio(PrayerName.DHUHR) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MosqueGoldAccent)
                Text("تجربة باقي الصلوات", color = MosqueGoldAccent, fontSize = 12.sp)
            }
            if (state.isAudioPlaying) {
                OutlinedButton(onClick = { viewModel.stopAudio() }) {
                    Icon(Icons.Default.Stop, contentDescription = "إيقاف الصوت", tint = Color(0xFFFF8A80))
                }
            }
        }

        Button(
            onClick = {
                viewModel.startFullCycleSimulation(PrayerName.ASR)
                onCloseSettingsForSimulation()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldAccent),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("simulate_full_cycle_button")
        ) {
            Text(
                text = "اختبار الدورة الكاملة: أذان ← عد إقامة ← إغلاق الهاتف ← أذكار",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PostPrayerAdhkarContent(
    state: MosqueUiState,
    viewModel: MosqueViewModel,
    onCloseSettingsForSimulation: () -> Unit
) {
    val s = state.settings
    var showAddDhikrDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("تفعيل عرض الأذكار تلقائياً بعد الصلاة", color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Switch(
                checked = s.adhkarEnabled,
                onCheckedChange = { enabled -> viewModel.updateSettings { it.copy(adhkarEnabled = enabled) } },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = MosqueGoldAccent)
            )
        }

        StepperSettingRow(
            label = "بداية العرض بعد انتهاء الإقامة",
            valueText = "${s.adhkarStartDelaySeconds} ثانية",
            onDecrease = { viewModel.updateSettings { it.copy(adhkarStartDelaySeconds = (it.adhkarStartDelaySeconds - 10).coerceAtLeast(0)) } },
            onIncrease = { viewModel.updateSettings { it.copy(adhkarStartDelaySeconds = (it.adhkarStartDelaySeconds + 10).coerceAtMost(900)) } }
        )

        StepperSettingRow(
            label = "إجمالي مدة عرض الأذكار",
            valueText = "${s.adhkarTotalDurationSeconds} ثانية",
            onDecrease = { viewModel.updateSettings { it.copy(adhkarTotalDurationSeconds = (it.adhkarTotalDurationSeconds - 30).coerceAtLeast(30)) } },
            onIncrease = { viewModel.updateSettings { it.copy(adhkarTotalDurationSeconds = (it.adhkarTotalDurationSeconds + 30).coerceAtMost(900)) } }
        )

        StepperSettingRow(
            label = "مدة ظهور كل ذكر قبل الانتقال التلقائي",
            valueText = "${s.adhkarItemDurationSeconds} ثانية",
            onDecrease = { viewModel.updateSettings { it.copy(adhkarItemDurationSeconds = (it.adhkarItemDurationSeconds - 3).coerceAtLeast(5)) } },
            onIncrease = { viewModel.updateSettings { it.copy(adhkarItemDurationSeconds = (it.adhkarItemDurationSeconds + 3).coerceAtMost(120)) } }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    viewModel.toggleManualAdhkarDisplay()
                    onCloseSettingsForSimulation()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("تشغيل عرض الأذكار الآن", color = MosqueGoldAccent)
            }
            OutlinedButton(
                onClick = { showAddDhikrDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة ذكر", color = Color.White)
            }
        }

        OutlinedButton(
            onClick = { viewModel.resetAdhkarToCatalog() },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reload_full_adhkar_button")
        ) {
            Text(
                text = "تحديث واستعادة أذكار ما بعد الصلاة الكاملة (13 ذكراً)",
                color = Color(0xFF80CBC4),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text("قائمة الأذكار المحفوظة (${state.adhkarList.size} ذكر):", color = Color(0xFFB2DFDB), fontSize = 13.sp)

        state.adhkarList.forEach { dhikr ->
            Surface(
                color = SettingsInputBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(dhikr.textAr, color = Color.White, fontSize = 13.sp, maxLines = 6)
                        Text("${dhikr.repeatLabel} • ${dhikr.sourceAr}", color = MosqueGoldAccent, fontSize = 11.sp)
                    }
                    Switch(
                        checked = dhikr.isEnabled,
                        onCheckedChange = { viewModel.toggleAdhkarItemEnabled(dhikr) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = MosqueGoldAccent)
                    )
                }
            }
        }
    }

    if (showAddDhikrDialog) {
        var textAr by remember { mutableStateOf("") }
        var sourceAr by remember { mutableStateOf("") }
        var repeatAr by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDhikrDialog = false },
            containerColor = SettingsCardDark,
            title = { Text("إضافة ذكر جديد", color = MosqueGoldAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = textAr, onValueChange = { textAr = it },
                        label = { Text("نص الذكر") }, colors = readableTextFieldColors()
                    )
                    OutlinedTextField(
                        value = repeatAr, onValueChange = { repeatAr = it },
                        label = { Text("التكرار (مثلاً: ثلاث مرات)") }, singleLine = true, colors = readableTextFieldColors()
                    )
                    OutlinedTextField(
                        value = sourceAr, onValueChange = { sourceAr = it },
                        label = { Text("المصدر (اختياري)") }, singleLine = true, colors = readableTextFieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCustomAdhkarItem(textAr, sourceAr, repeatAr)
                        showAddDhikrDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldAccent)
                ) { Text("حفظ", color = Color.Black, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDhikrDialog = false }) { Text("إلغاء", color = Color.White) }
            }
        )
    }
}
