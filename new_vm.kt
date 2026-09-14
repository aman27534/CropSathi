import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow

class IrrigationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: IrrigationRepository

    val uiState: StateFlow<List<IrrigationHistory>>
    
    private val _weatherWarning = MutableStateFlow<String?>(null)
    val weatherWarning: StateFlow<String?> = _weatherWarning

    init {
        val dao = IrrigationDatabase.getDatabase(application).irrigationDao()
        repository = IrrigationRepository(dao)
        uiState = repository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        checkWeatherForecast()
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
