
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val locations = listOf("Indore", "Rewa", "Ujjain")
    val context = LocalContext.current
    
    var isListening by remember { mutableStateOf(false) }
    var hasAudioPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Microphone access granted. Tap mic to speak.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Microphone permission required for voice search.", Toast.LENGTH_SHORT).show()
        }
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) { 
                isListening = false 
                Toast.makeText(context, "Voice recognition failed", Toast.LENGTH_SHORT).show()
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    val match = locations.find { spokenText.contains(it, ignoreCase = true) }
                    if (match != null) {
                        viewModel.selectLocation(match)
                        Toast.makeText(context, "Switched to $match", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Location not found: $spokenText", Toast.LENGTH_SHORT).show()
                    }
                }
                isListening = false
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    LaunchedEffect(speechRecognizer) {
        speechRecognizer.setRecognitionListener(recognitionListener)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dashboard", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    if (!hasAudioPermission) {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        if (isListening) {
                            speechRecognizer.stopListening()
                            isListening = false
                        } else {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            }
                            speechRecognizer.startListening(intent)
                            isListening = true
                        }
                    }
                },
                modifier = Modifier.background(
                    if (isListening) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "Voice Search",
                    tint = if (isListening) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(locations.size) { index ->
                val location = locations[index]
                FilterChip(
                    selected = location == selectedLocation,
                    onClick = { viewModel.selectLocation(location) },
                    label = { Text(location) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Crop Recommendation Card", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        DataPill("Sentinel-2 NDVI: ${uiState.ndvi}", Color(0xFF2E7D32))
                    }
                    item {
                        DataPill("IMD Weather: ${uiState.weather}", Color(0xFF1565C0))
                    }
                    item {
                        DataPill("Soil NPK: ${uiState.npk}", Color(0xFFEF6C00))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Recommended: ${uiState.recommendedCrop}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Market Insights", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { uiState.yieldForecast },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
