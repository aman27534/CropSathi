CREATE SCHEMA IF NOT EXISTS cropsaathi_dw;

CREATE TABLE IF NOT EXISTS cropsaathi_dw.state_cooperation_nodes (
    node_id STRING,
    node_name STRING,
    state STRING,
    district STRING,
    location GEOGRAPHY,
    created_at TIMESTAMP,
    PRIMARY KEY (node_id) NOT ENFORCED
);

CREATE TABLE IF NOT EXISTS cropsaathi_dw.farm_parcels (
    parcel_id STRING,
    farmer_id STRING,
    node_id STRING,
    parcel_boundary GEOGRAPHY,
    area_acres FLOAT64,
    created_at TIMESTAMP,
    PRIMARY KEY (parcel_id) NOT ENFORCED
);

CREATE TABLE IF NOT EXISTS cropsaathi_dw.sentinel2_satellite_telemetry (
    telemetry_id STRING,
    parcel_id STRING,
    scan_date DATE,
    ndvi_score FLOAT64,
    cloud_cover FLOAT64,
    raw_image_uri STRING,
    PRIMARY KEY (telemetry_id) NOT ENFORCED
);

CREATE TABLE IF NOT EXISTS cropsaathi_dw.imd_weather_bulletins (
    bulletin_id STRING,
    region STRING,
    forecast_date DATE,
    temp_c FLOAT64,
    rainfall_mm FLOAT64,
    humidity_percent FLOAT64,
    PRIMARY KEY (bulletin_id) NOT ENFORCED
);

CREATE TABLE IF NOT EXISTS cropsaathi_dw.soil_health_records (
    record_id STRING,
    parcel_id STRING,
    test_date DATE,
    nitrogen_level STRING,
    phosphorus_level STRING,
    potassium_level STRING,
    ph_level FLOAT64,
    PRIMARY KEY (record_id) NOT ENFORCED
);

CREATE TABLE IF NOT EXISTS cropsaathi_dw.crop_disease_scans (
    scan_id STRING,
    parcel_id STRING,
    scan_timestamp TIMESTAMP,
    image_uri STRING,
    ai_confidence FLOAT64,
    detected_pathogen STRING,
    PRIMARY KEY (scan_id) NOT ENFORCED
);

CREATE TABLE IF NOT EXISTS cropsaathi_dw.fcm_outbreak_alerts (
    alert_id STRING,
    region_polygon GEOGRAPHY,
    issue_date TIMESTAMP,
    pathogen STRING,
    severity STRING,
    message STRING,
    PRIMARY KEY (alert_id) NOT ENFORCED
);
