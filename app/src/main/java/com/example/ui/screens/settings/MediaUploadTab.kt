package com.example.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MediaSlot
import com.example.data.model.PrayerName
import com.example.media.BuiltinAdhanTone
import com.example.ui.components.TimeFormatters
import com.example.ui.theme.MosqueGoldAccent
import com.example.ui.theme.SettingsInputBg
import com.example.ui.viewmodel.MosqueUiState
import com.example.ui.viewmodel.MosqueViewModel

private data class StagedAudioSelection(
    val slot: MediaSlot,
    val label: String,
    val uri: Uri,
    val displayName: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaUploadTab(
    state: MosqueUiState,
    viewModel: MosqueViewModel
) {
    val context = LocalContext.current
    val s = state.settings
    var pendingImageSlot by remember { mutableStateOf<MediaSlot?>(null) }
    var pendingAudioSlot by remember { mutableStateOf<Pair<MediaSlot, String>?>(null) }
    var stagedAudio by remember { mutableStateOf<StagedAudioSelection?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val slot = pendingImageSlot
        if (uri != null && slot != null) {
            viewModel.savePickedMedia(slot, uri)
        }
        pendingImageSlot = null
    }

    val fallbackImageDocumentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        val slot = pendingImageSlot
        if (uri != null && slot != null) {
            viewModel.savePickedMedia(slot, uri)
        }
        pendingImageSlot = null
    }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        val pending = pendingAudioSlot
        if (uri != null && pending != null) {
            val fileName = viewModel.resolvePickedFileName(uri)
            stagedAudio = StagedAudioSelection(
                slot = pending.first,
                label = pending.second,
                uri = uri,
                displayName = fileName
            )
        }
        pendingAudioSlot = null
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "الخلفيات والصور (حفظ دائم ومستقل لكل قسم):",
            color = MosqueGoldAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "اختر صور الخلفية للشاشة الرئيسية أو صندوق الساعة أو شاشات الأذان والإقامة والأذكار.",
            color = Color(0xFFB0C4C7),
            fontSize = 12.sp
        )

        Text(
            text = "سطوع الخلفية وتعتيم التباين: ${((1f - s.bgBrightnessAlpha) * 100).toInt()}%",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = 1f - s.bgBrightnessAlpha,
            onValueChange = { brightness ->
                val scrim = (1f - brightness).coerceIn(0.05f, 0.78f)
                viewModel.updateSettings(successMessage = null) { it.copy(bgBrightnessAlpha = scrim) }
            },
            valueRange = 0.22f..0.95f,
            colors = SliderDefaults.colors(thumbColor = MosqueGoldAccent, activeTrackColor = MosqueGoldAccent)
        )

        val imageSlots = listOf(
            Triple(MediaSlot.MAIN_BG, "خلفية الشاشة الرئيسية", s.mainBgImagePath),
            Triple(MediaSlot.CLOCK_BG, "خلفية صندوق الساعة الرئيسية", s.clockBoxBgImagePath),
            Triple(MediaSlot.ADHAN_IMG, "صورة الأذان", s.adhanImagePath),
            Triple(MediaSlot.IQAMA_BG, "خلفية العد التنازلي للإقامة", s.iqamaBgImagePath),
            Triple(MediaSlot.PHONE_OFF_IMG, "صورة إغلاق الهاتف عند الإقامة", s.phoneOffImagePath),
            Triple(MediaSlot.ADHKAR_BG, "خلفية أذكار ما بعد الصلاة", s.adhkarBgImagePath)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            imageSlots.forEach { (slot, label, path) ->
                val savedFile = TimeFormatters.validFileOrNull(path)
                MediaSlotRow(
                    label = label,
                    statusText = if (savedFile != null) "مخصص محفوظ (${savedFile.name})" else if (slot == MediaSlot.CLOCK_BG) "بدون صورة (لون الصندوق فقط)" else "الصورة الافتراضية المدمجة",
                    isCustom = savedFile != null,
                    isAudio = false,
                    isPlaying = false,
                    testTagPrefix = slot.prefix,
                    onPick = {
                        pendingImageSlot = slot
                        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                            runCatching {
                                imagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }.onFailure {
                                runCatching { fallbackImageDocumentPicker.launch(arrayOf("image/*")) }
                            }
                        } else {
                            runCatching { fallbackImageDocumentPicker.launch(arrayOf("image/*")) }
                        }
                    },
                    onPlayToggle = {},
                    onReset = { viewModel.clearPickedMedia(slot) }
                )
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

        Text(
            text = "أصوات الأذان (استماع قبل الاعتماد + حفظ دائم):",
            color = MosqueGoldAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        val staged = stagedAudio
        if (staged != null) {
            Surface(
                color = Color(0xFF1A3A2E),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, MosqueGoldAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("staged_audio_preview_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "معاينة قبل الاعتماد: ${staged.label}",
                        color = MosqueGoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "الملف المختار: ${staged.displayName}",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                if (state.isAudioPlaying) viewModel.stopAudio()
                                else viewModel.previewPickedAudioUri(staged.uri)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("preview_staged_audio_button")
                        ) {
                            Icon(
                                imageVector = if (state.isAudioPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = MosqueGoldAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.isAudioPlaying) "إيقاف المعاينة" else "استماع قبل الحفظ",
                                color = MosqueGoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.stopAudio()
                                viewModel.savePickedMedia(staged.slot, staged.uri)
                                stagedAudio = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldAccent),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("commit_staged_audio_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اعتماد وحفظ", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.stopAudio()
                            stagedAudio = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إلغاء", color = Color(0xFFFFAB91))
                    }
                }
            }
        }

        val audioSlots = listOf(
            Triple(MediaSlot.FAJR_AUDIO, "صوت أذان الفجر", s.fajrAdhanAudioPath to PrayerName.FAJR),
            Triple(MediaSlot.DEFAULT_AUDIO, "صوت أذان بقية الصلوات", s.defaultAdhanAudioPath to PrayerName.DHUHR)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            audioSlots.forEach { (slot, label, pathAndPrayer) ->
                val (path, prayer) = pathAndPrayer
                val savedFile = TimeFormatters.validFileOrNull(path)
                MediaSlotRow(
                    label = label,
                    statusText = if (savedFile != null) "ملف مخصص محفوظ (${savedFile.name})" else "النغمة المدمجة المختارة",
                    isCustom = savedFile != null,
                    isAudio = true,
                    isPlaying = state.isAudioPlaying,
                    testTagPrefix = slot.prefix,
                    onPick = {
                        pendingAudioSlot = slot to label
                        audioPicker.launch(arrayOf("audio/*"))
                    },
                    onPlayToggle = {
                        if (state.isAudioPlaying) viewModel.stopAudio()
                        else viewModel.previewAdhanAudio(prayer)
                    },
                    onReset = {
                        viewModel.stopAudio()
                        viewModel.clearPickedMedia(slot)
                    }
                )
            }
        }

        Text("اختيار النغمة المدمجة الاحتياطية (مع الاستماع الفوري):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BuiltinAdhanTone.entries.forEach { tone ->
                FilterChip(
                    selected = s.defaultAdhanToneKey == tone.key,
                    onClick = {
                        viewModel.updateSettings("✓ تم حفظ نغمة الأذان المدمجة") {
                            it.copy(defaultAdhanToneKey = tone.key)
                        }
                        viewModel.previewBuiltinTone(tone.key)
                    },
                    label = { Text(tone.arabicName, fontSize = 12.sp) },
                    colors = chipColors()
                )
            }
        }
    }
}

