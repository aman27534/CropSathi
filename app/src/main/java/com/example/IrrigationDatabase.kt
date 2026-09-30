package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Entity(tableName = "irrigation_history")
data class IrrigationHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cropStage: String,
    val soilTexture: String,
    val temperature: Float,
    val currentMoisture: Float,
    val recommendedVolumeLiters: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface IrrigationDao {
    @Query("SELECT * FROM irrigation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<IrrigationHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: IrrigationHistory)

    @Query("DELETE FROM irrigation_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)
}

@Database(entities = [IrrigationHistory::class], version = 1, exportSchema = false)
abstract class IrrigationDatabase : RoomDatabase() {
    abstract fun irrigationDao(): IrrigationDao

    companion object {
        @Volatile
        private var INSTANCE: IrrigationDatabase? = null

        fun getDatabase(context: android.content.Context): IrrigationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IrrigationDatabase::class.java,
                    "irrigation_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class IrrigationRepository(private val dao: IrrigationDao) {
    val allHistory: Flow<List<IrrigationHistory>> = dao.getAllHistory()

    suspend fun insert(history: IrrigationHistory) = dao.insertHistory(history)
    suspend fun deleteById(id: Int) = dao.deleteHistoryById(id)
}


class IrrigationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: IrrigationRepository

    val uiState: StateFlow<List<IrrigationHistory>>

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _weatherWarning = MutableStateFlow<String?>(null)
    val weatherWarning: StateFlow<String?> = _weatherWarning

    private val _seasonalTip = MutableStateFlow<String?>(null)
    val seasonalTip: StateFlow<String?> = _seasonalTip

    private val _isFetchingTip = MutableStateFlow(false)
    val isFetchingTip: StateFlow<Boolean> = _isFetchingTip

    init {
        val dao = IrrigationDatabase.getDatabase(application).irrigationDao()
        repository = IrrigationRepository(dao)
        uiState = repository.allHistory
            .onEach { if (_isLoading.value) { delay(800); _isLoading.value = false } }
            .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        checkWeatherForecast()
    }
    
    fun fetchSeasonalTip(temperature: Float, moisture: Float) {
        viewModelScope.launch {
            _isFetchingTip.value = true
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey == "MY_GEMINI_API_KEY") {
                    _seasonalTip.value = "⚠️ Gemini API Key required for AI tips."
                    return@launch
                }
                val currentMonth = java.time.LocalDate.now().month.name
                val prompt = "It is currently $currentMonth. The local temperature is $temperature°C and soil moisture is $moisture%. Provide a very short, actionable seasonal crop management tip (maximum 2 sentences) based on these conditions."
                val request = GenerateContentRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))))
                val response = GeminiNetwork.api.generateContent(model = "gemini-1.5-flash", apiKey = apiKey, request = request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No tip available at this time."
                _seasonalTip.value = text.trim()
            } catch (e: Exception) {
                Log.e("IrrigationViewModel", "Failed to fetch tip", e)
                _seasonalTip.value = "Unable to fetch AI tip."
            } finally {
                _isFetchingTip.value = false
            }
        }
    }

    fun checkWeatherForecast(city: String = "Indore") {
        viewModelScope.launch {
            try {
                if (BuildConfig.OPENWEATHER_API_KEY == "MY_OPENWEATHER_API_KEY") {
                    _weatherWarning.value = "⚠️ Weather Forecast requires OpenWeather API Key (configure in Secrets)."
                    return@launch
                }

                val response = WeatherNetwork.api.getForecast(city = city)
                var hasExtremeHeat = false
                var hasHeavyRain = false
                
                for (item in response.list.take(8)) {
                    if (item.main.temp_max > 35f) {
                        hasExtremeHeat = true
                    }
                    if (item.weather.any { it.main.equals("Rain", ignoreCase = true) }) {
                        hasHeavyRain = true
                    }
                }

                if (hasExtremeHeat && hasHeavyRain) {
                    _weatherWarning.value = "Extreme heat and rain forecasted. Delay irrigation to let rain do the work and prevent runoff."
                } else if (hasExtremeHeat) {
                    _weatherWarning.value = "Extreme heat forecasted (>35°C). Consider irrigating early morning or late evening to minimize evaporation."
                } else if (hasHeavyRain) {
                    _weatherWarning.value = "Heavy rainfall forecasted. You may want to skip or reduce today's irrigation."
                } else {
                    _weatherWarning.value = null
                }
            } catch (e: Exception) {
                Log.e("WeatherService", "Failed to fetch weather", e)
                _weatherWarning.value = "Unable to fetch local weather forecast."
            }
        }
    }

    fun saveHistory(
        cropStage: String,
        soilTexture: String,
        temperature: Float,
        currentMoisture: Float,
        recommendedVolumeLiters: Float
    ) {
        viewModelScope.launch {
            repository.insert(
                IrrigationHistory(
                    cropStage = cropStage,
                    soilTexture = soilTexture,
                    temperature = temperature,
                    currentMoisture = currentMoisture,
                    recommendedVolumeLiters = recommendedVolumeLiters
                )
            )
        }
    }
    
    fun deleteHistory(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
}
