package com.example

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val transcribing by viewModel.transcribing.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    
    val models = listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash-lite")
    var selectedModel by remember { mutableStateOf(models[0]) }
    var useMaps by remember { mutableStateOf(false) }
    
    var isRecording by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val prefLanguage = LanguageManager.getLanguage(context)
    val recorderHelper = remember { AudioRecorderHelper(context) }
    
    var hasRecordPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasRecordPermission = granted
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FC))) {
        // Options row
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedModel.replace("gemini-", ""),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().width(200.dp).height(50.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    models.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model.replace("gemini-", "")) },
                            onClick = { selectedModel = model; expanded = false }
                        )
                    }
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Map, contentDescription = "Maps Grounding", tint = if (useMaps) MaterialTheme.colorScheme.primary else Color.Gray)
                Switch(checked = useMaps, onCheckedChange = { useMaps = it }, modifier = Modifier.scale(0.8f))
            }
        }

        // Chat messages
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isUser) MaterialTheme.colorScheme.primary else if (msg.isError) MaterialTheme.colorScheme.errorContainer else Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = msg.text,
                            modifier = Modifier.padding(12.dp),
                            color = if (isUser) MaterialTheme.colorScheme.onPrimary else if (msg.isError) MaterialTheme.colorScheme.onErrorContainer else Color.Black
                        )
                    }
                }
            }
            if (isLoading) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.Start) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text(if (transcribing) "Transcribing..." else "Ask CropSathi...") },
                modifier = Modifier.weight(1f),
                enabled = !transcribing,
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            
            FloatingActionButton(
                onClick = {
                    if (!hasRecordPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        return@FloatingActionButton
                    }
                    if (isRecording) {
                        isRecording = false
                        val file = recorderHelper.stopRecording()
                        if (file != null) {
                            viewModel.transcribeAudio(file, BuildConfig.GEMINI_API_KEY) { text ->
                                inputText = text
                            }
                        }
                    } else {
                        isRecording = true
                        recorderHelper.startRecording()
                    }
                },
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(if (isRecording) Icons.Filled.MicOff else Icons.Filled.Mic, contentDescription = "Record")
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            FloatingActionButton(
                onClick = {
                    if (inputText.isNotBlank() && !isLoading && !transcribing) {
                        viewModel.sendMessage(inputText, selectedModel, BuildConfig.GEMINI_API_KEY, useMaps, prefLanguage)
                        inputText = ""
                    }
                    
                },
                containerColor = if (inputText.isNotBlank() && !isLoading && !transcribing) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(48.dp),
                
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send")
            }
        }
    }
}
