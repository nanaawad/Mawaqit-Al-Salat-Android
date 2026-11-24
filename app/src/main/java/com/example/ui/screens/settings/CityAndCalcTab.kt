package com.example.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.catalog.WorldCitiesCatalog
import com.example.data.model.AsrMadhab
import com.example.data.model.CalculationMethod
import com.example.data.model.DstMode
import com.example.data.model.HighLatitudeRule
import com.example.ui.components.TimeFormatters
import com.example.ui.theme.MosqueGoldAccent
import com.example.ui.theme.SettingsCardDark
import com.example.ui.theme.SettingsInputBg
import com.example.ui.viewmodel.MosqueUiState
import com.example.ui.viewmodel.MosqueViewModel
import kotlinx.coroutines.delay
import java.util.TimeZone

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CityAndCalcTab(
    state: MosqueUiState,
    viewModel: MosqueViewModel
) {
    val currentCity = state.currentCity
    var searchQuery by remember { mutableStateOf("") }
    var selectedCountry by remember(currentCity?.countryAr) { mutableStateOf(currentCity?.countryAr) }
    var countryDropdownExpanded by remember { mutableStateOf(false) }
    var cityDropdownExpanded by remember { mutableStateOf(false) }
    var showCalcDetails by remember { mutableStateOf(false) }
    var showMosqueMeta by remember { mutableStateOf(false) }
    var showAddCityDialog by remember { mutableStateOf(false) }

    val countries = remember(state.allCities) {
        state.allCities.map { it.countryAr to it.countryEn }.distinctBy { it.first }
    }

    val countryCities = remember(state.allCities, selectedCountry) {
        if (selectedCountry == null) state.allCities
        else state.allCities.filter { it.countryAr == selectedCountry }
    }

    val filteredCities = remember(state.allCities, searchQuery, selectedCountry) {
        val normQuery = WorldCitiesCatalog.normalizeArabic(searchQuery)
        if (normQuery.isBlank()) {
            countryCities
        } else {
            state.allCities.filter { city ->
                WorldCitiesCatalog.normalizeArabic(city.cityAr).contains(normQuery) ||
                    WorldCitiesCatalog.normalizeArabic(city.countryAr).contains(normQuery) ||
                    city.cityEn.lowercase().contains(normQuery) ||
                    city.countryEn.lowercase().contains(normQuery)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1. Country Dropdown (All 84+ Countries)
        Text(
            text = "1. اختيار الدولة (${countries.size} دولة متاحة)",
            color = MosqueGoldAccent,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                onClick = { countryDropdownExpanded = true },
                color = SettingsInputBg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MosqueGoldAccent.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("country_dropdown_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = MosqueGoldAccent)
                        Text(
                            text = selectedCountry ?: "جميع الدول (${countries.size} دولة)",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "فتح قائمة الدول", tint = MosqueGoldAccent)
                }
            }

            DropdownMenu(
                expanded = countryDropdownExpanded,
                onDismissRequest = { countryDropdownExpanded = false },
                containerColor = SettingsCardDark,
                modifier = Modifier
                    .fillMaxWidth(0.84f)
                    .heightIn(max = 380.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("عرض جميع الدول (${countries.size} دولة)", color = MosqueGoldAccent, fontWeight = FontWeight.Bold) },
                    onClick = {
                        selectedCountry = null
                        searchQuery = ""
                        countryDropdownExpanded = false
                    }
                )
                countries.forEach { (cAr, cEn) ->
                    val isSel = selectedCountry == cAr
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("$cAr — $cEn", color = if (isSel) MosqueGoldAccent else Color.White, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                if (isSel) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MosqueGoldAccent, modifier = Modifier.size(16.dp))
                            }
                        },
                        onClick = {
                            selectedCountry = cAr
                            searchQuery = ""
                            countryDropdownExpanded = false
                            // Auto-open city dropdown or auto-select if current city isn't in this country
                            val firstCityInCountry = state.allCities.firstOrNull { it.countryAr == cAr }
                            if (firstCityInCountry != null && currentCity?.countryAr != cAr) {
                                viewModel.selectCity(firstCityInCountry.cityId)
                            }
                        }
                    )
                }
            }
        }

        // 2. City Dropdown (All cities inside selected country)
        Text(
            text = "2. اختيار المدينة (${countryCities.size} مدينة${if (selectedCountry != null) " في $selectedCountry" else ""})",
            color = MosqueGoldAccent,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                onClick = { cityDropdownExpanded = true },
                color = SettingsInputBg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MosqueGoldAccent.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("city_dropdown_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocationCity, contentDescription = null, tint = MosqueGoldAccent)
                        Column {
                            Text(
                                text = currentCity?.let { "${it.cityAr} (${it.cityEn})" } ?: "اختر المدينة",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            currentCity?.let {
                                Text(
                                    text = "${it.countryAr} • ${it.timeZoneId}",
                                    color = Color(0xFFB0C4C7),
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "فتح قائمة المدن", tint = MosqueGoldAccent)
                }
            }

            DropdownMenu(
                expanded = cityDropdownExpanded,
                onDismissRequest = { cityDropdownExpanded = false },
                containerColor = SettingsCardDark,
                modifier = Modifier
                    .fillMaxWidth(0.84f)
                    .heightIn(max = 380.dp)
            ) {
                countryCities.forEach { city ->
                    val isSelected = city.cityId == currentCity?.cityId
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (selectedCountry == null) "${city.cityAr} — ${city.countryAr}" else city.cityAr,
                                        color = if (isSelected) MosqueGoldAccent else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    )
                                    Text("${city.cityEn} • ${city.timeZoneId}", color = Color(0xFF9FB6B9), fontSize = 11.sp)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "مختارة", tint = MosqueGoldAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        onClick = {
                            selectedCountry = city.countryAr
                            viewModel.selectCity(city.cityId)
                            cityDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Quick Search across all countries & cities
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث سريع عن أي مدينة أو دولة (برلين، كاسل، عمان، الرياض)...", color = Color(0xFF9FB6B9), fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = MosqueGoldAccent) },
            singleLine = true,
            colors = readableTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("city_search_input")
        )

        if (searchQuery.isNotBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                filteredCities.take(15).forEach { city ->
                    val isSelected = city.cityId == currentCity?.cityId
                    Surface(
                        onClick = {
                            selectedCountry = city.countryAr
                            viewModel.selectCity(city.cityId)
                            searchQuery = ""
                        },
                        color = if (isSelected) Color(0xFF174A40) else SettingsInputBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isSelected) MosqueGoldAccent else Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("city_item_${city.cityId}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${city.cityAr} — ${city.countryAr}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("${city.cityEn}, ${city.countryEn}", color = Color(0xFFB0C4C7), fontSize = 11.5.sp)
                            }
                            if (isSelected) Icon(Icons.Default.CheckCircle, contentDescription = "مختارة", tint = MosqueGoldAccent)
                        }
                    }
                }
            }
        }

        // Quick country shortcuts
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("الأردن", "ألمانيا", "السعودية", "فلسطين", "مصر", "تركيا", "الإمارات").forEach { quickCountry ->
                FilterChip(
                    selected = selectedCountry == quickCountry,
                    onClick = {
                        selectedCountry = quickCountry
                        searchQuery = ""
                        val first = state.allCities.firstOrNull { it.countryAr == quickCountry }
                        if (first != null && currentCity?.countryAr != quickCountry) {
                            viewModel.selectCity(first.cityId)
                        }
                    },
                    label = { Text(quickCountry, fontSize = 12.sp) },
                    colors = chipColors()
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { viewModel.syncPrayerTimesNow() },
                enabled = !state.isSyncingPrayerTimes,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF145A44)),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("sync_prayer_times_button")
            ) {
                if (state.isSyncingPrayerTimes) {
                    CircularProgressIndicator(color = MosqueGoldAccent, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("جاري المزامنة...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مزامنة المواقيت الآن", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            }
            OutlinedButton(
                onClick = { showAddCityDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("add_custom_city_button")
            ) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = MosqueGoldAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة مدينة أخرى", color = MosqueGoldAccent, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (currentCity != null) {
            Text(
                text = "المصدر: ${state.todaySchedule?.sourceLabel ?: "حساب فلكي محلي"} • ${TimeFormatters.formatSyncTimestamp(state.todaySchedule?.lastSyncedAtMillis)}",
                color = Color(0xFFB0C4C7),
                fontSize = 11.5.sp
            )

            // Sub-accordion: Mosque Metadata for active city
            SubToggleBar(
                title = "اسم المسجد والحي لمدينة ${currentCity.cityAr}",
                expanded = showMosqueMeta,
                onToggle = { showMosqueMeta = !showMosqueMeta }
            )
            AnimatedVisibility(visible = showMosqueMeta) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    var cityMosqueDraft by remember(currentCity.cityId, currentCity.mosqueName) { mutableStateOf(currentCity.mosqueName) }
                    var districtDraft by remember(currentCity.cityId, currentCity.district) { mutableStateOf(currentCity.district) }
                    var streetDraft by remember(currentCity.cityId, currentCity.street) { mutableStateOf(currentCity.street) }

                    LaunchedEffect(cityMosqueDraft, districtDraft, streetDraft) {
                        if (cityMosqueDraft != currentCity.mosqueName ||
                            districtDraft != currentCity.district ||
                            streetDraft != currentCity.street
                        ) {
                            delay(450L)
                            viewModel.updateCityMosqueMetadata(
                                mosqueName = cityMosqueDraft.trim(),
                                district = districtDraft.trim(),
                                street = streetDraft.trim()
                            )
                        }
                    }

                    OutlinedTextField(
                        value = cityMosqueDraft,
                        onValueChange = { cityMosqueDraft = it },
                        label = { Text("اسم المسجد (مستقل لهذه المدينة)") },
                        placeholder = { Text(state.settings.mosqueName.ifBlank { "مسجد النور" }, color = Color(0xFF9FB6B9)) },
                        singleLine = true,
                        colors = readableTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("city_mosque_name_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = districtDraft,
                            onValueChange = { districtDraft = it },
                            label = { Text("الحي (اختياري)") },
                            singleLine = true,
                            colors = readableTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = streetDraft,
                            onValueChange = { streetDraft = it },
                            label = { Text("الشارع (اختياري)") },
                            singleLine = true,
                            colors = readableTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Sub-accordion: Astronomical Calculation Method & DST
            SubToggleBar(
                title = "الحساب الفلكي والتوقيت الصيفي والهجري (${currentCity.calculationMethod.arabicName})",
                expanded = showCalcDetails,
                onToggle = { showCalcDetails = !showCalcDetails }
            )
            AnimatedVisibility(visible = showCalcDetails) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("طريقة الحساب المعتمدة:", color = Color(0xFFE0ECEA), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    CalculationMethod.entries.forEach { method ->
                        val selected = currentCity.calculationMethod == method
                        Surface(
                            onClick = { viewModel.setCityCalculationParams(method = method) },
                            color = if (selected) Color(0xFF174A40) else SettingsInputBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (selected) MosqueGoldAccent else Color.Transparent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(method.arabicName, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                                    val desc = if (method.ishaMinutesAfterMaghrib > 0) {
                                        "الفجر ${method.fajrAngle}° • العشاء +${method.ishaMinutesAfterMaghrib} د بعد المغرب"
                                    } else {
                                        "الفجر ${method.fajrAngle}° • العشاء ${method.ishaAngle}°"
                                    }
                                    Text(desc, color = Color(0xFF9FB6B9), fontSize = 11.5.sp)
                                }
                                if (selected) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MosqueGoldAccent)
                            }
                        }
                    }

                    var fajrAngleText by remember(currentCity.cityId, currentCity.calculationMethod, currentCity.customFajrAngle) {
                        mutableStateOf(currentCity.effectiveFajrAngle().toString())
                    }
                    var ishaAngleText by remember(currentCity.cityId, currentCity.calculationMethod, currentCity.customIshaAngle, currentCity.customIshaMinutes) {
                        mutableStateOf(
                            if (currentCity.effectiveIshaMinutes() > 0) "${currentCity.effectiveIshaMinutes()}m"
                            else currentCity.effectiveIshaAngle().toString()
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fajrAngleText,
                            onValueChange = { fajrAngleText = it },
                            label = { Text("زاوية الفجر (°)") },
                            singleLine = true,
                            colors = readableTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = ishaAngleText,
                            onValueChange = { ishaAngleText = it },
                            label = { Text("زاوية العشاء (° أو 90m)") },
                            singleLine = true,
                            colors = readableTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            val f = fajrAngleText.trim().toDoubleOrNull()
                            val iRaw = ishaAngleText.trim().lowercase()
                            val isMins = iRaw.endsWith("m") || iRaw.endsWith("د")
                            val ishaNum = iRaw.replace("m", "").replace("د", "").trim().toDoubleOrNull()
                            if (f != null || ishaNum != null) {
                                viewModel.setCityCalculationParams(
                                    customFajrAngle = f,
                                    customIshaAngle = if (!isMins) ishaNum else null,
                                    clearCustomIshaAngle = isMins,
                                    customIshaMinutes = if (isMins) ishaNum?.toInt() else null,
                                    clearCustomIshaMinutes = !isMins
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ الزوايا المخصصة", color = MosqueGoldAccent, fontWeight = FontWeight.Bold)
                    }

                    Text("مذهب حساب العصر:", color = Color(0xFFE0ECEA), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    AsrMadhab.entries.forEach { madhab ->
                        val selected = currentCity.asrMadhab == madhab
                        Surface(
                            onClick = { viewModel.setCityCalculationParams(madhab = madhab) },
                            color = if (selected) Color(0xFF174A40) else SettingsInputBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (selected) MosqueGoldAccent else Color.Transparent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(madhab.arabicName, color = Color.White, fontSize = 13.5.sp)
                                if (selected) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MosqueGoldAccent)
                            }
                        }
                    }

                    Text("خطوط العرض العليا:", color = Color(0xFFE0ECEA), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    HighLatitudeRule.entries.forEach { rule ->
                        val selected = currentCity.highLatitudeRule == rule
                        Surface(
                            onClick = { viewModel.setCityCalculationParams(highLat = rule) },
                            color = if (selected) Color(0xFF174A40) else SettingsInputBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (selected) MosqueGoldAccent else Color.Transparent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(rule.arabicName, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                if (selected) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MosqueGoldAccent)
                            }
                        }
                    }

                    Text("التوقيت الصيفي / الشتوي:", color = Color(0xFFE0ECEA), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    DstMode.entries.forEach { dst ->
                        val selected = currentCity.dstMode == dst
                        Surface(
                            onClick = { viewModel.setCityCalculationParams(dstMode = dst) },
                            color = if (selected) Color(0xFF174A40) else SettingsInputBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (selected) MosqueGoldAccent else Color.Transparent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(dst.arabicName, color = Color.White, fontSize = 13.sp)
                                if (selected) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MosqueGoldAccent)
                            }
                        }
                    }

                    StepperSettingRow(
                        label = "تعديل التاريخ الهجري (بالأيام)",
                        valueText = "${state.settings.hijriOffsetDays} يوم",
                        onDecrease = {
                            val next = (state.settings.hijriOffsetDays - 1).coerceIn(-3, 3)
                            viewModel.updateSettings { it.copy(hijriOffsetDays = next) }
                        },
                        onIncrease = {
                            val next = (state.settings.hijriOffsetDays + 1).coerceIn(-3, 3)
                            viewModel.updateSettings { it.copy(hijriOffsetDays = next) }
                        }
                    )
                }
            }
        }
    }

    if (showAddCityDialog) {
        AddCustomCityDialog(
            onDismiss = { showAddCityDialog = false },
            onAdd = { cAr, cEn, cityAr, cityEn, lat, lon, tz, method ->
                selectedCountry = cAr
                viewModel.addCustomCity(cAr, cEn, cityAr, cityEn, lat, lon, tz, method)
                showAddCityDialog = false
            }
        )
    }
}

@Composable
private fun SubToggleBar(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        color = SettingsInputBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color(0xFFE0ECEA), fontSize = 13.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "إغلاق" else "فتح",
                tint = MosqueGoldAccent
            )
        }
    }
}

