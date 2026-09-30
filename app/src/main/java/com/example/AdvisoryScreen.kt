package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Hardcoded advisory data — works fully offline, no API key needed ─────────
// Season is determined from current month. Data is specific to Madhya Pradesh.

private data class SeasonalAdvisory(
    val season: String,
    val months: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val colour: Color,
    val weatherSummary: String,
    val primaryCrops: List<String>,
    val ndviStatus: String,
    val ndviValue: Float,
    val tips: List<String>,
    val warningIfAny: String?
)

private val ADVISORIES = mapOf(
    "Kharif" to SeasonalAdvisory(
        season = "Kharif (Rainy Season)",
        months = "June – October",
        icon = Icons.Filled.WaterDrop,
        colour = Color(0xFF1565C0),
        weatherSummary = "High rainfall expected (800–1200 mm). Humidity 75–90%. Temperature 28–35°C. Possible waterlogging in low-lying fields.",
        primaryCrops = listOf("Soybean", "Maize", "Cotton", "Pigeonpea", "Paddy"),
        ndviStatus = "Actively growing",
        ndviValue = 0.68f,
        tips = listOf(
            "Apply Phosphorus fertiliser before sowing — it doesn't move in soil.",
            "Maintain 45 cm row spacing for Soybean to improve air circulation and reduce Leaf Mold risk.",
            "Scout for Stem Fly in Soybean between 10–20 DAS. Spray Thiamethoxam 25 WG if 5% incidence.",
            "Avoid nitrogen top-dressing on waterlogged fields — converts to harmful nitrous oxide.",
            "Check bunds for seepage. Raise crop beds if daily rainfall exceeds 80 mm."
        ),
        warningIfAny = "La Niña conditions increase rainfall probability by 20% this year. Keep drainage channels clear."
    ),
    "Rabi" to SeasonalAdvisory(
        season = "Rabi (Winter Season)",
        months = "November – March",
        icon = Icons.Filled.AcUnit,
        colour = Color(0xFF00695C),
        weatherSummary = "Cool dry weather. Temperature 10–25°C. Frost possible Dec–Jan in Malwa plateau. Minimal rainfall (50–100 mm).",
        primaryCrops = listOf("Wheat", "Chickpea", "Mustard", "Lentil", "Potato"),
        ndviStatus = "Establishing / tillering",
        ndviValue = 0.45f,
        tips = listOf(
            "Apply urea in split doses (50% basal, 50% at crown root initiation) for Wheat.",
            "Chickpea requires only 1 irrigation at flower initiation — excess water causes root rot.",
            "Protect Mustard from Aphids: apply Imidacloprid 17.8 SL @ 0.5 ml/L if > 20 aphids/plant.",
            "Irrigate Wheat every 21 days in sandy soils, every 28 days in clay soils.",
            "Late sowing (after Nov 25) reduces Wheat yield 30–40 kg/ha per day — sow on time."
        ),
        warningIfAny = "Cold wave forecast for December in northern MP. Cover nursery beds with polythene sheets."
    ),
    "Zaid" to SeasonalAdvisory(
        season = "Zaid (Summer Season)",
        months = "April – May",
        icon = Icons.Filled.WbSunny,
        colour = Color(0xFFE65100),
        weatherSummary = "Hot dry summer. Temperature 38–48°C. Extreme heat stress on crops. Low humidity. High evapotranspiration.",
        primaryCrops = listOf("Watermelon", "Muskmelon", "Bitter Gourd", "Moong", "Cucumber"),
        ndviStatus = "Stress / fallow",
        ndviValue = 0.25f,
        tips = listOf(
            "Irrigate vegetable crops daily in the morning (before 8 AM) to reduce evaporation losses.",
            "Apply 2% KNO3 foliar spray to reduce heat stress on fruit crops.",
            "Grow Green Manure (Dhaincha) and plough in before Kharif for organic matter enrichment.",
            "Mulch with paddy straw (5 cm layer) to keep soil temperature 8–10°C below ambient.",
            "Install shade nets (35–50% shade) for nursery beds and vegetable transplants."
        ),
        warningIfAny = "Heat wave advisory active for Malwa region. Avoid field work between 11 AM – 4 PM."
    )
)

