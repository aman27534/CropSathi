package com.example

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

// ─── Result data classes ─────────────────────────────────────────────────────

data class DiagnosisResult(
    val label: String,
    val confidence: Float,
    val isUnclear: Boolean,
    val treatmentTip: String,
    val rawLabel: String  // un-prettified, for internal use
)

// ─── Treatment tips map — hardcoded, works fully offline ─────────────────────
//
// Key   : exact class name as in disease_labels.txt
// Value : actionable tip targeting MP/Central India farmers
// ─────────────────────────────────────────────────────────────────────────────
private val TREATMENT_TIPS: Map<String, String> = mapOf(
    "Corn_Common_Rust" to
        "Apply Propiconazole 25% EC @ 1 ml/L water. Spray in early morning. " +
        "Repeat after 14 days if symptoms persist. " +
        "Use resistant hybrid (DKC 6142 / P3396) next season.",

    "Corn_Northern_Leaf_Blight" to
        "Apply Azoxystrobin 23% SC @ 1 ml/L water. Spray at first sign of lesions. " +
        "Remove and destroy infected plant debris after harvest. " +
        "Ensure proper row spacing (75 cm) for air circulation.",

    "Corn_Healthy" to
        "Crop looks healthy! Ensure adequate nitrogen (150-180 kg/ha). " +
        "Monitor weekly for pest entry. Apply DAP at V6 stage if not done.",

    "Potato_Early_Blight" to
        "Apply Chlorothalonil 75% WP @ 2 g/L water. Remove infected lower leaves. " +
        "Ensure 80 kg/ha potassium supply — K builds disease resistance. " +
        "Avoid overhead irrigation; water at base only.",

    "Potato_Late_Blight" to
        "\uD83D\uDEA8 URGENT: Apply Cymoxanil + Mancozeb @ 3 g/L immediately. " +
        "Stop overhead irrigation today. Destroy badly infected plants — " +
        "do not compost. Scout neighbouring plots — Late Blight spreads within 2 days.",

    "Potato_Healthy" to
        "Crop looks healthy! Monitor humidity closely. " +
        "Apply preventive Mancozeb spray if daily rainfall exceeds 20 mm. " +
        "Maintain adequate calcium nutrition.",

    "Tomato_Early_Blight" to
        "Apply Mancozeb 75% WP @ 2.5 g/L water. " +
        "Remove and burn infected lower leaves. " +
        "Maintain 60 cm plant spacing for air movement. " +
        "Mulch soil surface to reduce spore splash.",

    "Tomato_Late_Blight" to
        "Apply Metalaxyl + Mancozeb @ 2.5 g/L every 7 days. " +
        "Switch to drip irrigation — avoid overhead watering. " +
        "Remove infected fruit immediately. " +
        "Fumigate storage area before harvest.",

    "Tomato_Leaf_Mold" to
        "Improve ventilation — thin the canopy if overcrowded. " +
        "Apply Chlorothalonil @ 2 g/L water. " +
        "Target relative humidity below 85% in poly-houses. " +
        "Use resistant varieties (Arka Rakshak) next season.",

    "Tomato_Healthy" to
        "Crop looks healthy! Continue balanced NPK (19:19:19 @ 5 g/L foliar). " +
        "Monitor for whitefly — they spread Tomato Yellow Leaf Curl Virus. " +
        "Install yellow sticky traps."
)

// ─── DiseaseClassifier ───────────────────────────────────────────────────────

class DiseaseClassifier(private val context: Context) {

    private val TAG = "DiseaseClassifier"

    // Must match training IMG_SIZE
    private val INPUT_SIZE = 224

    // Below this confidence → show "Unclear — Retake Photo"
    // Matches CONFIDENCE_THRESHOLD in train_disease_classifier.py
    private val CONFIDENCE_THRESHOLD = 0.60f

    private var interpreter: org.tensorflow.lite.Interpreter? = null
    private var labels: List<String> = emptyList()

    // True when model loaded successfully from assets
    val isModelAvailable: Boolean get() = interpreter != null