@Composable
fun CollapsibleAccordionSection(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    expanded: Boolean,
    onToggle: () -> Unit,
    testTag: String? = null,
    content: @Composable () -> Unit
) {
    Surface(
        color = SettingsCardDark,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (expanded) 1.3.dp else 1.dp,
            color = if (expanded) MosqueGoldAccent.copy(alpha = 0.52f) else Color.White.copy(alpha = 0.10f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .let { if (testTag != null) it.testTag(testTag) else it }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 58.dp)
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MosqueGoldAccent.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = MosqueGoldAccent, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            color = if (expanded) MosqueGoldAccent else Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                color = Color(0xFF9FB6B9),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .size(48.dp)
                        .let { if (testTag != null) it.testTag("${testTag}_arrow") else it }
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "طي القائمة" else "فتح القائمة",
                        tint = MosqueGoldAccent
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCustomCityDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, Double, Double, String, CalculationMethod) -> Unit
) {
    var countryAr by remember { mutableStateOf("") }
    var countryEn by remember { mutableStateOf("") }
    var cityAr by remember { mutableStateOf("") }
    var cityEn by remember { mutableStateOf("") }
    var latStr by remember { mutableStateOf("51.3127") }
    var lonStr by remember { mutableStateOf("9.4797") }
    var tzId by remember { mutableStateOf(TimeZone.getDefault().id) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SettingsCardDark,
        titleContentColor = MosqueGoldAccent,
        textContentColor = Color.White,
        title = { Text("إضافة مدينة جديدة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = countryAr, onValueChange = { countryAr = it },
                    label = { Text("اسم الدولة بالعربية") }, singleLine = true, colors = readableTextFieldColors()
                )
                OutlinedTextField(
                    value = cityAr, onValueChange = { cityAr = it },
                    label = { Text("اسم المدينة بالعربية") }, singleLine = true, colors = readableTextFieldColors()
                )
                OutlinedTextField(
                    value = cityEn, onValueChange = { cityEn = it },
                    label = { Text("اسم المدينة بالإنجليزية (اختياري)") }, singleLine = true, colors = readableTextFieldColors()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latStr, onValueChange = { latStr = it },
                        label = { Text("خط العرض") }, singleLine = true, colors = readableTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lonStr, onValueChange = { lonStr = it },
                        label = { Text("خط الطول") }, singleLine = true, colors = readableTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = tzId, onValueChange = { tzId = it },
                    label = { Text("المنطقة الزمنية (IANA مثلاً Europe/Berlin)") }, singleLine = true, colors = readableTextFieldColors()
                )
                if (errorText != null) {
                    Text(errorText!!, color = Color(0xFFFF8A80), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latStr.toDoubleOrNull()
                    val lon = lonStr.toDoubleOrNull()
                    if (countryAr.isBlank() || cityAr.isBlank() || lat == null || lon == null) {
                        errorText = "يرجى إدخال اسم الدولة والمدينة وإحداثيات رقمية صحيحة"
                    } else {
                        onAdd(countryAr, countryEn, cityAr, cityEn, lat, lon, tzId, CalculationMethod.MWL)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldAccent)
            ) {
                Text("حفظ وإضافة", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = Color.White) }
        }
    )
}

@Composable
fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        color = SettingsCardDark,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = MosqueGoldAccent,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun StepperSettingRow(
    label: String,
    valueText: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDecrease,
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text("-", color = Color.White, fontSize = 20.sp) }
            Text(
                text = valueText,
                color = MosqueGoldAccent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                onClick = onIncrease,
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text("+", color = Color.White, fontSize = 20.sp) }
        }
    }
}

@Composable
fun readableTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = SettingsInputBg,
    unfocusedContainerColor = SettingsInputBg,
    focusedBorderColor = MosqueGoldAccent,
    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
    focusedLabelColor = MosqueGoldAccent,
    unfocusedLabelColor = Color(0xFFB0C4C7),
    cursorColor = MosqueGoldAccent
)

@Composable
fun chipColors() = FilterChipDefaults.filterChipColors(
    containerColor = SettingsInputBg,
    labelColor = Color.White,
    selectedContainerColor = MosqueGoldAccent,
    selectedLabelColor = Color.Black
)