private fun currentSeasonKey(): String {
    val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
    return when (month) {
        in 6..10 -> "Kharif"
        in 11..12, in 1..3 -> "Rabi"
        else -> "Zaid"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvisoryScreen() {
    val scrollState = rememberScrollState()

    var selectedSeason by remember { mutableStateOf(currentSeasonKey()) }
    val advisory = ADVISORIES[selectedSeason]!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Agriculture, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("CROP ADVISORY", style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp)
        }
        Text("Seasonal Field Guide", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Madhya Pradesh · ${advisory.months}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        // ── Season tabs ───────────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Kharif", "Rabi", "Zaid").forEach { season ->
                FilterChip(
                    selected = selectedSeason == season,
                    onClick = { selectedSeason = season },
                    label = { Text(season) },
                    leadingIcon = if (selectedSeason == season) {
                        { Icon(Icons.Filled.Check, contentDescription = null,
                            modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        // ── Warning banner ────────────────────────────────────────────────────
        advisory.warningIfAny?.let { warning ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFEBEE)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null,
                        tint = Color(0xFFB71C1C), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(warning, style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB71C1C), lineHeight = 20.sp)
                }
            }
        }

        // ── Weather summary card ──────────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = advisory.colour.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(10.dp),
                        color = advisory.colour.copy(alpha = 0.15f)) {
                        Icon(advisory.icon, contentDescription = null,
                            tint = advisory.colour,
                            modifier = Modifier.padding(8.dp).size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(advisory.season, style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = advisory.colour)
                        Text("Weather Outlook", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(advisory.weatherSummary, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground, lineHeight = 22.sp)
            }
        }

        // ── NDVI / Crop Health Card ───────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Satellite, contentDescription = null,
                        tint = Color(0xFF2E7D32))
                    Spacer(Modifier.width(8.dp))
                    Text("NDVI Satellite Index",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE8F5E9)) {
                        Text(advisory.ndviStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "%.2f".format(advisory.ndviValue),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            advisory.ndviValue > 0.5f -> Color(0xFF2E7D32)
                            advisory.ndviValue > 0.3f -> Color(0xFFE65100)
                            else -> Color(0xFFB71C1C)
                        }
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vegetation Health", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { advisory.ndviValue },
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when {
                                advisory.ndviValue > 0.5f -> Color(0xFF2E7D32)
                                advisory.ndviValue > 0.3f -> Color(0xFFE65100)
                                else -> Color(0xFFB71C1C)
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text("Scale: 0.0 (bare soil) → 1.0 (dense canopy)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }

        // ── Recommended crops ─────────────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Eco, contentDescription = null,
                        tint = Color(0xFF558B2F))
                    Spacer(Modifier.width(8.dp))
                    Text("Recommended Crops",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    advisory.primaryCrops.chunked(3).forEach { row ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { crop ->
                                Surface(shape = RoundedCornerShape(8.dp),
                                    color = advisory.colour.copy(alpha = 0.1f)) {
                                    Text(crop, style = MaterialTheme.typography.labelMedium,
                                        color = advisory.colour,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Field tips ────────────────────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lightbulb, contentDescription = null,
                        tint = Color(0xFFF9A825))
                    Spacer(Modifier.width(8.dp))
                    Text("Field Advisory Tips",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                }
                advisory.tips.forEachIndexed { i, tip ->
                    Row(verticalAlignment = Alignment.Top) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = advisory.colour.copy(alpha = 0.15f),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${i + 1}", style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold, color = advisory.colour)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(tip, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 20.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // ── KVK Hotline ───────────────────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = advisory.colour,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Phone, contentDescription = null, tint = Color.White,
                    modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Kisan Call Centre", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Text("1800-180-1551  ·  Free · 24/7 · Hindi/English",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f))
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
