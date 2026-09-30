from .earth_engine_service import EarthEngineService
from .weather_imd_service import WeatherIMDService
from .soil_health_service import SoilHealthService

class CropRecommender:
    @staticmethod
    def recommend_crop(lat: float, lng: float, parcel_id: str):
        # Fetch telemetry across federated services
        ndvi = EarthEngineService.get_ndvi_telemetry(lat, lng)
        weather = WeatherIMDService.get_forecast(lat, lng)
        soil = SoilHealthService.get_soil_card(parcel_id)

        # Multi-Source Data Fusion Engine (Mock Logic)
        recommended_crop = "Soybean JS-9560"
        water_efficiency_score = 0.85
        yield_forecast_confidence = 0.75

        return {
            "recommended_crop": recommended_crop,
            "water_efficiency_score": water_efficiency_score,
            "yield_forecast_confidence": yield_forecast_confidence,
            "telemetry_fusion": {
                "ndvi": ndvi,
                "weather": weather,
                "soil": soil
            }
        }
