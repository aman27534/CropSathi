package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Simulated Data Model
data class PricePoint(val day: String, val price: Float)

class MarketTrendsViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedCrop = MutableStateFlow("Wheat")
    val selectedCrop: StateFlow<String> = _selectedCrop.asStateFlow()

    private val _priceData = MutableStateFlow<List<PricePoint>>(emptyList())
    val priceData: StateFlow<List<PricePoint>> = _priceData.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError.asStateFlow()

    val supportedCrops = listOf("Wheat", "Rice", "Cotton", "Soybean")

    init {
        fetchPriceData("Wheat")
    }

    fun selectCrop(crop: String) {
        _selectedCrop.value = crop
        fetchPriceData(crop)
    }

    private fun fetchPriceData(crop: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _isError.value = false
            // Simulate network delay for API call
            delay(1200)

            // We assume the API call fails or we just return the mock fallback data
            try {
                // throw Exception("Simulated API Error") // Simulated failure
                
                // Fallback Mock Data based on crop type
                val basePrice = when (crop) {
                    "Wheat" -> 2200f
                    "Rice" -> 3500f
                    "Cotton" -> 7200f
                    "Soybean" -> 4500f
                    else -> 2000f
                }
                
                val mockData = listOf(
                    PricePoint("Mon", basePrice + (-50..50).random()),
                    PricePoint("Tue", basePrice + (-50..100).random()),
                    PricePoint("Wed", basePrice + (-100..150).random()),
                    PricePoint("Thu", basePrice + (-50..200).random()),
                    PricePoint("Fri", basePrice + (0..150).random()),
                    PricePoint("Sat", basePrice + (50..300).random()),
                    PricePoint("Sun", basePrice + (100..400).random())
                )
                
                _priceData.value = mockData
            } catch (e: Exception) {
                _isError.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketTrendsChart(viewModel: MarketTrendsViewModel = viewModel()) {
    val isLoading by viewModel.isLoading.collectAsState()
    val priceData by viewModel.priceData.collectAsState()
    val selectedCrop by viewModel.selectedCrop.collectAsState()
    val isError by viewModel.isError.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            
            // Header Row
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.TrendingUp, contentDescription = "Market Trends", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Weekly Market Prices",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Dropdown for Crop Selection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.menuAnchor()
                    ) {
                        Text(
                            text = selectedCrop,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        viewModel.supportedCrops.forEach { crop ->
                            DropdownMenuItem(
                                text = { Text(crop) },
                                onClick = {
                                    viewModel.selectCrop(crop)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (isError) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Failed to load market data.", color = MaterialTheme.colorScheme.error)
                }
            } else if (priceData.isNotEmpty()) {
                val entries = priceData.mapIndexed { index, point ->
                    FloatEntry(x = index.toFloat(), y = point.price)
                }
                
                val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                    priceData.getOrNull(value.toInt())?.day ?: ""
                }
                
                val startAxisValueFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
                    "₹${value.toInt()}"
                }
                
                Chart(
                    chart = lineChart(),
                    model = entryModelOf(entries),
                    startAxis = rememberStartAxis(valueFormatter = startAxisValueFormatter),
                    bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter),
                    modifier = Modifier.height(200.dp).fillMaxWidth()
                )
            }
        }
    }
}