@Composable
private fun MediaSlotRow(
    label: String,
    statusText: String,
    isCustom: Boolean,
    isAudio: Boolean,
    isPlaying: Boolean,
    testTagPrefix: String,
    onPick: () -> Unit,
    onPlayToggle: () -> Unit,
    onReset: () -> Unit
) {
    Surface(
        color = SettingsInputBg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isAudio) Icons.Default.AudioFile else Icons.Default.Image,
                    contentDescription = null,
                    tint = MosqueGoldAccent
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = statusText,
                        color = if (isCustom) Color(0xFF80CBC4) else Color(0xFF9FB6B9),
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onPick,
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldAccent),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("pick_$testTagPrefix")
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCustom) "استبدال" else "اختيار من الجهاز",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isAudio) {
                    OutlinedButton(
                        onClick = onPlayToggle,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("listen_$testTagPrefix")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "استماع",
                            tint = MosqueGoldAccent
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPlaying) "إيقاف" else "استماع", color = MosqueGoldAccent, fontSize = 12.sp)
                    }
                }
                if (isCustom) {
                    OutlinedButton(
                        onClick = onReset,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("reset_$testTagPrefix")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف واستعادة الافتراضي", tint = Color(0xFFFFAB91))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حذف", color = Color(0xFFFFAB91))
                    }
                }
            }
        }
    }
}