    // ── Initialise ────────────────────────────────────────────────────────────
    init {
        try {
            val modelBuffer = loadModelFile()
            interpreter = org.tensorflow.lite.Interpreter(modelBuffer)
            labels = loadLabels()
            Log.i(TAG, "TFLite model loaded. Classes: $labels")
        } catch (e: Exception) {
            // Model file not yet in assets (training not done yet)
            // App falls back to Gemini API in AiAssistantScreen
            Log.w(TAG, "TFLite model not available: ${e.message}. Falling back to Gemini API.")
            interpreter = null
        }
    }

    // ── Inference ─────────────────────────────────────────────────────────────
    /**
     * Classify a leaf image on-device.
     *
     * Preprocessing matches Python test_model.py exactly:
     *   1. Resize bitmap to 224×224
     *   2. Extract RGB floats, normalize to [0, 1]
     *   3. Run TFLite inference
     *   4. Apply confidence threshold
     *   5. Return DiagnosisResult
     */
    fun classify(bitmap: Bitmap): DiagnosisResult {
        val interp = interpreter
            ?: return errorResult("Model not available. Running Gemini API fallback.")

        val resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val inputBuffer = bitmapToByteBuffer(resized)

        val outputArray = Array(1) { FloatArray(labels.size) }
        interp.run(inputBuffer, outputArray)

        val scores = outputArray[0]
        val maxIdx = scores.indices.maxByOrNull { scores[it] } ?: 0
        val confidence = scores[maxIdx]
        val rawLabel = labels.getOrElse(maxIdx) { "Unknown" }

        Log.d(TAG, "Top class: $rawLabel  confidence: ${"%.2f".format(confidence)}")

        val isUnclear = confidence < CONFIDENCE_THRESHOLD
        return if (isUnclear) {
            DiagnosisResult(
                label = "Unclear — Retake Photo",
                confidence = confidence,
                isUnclear = true,
                treatmentTip = "Photo is too blurry, dark, or doesn't show a clear leaf.\n\n" +
                        "• Move closer to the affected leaf (30–40 cm)\n" +
                        "• Ensure good natural lighting — avoid shadows\n" +
                        "• Steady your phone before tapping capture",
                rawLabel = rawLabel
            )
        } else {
            DiagnosisResult(
                label = rawLabel.replace("_", " "),
                confidence = confidence,
                isUnclear = false,
                treatmentTip = TREATMENT_TIPS[rawLabel]
                    ?: "Consult your local Krishi Vigyan Kendra (KVK) for detailed treatment advice.",
                rawLabel = rawLabel
            )
        }
    }

    // ── Preprocessing ─────────────────────────────────────────────────────────
    /**
     * Convert Bitmap → ByteBuffer with float32 RGB values in [0, 1].
     * Order: R G B R G B ... (row-major, matching TF's channels_last convention).
     */
    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        // 4 bytes per float × 3 channels × INPUT_SIZE × INPUT_SIZE
        val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixel in intValues) {
            byteBuffer.putFloat((pixel shr 16 and 0xFF) / 255.0f)  // R
            byteBuffer.putFloat((pixel shr 8  and 0xFF) / 255.0f)  // G
            byteBuffer.putFloat((pixel        and 0xFF) / 255.0f)  // B
        }
        return byteBuffer
    }

    // ── Asset loading ─────────────────────────────────────────────────────────
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("disease_classifier_quantized.tflite")
        return FileInputStream(fileDescriptor.fileDescriptor).channel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    private fun loadLabels(): List<String> {
        return BufferedReader(InputStreamReader(context.assets.open("disease_labels.txt")))
            .readLines()
            .filter { it.isNotBlank() }
    }

    private fun errorResult(msg: String) = DiagnosisResult(
        label = "Model Unavailable",
        confidence = 0f,
        isUnclear = true,
        treatmentTip = msg,
        rawLabel = ""
    )

    // ── Cleanup ───────────────────────────────────────────────────────────────
    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
