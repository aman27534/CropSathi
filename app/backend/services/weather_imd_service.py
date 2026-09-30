class WeatherIMDService:
    @staticmethod
    def get_forecast(lat: float, lng: float):
        return {
            "source": "IMD",
            "condition": "High Rain",
            "rainfall_probability": 0.80,
            "temp_c": 28.5,
            "forecast_days": 5
        }
