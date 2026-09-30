from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .config import settings

app = FastAPI(
    title="CropSaathi Backend API",
    description="Backend services for DPG Federation, AI Advisory, and Telemetry Fusion",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health")
async def health_check():
    return {
        "status": "healthy", 
        "project": settings.GCP_PROJECT_ID,
        "service": "CropSaathi API"
    }
