# 🌾 CropSathi: AI-Powered Smart Farming Assistant

CropSathi is a next-generation Android application designed to assist farmers and agronomists. Built entirely with Kotlin and Jetpack Compose, it leverages Artificial Intelligence (Gemini API) and smart data visualization to provide actionable insights for crop management, disease detection, and market analysis.

## ✨ Key Features

* **🤖 AI Crop Diagnosis:** Upload or take a picture of a diseased leaf, and the AI will analyze it to provide an accurate diagnosis, confidence level, and actionable treatment recommendations.
* **💬 Multi-lingual Chatbot (CropSathi):** An intelligent farming assistant that answers agricultural queries in 7 languages (English, Hindi, Marathi, Punjabi, Gujarati, Tamil, Telugu).
* **📈 Real-Time Market Trends:** Interactive line charts (powered by Vico) displaying weekly historical price trends for local crops like Wheat, Rice, Cotton, and Soybean.
* **📡 Smart Dashboard:** A comprehensive overview featuring NDVI Satellite feeds, IoT Sensor Network data (Soil Moisture, NPK levels), and voice-assisted location switching.
* **💧 Irrigation & Seed Calculators:** Tools to help manage water usage efficiently and calculate optimal seed requirements based on land area.
* **📴 Smart Offline Fallback (Demo Mode):** If the network drops or the API key limit is reached, the app seamlessly switches to a simulated offline mode so the user experience is never interrupted.

## 🛠️ Tech Stack

* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material Design 3)
* **Architecture:** MVVM (Model-View-ViewModel)
* **AI Integration:** Google Gemini API
* **Data Visualization:** Vico Charts
* **Asynchronous Programming:** Kotlin Coroutines & Flows
* **Authentication:** Firebase / Google Identity Services

## 🚀 Getting Started

### Prerequisites
* Android Studio (Latest version)
* Minimum SDK: Android 8.0 (API level 26)

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/CropSathi.git
