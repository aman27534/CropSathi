@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun IrrigateScreen() {
    val scrollState = androidx.compose.foundation.rememberScrollState()
    var cropType by remember { mutableStateOf("Wheat") }
    var soilType by remember { mutableStateOf("Loamy") }
    var temperature by remember { mutableStateOf(28f) }
    var currentMoisture by remember { mutableStateOf(45f) }
    
    val cropTypes = listOf("Wheat", "Rice", "Sugarcane", "Cotton")
    val soilTypes = listOf("Sandy", "Loamy", "Clay")
    
    var cropDropdownExpanded by remember { mutableStateOf(false) }
    var soilDropdownExpanded by remember { mutableStateOf(false) }

    // Simplified Penman-Monteith (mocked ET0)
    // ET0 ≈ (temperature * 0.15f) + 1.5f (mm/day)
    val et0 = (temperature * 0.15f) + 1.5f
    
    val cropCoefficient = when (cropType) {
        "Rice" -> 1.2f
        "Sugarcane" -> 1.05f
        "Cotton" -> 0.9f
        else -> 0.8f // Wheat
    }
    
    val soilFactor = when (soilType) {
        "Sandy" -> 1.2f
        "Clay" -> 0.8f
        else -> 1.0f // Loamy
    }
    
    // Moisture deficit (1.0 = completely dry, 0.0 = fully saturated)
    val moistureDeficit = (100f - currentMoisture) / 100f
    
    // Calculate water requirement in mL per plant (assuming 0.25 m^2 canopy area)
    // 1 mm of water over 1 m^2 = 1000 mL
    // So 1 mm over 0.25 m^2 = 250 mL
    val areaMultiplier = 250f 
    
    // mL/day = ET0 * Kc * Area * SoilFactor * MoistureDeficit
    val requiredWaterMl = (et0 * cropCoefficient * areaMultiplier * soilFactor * moistureDeficit).toInt().coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.WaterDrop,
                contentDescription = "Irrigation",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Smart Irrigation", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Text("Penman-Monteith Calculator", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Calculator Form
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Evapotranspiration (ET) Config", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                }

                // Crop Selection
                Column {
                    Text("Crop Type", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            cropTypes.forEach { type ->
                                val isSelected = cropType == type
                                Surface(
                                    modifier = Modifier.weight(1f).clickable { cropType = type },
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                ) {
                                    Text(
                                        text = type,
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Soil Selection
                Column {
                    Text("Soil Type", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            soilTypes.forEach { type ->
                                val isSelected = soilType == type
                                Surface(
                                    modifier = Modifier.weight(1f).clickable { soilType = type },
                                    color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                                ) {
                                    Text(
                                        text = type,
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Temperature Slider
                Column {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Current Temperature", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${temperature.toInt()}°C", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    androidx.compose.material3.Slider(
                        value = temperature,
                        onValueChange = { temperature = it },
                        valueRange = 10f..50f,
                        steps = 39,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }

                // Moisture Slider
                Column {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Current Soil Moisture", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${currentMoisture.toInt()}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                    }
                    androidx.compose.material3.Slider(
                        value = currentMoisture,
                        onValueChange = { currentMoisture = it },
                        valueRange = 0f..100f,
                        steps = 99,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.tertiary,
                            activeTrackColor = MaterialTheme.colorScheme.tertiary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }

        // Calculation Result
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DAILY WATER REQUIREMENT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$requiredWaterMl", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(" mL/plant", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(bottom = 8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Based on Simplified Penman-Monteith (ET0), ${cropType} coefficient (Kc), $soilType factor, and ${100 - currentMoisture.toInt()}% moisture deficit.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Pump Control
        var isPumpActive by remember { mutableStateOf(false) }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            ) {
                Column {
                    Text("Remote Pump Power", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(if (isPumpActive) "Pump is currently ON" else "Pump is currently OFF", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isPumpActive,
                    onCheckedChange = { isPumpActive = it },
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
