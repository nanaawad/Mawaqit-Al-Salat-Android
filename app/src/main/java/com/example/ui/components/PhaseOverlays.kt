package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.GlobalSettingsEntity
import com.example.data.model.MosquePhase
import com.example.ui.theme.MosqueGoldAccent

@Composable
fun MosqueBackgroundLayer(
    customFilePath: String?,
    defaultDrawableRes: Int,
    fallbackColor: Color,
    scrimAlpha: Float = 0.45f
) {
    val file = TimeFormatters.validFileOrNull(customFilePath)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fallbackColor)
    ) {
        if (file != null) {
            AsyncImage(
                model = file,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = defaultDrawableRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = scrimAlpha),
                            Color.Black.copy(alpha = (scrimAlpha + 0.15f).coerceAtMost(0.85f))
                        )
                    )
                )
        )
    }
}

@Composable
fun AdhanOverlay(
    phase: MosquePhase.AdhanActive,
    settings: GlobalSettingsEntity,
    clockText: String,
    isAudioPlaying: Boolean,
    onStopAudio: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("adhan_overlay")
    ) {
        MosqueBackgroundLayer(
            customFilePath = settings.adhanImagePath,
            defaultDrawableRes = R.drawable.img_bg_adhan,
            fallbackColor = Color(0xFF1B1205),
            scrimAlpha = 0.40f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "${settings.mosqueName.ifBlank { "مسجد النور" }}  •  $clockText",
                    color = Color.White,
                    fontSize = (22 * settings.textScale).sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                )
            }

            Surface(
                color = Color(0xCC0A1D19),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.widthIn(max = 760.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 40.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = phase.message,
                        color = MosqueGoldAccent,
                        fontSize = (38 * settings.textScale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = (50 * settings.textScale).sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "الصلاة خير من النوم • الله أكبر"
                            .takeIf { phase.prayer.name == "FAJR" }
                            ?: "حيّ على الصلاة • حيّ على الفلاح",
                        color = Color.White,
                        fontSize = (22 * settings.textScale).sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (isAudioPlaying) {
                    FilledTonalButton(
                        onClick = onStopAudio,
                        modifier = Modifier.testTag("stop_adhan_audio_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeOff, contentDescription = "كتم صوت الأذان")
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("كتم الصوت")
                    }
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldAccent),
                    modifier = Modifier.testTag("dismiss_adhan_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق شاشة الأذان", tint = Color.Black)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("متابعة", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun IqamaCountdownOverlay(
    phase: MosquePhase.IqamaCountdown,
    settings: GlobalSettingsEntity,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("iqama_overlay")
    ) {
        MosqueBackgroundLayer(
            customFilePath = settings.iqamaBgImagePath ?: settings.mainBgImagePath,
            defaultDrawableRes = R.drawable.img_bg_mosque,
            fallbackColor = Color(0xFF061E1A),
            scrimAlpha = 0.55f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "العد التنازلي لإقامة صلاة ${phase.prayer.arabicName}",
                color = MosqueGoldAccent,
                fontSize = (32 * settings.textScale).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                color = Color(0xDD0A2E28),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text(
                    text = TimeFormatters.formatDurationMs(phase.remainingSeconds),
                    color = Color.White,
                    fontSize = (92 * settings.countdownScale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 48.dp, vertical = 20.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "يستحب الدعاء بين الأذان والإقامة",
                color = Color(0xFFE0F2F1),
                fontSize = (20 * settings.textScale).sp
            )
            Spacer(modifier = Modifier.height(28.dp))
            FilledTonalButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_iqama_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = "تخطي العد التنازلي")
                Spacer(modifier = Modifier.size(8.dp))
                Text("العودة للشاشة الرئيسية")
            }
        }
    }
}

@Composable
fun TurnOffPhoneOverlay(
    phase: MosquePhase.TurnOffPhoneScreen,
    settings: GlobalSettingsEntity,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }

    val customFile = TimeFormatters.validFileOrNull(settings.phoneOffImagePath)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10161A))
            .testTag("phone_off_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A2328)),
                contentAlignment = Alignment.Center
            ) {
                if (customFile != null) {
                    AsyncImage(
                        model = customFile,
                        contentDescription = "الرجاء إغلاق الهاتف",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_turn_off_phone),
                        contentDescription = "الرجاء إغلاق الهاتف",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "الرجاء إغلاق الهاتف أو وضعه على الصامت",
                color = Color(0xFFFF6B5B),
                fontSize = (34 * settings.textScale).sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "حان الآن وقت إقامة صلاة ${phase.prayer.arabicName} • سوّوا صفوفكم",
                color = Color.White,
                fontSize = (22 * settings.textScale).sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "${phase.remainingSeconds} ثانية",
                color = Color(0xFFB0BEC5),
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            FilledTonalButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_phone_off_button")
            ) {
                Text("إغلاق")
            }
        }
    }
}

@Composable
fun PostPrayerAdhkarOverlay(
    phase: MosquePhase.PostPrayerAdhkar,
    settings: GlobalSettingsEntity,
    onTogglePause: () -> Unit,
    onNext: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("adhkar_overlay")
    ) {
        MosqueBackgroundLayer(
            customFilePath = settings.adhkarBgImagePath,
            defaultDrawableRes = R.drawable.img_bg_adhkar,
            fallbackColor = Color(0xFF071B26),
            scrimAlpha = 0.42f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xBB0B2A38),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "أذكار ما بعد الصلاة",
                        color = MosqueGoldAccent,
                        fontSize = (22 * settings.textScale).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
                Surface(
                    color = Color(0xBB0B2A38),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "${phase.currentIndex + 1} / ${phase.totalItems}  •  ${TimeFormatters.formatDurationMs(phase.remainingSeconds)}",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            Surface(
                color = Color(0xCC0A222E),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 920.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 36.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = phase.dhikrText,
                        color = Color.White,
                        fontSize = (28 * settings.textScale).sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = (44 * settings.textScale).sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "${phase.repeatLabel}  •  ${phase.dhikrSource}",
                        color = MosqueGoldAccent,
                        fontSize = (17 * settings.textScale).sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(
                    onClick = onTogglePause,
                    modifier = Modifier.testTag("adhkar_pause_button")
                ) {
                    Icon(
                        if (phase.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (phase.isPaused) "استئناف الأذكار" else "إيقاف مؤقت"
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(if (phase.isPaused) "استئناف" else "إيقاف مؤقت")
                }
                FilledTonalButton(
                    onClick = onNext,
                    modifier = Modifier.testTag("adhkar_next_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "الذكر التالي")
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("التالي")
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldAccent),
                    modifier = Modifier.testTag("adhkar_close_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "إنهاء الأذكار", tint = Color.Black)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("إغلاق", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
