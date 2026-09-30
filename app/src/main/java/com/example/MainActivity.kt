package com.example
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.foundation.shape.RoundedCornerShape
import android.os.Bundle
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.IconButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.MyApplicationTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.core.content.ContextCompat
import android.util.Log
import java.util.concurrent.Executors
import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox



import androidx.compose.runtime.rememberCoroutineScope
import com.google.android.gms.maps.CameraUpdateFactory

import android.content.Context

import androidx.datastore.core.DataStore

import androidx.datastore.preferences.core.Preferences

import androidx.datastore.preferences.core.doublePreferencesKey

import androidx.datastore.preferences.core.edit

import androidx.datastore.preferences.core.floatPreferencesKey

import androidx.datastore.preferences.preferencesDataStore

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull


import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "map_prefs")

object MapPreferencesKeys {

    val LATITUDE = doublePreferencesKey("latitude")

    val LONGITUDE = doublePreferencesKey("longitude")

    val ZOOM = floatPreferencesKey("zoom")

}


class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CropSaathiApp()
      }
    }
  }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    object AI : Screen("ai_assistant", "Diagnosis", Icons.Filled.BugReport)
    object Chat : Screen("chat", "Chatbot", Icons.Filled.Chat)
    object Map : Screen("map", "Map", Icons.Filled.Map)
    object Irrigate : Screen("irrigate", "Irrigation", Icons.Filled.WaterDrop)
    object Calculator : Screen("calculator", "Calculator", Icons.Filled.Calculate)
    object Seeds : Screen("seeds", "Seeds", Icons.Filled.Eco)
    object Marketplace : Screen("marketplace", "Market", Icons.Filled.StoreMallDirectory)
    object Advisory : Screen("advisory", "Advisory", Icons.Filled.Lightbulb)
}

val items = listOf(
    Screen.Dashboard,
    Screen.AI,
    Screen.Marketplace,
    Screen.Advisory,
    Screen.Chat
)

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun CropSaathiApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isProfile = currentDestination?.route == "profile"
    
    
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary) {
                            Icon(Icons.Filled.Agriculture, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp).size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CropSathi", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                },
                navigationIcon = { if (isProfile) { androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } } },
                actions = {
                    androidx.compose.material3.IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    }
                    if (!isProfile) { androidx.compose.material3.IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile")
                    } }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                
                
                
                items.forEach { screen ->
                    NavigationBarItem(
                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        val context = androidx.compose.ui.platform.LocalContext.current
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.AI.route) { AiAssistantScreen() }
            composable(Screen.Chat.route) { ChatScreen() }
            composable(Screen.Map.route) { MapScreen() }
            composable(Screen.Marketplace.route) { MarketplaceScreen() }
            composable(Screen.Advisory.route) { AdvisoryScreen() }
            composable(Screen.Irrigate.route) { val vm: IrrigationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as android.app.Application)); IrrigationDashboardScreen(viewModel = vm, onNavigateToHistory = { navController.navigate(Screen.Calculator.route) }) }
            composable(Screen.Calculator.route) { val vm: IrrigationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as android.app.Application)); IrrigateScreen(viewModel = vm) }
            composable(Screen.Seeds.route) { SeedCalculatorScreen() }
            composable("profile") { ProfileScreen(onNavigateBack = { navController.popBackStack() }) }
        }
    }
}

@Entity(tableName = "dashboard_data")
data class DashboardData(
    @PrimaryKey
    val location: String,
    val ndvi: String,
    val weather: String,
    val npk: String,
    val crop: String,
    val waterEfficiency: Float,
    val yieldForecast: Float
)

@Dao
interface DashboardDao {
    @Query("SELECT * FROM dashboard_data")
    fun getAllData(): Flow<List<DashboardData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(data: List<DashboardData>)
}

@Database(entities = [DashboardData::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dashboardDao(): DashboardDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cropsaathi_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).dashboardDao()

    private val _selectedLocation = MutableStateFlow("Indore")
    val selectedLocation: StateFlow<String> = _selectedLocation.asStateFlow()

    private val _uiState = MutableStateFlow(
        DashboardData("Indore", "0.62", "High Rain", "Low N", "Soybean JS-9560", 0.85f, 0.75f)
    )
    val uiState: StateFlow<DashboardData> = _uiState.asStateFlow()

    private var allDataMap = mutableMapOf<String, DashboardData>()

    init {
        viewModelScope.launch {
            dao.getAllData().collect { dataList ->
                if (dataList.isEmpty()) {
                    val mockData = listOf(
                        DashboardData("Indore", "0.62", "High Rain", "Low N", "Soybean JS-9560", 0.85f, 0.75f),
                        DashboardData("Rewa", "0.55", "Moderate", "Optimal", "Wheat GW-322", 0.70f, 0.80f),
                        DashboardData("Ujjain", "0.45", "Dry", "High K", "Chickpea", 0.60f, 0.65f)
                    )
                    dao.insertData(mockData)
                } else {
                    allDataMap.clear()
                    allDataMap.putAll(dataList.associateBy { it.location })
                    updateUiState(_selectedLocation.value)
                }
            }
        }
    }

    private val _isRefreshing = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()


    fun refreshMapData() {

        viewModelScope.launch {

            _isRefreshing.value = true

            kotlinx.coroutines.delay(1500)

            // Simulate refetching data

            updateUiState(_selectedLocation.value)

            _isRefreshing.value = false

        }

    }


    fun selectLocation(location: String) {
        _selectedLocation.value = location
        updateUiState(location)
    }

    private fun updateUiState(location: String) {
        allDataMap[location]?.let {
            _uiState.value = it
        }
    }
}

