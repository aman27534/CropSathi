class EarthEngineService:
    @staticmethod
    def get_ndvi_telemetry(lat: float, lng: float):
        return {
            "source": "Sentinel-2",
            "ndvi_score": 0.62,
            "cloud_cover_percent": 5.0,
            "status": "Optimal"
        }
