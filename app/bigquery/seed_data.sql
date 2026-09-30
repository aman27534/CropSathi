-- Seed data for Madhya Pradesh (Malwa Region)

INSERT INTO cropsaathi_dw.state_cooperation_nodes (node_id, node_name, state, district, location, created_at)
VALUES 
    ('N-IN-01', 'Indore Test Node', 'Madhya Pradesh', 'Indore', ST_GEOGPOINT(75.8577, 22.7196), CURRENT_TIMESTAMP()),
    ('N-UJ-01', 'Ujjain Krishi Kendra', 'Madhya Pradesh', 'Ujjain', ST_GEOGPOINT(75.7849, 23.1765), CURRENT_TIMESTAMP());

INSERT INTO cropsaathi_dw.farm_parcels (parcel_id, farmer_id, node_id, parcel_boundary, area_acres, created_at)
VALUES 
    ('P-1001', 'F-5521', 'N-IN-01', ST_GEOGFROMTEXT('POLYGON((75.8500 22.7100, 75.8550 22.7100, 75.8550 22.7150, 75.8500 22.7150, 75.8500 22.7100))'), 5.2, CURRENT_TIMESTAMP()),
    ('P-1002', 'F-5522', 'N-UJ-01', ST_GEOGFROMTEXT('POLYGON((75.7800 23.1700, 75.7850 23.1700, 75.7850 23.1750, 75.7800 23.1750, 75.7800 23.1700))'), 3.8, CURRENT_TIMESTAMP());

INSERT INTO cropsaathi_dw.sentinel2_satellite_telemetry (telemetry_id, parcel_id, scan_date, ndvi_score, cloud_cover, raw_image_uri)
VALUES 
    ('T-9901', 'P-1001', CURRENT_DATE(), 0.62, 5.0, 'gs://cropsaathi-sat-imagery/T-9901.tif'),
    ('T-9902', 'P-1002', CURRENT_DATE(), 0.45, 12.5, 'gs://cropsaathi-sat-imagery/T-9902.tif');

INSERT INTO cropsaathi_dw.imd_weather_bulletins (bulletin_id, region, forecast_date, temp_c, rainfall_mm, humidity_percent)
VALUES 
    ('W-IN-0905', 'Indore', CURRENT_DATE(), 28.5, 45.2, 80.0),
    ('W-UJ-0905', 'Ujjain', CURRENT_DATE(), 30.1, 12.0, 65.0);

INSERT INTO cropsaathi_dw.soil_health_records (record_id, parcel_id, test_date, nitrogen_level, phosphorus_level, potassium_level, ph_level)
VALUES 
    ('SH-1001', 'P-1001', CURRENT_DATE(), 'Low', 'Optimal', 'Optimal', 6.8),
    ('SH-1002', 'P-1002', CURRENT_DATE(), 'Optimal', 'High', 'Optimal', 7.2);

INSERT INTO cropsaathi_dw.crop_disease_scans (scan_id, parcel_id, scan_timestamp, image_uri, ai_confidence, detected_pathogen)
VALUES 
    ('SC-2001', 'P-1001', CURRENT_TIMESTAMP(), 'gs://cropsaathi-scans/SC-2001.jpg', 0.94, 'Yellow Rust');

INSERT INTO cropsaathi_dw.fcm_outbreak_alerts (alert_id, region_polygon, issue_date, pathogen, severity, message)
VALUES 
    ('ALT-501', ST_GEOGFROMTEXT('POLYGON((75.0 22.0, 76.0 22.0, 76.0 24.0, 75.0 24.0, 75.0 22.0))'), CURRENT_TIMESTAMP(), 'Yellow Rust', 'CRITICAL', 'Rust Outbreak Warning: FCM Dispatch Active');