@Composable
fun DataPill(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)

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
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Welcome Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Top Tag
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "SYSTEM OPERATIONAL • FARM ID: #AG-7782",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Good morning, Farmer. All systems are green.",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    androidx.compose.material3.IconButton(
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
                            if (isListening) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Voice Search Location",
                            tint = if (isListening) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "North Sector soil moisture is optimal. Perfect conditions for early morning field scouting.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Location Selector
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
            }
        }
        
        // Smart Modules Section
        Text("Smart Modules", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Surface(modifier = Modifier.weight(1f).height(120.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Disease Diagnosis", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Instant scan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Surface(modifier = Modifier.weight(1f).height(120.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Column {
                        Text("Smart Irrigation", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Water alerts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        
        // Telemetry & Health Grid
        // NDVI Satellite Feed
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("NDVI SATELLITE FEED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Crop Health Index", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Icon(Icons.Filled.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(uiState.ndvi, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("+2.4% from last week", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("Optimal vegetative growth", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(progress = { 0.94f }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
            }
        }
        
        // IOT Sensor Network
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("IOT SENSOR NETWORK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Soil Moisture & NPK", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    ) {
                        Icon(Icons.Filled.Sensors, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("N", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Med", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("P", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Opt", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("K", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("High", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
        
        MarketTrendsChart()

        // Help Hotline Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primary,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.2f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "24/7 EXPERT ASSISTANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Need Agronomist Consultation?", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Connect instantly with certified agricultural scientists.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    onClick = { /* Call Action */ }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call Helpline", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MapScreen(viewModel: DashboardViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var isMapInitialized by remember { mutableStateOf(false) }

    val indore = LatLng(22.7196, 75.8577)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(indore, 15f)
    }

    val polygonPoints = listOf(
        LatLng(22.7210, 75.8550),
        LatLng(22.7210, 75.8600),
        LatLng(22.7170, 75.8600),
        LatLng(22.7170, 75.8550)
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val prefs = context.dataStore.data.first()
        val lat = prefs[MapPreferencesKeys.LATITUDE] ?: indore.latitude
        val lng = prefs[MapPreferencesKeys.LONGITUDE] ?: indore.longitude
        val zoom = prefs[MapPreferencesKeys.ZOOM] ?: 15f
        cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), zoom)
        isMapInitialized = true
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving && isMapInitialized) {
            val pos = cameraPositionState.position
            context.dataStore.edit { prefs ->
                prefs[MapPreferencesKeys.LATITUDE] = pos.target.latitude
                prefs[MapPreferencesKeys.LONGITUDE] = pos.target.longitude
                prefs[MapPreferencesKeys.ZOOM] = pos.zoom
            }
        }
    }


    val isRefreshing by viewModel.isRefreshing.collectAsState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshMapData() },
        modifier = Modifier.fillMaxSize()
    ) {
        val uiSettings = remember { MapUiSettings(zoomControlsEnabled = true) }
        GoogleMap(
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings
        ) {
            Polygon(
                points = polygonPoints,
                fillColor = Color(0x444CAF50), // Semi-transparent green per instruction
                strokeColor = Color(0xFF388E3C),
                strokeWidth = 2f
            )
            MarkerInfoWindowContent(
                state = MarkerState(position = indore),
                title = "IPS Academy Test Node"
            ) { marker ->
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(marker.title ?: "", style = MaterialTheme.typography.titleSmall, color = Color.Black)
                    Text("Soil Moisture: ${(uiState.waterEfficiency * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                }
            }
        }

        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(800),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            )
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .alpha(alpha)
                .semantics {
                    contentDescription = "Alert: Rust Outbreak Warning. FCM Dispatch Active."
                    liveRegion = LiveRegionMode.Polite
                },
            color = MaterialTheme.colorScheme.error,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning Icon",
                    tint = MaterialTheme.colorScheme.onError
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rust Outbreak Warning: FCM Dispatch Active",
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(indore, 15f)
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Recenter Map on Indore"
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedCalculatorScreen() {
    var expanded by remember { mutableStateOf(false) }
    val crops = listOf("Soybean (JS-9560)", "Wheat (GW-322)", "Chickpea (Desi)", "Mustard")
    var selectedCrop by remember { mutableStateOf(crops[0]) }
    var areaInput by remember { mutableStateOf("") }
    
    val area = areaInput.toDoubleOrNull() ?: 0.0
    val seedRate = when {
        selectedCrop.contains("Soybean") -> 30.0
        selectedCrop.contains("Wheat") -> 40.0
        selectedCrop.contains("Chickpea") -> 25.0
        else -> 2.0 // Mustard
    } // kg per acre
    val spacing = when {
        selectedCrop.contains("Soybean") -> "45 cm x 15 cm"
        selectedCrop.contains("Wheat") -> "22.5 cm row spacing"
        selectedCrop.contains("Chickpea") -> "30 cm x 10 cm"
        else -> "45 cm x 15 cm" // Mustard
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Eco,
            contentDescription = "Seed Icon",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Seed Planning", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text("Calculate optimal seed requirements", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCrop,
                onValueChange = {},
                readOnly = true,
                label = { Text("Regional Crop") },
                leadingIcon = { Icon(Icons.Default.Eco, contentDescription = null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                crops.forEach { crop ->
                    DropdownMenuItem(
                        text = { Text(crop) },
                        onClick = {
                            selectedCrop = crop
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = areaInput,
            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) areaInput = it },
            label = { Text("Farm Area (Acres)") },
            leadingIcon = { Icon(Icons.Default.Map, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(24.dp)
            ) {
                Text("Requirement Analysis", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Seed Required:", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("${(area * seedRate).toInt()} kg", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recommended Spacing:", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(spacing, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

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
    var volumeUnit by remember { mutableStateOf("Liters") }
    val displayVolume: Double = when (volumeUnit) {
        "Gallons" -> requiredWaterLiters.toDouble() * 0.264172052358148
        "Milliliters" -> requiredWaterLiters.toDouble() * 1000.0
        else -> requiredWaterLiters.toDouble()
    }
    val formattedWaterLiters = when (volumeUnit) {
        "Gallons" -> String.format(java.util.Locale.US, "%.4f", displayVolume)
        "Milliliters" -> String.format(java.util.Locale.US, "%.0f", displayVolume)
        else -> String.format(java.util.Locale.US, "%.2f", displayVolume)
    }
    val formattedTemp = String.format(java.util.Locale.US, "%.1f", temperature)
    val formattedMoisture = String.format(java.util.Locale.US, "%.1f", currentMoisture)
    val formattedDeficit = String.format(java.util.Locale.US, "%.1f", 100f - currentMoisture)

    val historyItems by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val weatherWarning by viewModel.weatherWarning.collectAsStateWithLifecycle()
    val seasonalTip by viewModel.seasonalTip.collectAsStateWithLifecycle()
    val isFetchingTip by viewModel.isFetchingTip.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Weekly Usage Dashboard
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp), color = MaterialTheme.colorScheme.primary)
            }
        } else if (historyItems.isNotEmpty()) {
            val currentWeekVolume = remember(historyItems) {
                val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
                historyItems.filter { it.timestamp >= sevenDaysAgo }
                    .sumOf { it.recommendedVolumeLiters.toDouble() }
                    .toFloat()
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Past 7 Days Usage",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.0f", currentWeekVolume)} L",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.WaterDrop,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                }
            }
        }

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
        // Weather Warning Banner
        weatherWarning?.let { warningMsg ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = "Weather Warning", tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = warningMsg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
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
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            growthStages.forEach { stage ->
                                DropdownMenuItem(
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("RECOMMENDED DAILY VOLUME", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer); var unitExpanded by remember { mutableStateOf(false) }; Box { TextButton(onClick = { unitExpanded = true }) { Text(volumeUnit, color = MaterialTheme.colorScheme.onPrimaryContainer); Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer) }; DropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) { listOf("Liters", "Milliliters", "Gallons").forEach { unit -> DropdownMenuItem(text = { Text(unit) }, onClick = { volumeUnit = unit; unitExpanded = false }) } } } }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(formattedWaterLiters, style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(" $volumeUnit", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(bottom = 8.dp))
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
                androidx.compose.material3.Button(
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

        // AI Seasonal Tip
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Seasonal Tip", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    androidx.compose.material3.TextButton(onClick = { viewModel.fetchSeasonalTip(temperature, currentMoisture) }) {
                        Text(if (seasonalTip == null) "Generate" else "Refresh", color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (isFetchingTip) {
                    Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                } else if (seasonalTip != null) {
                    Text(
                        text = seasonalTip!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    Text(
                        text = "Tap Generate to get a customized seasonal tip from Gemini based on the current date and climate.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Soil Maintenance Tips
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Soil Maintenance Tip", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                Text(
                    text = when (soilTexture) {
                        "Sand" -> "Sandy soil drains quickly. Consider adding organic matter (like compost) to improve water retention and reduce irrigation frequency."
                        "Clay" -> "Clay soil retains water well but can become compacted. Aerate regularly and avoid overwatering to prevent root rot."
                        else -> "Loam is ideal for agriculture. Maintain a regular mulching schedule to preserve topsoil moisture and prevent erosion."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        // Crop Growth Trend Chart
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp), color = MaterialTheme.colorScheme.primary)
            }
        } else if (historyItems.isNotEmpty()) {
            CropGrowthChart(historyItems)
        }

        // History Section
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp), color = MaterialTheme.colorScheme.primary)
            }
        } else if (historyItems.isNotEmpty()) {
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
                                androidx.compose.material3.IconButton(onClick = { viewModel.deleteHistory(item.id) }) {
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
