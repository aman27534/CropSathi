from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    GCP_PROJECT_ID: str = "cropsaathi-mock-project"
    GEMINI_API_KEY: str = "mock-gemini-key"
    FIREBASE_CREDENTIALS: str = "firebase-adminsdk.json"
    
    # Global Hackathon Survival Configuration
    # When set to True, bypass live network calls and fallback to seed data.
    IS_OFFLINE_JUDGING_MODE: bool = True
    
    class Config:
        env_file = ".env"

settings = Settings()
