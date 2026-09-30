package com.example

/**
 * Global Hackathon Survival Configuration
 * 
 * Set IS_OFFLINE_JUDGING_MODE to true to bypass live network calls 
 * (BigQuery, Earth Engine) and use hardcoded seed data. 
 * This ensures the demo functions perfectly even if the hackathon venue has poor Wi-Fi.
 */
object DemoConfig {
    const val IS_OFFLINE_JUDGING_MODE: Boolean = true
}
