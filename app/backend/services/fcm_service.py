import asyncio
import firebase_admin
from firebase_admin import credentials, messaging
from fastapi import BackgroundTasks
from ..config import settings

# Initialize Firebase App
try:
    # In a real environment, load credentials from settings.FIREBASE_CREDENTIALS
    # cred = credentials.Certificate(settings.FIREBASE_CREDENTIALS)
    # firebase_admin.initialize_app(cred)
    firebase_admin.get_app()
except ValueError:
    pass # App not initialized, bypassing for mock setup

def _sync_dispatch_outbreak(disease: str, radius_km: int, lat: float, lng: float):
    """
    Synchronous FCM multicast dispatch. 
    Using a topic format based on geo-hashing or radius logic.
    """
    topic_name = f"alert_zone_{int(lat)}_{int(lng)}"
    
    message = messaging.Message(
        notification=messaging.Notification(
            title="🚨 Rust Outbreak Warning",
            body=f"FCM Dispatch Active: High risk of {disease} detected within {radius_km}km of your farm."
        ),
        data={
            "disease": disease,
            "severity": "CRITICAL",
            "lat": str(lat),
            "lng": str(lng)
        },
        topic=topic_name
    )
    
    # In a live environment:
    # response = messaging.send(message)
    # return response
    return f"Successfully dispatched mock FCM to {topic_name}"

async def dispatch_outbreak_warning(disease: str, radius_km: int, lat: float, lng: float, background_tasks: BackgroundTasks = None):
    """
    Asynchronous wrapper to prevent blocking the FastAPI event loop during network I/O.
    """
    if background_tasks:
        # Fire and forget
        background_tasks.add_task(_sync_dispatch_outbreak, disease, radius_km, lat, lng)
        return {"status": "queued", "message": "FCM warning queued in background"}
    else:
        # Wait for the thread to finish and return result
        result = await asyncio.to_thread(_sync_dispatch_outbreak, disease, radius_km, lat, lng)
        return {"status": "dispatched", "result": result}
