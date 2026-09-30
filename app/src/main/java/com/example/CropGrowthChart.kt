package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CropGrowthChart(historyItems: List<IrrigationHistory>) {
    // 1. Filter last 30 days and sort by timestamp ascending
    val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
    val cutoffTime = System.currentTimeMillis() - thirtyDaysInMillis
    
    val recentItems = historyItems
        .filter { it.timestamp >= cutoffTime }
        .sortedBy { it.timestamp }
        
    if (recentItems.isEmpty()) return
    
    // 2. Map stages to numeric values
    val stageMap = mapOf(
        "Initial" to 1f,
        "Crop Development" to 2f,
        "Mid-Season" to 3f,
        "Late Season" to 4f
    )
    
    val entries = recentItems.mapIndexed { index, item ->
        val yValue = stageMap[item.cropStage] ?: 1f
        FloatEntry(x = index.toFloat(), y = yValue)
    }
    
    // Axis formatters
    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val item = recentItems.getOrNull(value.toInt())
        if (item != null) {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(Date(item.timestamp))
        } else {
            ""
        }
    }
    
    val startAxisValueFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        when(value.toInt()) {
            1 -> "Init"
            2 -> "Dev"
            3 -> "Mid"
            4 -> "Late"
            else -> ""
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Crop Growth Trend (30 Days)", 
                style = MaterialTheme.typography.titleMedium, 
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Chart(
                chart = lineChart(),
                model = entryModelOf(entries),
                startAxis = rememberStartAxis(valueFormatter = startAxisValueFormatter, labelRotationDegrees = 0f),
                bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter, labelRotationDegrees = 45f),
                modifier = Modifier.height(200.dp)
            )
        }
    }
}
