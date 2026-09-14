@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun IrrigateScreen(viewModel: IrrigationViewModel) {
    val scrollState = androidx.compose.foundation.rememberScrollState()
    var growthStage by remember { mutableStateOf("Mid-Season") }
    var soilTexture by remember { mutableStateOf("Loam") }
    var temperature by remember { mutableStateOf(28f) }
    var currentMoisture by remember { mutableStateOf(45f) }
    
    val growthStages = listOf("Initial", "Crop Development", "Mid-Season", "Late Season")
    val soilTextures = listOf("Sand", "Loam", "Clay")
    
    var expandedDropdown by remember { mutableStateOf(false) }

    // Calculation Logic
    val et0 = (temperature * 0.15f) + 1.5f
    val kc = when (growthStage) {
        "Initial" -> 0.4f
        "Crop Development" -> 0.8f
        "Mid-Season" -> 1.15f
        "Late Season" -> 0.7f
        else -> 1.0f
    }
    val soilFactor = when (soilTexture) {
        "Sand" -> 1.2f
        "Clay" -> 0.8f
        else -> 1.0f // Loam
    }
    val moistureDeficit = (100f - currentMoisture) / 100f
    
    // Calculate Liters per hectare (L/ha) - 1 mm over 1 ha = 10,000 Liters
    val areaMultiplier = 10000f 
    val requiredWaterLiters = (et0 * kc * areaMultiplier * soilFactor * moistureDeficit).coerceAtLeast(0f)
    val formattedWaterLiters = String.format(java.util.Locale.US, "%.2f", requiredWaterLiters)
    val formattedTemp = String.format(java.util.Locale.US, "%.1f", temperature)
    val formattedMoisture = String.format(java.util.Locale.US, "%.1f", currentMoisture)
    val formattedDeficit = String.format(java.util.Locale.US, "%.1f", 100f - currentMoisture)

    val historyItems by viewModel.uiState.androidx.lifecycle.compose.collectAsStateWithLifecycle()

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
            Text("Water Requirement Calculator", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("Calculation Parameters", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                }

                // Dropdown for Growth Stage
                Column {
                    Text("Crop Growth Stage", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.material3.ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.OutlinedTextField(
                            value = growthStage,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = androidx.compose.material3.ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        androidx.compose.material3.ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            growthStages.forEach { stage ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(stage) },
                                    onClick = {
                                        growthStage = stage
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Radio group for Soil Texture
                Column {
                    Text("Soil Texture", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            soilTextures.forEach { texture ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { soilTexture = texture }.padding(end = 4.dp)) {
                                    androidx.compose.material3.RadioButton(
                                        selected = soilTexture == texture,
                                        onClick = { soilTexture = texture }
                                    )
                                    Text(texture, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                // Temperature Slider
                Column {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Current Temperature", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${formattedTemp}°C", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    androidx.compose.material3.Slider(
                        value = temperature,
                        onValueChange = { temperature = it },
                        valueRange = 10f..50f,
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
                        Text("${formattedMoisture}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                    }
                    androidx.compose.material3.Slider(
                        value = currentMoisture,
                        onValueChange = { currentMoisture = it },
                        valueRange = 0f..100f,
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
                Text("RECOMMENDED DAILY VOLUME", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(formattedWaterLiters, style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(" Liters", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(bottom = 8.dp))
                }
                Text("per hectare", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Based on $growthStage (Kc=${kc}), $soilTexture factor, ${formattedDeficit}% deficit.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.saveHistory(
                            cropStage = growthStage,
                            soilTexture = soilTexture,
                            temperature = temperature,
                            currentMoisture = currentMoisture,
                            recommendedVolumeLiters = requiredWaterLiters
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save to History")
                }
            }
        }

        // History Section
        if (historyItems.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Irrigation History", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    
                    historyItems.forEach { item ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val date = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(item.timestamp))
                                    Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${String.format(java.util.Locale.US, "%.2f", item.recommendedVolumeLiters)} L/ha", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    Text("${item.cropStage} | ${item.soilTexture} | ${String.format(java.util.Locale.US, "%.1f", item.temperature)}°C", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { viewModel.deleteHistory(item.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
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
