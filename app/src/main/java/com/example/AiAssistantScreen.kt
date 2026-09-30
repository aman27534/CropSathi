package com.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import android.speech.tts.TextToSpeech
import java.util.Locale

// ── Colour palette for diagnosis result cards ─────────────────────────────────
private val COLOUR_URGENT = Color(0xFFFFEBEE)   // Potato Late Blight etc.
private val COLOUR_WARNING = Color(0xFFFFF8E1)  // Blights, rusts
private val COLOUR_HEALTHY = Color(0xFFE8F5E9)  // Healthy crops
private val COLOUR_UNCLEAR = Color(0xFFF3F3F3)  // Below threshold

private val COLOUR_URGENT_TEXT  = Color(0xFFB71C1C)
private val COLOUR_WARNING_TEXT = Color(0xFFE65100)
private val COLOUR_HEALTHY_TEXT = Color(0xFF2E7D32)
private val COLOUR_UNCLEAR_TEXT = Color(0xFF616161)

// ── Derive card colour from label ─────────────────────────────────────────────
private fun cardColourFor(result: DiagnosisResult): Pair<Color, Color> = when {
    result.isUnclear                              -> Pair(COLOUR_UNCLEAR, COLOUR_UNCLEAR_TEXT)
    result.rawLabel.contains("Healthy")           -> Pair(COLOUR_HEALTHY, COLOUR_HEALTHY_TEXT)
    result.rawLabel.contains("Late_Blight")       -> Pair(COLOUR_URGENT, COLOUR_URGENT_TEXT)
    else                                          -> Pair(COLOUR_WARNING, COLOUR_WARNING_TEXT)
}

// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen() {
    val context      = LocalContext.current
    val prefLanguage = LanguageManager.getLanguage(context)
    val scope        = rememberCoroutineScope()
    val scrollState  = rememberScrollState()

    // ── State ──────────────────────────────────────────────────────────────────
    var selectedBitmap   by remember { mutableStateOf<Bitmap?>(null) }
    var symptoms         by remember { mutableStateOf("") }
    var isLoading        by remember { mutableStateOf(false) }

    // TFLite result (on-device inference)
    var tfliteResult     by remember { mutableStateOf<DiagnosisResult?>(null) }

    // Gemini API result (fallback / supplementary)
    var geminiResult     by remember { mutableStateOf<String?>(null) }

    // Mode: "tflite" | "gemini" (determined at runtime)
    var inferenceMode    by remember { mutableStateOf("tflite") }

    // Lazy-initialise the classifier; closed when screen leaves composition
    val classifier = remember { DiseaseClassifier(context) }
    DisposableEffect(Unit) { onDispose { classifier.close() } }

    // ── Text-To-Speech ────────────────────────────────────────────────────────
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try setting Hindi locale if available, else fallback to default
                val loc = Locale("hi", "IN")
                tts?.setLanguage(loc)
            }
        }
        tts = ttsInstance
        onDispose { ttsInstance.shutdown() }
    }

    // ── Image picker ──────────────────────────────────────────────────────────
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let {
                val stream = context.contentResolver.openInputStream(it)
                selectedBitmap = BitmapFactory.decodeStream(stream)
                // Clear previous results when new image selected
                tfliteResult = null
                geminiResult = null
            }
        }
    )

    // ── Layout ────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Header ─────────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.BugReport, contentDescription = null, tint = Color(0xFF00796B))
            Spacer(Modifier.width(8.dp))
            Text("PLANT PATHOLOGY", style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold, color = Color(0xFF00796B), letterSpacing = 1.sp)
        }
        Text("AI Disease Diagnosis", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

        // Inference mode badge
        if (!classifier.isModelAvailable) {
            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFF3E0)) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Cloud, contentDescription = null, tint = Color(0xFFE65100),
                        modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cloud mode (TFLite model not yet trained)",
                        style = MaterialTheme.typography.labelSmall, color = Color(0xFFE65100))
                }
            }
        } else {
            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE8F5E9)) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.PhoneAndroid, contentDescription = null,
                        tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("On-device inference  ·  Works offline",
                        style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                }
            }
        }

        // ── Image card ─────────────────────────────────────────────────────────
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
            shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Text("1. Upload Leaf Photo", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)

                if (selectedBitmap != null) {
                    Box {
                        Image(bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Leaf image",
                            modifier = Modifier.fillMaxWidth().height(220.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop)
                        // Overlay badge
                        Surface(shape = RoundedCornerShape(8.dp), color = Color.Black.copy(0.5f),
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                            Text("Ready for analysis", style = MaterialTheme.typography.labelSmall,
                                color = Color.White, modifier = Modifier.padding(6.dp))
                        }
                    }
                    TextButton(onClick = { selectedBitmap = null; tfliteResult = null; geminiResult = null }) {
                        Icon(Icons.Filled.Delete, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.fillMaxWidth().height(130.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp, Color(0xFF00796B).copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00796B))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null,
                                modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Select from gallery", fontWeight = FontWeight.Medium)
                            Text("JPG, PNG supported",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF00796B).copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }

        // ── Symptoms card ─────────────────────────────────────────────────────
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
            shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("2. Describe Symptoms (Optional)",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    placeholder = { Text("e.g. yellow spots, wilting, brown edges...") },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF00796B)
                    )
                )
            }
        }

        // ── Analyse button ────────────────────────────────────────────────────
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    tfliteResult = null
                    geminiResult = null

                    if (classifier.isModelAvailable && selectedBitmap != null) {
                        // ── On-device TFLite path (fast, offline) ─────────────
                        inferenceMode = "tflite"
                        val bmp = selectedBitmap!!
                        val result = withContext(Dispatchers.Default) {
                            classifier.classify(bmp)
                        }
                        tfliteResult = result

                        // Supplement with Gemini only if disease detected (not unclear/healthy)
                        if (!result.isUnclear && !result.rawLabel.contains("Healthy")) {
                            // Fire supplementary Gemini call for richer advice
                            launch(Dispatchers.IO) {
                                try {
                                    val apiKey = BuildConfig.GEMINI_API_KEY
                                    if (apiKey != "MY_GEMINI_API_KEY") {
                                        val prompt = "The local TFLite model detected '${result.label}' " +
                                            "with ${(result.confidence * 100).toInt()}% confidence. " +
                                            "As an expert agronomist for Madhya Pradesh farmers: " +
                                            "1) Confirm or refine this diagnosis. " +
                                            "2) Give 2-3 specific actionable treatment steps. " +
                                            "3) Mention any pesticides approved in India for this disease. " +
                                            "Reply in $prefLanguage. Keep it under 150 words."
                                        val request = GenerateContentRequest(
                                            contents = listOf(Content(parts = listOf(Part(text = prompt))))
                                        )
                                        val response = GeminiNetwork.api.generateContent(
                                            model = "gemini-1.5-flash", apiKey = apiKey, request = request)
                                        geminiResult = response.candidates?.firstOrNull()
                                            ?.content?.parts?.firstOrNull()?.text
                                    }
                                } catch (_: Exception) { /* supplementary — ignore failure */ }
                            }
                        }

                    } else {
                        // ── Gemini API fallback (cloud, requires internet) ─────
                        inferenceMode = "gemini"
                        val result = withContext(Dispatchers.IO) {
                            try {
                                val apiKey = BuildConfig.GEMINI_API_KEY
                                if (apiKey == "MY_GEMINI_API_KEY") {
                                    kotlinx.coroutines.delay(1200)
                                    return@withContext "[DEMO] Early Blight detected (simulated). " +
                                        "Apply Mancozeb 75% WP @ 2.5g/L."
                                }
                                val parts = mutableListOf<Part>()
                                var prompt = "You are an expert plant pathologist for Indian farmers. "
                                if (selectedBitmap != null) {
                                    prompt += "Analyse the attached leaf image. "
                                    val out = ByteArrayOutputStream()
                                    selectedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 70, out)
                                    val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                                    parts.add(Part(inlineData = InlineData("image/jpeg", b64)))
                                }
                                if (symptoms.isNotBlank()) prompt += "Symptoms: $symptoms. "
                                prompt += "Diagnose the disease. Give: disease name, confidence estimate, " +
                                    "and 2-3 treatment steps using pesticides available in India. " +
                                    "Reply in $prefLanguage. Keep under 150 words."
                                parts.add(0, Part(text = prompt))
                                val request = GenerateContentRequest(
                                    contents = listOf(Content(parts = parts)))
                                val response = GeminiNetwork.api.generateContent(
                                    model = "gemini-1.5-flash",
                                    apiKey = apiKey,
                                    request = request)
                                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                                    ?: "No diagnosis returned."
                            } catch (e: Exception) {
                                "[FALLBACK DEMO] Network error. Simulated: Leaf Rust detected.\n" +
                                    "Apply Propiconazole 25% EC @ 1 ml/L."
                            }
                        }
                        geminiResult = result
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
            enabled = !isLoading && (symptoms.isNotBlank() || selectedBitmap != null)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp)
                Spacer(Modifier.width(12.dp))
                Text("Analysing...", style = MaterialTheme.typography.titleMedium, color = Color.White)
            } else {
                Icon(if (classifier.isModelAvailable) Icons.Filled.PhoneAndroid else Icons.Filled.AutoAwesome,
                    contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(if (classifier.isModelAvailable) "Analyse On-Device" else "Analyse with AI",
                    style = MaterialTheme.typography.titleMedium)
            }
        }

        // ── TFLite Result card ────────────────────────────────────────────────
        tfliteResult?.let { result ->
            AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
                val (bgColour, textColour) = cardColourFor(result)
                Surface(shape = RoundedCornerShape(16.dp), color = bgColour,
                    modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        // Header row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (result.isUnclear) Icons.Filled.CameraAlt
                                else if (result.rawLabel.contains("Healthy")) Icons.Filled.CheckCircle
                                else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = textColour
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("On-Device Diagnosis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, color = textColour)
                            Spacer(Modifier.weight(1f))
                            Surface(shape = RoundedCornerShape(6.dp),
                                color = textColour.copy(alpha = 0.15f)) {
                                Text(
                                    "${(result.confidence * 100).toInt()}% confident",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColour,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Disease name
                        Text(result.label, style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold, color = textColour)

                        // Confidence progress bar
                        LinearProgressIndicator(
                            progress = { result.confidence },
                            modifier = Modifier.fillMaxWidth().height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = textColour,
                            trackColor = textColour.copy(alpha = 0.2f)
                        )

                        // Treatment tip
                        HorizontalDivider(color = textColour.copy(alpha = 0.2f))
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Lightbulb, contentDescription = null,
                                tint = textColour, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(result.treatmentTip, style = MaterialTheme.typography.bodyMedium,
                                color = textColour, lineHeight = 22.sp, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { 
                                    tts?.speak("${result.label}. ${result.treatmentTip}", TextToSpeech.QUEUE_FLUSH, null, null) 
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = "Read aloud", tint = textColour)
                            }
                        }
                    }
                }
            }
        }

        // ── Gemini supplementary / fallback result ─────────────────────────────
        geminiResult?.let { geminiText ->
            AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
                Surface(shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE3F2FD), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null,
                                tint = Color(0xFF1565C0))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (inferenceMode == "tflite") "Gemini Expert Advisory"
                                else "Gemini AI Diagnosis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                            Spacer(Modifier.weight(1f))
                            IconButton(
                                onClick = { tts?.speak(geminiText, TextToSpeech.QUEUE_FLUSH, null, null) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = "Read aloud", tint = Color(0xFF1565C0))
                            }
                        }
                        Text(geminiText, style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF0D47A1), lineHeight = 22.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
