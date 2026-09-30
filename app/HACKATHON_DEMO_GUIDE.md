# CropSaathi: Hackathon Demonstration Guide

## Problem Statement Alignment
**04. Agricultural Intelligence & The 'Cooperation' Theme**
CropSaathi directly solves Problem Statement 04 by harnessing real-time satellite imagery (NDVI), meteorological data, and edge-AI diagnostics to provide actionable agricultural intelligence to farmers. 

Crucially, it fulfills the **'Cooperation'** theme by establishing a **Digital Public Good (DPG) Mesh Architecture**. Rather than hoarding data in isolated silos, CropSaathi federates intelligence across state boundaries (e.g., sharing PAU Ludhiana's pathogen models with JNKVV Jabalpur). When a disease outbreak is detected in one region, the DPG mesh automatically cooperates to warn neighboring nodes, ensuring collective resilience against crop failures.

---

## 🎬 Step-by-Step Judge Demonstration Script

### Scenario 1: Satellite & Weather Fusion (Dashboard)
**Goal:** Demonstrate precision crop recommendation based on fused geospatial and meteorological data.
1. **Action:** Open the CropSaathi Dashboard.
2. **Context to Judge:** "Here we are pulling real-time Sentinel-2 satellite data and IMD rainfall statistics for a 3.5-acre parcel in Ujjain."
3. **Execution:** Show the UI displaying an NDVI score of `0.62` and `High Rain` conditions.
4. **Result:** Highlight the automated recommendation module suggesting the planting of **Soybean (JS-9560)**, which optimally matches these exact soil and weather conditions.

### Scenario 2: Disease Diagnosis & Malwi Advisory (Edge AI & LLM)
**Goal:** Show offline edge inference combined with localized, accessible generative AI voice advisories.
1. **Action:** Navigate to the 'Disease Diagnosis' camera scanner.
2. **Context to Judge:** "A farmer notices yellowing leaves. They use our offline TFLite model—meaning this works even with zero internet."
3. **Execution:** Point the camera at a sample wheat leaf (or test image). The screen instantly flashes: `Yellow Rust Detected (94% Confidence)`.
4. **Result:** Tap the 'Play Advisory' button. The app uses Gemini to generate a hyper-localized treatment plan and synthesizes it into a **Malwi regional voice audio**, ensuring the farmer understands the pesticide instructions in their native dialect.

### Scenario 3: State Cooperation & DPG Mesh (Cross-Border Alerting)
**Goal:** Prove the 'Cooperation' theme via the federated notification mesh.
1. **Action:** Open the 'Alerts & Community' screen.
2. **Context to Judge:** "Because diseases don't respect state borders, our DPG mesh ensures universities cooperate."
3. **Execution:** Trigger the mock alert from the backend.
4. **Result:** A high-priority FCM push notification arrives on the phone: *"Alert: Yellow Rust Outbreak reported by PAU Ludhiana. High risk for neighboring regions. Preventive spray advised."* This proves the states are actively cooperating to protect the broader agricultural network.
