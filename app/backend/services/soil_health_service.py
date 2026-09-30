class SoilHealthService:
    @staticmethod
    def get_soil_card(parcel_id: str):
        return {
            "source": "State Soil Health Card",
            "nitrogen": "Low N",
            "phosphorus": "Optimal",
            "potassium": "Optimal",
            "ph_level": 6.8
        }
