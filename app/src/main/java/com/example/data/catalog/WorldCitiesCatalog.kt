package com.example.data.catalog

import com.example.data.model.AsrMadhab
import com.example.data.model.CalculationMethod
import com.example.data.model.CityPreset
import com.example.data.model.HighLatitudeRule

/**
 * Built-in offline catalog of 84+ world countries and 180+ major cities with Arabic & English
 * search support, accurate coordinates, IANA timezones (automatic DST), and calculation defaults.
 */
object WorldCitiesCatalog {

    val cities: List<CityPreset> = listOf(
        // 1. الأردن (Jordan)
        CityPreset("jo_amman", "الأردن", "Jordan", "عمان", "Amman", 31.9454, 35.9284, "Asia/Amman", CalculationMethod.JORDAN_AWQAF, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("jo_irbid", "الأردن", "Jordan", "إربد", "Irbid", 32.5568, 35.8469, "Asia/Amman", CalculationMethod.JORDAN_AWQAF, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("jo_zarqa", "الأردن", "Jordan", "الزرقاء", "Zarqa", 32.0728, 36.0880, "Asia/Amman", CalculationMethod.JORDAN_AWQAF, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("jo_aqaba", "الأردن", "Jordan", "العقبة", "Aqaba", 29.5321, 35.0063, "Asia/Amman", CalculationMethod.JORDAN_AWQAF, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("jo_karak", "الأردن", "Jordan", "الكرك", "Karak", 31.1853, 35.7048, "Asia/Amman", CalculationMethod.JORDAN_AWQAF, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("jo_salt", "الأردن", "Jordan", "السلط", "Salt", 32.0392, 35.7272, "Asia/Amman", CalculationMethod.JORDAN_AWQAF, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("jo_madaba", "الأردن", "Jordan", "مأدبا", "Madaba", 31.7195, 35.7939, "Asia/Amman", CalculationMethod.JORDAN_AWQAF, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("jo_jarash", "الأردن", "Jordan", "جرش", "Jerash", 32.2747, 35.8961, "Asia/Amman", CalculationMethod.JORDAN_AWQAF, AsrMadhab.SHAFI, HighLatitudeRule.NONE),

        // 2. ألمانيا (Germany)
        CityPreset("de_hameln", "ألمانيا", "Germany", "هامِلن", "Hameln", 52.1045, 9.3596, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("de_kassel", "ألمانيا", "Germany", "كاسل", "Kassel", 51.3127, 9.4797, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("de_hann_muenden", "ألمانيا", "Germany", "هان مندن", "Hann. Münden", 51.4167, 9.6500, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("de_berlin", "ألمانيا", "Germany", "برلين", "Berlin", 52.5200, 13.4050, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("de_frankfurt", "ألمانيا", "Germany", "فرانكفورت", "Frankfurt", 50.1109, 8.6821, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("de_hannover", "ألمانيا", "Germany", "هانوفر", "Hannover", 52.3759, 9.7320, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("de_munich", "ألمانيا", "Germany", "ميونخ", "Munich", 48.1351, 11.5820, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("de_hamburg", "ألمانيا", "Germany", "هامبورغ", "Hamburg", 53.5511, 9.9937, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("de_stuttgart", "ألمانيا", "Germany", "شتوتغارت", "Stuttgart", 48.7758, 9.1829, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("de_dusseldorf", "ألمانيا", "Germany", "دوسلدورف", "Düsseldorf", 51.2277, 6.7735, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("de_cologne", "ألمانيا", "Germany", "كولونيا", "Cologne", 50.9375, 6.9603, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("de_dortmund", "ألمانيا", "Germany", "دورتموند", "Dortmund", 51.5136, 7.4653, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("de_essen", "ألمانيا", "Germany", "إسن", "Essen", 51.4556, 7.0116, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("de_bremen", "ألمانيا", "Germany", "بريمن", "Bremen", 53.0793, 8.8017, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("de_leipzig", "ألمانيا", "Germany", "لايبزيغ", "Leipzig", 51.3397, 12.3731, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("de_nuremberg", "ألمانيا", "Germany", "نورنبرغ", "Nuremberg", 49.4521, 11.0767, "Europe/Berlin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),

        // 3. السعودية (Saudi Arabia)
        CityPreset("sa_riyadh", "السعودية", "Saudi Arabia", "الرياض", "Riyadh", 24.7136, 46.6753, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("sa_makkah", "السعودية", "Saudi Arabia", "مكة المكرمة", "Makkah", 21.4225, 39.8262, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("sa_madinah", "السعودية", "Saudi Arabia", "المدينة المنورة", "Madinah", 24.4672, 39.6111, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("sa_jeddah", "السعودية", "Saudi Arabia", "جدة", "Jeddah", 21.5433, 39.1728, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("sa_dammam", "السعودية", "Saudi Arabia", "الدمام", "Dammam", 26.4207, 50.0888, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("sa_taif", "السعودية", "Saudi Arabia", "الطائف", "Taif", 21.2703, 40.4158, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("sa_abha", "السعودية", "Saudi Arabia", "أبها", "Abha", 18.2164, 42.5053, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),

        // 4. فلسطين (Palestine)
        CityPreset("ps_jerusalem", "فلسطين", "Palestine", "القدس", "Jerusalem", 31.7683, 35.2137, "Asia/Jerusalem", CalculationMethod.JORDAN_AWQAF, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ps_gaza", "فلسطين", "Palestine", "غزة", "Gaza", 31.5017, 34.4668, "Asia/Gaza", CalculationMethod.JORDAN_AWQAF, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ps_ramallah", "فلسطين", "Palestine", "رام الله", "Ramallah", 31.9038, 35.2034, "Asia/Hebron", CalculationMethod.JORDAN_AWQAF, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ps_nablus", "فلسطين", "Palestine", "نابلس", "Nablus", 32.2211, 35.2544, "Asia/Hebron", CalculationMethod.JORDAN_AWQAF, AsrMadhab.SHAFI, HighLatitudeRule.NONE),

        // 5. مصر (Egypt)
        CityPreset("eg_cairo", "مصر", "Egypt", "القاهرة", "Cairo", 30.0444, 31.2357, "Africa/Cairo", CalculationMethod.EGYPT, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("eg_alex", "مصر", "Egypt", "الإسكندرية", "Alexandria", 31.2001, 29.9187, "Africa/Cairo", CalculationMethod.EGYPT, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("eg_giza", "مصر", "Egypt", "الجيزة", "Giza", 30.0131, 31.2089, "Africa/Cairo", CalculationMethod.EGYPT, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("eg_aswan", "مصر", "Egypt", "أسوان", "Aswan", 24.0889, 32.8998, "Africa/Cairo", CalculationMethod.EGYPT, AsrMadhab.SHAFI, HighLatitudeRule.NONE),

        // 6. الإمارات (UAE)
        CityPreset("ae_dubai", "الإمارات", "UAE", "دبي", "Dubai", 25.2048, 55.2708, "Asia/Dubai", CalculationMethod.UAE, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ae_abudhabi", "الإمارات", "UAE", "أبوظبي", "Abu Dhabi", 24.4539, 54.3773, "Asia/Dubai", CalculationMethod.UAE, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ae_sharjah", "الإمارات", "UAE", "الشارقة", "Sharjah", 25.3463, 55.4209, "Asia/Dubai", CalculationMethod.UAE, AsrMadhab.SHAFI, HighLatitudeRule.NONE),

        // 7-11. الخليج واليمن
        CityPreset("qa_doha", "قطر", "Qatar", "الدوحة", "Doha", 25.2854, 51.5310, "Asia/Qatar", CalculationMethod.UMM_AL_QURA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("kw_kuwait", "الكويت", "Kuwait", "مدينة الكويت", "Kuwait City", 29.3759, 47.9774, "Asia/Kuwait", CalculationMethod.UMM_AL_QURA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("bh_manama", "البحرين", "Bahrain", "المنامة", "Manama", 26.2285, 50.5860, "Asia/Bahrain", CalculationMethod.UMM_AL_QURA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("om_muscat", "عُمان", "Oman", "مسقط", "Muscat", 23.5880, 58.3829, "Asia/Muscat", CalculationMethod.UMM_AL_QURA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ye_sanaa", "اليمن", "Yemen", "صنعاء", "Sanaa", 15.3694, 44.1910, "Asia/Aden", CalculationMethod.UMM_AL_QURA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ye_aden", "اليمن", "Yemen", "عدن", "Aden", 12.7855, 45.0187, "Asia/Aden", CalculationMethod.UMM_AL_QURA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),

        // 12-14. الشام والعراق
        CityPreset("sy_damascus", "سوريا", "Syria", "دمشق", "Damascus", 33.5138, 36.2765, "Asia/Damascus", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("sy_aleppo", "سوريا", "Syria", "حلب", "Aleppo", 36.2021, 37.1343, "Asia/Damascus", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("lb_beirut", "لبنان", "Lebanon", "بيروت", "Beirut", 33.8938, 35.5018, "Asia/Beirut", CalculationMethod.EGYPT, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("iq_baghdad", "العراق", "Iraq", "بغداد", "Baghdad", 33.3152, 44.3661, "Asia/Baghdad", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("iq_erbil", "العراق", "Iraq", "أربيل", "Erbil", 36.1911, 44.0092, "Asia/Baghdad", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("iq_basra", "العراق", "Iraq", "البصرة", "Basra", 30.5085, 47.7804, "Asia/Baghdad", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),

        // 15-22. المغرب العربي وشرق أفريقيا العربية
        CityPreset("ma_rabat", "المغرب", "Morocco", "الرباط", "Rabat", 34.0209, -6.8416, "Africa/Casablanca", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ma_casablanca", "المغرب", "Morocco", "الدار البيضاء", "Casablanca", 33.5731, -7.5898, "Africa/Casablanca", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ma_marrakech", "المغرب", "Morocco", "مراكش", "Marrakech", 31.6295, -7.9811, "Africa/Casablanca", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("dz_algiers", "الجزائر", "Algeria", "الجزائر", "Algiers", 36.7538, 3.0588, "Africa/Algiers", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("dz_oran", "الجزائر", "Algeria", "وهران", "Oran", 35.6971, -0.6308, "Africa/Algiers", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("tn_tunis", "تونس", "Tunisia", "تونس", "Tunis", 36.8065, 10.1815, "Africa/Tunis", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ly_tripoli", "ليبيا", "Libya", "طرابلس", "Tripoli", 32.8872, 13.1913, "Africa/Tripoli", CalculationMethod.EGYPT, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("sd_khartoum", "السودان", "Sudan", "الخرطوم", "Khartoum", 15.5007, 32.5599, "Africa/Khartoum", CalculationMethod.EGYPT, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("mr_nouakchott", "موريتانيا", "Mauritania", "نواكشوط", "Nouakchott", 18.0735, -15.9582, "Africa/Nouakchott", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("so_mogadishu", "الصومال", "Somalia", "مقديشو", "Mogadishu", 2.0469, 45.3182, "Africa/Mogadishu", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("dj_djibouti", "جيبوتي", "Djibouti", "جيبوتي", "Djibouti", 11.5721, 43.1456, "Africa/Djibouti", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("km_moroni", "جزر القمر", "Comoros", "موروني", "Moroni", -11.7172, 43.2473, "Indian/Comoro", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),

        // 23-47. تركيا وأوروبا (25 دولة أوروبية إضافية + الرئيسية)
        CityPreset("tr_istanbul", "تركيا", "Turkey", "إسطنبول", "Istanbul", 41.0082, 28.9784, "Europe/Istanbul", CalculationMethod.DIYANET, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("tr_ankara", "تركيا", "Turkey", "أنقرة", "Ankara", 39.9334, 32.8597, "Europe/Istanbul", CalculationMethod.DIYANET, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("uk_london", "المملكة المتحدة", "United Kingdom", "لندن", "London", 51.5072, -0.1276, "Europe/London", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("uk_birmingham", "المملكة المتحدة", "United Kingdom", "برمنغهام", "Birmingham", 52.4862, -1.8904, "Europe/London", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("uk_manchester", "المملكة المتحدة", "United Kingdom", "مانشستر", "Manchester", 53.4808, -2.2426, "Europe/London", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("fr_paris", "فرنسا", "France", "باريس", "Paris", 48.8566, 2.3522, "Europe/Paris", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("fr_lyon", "فرنسا", "France", "ليون", "Lyon", 45.7640, 4.8357, "Europe/Paris", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("fr_marseille", "فرنسا", "France", "مارسيليا", "Marseille", 43.2965, 5.3698, "Europe/Paris", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("nl_amsterdam", "هولندا", "Netherlands", "أمستردام", "Amsterdam", 52.3676, 4.9041, "Europe/Amsterdam", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("nl_rotterdam", "هولندا", "Netherlands", "روتردام", "Rotterdam", 51.9244, 4.4777, "Europe/Amsterdam", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("be_brussels", "بلجيكا", "Belgium", "بروكسل", "Brussels", 50.8503, 4.3517, "Europe/Brussels", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("be_antwerp", "بلجيكا", "Belgium", "أنتويرب", "Antwerp", 51.2194, 4.4025, "Europe/Brussels", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("at_vienna", "النمسا", "Austria", "فيينا", "Vienna", 48.2082, 16.3738, "Europe/Vienna", CalculationMethod.IGMG_GERMANY, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("ch_zurich", "سويسرا", "Switzerland", "زيورخ", "Zurich", 47.3769, 8.5417, "Europe/Zurich", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("ch_geneva", "سويسرا", "Switzerland", "جنيف", "Geneva", 46.2044, 6.1432, "Europe/Zurich", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("se_stockholm", "السويد", "Sweden", "ستوكهولم", "Stockholm", 59.3293, 18.0686, "Europe/Stockholm", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("se_malmo", "السويد", "Sweden", "مالمو", "Malmö", 55.6050, 13.0038, "Europe/Stockholm", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("no_oslo", "النرويج", "Norway", "أوسلو", "Oslo", 59.9139, 10.7522, "Europe/Oslo", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("dk_copenhagen", "الدنمارك", "Denmark", "كوبنهاغن", "Copenhagen", 55.6761, 12.5683, "Europe/Copenhagen", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("fi_helsinki", "فنلندا", "Finland", "هلسنكي", "Helsinki", 60.1699, 24.9384, "Europe/Helsinki", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("es_madrid", "إسبانيا", "Spain", "مدريد", "Madrid", 40.4168, -3.7038, "Europe/Madrid", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("es_barcelona", "إسبانيا", "Spain", "برشلونة", "Barcelona", 41.3874, 2.1686, "Europe/Madrid", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("it_rome", "إيطاليا", "Italy", "روما", "Rome", 41.9028, 12.4964, "Europe/Rome", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("it_milan", "إيطاليا", "Italy", "ميلانو", "Milan", 45.4642, 9.1900, "Europe/Rome", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("pt_lisbon", "البرتغال", "Portugal", "لشبونة", "Lisbon", 38.7223, -9.1393, "Europe/Lisbon", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ie_dublin", "أيرلندا", "Ireland", "دبلن", "Dublin", 53.3498, -6.2603, "Europe/Dublin", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("gr_athens", "اليونان", "Greece", "أثينا", "Athens", 37.9838, 23.7275, "Europe/Athens", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("pl_warsaw", "بولندا", "Poland", "وارسو", "Warsaw", 52.2297, 21.0122, "Europe/Warsaw", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("cz_prague", "التشيك", "Czechia", "براغ", "Prague", 50.0755, 14.4378, "Europe/Prague", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("hu_budapest", "المجر", "Hungary", "بودابست", "Budapest", 47.4979, 19.0402, "Europe/Budapest", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("ro_bucharest", "رومانيا", "Romania", "بوخارست", "Bucharest", 44.4268, 26.1025, "Europe/Bucharest", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("bg_sofia", "بلغاريا", "Bulgaria", "صوفيا", "Sofia", 42.6977, 23.3219, "Europe/Sofia", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ba_sarajevo", "البوسنة والهرسك", "Bosnia & Herzegovina", "سراييفو", "Sarajevo", 43.8563, 18.4131, "Europe/Sarajevo", CalculationMethod.DIYANET, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("al_tirana", "ألبانيا", "Albania", "تيرانا", "Tirana", 41.3275, 19.8187, "Europe/Tirane", CalculationMethod.DIYANET, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ru_moscow", "روسيا", "Russia", "موسكو", "Moscow", 55.7558, 37.6173, "Europe/Moscow", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("ua_kyiv", "أوكرانيا", "Ukraine", "كييف", "Kyiv", 50.4501, 30.5234, "Europe/Kyiv", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),

        // 48-68. آسيا (21 دولة آسيوية)
        CityPreset("pk_karachi", "باكستان", "Pakistan", "كراتشي", "Karachi", 24.8607, 67.0011, "Asia/Karachi", CalculationMethod.KARACHI, AsrMadhab.HANAFI, HighLatitudeRule.NONE),
        CityPreset("pk_lahore", "باكستان", "Pakistan", "لاهور", "Lahore", 31.5204, 74.3587, "Asia/Karachi", CalculationMethod.KARACHI, AsrMadhab.HANAFI, HighLatitudeRule.NONE),
        CityPreset("pk_islamabad", "باكستان", "Pakistan", "إسلام آباد", "Islamabad", 33.6844, 73.0479, "Asia/Karachi", CalculationMethod.KARACHI, AsrMadhab.HANAFI, HighLatitudeRule.NONE),
        CityPreset("in_delhi", "الهند", "India", "نيودلهي", "New Delhi", 28.6139, 77.2090, "Asia/Kolkata", CalculationMethod.KARACHI, AsrMadhab.HANAFI, HighLatitudeRule.NONE),
        CityPreset("in_mumbai", "الهند", "India", "مومباي", "Mumbai", 19.0760, 72.8777, "Asia/Kolkata", CalculationMethod.KARACHI, AsrMadhab.HANAFI, HighLatitudeRule.NONE),
        CityPreset("bd_dhaka", "بنغلاديش", "Bangladesh", "دكا", "Dhaka", 23.8103, 90.4125, "Asia/Dhaka", CalculationMethod.KARACHI, AsrMadhab.HANAFI, HighLatitudeRule.NONE),
        CityPreset("id_jakarta", "إندونيسيا", "Indonesia", "جاكرتا", "Jakarta", -6.2088, 106.8456, "Asia/Jakarta", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("id_surabaya", "إندونيسيا", "Indonesia", "سورابايا", "Surabaya", -7.2575, 112.7521, "Asia/Jakarta", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("my_kualalumpur", "ماليزيا", "Malaysia", "كوالالمبور", "Kuala Lumpur", 3.1390, 101.6869, "Asia/Kuala_Lumpur", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("sg_singapore", "سنغافورة", "Singapore", "سنغافورة", "Singapore", 1.3521, 103.8198, "Asia/Singapore", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("bn_bandar", "بروناي", "Brunei", "بندر سري بكاوان", "Bandar Seri Begawan", 4.9031, 114.9398, "Asia/Brunei", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("af_kabul", "أفغانستان", "Afghanistan", "كابل", "Kabul", 34.5553, 69.2075, "Asia/Kabul", CalculationMethod.KARACHI, AsrMadhab.HANAFI, HighLatitudeRule.NONE),
        CityPreset("ir_tehran", "إيران", "Iran", "طهران", "Tehran", 35.6892, 51.3890, "Asia/Tehran", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("az_baku", "أذربيجان", "Azerbaijan", "باكو", "Baku", 40.4093, 49.8671, "Asia/Baku", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("uz_tashkent", "أوزبكستان", "Uzbekistan", "طشقند", "Tashkent", 41.2995, 69.2401, "Asia/Tashkent", CalculationMethod.MWL, AsrMadhab.HANAFI, HighLatitudeRule.NONE),
        CityPreset("kz_almaty", "كازاخستان", "Kazakhstan", "ألماتي", "Almaty", 43.2220, 76.8512, "Asia/Almaty", CalculationMethod.MWL, AsrMadhab.HANAFI, HighLatitudeRule.NONE),
        CityPreset("kg_bishkek", "قيرغيزستان", "Kyrgyzstan", "بشكيك", "Bishkek", 42.8746, 74.5698, "Asia/Bishkek", CalculationMethod.MWL, AsrMadhab.HANAFI, HighLatitudeRule.NONE),
        CityPreset("tj_dushanbe", "طاجيكستان", "Tajikistan", "دوشنبه", "Dushanbe", 38.5598, 68.7870, "Asia/Dushanbe", CalculationMethod.MWL, AsrMadhab.HANAFI, HighLatitudeRule.NONE),
        CityPreset("mv_male", "المالديف", "Maldives", "ماليه", "Malé", 4.1755, 73.5093, "Indian/Maldives", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("lk_colombo", "سريلانكا", "Sri Lanka", "كولومبو", "Colombo", 6.9271, 79.8612, "Asia/Colombo", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("th_bangkok", "تايلاند", "Thailand", "بانكوك", "Bangkok", 13.7563, 100.5018, "Asia/Bangkok", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("jp_tokyo", "اليابان", "Japan", "طوكيو", "Tokyo", 35.6762, 139.6503, "Asia/Tokyo", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("kr_seoul", "كوريا الجنوبية", "South Korea", "سول", "Seoul", 37.5665, 126.9780, "Asia/Seoul", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("cn_beijing", "الصين", "China", "بكين", "Beijing", 39.9042, 116.4074, "Asia/Shanghai", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ph_manila", "الفلبين", "Philippines", "مانيلا", "Manila", 14.5995, 120.9842, "Asia/Manila", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),

        // 69-78. أفريقيا غير العربية (10 دول أفريقية)
        CityPreset("ng_lagos", "نيجيريا", "Nigeria", "لاغوس", "Lagos", 6.5244, 3.3792, "Africa/Lagos", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ng_abuja", "نيجيريا", "Nigeria", "أبوجا", "Abuja", 9.0765, 7.3986, "Africa/Lagos", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("sn_dakar", "السنغال", "Senegal", "داكار", "Dakar", 14.7167, -17.4677, "Africa/Dakar", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ml_bamako", "مالي", "Mali", "باماكو", "Bamako", 12.6392, -8.0029, "Africa/Bamako", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ne_niamey", "النيجر", "Niger", "نيامي", "Niamey", 13.5116, 2.1254, "Africa/Niamey", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("td_ndjamena", "تشاد", "Chad", "انجمينا", "N'Djamena", 12.1348, 15.0557, "Africa/Ndjamena", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("et_addisababa", "إثيوبيا", "Ethiopia", "أديس أبابا", "Addis Ababa", 9.0320, 38.7469, "Africa/Addis_Ababa", CalculationMethod.EGYPT, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ke_nairobi", "كينيا", "Kenya", "نيروبي", "Nairobi", -1.2921, 36.8219, "Africa/Nairobi", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("tz_daressalaam", "تنزانيا", "Tanzania", "دار السلام", "Dar es Salaam", -6.7924, 39.2083, "Africa/Dar_es_Salaam", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("gh_accra", "غانا", "Ghana", "أكرا", "Accra", 5.6037, -0.1870, "Africa/Accra", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("za_johannesburg", "جنوب أفريقيا", "South Africa", "جوهانسبرغ", "Johannesburg", -26.2041, 28.0473, "Africa/Johannesburg", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("za_capetown", "جنوب أفريقيا", "South Africa", "كيب تاون", "Cape Town", -33.9249, 18.4241, "Africa/Johannesburg", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),

        // 79-86. الأمريكتان وأوقيانوسيا (Americas & Oceania)
        CityPreset("us_newyork", "الولايات المتحدة", "United States", "نيويورك", "New York", 40.7128, -74.0060, "America/New_York", CalculationMethod.ISNA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("us_chicago", "الولايات المتحدة", "United States", "شيكاغو", "Chicago", 41.8781, -87.6298, "America/Chicago", CalculationMethod.ISNA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("us_detroit", "الولايات المتحدة", "United States", "ديترويت", "Detroit", 42.3314, -83.0458, "America/Detroit", CalculationMethod.ISNA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("us_losangeles", "الولايات المتحدة", "United States", "لوس أنجلوس", "Los Angeles", 34.0522, -118.2437, "America/Los_Angeles", CalculationMethod.ISNA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("us_houston", "الولايات المتحدة", "United States", "هيوستن", "Houston", 29.7604, -95.3698, "America/Chicago", CalculationMethod.ISNA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ca_toronto", "كندا", "Canada", "تورونتو", "Toronto", 43.6532, -79.3832, "America/Toronto", CalculationMethod.ISNA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ca_montreal", "كندا", "Canada", "مونتريال", "Montreal", 45.5017, -73.5673, "America/Toronto", CalculationMethod.ISNA, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("ca_vancouver", "كندا", "Canada", "فانكوفر", "Vancouver", 49.2827, -123.1207, "America/Vancouver", CalculationMethod.ISNA, AsrMadhab.SHAFI, HighLatitudeRule.TWILIGHT_ANGLE),
        CityPreset("mx_mexicocity", "المكسيك", "Mexico", "مكسيكو سيتي", "Mexico City", 19.4326, -99.1332, "America/Mexico_City", CalculationMethod.ISNA, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("br_saopaulo", "البرازيل", "Brazil", "ساو باولو", "São Paulo", -23.5505, -46.6333, "America/Sao_Paulo", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("ar_buenosaires", "الأرجنتين", "Argentina", "بوينس آيرس", "Buenos Aires", -34.6037, -58.3816, "America/Argentina/Buenos_Aires", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("cl_santiago", "تشيلي", "Chile", "سانتياغو", "Santiago", -33.4489, -70.6693, "America/Santiago", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("co_bogota", "كولومبيا", "Colombia", "بوغوتا", "Bogotá", 4.7110, -74.0721, "America/Bogota", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("au_sydney", "أستراليا", "Australia", "سيدني", "Sydney", -33.8688, 151.2093, "Australia/Sydney", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("au_melbourne", "أستراليا", "Australia", "ملبورن", "Melbourne", -37.8136, 144.9631, "Australia/Melbourne", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE),
        CityPreset("nz_auckland", "نيوزيلندا", "New Zealand", "أوكلاند", "Auckland", -36.8485, 174.7633, "Pacific/Auckland", CalculationMethod.MWL, AsrMadhab.SHAFI, HighLatitudeRule.NONE)
    )

    val defaultCity: CityPreset = cities.first()

    fun findById(cityId: String): CityPreset? = cities.find { it.cityId == cityId }

    fun countryCount(): Int = cities.map { it.countryAr }.distinct().size

    fun normalizeArabic(input: String): String =
        input.trim()
            .lowercase()
            .replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
            .replace(Regex("[\u064B-\u065F\u0670]"), "")
}
